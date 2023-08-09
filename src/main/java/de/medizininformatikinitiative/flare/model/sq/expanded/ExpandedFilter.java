package de.medizininformatikinitiative.flare.model.sq.expanded;

import de.medizininformatikinitiative.flare.model.fhir.QueryParams;

import java.util.Collection;

import static java.util.Objects.requireNonNull;

/**
 * A filter part which can be used a value filter part and attribute filter part.
 */
public interface ExpandedFilter {

    /**
     * Takes collection of filters and returns the combined {@link QueryParams} of all filters.
     *
     * @param filters filter part those {@link #toParams() params} should be combined
     * @return the combined {@link QueryParams} of all filters
     */
    static QueryParams toParams(Collection<ExpandedFilter> filters) {
        return filters.stream().map(ExpandedFilter::toParams).reduce(QueryParams.EMPTY, QueryParams::appendParams);
    }

    /**
     * Transforms this filter part into {@link QueryParams query params}.
     *
     * @return the query params of this filter part
     */
    QueryParams toParams();

    static String combinedSearchParam(String referenceSearchParameter, String searchParameter) {
        requireNonNull(searchParameter);
        return referenceSearchParameter == null ? searchParameter : referenceSearchParameter + "." + searchParameter;
    }

}
