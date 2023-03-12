package de.medizininformatikinitiative.flare.service;

import de.medizininformatikinitiative.flare.model.sq.Criterion;
import de.medizininformatikinitiative.flare.model.sq.StructuredQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class StructuredQueryService {

    private static final Logger logger = LoggerFactory.getLogger(StructuredQueryService.class);

    private final FhirQueryService fhirQueryService;
    private final Translator translator;

    public StructuredQueryService(@Qualifier("cachingFhirQueryService") FhirQueryService fhirQueryService,
                                  Translator translator) {
        this.fhirQueryService = Objects.requireNonNull(fhirQueryService);
        this.translator = Objects.requireNonNull(translator);
    }

    /**
     * Executes {@code query} and returns the number of Patients qualifying the criteria.
     *
     * @param query the query to execute
     * @return the number of Patients qualifying the criteria
     */
    public Mono<Integer> execute(StructuredQuery query) {
        var includedPatients = executeConjunctiveNormalForm(query.inclusionCriteria())
                .defaultIfEmpty(Set.of());
        var excludedPatients = executeConjunctiveNormalFormOr(expandExclusionCriteria(query.exclusionCriteria()))
                .defaultIfEmpty(Set.of());
        return includedPatients
                .flatMap(i -> excludedPatients.map(e -> difference(i, e)))
                .map(Set::size);
    }

    /**
     * Calculates a conjunctive normal form of criteria (A, B, C, D) using set algebra.
     *
     * <pre>
     * (A v B) ^ (C v D)
     * </pre>
     */
    private Mono<Set<String>> executeConjunctiveNormalForm(List<List<Criterion>> criteria) {
        return Flux.fromIterable(criteria)
                .flatMap(this::executeOr)
                .reduce(StructuredQueryService::intersection);
    }

    /**
     * Calculates a boolean or of criteria (A, B) using set algebra.
     *
     * <pre>
     * A v B
     * </pre>
     */
    private Mono<Set<String>> executeOr(List<Criterion> criteria) {
        return Flux.fromIterable(criteria)
                .flatMap(this::executeSingle)
                .reduce(StructuredQueryService::union);
    }

    /**
     * Calculates a boolean or of conjunctive normal forms of criteria (A, B, C, D, E, F, G, H) using set algebra.
     *
     * <pre>
     * ((A v B) ^ (C v D)) v ((E v F) ^ (G v H))
     * </pre>
     */
    private Mono<Set<String>> executeConjunctiveNormalFormOr(List<List<List<Criterion>>> criteria) {
        return Flux.fromIterable(criteria)
                .flatMap(this::executeConjunctiveNormalForm)
                .reduce(StructuredQueryService::union);
    }

    /**
     *
     */
    private Flux<Set<String>> executeSingle(Criterion criterion) {
        logger.info("execute single criterion {}", criterion);
        return translator.toQuery(criterion).flux()
                .flatMap(Flux::fromIterable)
                .flatMap(query -> Mono.fromFuture(fhirQueryService.execute(query)));
    }

    private static List<List<List<Criterion>>> expandExclusionCriteria(List<List<Criterion>> criteria) {
        return criteria.stream()
                .map(c -> c.stream().map(List::of).toList())
                .toList();
    }

    private static Set<String> intersection(Set<String> a, Set<String> b) {
        var ret = new HashSet<>(a);
        ret.retainAll(b);
        return Set.copyOf(ret);
    }

    private static Set<String> union(Set<String> a, Set<String> b) {
        var ret = new HashSet<>(a);
        ret.addAll(b);
        return Set.copyOf(ret);
    }

    private static Set<String> difference(Set<String> a, Set<String> b) {
        var ret = new HashSet<>(a);
        ret.removeAll(b);
        return Set.copyOf(ret);
    }
}
