package de.medizininformatikinitiative.flare.model.sq.expanded;

import de.medizininformatikinitiative.flare.model.Query;
import de.medizininformatikinitiative.flare.model.sq.Criterion;
import de.medizininformatikinitiative.flare.model.sq.QueryParams;
import de.numcodex.sq2cql.model.common.TermCode;

import java.util.LinkedList;
import java.util.List;

import static de.medizininformatikinitiative.flare.model.sq.expanded.Filter.toParams;
import static java.util.Objects.requireNonNull;

/**
 * A concept criterion that is already expanded from a {@link Criterion} of the structured query.
 *
 * @param resourceType     the type of the resource like Condition or Observation
 * @param searchParameter  the FHIR search parameter code to use for the {@code termCode}
 * @param termCode         the main code constraining the resources
 * @param attributeFilters additional attribute filters
 */
public record ExpandedConceptCriterion(String resourceType, String searchParameter, TermCode termCode,
                                       List<Filter> attributeFilters) implements ExpandedCriterion {

    public ExpandedConceptCriterion {
        requireNonNull(resourceType);
        requireNonNull(searchParameter);
        requireNonNull(termCode);
        attributeFilters = List.copyOf(attributeFilters);
    }

    public static ExpandedConceptCriterion of(String resourceType, String searchParameter, TermCode termCode) {
        return new ExpandedConceptCriterion(resourceType, searchParameter, termCode, List.of());
    }

    public ExpandedConceptCriterion appendAttributeFilter(Filter attributeFilter) {
        var attributeFilters = new LinkedList<>(this.attributeFilters);
        attributeFilters.add(attributeFilter);
        return new ExpandedConceptCriterion(resourceType, searchParameter, termCode, attributeFilters);
    }

    @Override
    public Query toQuery() {
        return new Query(resourceType, QueryParams.of(searchParameter, termCode)
                .appendParams(toParams(attributeFilters)));
    }
}
