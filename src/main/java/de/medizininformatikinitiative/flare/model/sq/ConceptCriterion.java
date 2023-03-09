package de.medizininformatikinitiative.flare.model.sq;

import de.medizininformatikinitiative.flare.model.Query;
import de.medizininformatikinitiative.flare.model.mapping.MappingContext;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.structured_query.AttributeFilter;
import de.numcodex.sq2cql.model.structured_query.Concept;
import de.numcodex.sq2cql.model.structured_query.MappingNotFoundException;
import de.numcodex.sq2cql.model.structured_query.TimeRestriction;

import java.util.List;

/**
 * A {@code ConceptCriterion} will select all patients that have at least one resource represented
 * by that concept.
 * <p>
 * Examples are {@code Condition} resources representing the concept of a particular disease.
 */
public record ConceptCriterion(Concept concept, List<AttributeFilter> attributeFilters, TimeRestriction timeRestriction) implements Criterion {

    /**
     * Returns a {@code ConceptCriterion}.
     *
     * @param concept          the concept the criterion represents
     * @param attributeFilters additional filters on particular attributes
     * @return the {@code ConceptCriterion}.
     */
    public static ConceptCriterion of(Concept concept, AttributeFilter... attributeFilters) {
        return new ConceptCriterion(concept, List.of(attributeFilters), null);
    }

    /**
     * Returns a {@code ConceptCriterion}.
     *
     * @param concept          the concept the criterion represents
     * @param timeRestriction  the time restriction on the critieria
     * @param attributeFilters additional filters on particular attributes
     * @return the {@code ConceptCriterion}.
     */
    public static ConceptCriterion of(Concept concept, TimeRestriction timeRestriction,
                                      AttributeFilter... attributeFilters) {
        return new ConceptCriterion(concept, List.of(attributeFilters), timeRestriction);
    }

    @Override
    public List<Query> toQuery(MappingContext mappingContext) {
        return mappingContext.expandConcept(concept)
                .map(termCode -> query(mappingContext, termCode))
                .toList();
    }

    private Query query(MappingContext mappingContext, TermCode termCode) {
        var mapping = mappingContext.findMapping(termCode)
                .orElseThrow(() -> new MappingNotFoundException(termCode));
        return new Query(mapping.resourceType(), "%s=%s|%s".formatted(mapping.termCodeSearchParameter(),
                termCode.system(), termCode.code()));
    }
}
