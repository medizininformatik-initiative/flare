package de.medizininformatikinitiative.flare.service;

import de.medizininformatikinitiative.flare.model.mapping.MappingContext;
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

    private final QueryService queryService;
    private final MappingContext mappingContext;

    public StructuredQueryService(@Qualifier("cachingQueryService") QueryService queryService,
                                  MappingContext mappingContext) {
        this.queryService = Objects.requireNonNull(queryService);
        this.mappingContext = Objects.requireNonNull(mappingContext);
    }

    public Mono<Integer> execute(StructuredQuery query) {
        return Flux.fromIterable(query.inclusionCriteria())
                .flatMap(this::executeOr)
                .reduce(StructuredQueryService::intersection)
                .map(Set::size);
    }

    /*private Mono<Set<String>> executeAnd(List<Criterion> criteria) {
        return Flux.fromIterable(criteria)
                .flatMap(this::executeOr)
                .reduce(StructuredQueryService::intersection);
    }*/

    private Mono<Set<String>> executeOr(List<Criterion> criteria) {
        return Flux.fromIterable(criteria)
                .flatMap(this::executeSingle)
                .reduce(StructuredQueryService::union);
    }

    private Flux<Set<String>> executeSingle(Criterion criterion) {
        logger.info("execute single criterion {}", criterion);
        return Flux.fromStream(criterion.toQuery(mappingContext).stream())
                .flatMap(query -> Mono.fromFuture(queryService.execute(query)));
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
}
