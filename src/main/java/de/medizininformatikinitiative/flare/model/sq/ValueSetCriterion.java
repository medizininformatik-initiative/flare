package de.medizininformatikinitiative.flare.model.sq;

import de.medizininformatikinitiative.flare.model.mapping.Mapping;
import de.medizininformatikinitiative.flare.model.mapping.MappingContext;
import de.medizininformatikinitiative.flare.model.sq.expanded.ConceptFilter;
import de.medizininformatikinitiative.flare.model.sq.expanded.ExpandedCriterion;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.structured_query.Concept;
import de.numcodex.sq2cql.model.structured_query.TimeRestriction;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * A {@code ValueSetCriterion} will select all patients that have at least one resource represented
 * by that concept and coded value.
 * <p>
 * Examples are {@code Observation} resources representing the concept of a coded laboratory value.
 */
public record ValueSetCriterion(Concept concept, List<AttributeFilter> attributeFilters,
                                TimeRestriction timeRestriction, List<TermCode> selectedConcepts) implements Criterion {

    /**
     * Returns a {@code ValueSetCriterion}.
     *
     * @param concept          the concept the criterion represents
     * @param selectedConcepts at least one selected value concept
     * @return the {@code ValueSetCriterion}
     */
    public static ValueSetCriterion of(Concept concept, TermCode... selectedConcepts) {
        if (selectedConcepts == null || selectedConcepts.length == 0) {
            throw new IllegalArgumentException("empty selected concepts");
        }
        return new ValueSetCriterion(concept, List.of(), null, List.of(selectedConcepts));
    }

    /**
     * Returns a {@code ValueSetCriterion}.
     *
     * @param concept          the concept the criterion represents
     * @param selectedConcepts at least one selected value concept
     * @param attributeFilters additional filters on particular attributes
     * @return the {@code ValueSetCriterion}
     */
    public static ValueSetCriterion of(Concept concept, List<TermCode> selectedConcepts,
                                       AttributeFilter... attributeFilters) {
        if (selectedConcepts == null || selectedConcepts.isEmpty()) {
            throw new IllegalArgumentException("empty selected concepts");
        }
        return new ValueSetCriterion(concept, List.of(attributeFilters), null, List.copyOf(selectedConcepts));
    }

    /**
     * Returns a {@code ValueSetCriterion}.
     *
     * @param concept          the concept the criterion represents
     * @param timeRestriction  the timeRestriction applied to the concept
     * @param selectedConcepts at least one selected value concept
     * @param attributeFilters additional filters on particular attributes
     * @return the {@code ValueSetCriterion}
     */
    public static ValueSetCriterion of(Concept concept, List<TermCode> selectedConcepts, TimeRestriction timeRestriction,
                                       AttributeFilter... attributeFilters) {
        if (selectedConcepts == null || selectedConcepts.isEmpty()) {
            throw new IllegalArgumentException("empty selected concepts");
        }
        return new ValueSetCriterion(concept, List.of(attributeFilters), timeRestriction, List.copyOf(selectedConcepts));
    }

    @Override
    public Flux<ExpandedCriterion> expand(MappingContext mappingContext) {
        return mappingContext.expandConcept(concept).flatMap(termCode -> expandTermCode(mappingContext, termCode));
    }

    private Flux<ExpandedCriterion> expandTermCode(MappingContext mappingContext, TermCode termCode) {
        return mappingContext.findMapping(termCode).flux()
                .flatMap(mapping -> mapping.valueSearchParameter()
                        .map(valueSearchParameter -> Flux.fromIterable(selectedConcepts)
                                .map(selectedConcept -> expandedCriterion(termCode, mapping, valueSearchParameter,
                                        selectedConcept)))
                        .orElseGet(() -> Flux.error(new MissingValueSearchParameterException(termCode))));
    }

    private static ExpandedCriterion expandedCriterion(TermCode termCode, Mapping mapping, String valueSearchParameter,
                                                       TermCode selectedConcept) {
        return ExpandedCriterion.of(mapping.resourceType(), mapping.termCodeSearchParameter(), termCode)
                .appendFilter(new ConceptFilter(valueSearchParameter, selectedConcept));
    }
}
