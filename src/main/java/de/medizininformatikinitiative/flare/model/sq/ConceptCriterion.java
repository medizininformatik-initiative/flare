package de.medizininformatikinitiative.flare.model.sq;

import de.medizininformatikinitiative.flare.model.mapping.Mapping;
import de.medizininformatikinitiative.flare.model.mapping.MappingContext;
import de.medizininformatikinitiative.flare.model.sq.expanded.ExpandedCriterion;
import de.medizininformatikinitiative.flare.model.sq.expanded.Filter;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.structured_query.Concept;
import de.numcodex.sq2cql.model.structured_query.TimeRestriction;
import reactor.core.publisher.Flux;

import java.util.LinkedList;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * A {@code ConceptCriterion} will select all patients that have at least one resource represented
 * by that concept.
 * <p>
 * Examples are {@code Condition} resources representing the concept of a particular disease.
 */
public record ConceptCriterion(Concept concept, List<AttributeFilter> attributeFilters,
                               TimeRestriction timeRestriction) implements Criterion {

    public ConceptCriterion {
        requireNonNull(concept);
        attributeFilters = List.copyOf(attributeFilters);
    }

    /**
     * Returns a {@code ConceptCriterion}.
     *
     * @param concept the concept the criterion represents
     * @return the {@code ConceptCriterion}.
     */
    public static ConceptCriterion of(Concept concept) {
        return new ConceptCriterion(concept, List.of(), null);
    }

    public ConceptCriterion appendAttributeFilter(AttributeFilter attributeFilter) {
        var attributeFilters = new LinkedList<>(this.attributeFilters);
        attributeFilters.add(attributeFilter);
        return new ConceptCriterion(concept, attributeFilters, timeRestriction);
    }

    @Override
    public Flux<ExpandedCriterion> expand(MappingContext mappingContext) {
        return mappingContext.expandConcept(concept).flatMap(termCode -> expandTermCode(mappingContext, termCode));
    }

    private Flux<ExpandedCriterion> expandTermCode(MappingContext mappingContext, TermCode termCode) {
        return mappingContext.findMapping(termCode).flux()
                .flatMap(mapping -> Flux.fromIterable(attributeFilters)
                        .flatMap(attributeFilter -> attributeFilter.toFilter(mapping)
                                .map(filter -> expandedCriterion(mapping, termCode, List.of(filter))))
                        .defaultIfEmpty(expandedCriterion(mapping, termCode, List.of())));
    }

    private static ExpandedCriterion expandedCriterion(Mapping mapping, TermCode termCode,
                                                       List<Filter> attributeFilters) {
        return new ExpandedCriterion(mapping.resourceType(), mapping.termCodeSearchParameter(), termCode,
                attributeFilters);
    }
}
