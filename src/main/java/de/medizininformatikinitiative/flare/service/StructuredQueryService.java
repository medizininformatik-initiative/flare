package de.medizininformatikinitiative.flare.service;

import de.medizininformatikinitiative.flare.Util;
import de.medizininformatikinitiative.flare.model.sq.Criterion;
import de.medizininformatikinitiative.flare.model.sq.StructuredQuery;
import de.medizininformatikinitiative.flare.model.translate.Operator;
import de.medizininformatikinitiative.flare.model.translate.QueryExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static de.medizininformatikinitiative.flare.model.translate.Operator.Name.UNION;

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
                .flatMap(i -> excludedPatients.map(e -> Util.difference(i, e)))
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
                .reduce(Util::intersection);
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
                .reduce(Util::union);
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
                .reduce(Util::union);
    }

    private Flux<Set<String>> executeSingle(Criterion criterion) {
        logger.debug("execute single criterion {}", criterion);
        return translator.toQuery(criterion).flux()
                .flatMap(Flux::fromIterable)
                .flatMap(query -> Mono.fromFuture(fhirQueryService.execute(query)));
    }

    private static List<List<List<Criterion>>> expandExclusionCriteria(List<List<Criterion>> criteria) {
        return criteria.stream()
                .map(c -> c.stream().map(List::of).toList())
                .toList();
    }

    public Mono<Operator> translate(StructuredQuery query) {
        var inclusions = translateConjunctiveNormalForm(query.inclusionCriteria());
        var exclusions = translateConjunctiveNormalFormOr(expandExclusionCriteria(query.exclusionCriteria()));
        return inclusions.flatMap(i -> exclusions.map(e -> e.isEmpty() ? i : Operator.difference(i, e)));
    }

    private Mono<Operator> translateConjunctiveNormalForm(List<List<Criterion>> criteria) {
        return Flux.fromIterable(criteria)
                .flatMap(this::translateOr)
                .reduce(Operator.intersection(), Operator::add);
    }

    private Mono<Operator> translateOr(List<Criterion> criteria) {
        return Flux.fromIterable(criteria)
                .flatMap(this::translateSingle)
                .reduce(Operator::concat);
    }

    private Mono<Operator> translateConjunctiveNormalFormOr(List<List<List<Criterion>>> criteria) {
        return Flux.fromIterable(criteria)
                .flatMap(this::translateConjunctiveNormalForm)
                .reduce(Operator.union(), Operator::add);
    }

    private Mono<Operator> translateSingle(Criterion criterion) {
        logger.debug("translate single criterion {}", criterion);
        return translator.toQuery(criterion).map(queries ->
                new Operator(UNION, queries.stream().map(QueryExpression::new).toList()));
    }
}
