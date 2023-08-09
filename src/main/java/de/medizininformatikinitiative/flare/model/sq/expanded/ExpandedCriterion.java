package de.medizininformatikinitiative.flare.model.sq.expanded;

import de.medizininformatikinitiative.flare.model.fhir.Query;
import de.medizininformatikinitiative.flare.model.mapping.Mapping;
import de.medizininformatikinitiative.flare.model.sq.Criterion;
import de.medizininformatikinitiative.flare.model.sq.TermCode;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static de.medizininformatikinitiative.flare.model.sq.expanded.ExpandedFilter.toParams;
import static java.util.Objects.requireNonNull;

/**
 * A criterion that is already expanded from a {@link Criterion} of the structured query.
 * <p>
 * Expanded criterion {@link #toQuery() translate} to exactly one {@link Query query} and contain already all
 * {@link Mapping mapping information} needed.
 *
 * @param resourceType the type of the resource like Condition or Observation
 * @param filters      all filters
 */
public record ExpandedCriterion(String resourceType, List<ExpandedFilter> filters) {

    public ExpandedCriterion {
        requireNonNull(resourceType);
        filters = List.copyOf(filters);
    }

    public static ExpandedCriterion of(String resourceType) {
        return new ExpandedCriterion(resourceType, List.of());
    }

    public static ExpandedCriterion of(String resourceType, String searchParameter, TermCode termCode) {
        return new ExpandedCriterion(resourceType, List.of(new ExpandedConceptFilter(requireNonNull(searchParameter),
                requireNonNull(termCode))));
    }

    public ExpandedCriterion appendFilter(ExpandedFilter attributeFilter) {
        var attributeFilters = new LinkedList<>(this.filters);
        attributeFilters.add(attributeFilter);
        return new ExpandedCriterion(resourceType, attributeFilters);
    }

    public ExpandedCriterion appendFilters(Collection<ExpandedFilter> attributeFilters) {
        var newAttributeFilters = new LinkedList<>(this.filters);
        newAttributeFilters.addAll(attributeFilters);
        return new ExpandedCriterion(resourceType, newAttributeFilters);
    }

    public Query toQuery() {
        return new Query(resourceType, toParams(filters));
    }
}
