package de.medizininformatikinitiative.flare.service;

import de.medizininformatikinitiative.flare.Either;
import de.medizininformatikinitiative.flare.model.Population;
import de.medizininformatikinitiative.flare.model.sq.Criterion;
import de.medizininformatikinitiative.flare.model.sq.CriterionGroup;
import de.medizininformatikinitiative.flare.model.sq.StructuredQuery;
import de.medizininformatikinitiative.flare.model.translate.Expression;
import de.medizininformatikinitiative.flare.model.translate.Operator;
import de.medizininformatikinitiative.flare.model.translate.QueryExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static de.medizininformatikinitiative.flare.model.translate.Operator.Name.UNION;
import static java.util.Objects.requireNonNull;

@Service
public class StructuredQueryService {

    private static final Logger logger = LoggerFactory.getLogger(StructuredQueryService.class);

    private final FhirQueryService fhirQueryService;
    private final Translator translator;

    public StructuredQueryService(@Qualifier("memCachingFhirQueryService") FhirQueryService fhirQueryService,
                                  Translator translator) {
        this.fhirQueryService = requireNonNull(fhirQueryService);
        this.translator = requireNonNull(translator);
    }


    /**
     * Executes {@code query} and returns the Population of Patient IDs.
     *
     * @param id the ID of the query used for tracing purposes
     * @param query the query to execute
     * @return the Patient IDs qualifying the criteria
     */
    public Mono<Population> execute(UUID id, StructuredQuery query) {
        var includedPatients = query.inclusionCriteria().executeAndIntersection(group -> executeUnionGroup(id, group))
                .defaultIfEmpty(Population.of());
        var excludedPatients = query.exclusionCriteria().map(c -> c.map(CriterionGroup::wrapCriteria)
                        .executeAndUnion(group -> group.executeAndIntersection(group1 -> executeUnionGroup(id, group1)))
                        .defaultIfEmpty(Population.of()))
                .orElse(Mono.just(Population.of()));
        return includedPatients
                .flatMap(i -> excludedPatients.map(i::difference));
    }


    private Mono<Population> executeUnionGroup(UUID id, CriterionGroup<Criterion> group) {
        return group.executeAndUnion(criterion -> executeSingle(id, criterion));
    }

    private Flux<Population> executeSingle(UUID id, Criterion criterion) {
        logger.trace("Execute single criterion of query {}: {}", id, criterion);
        return translator.toQuery(criterion)
                .either(Flux::error, queries -> Flux.fromIterable(queries).flatMap(query -> fhirQueryService.execute(id, query)));
    }

    /**
     * Translates {@code query} and returns an {@link Expression expression} that explains the query execution.
     *
     * @param query the query to translate
     * @return an {@link Expression expression} that explains the query execution
     */
    public Either<Exception, Expression> translate(StructuredQuery query) {
        var inclusions = query.inclusionCriteria().translateAndIntersection(this::translateUnionGroup);
        var exclusions = query.exclusionCriteria().map(c -> c.map(CriterionGroup::wrapCriteria)
                .translateAndUnion(group -> group.translateAndIntersection(this::translateUnionGroup)));
        return exclusions
                .map(me -> inclusions.flatMap(i -> me.map(e -> (Expression) Operator.difference(i, e))))
                .orElse(inclusions);
    }

    private Either<Exception, Expression> translateUnionGroup(CriterionGroup<Criterion> group) {
        return group.translateAndConcat(this::translateSingle);
    }

    private Either<Exception, Operator<Expression>> translateSingle(Criterion criterion) {
        logger.trace("Translate single criterion {}", criterion);
        return translator.toQuery(criterion).map(queries -> new Operator<>(UNION, queries.stream()
                .map(QueryExpression::new).toList()));
    }
}
