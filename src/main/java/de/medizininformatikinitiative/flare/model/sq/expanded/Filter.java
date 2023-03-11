package de.medizininformatikinitiative.flare.model.sq.expanded;

import de.medizininformatikinitiative.flare.model.sq.QueryParams;

import java.util.Collection;

/**
 * A filter which can be used a value filter and attribute filter.
 */
public interface Filter {

    /**
     * Takes collection of filters and returns the combined {@link QueryParams} of all filters.
     *
     * @param filters filter those {@link #toParams() params} should be combined
     * @return the combined {@link QueryParams} of all filters
     */
    static QueryParams toParams(Collection<Filter> filters) {
        return filters.stream().map(Filter::toParams).reduce(QueryParams.EMPTY, QueryParams::appendParams);
    }

    /**
     * Transforms this filter into {@link QueryParams query params}.
     *
     * @return the query params of this filter
     */
    QueryParams toParams();
}
