package de.medizininformatikinitiative.flare.model.sq.expanded;

import de.medizininformatikinitiative.flare.model.Query;
import de.medizininformatikinitiative.flare.model.sq.Criterion;
import de.medizininformatikinitiative.flare.model.sq.QueryParams;
import de.numcodex.sq2cql.model.common.TermCode;

import java.util.List;

import static de.medizininformatikinitiative.flare.model.sq.expanded.Filter.toParams;

/**
 * A value set criterion that is already expanded from a {@link Criterion} of the structured query.
 *
 * @param resourceType     the type of the resource like Condition or Observation
 * @param searchParameter  the FHIR search parameter code to use for the {@code termCode}
 * @param termCode         the main code constraining the resources
 * @param valueFilter      the value filter with the selected concept
 * @param attributeFilters additional attribute filters
 */
public record ExpandedValueSetCriterion(String resourceType, String searchParameter, TermCode termCode,
                                        ConceptFilter valueFilter, List<Filter> attributeFilters)
        implements ExpandedCriterion {

    @Override
    public Query toQuery() {
        return new Query(resourceType, QueryParams.of(searchParameter, termCode)
                .appendParams(valueFilter.toParams())
                .appendParams(toParams(attributeFilters)));
    }
}
