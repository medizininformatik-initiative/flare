package de.medizininformatikinitiative.flare.model.sq.expanded;

import de.medizininformatikinitiative.flare.model.fhir.Query;
import de.medizininformatikinitiative.flare.model.fhir.QueryParams;
import de.medizininformatikinitiative.flare.model.mapping.Mapping;
import de.medizininformatikinitiative.flare.model.sq.Criterion;
import de.medizininformatikinitiative.flare.model.sq.TermCode;

import java.util.LinkedList;
import java.util.List;

import static de.medizininformatikinitiative.flare.model.fhir.QueryParams.conceptValue;
import static de.medizininformatikinitiative.flare.model.sq.expanded.ExpandedFilter.toParams;
import static java.util.Objects.requireNonNull;

/**
 * A criterion that is already expanded from a {@link Criterion} of the structured query.
 * <p>
 * Expanded criterion {@link #toQuery() translate} to exactly one {@link Query query} and contain already all
 * {@link Mapping mapping information} needed.
 *
 * @param resourceType    the type of the resource like Condition or Observation
 * @param searchParameter the FHIR search parameter code to use for the {@code termCode}
 * @param code            the main code constraining the resources
 * @param filters         additional attribute filters
 */
public record ExpandedCriterion(String resourceType, String searchParameter, TermCode code,
                                List<ExpandedFilter> filters) {

    public ExpandedCriterion {
        requireNonNull(resourceType);
        filters = List.copyOf(filters);
    }

    public static ExpandedCriterion of(String resourceType) {
        return new ExpandedCriterion(resourceType, null, null, List.of());
    }

    public static ExpandedCriterion of(String resourceType, String searchParameter, TermCode termCode) {
        return new ExpandedCriterion(resourceType, searchParameter, termCode, List.of());
    }

    public ExpandedCriterion appendFilter(ExpandedFilter attributeFilter) {
        var attributeFilters = new LinkedList<>(this.filters);
        attributeFilters.add(attributeFilter);
        return new ExpandedCriterion(resourceType, searchParameter, code, attributeFilters);
    }

    public Query toQuery() {
        return new Query(resourceType, startQueryParams().appendParams(toParams(filters)));
    }

    private QueryParams startQueryParams() {
        return searchParameter == null || code == null
                ? QueryParams.EMPTY
                : QueryParams.of(searchParameter, conceptValue(code));
    }
}
