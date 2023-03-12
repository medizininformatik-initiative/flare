package de.medizininformatikinitiative.flare.model.sq.expanded;

import de.medizininformatikinitiative.flare.model.fhir.QueryParams;

import java.util.Collection;

/**
 * A filterPart which can be used a value filterPart and attribute filterPart.
 */
public interface ExpandedFilter {

    /**
     * Takes collection of filters and returns the combined {@link QueryParams} of all filters.
     *
     * @param filters filterPart those {@link #toParams() params} should be combined
     * @return the combined {@link QueryParams} of all filters
     */
    static QueryParams toParams(Collection<ExpandedFilter> filters) {
        return filters.stream().map(ExpandedFilter::toParams).reduce(QueryParams.EMPTY, QueryParams::appendParams);
    }

    /**
     * Transforms this filterPart into {@link QueryParams query params}.
     *
     * @return the query params of this filterPart
     */
    QueryParams toParams();
}
