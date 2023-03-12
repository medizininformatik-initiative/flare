package de.medizininformatikinitiative.flare.model.sq.expanded;

import de.medizininformatikinitiative.flare.model.fhir.QueryParams;

import java.math.BigDecimal;

public record ExpandedRangeFilter(String searchParameter, BigDecimal lowerBound, BigDecimal upperBound, String unit)
        implements ExpandedFilter {

    @Override
    public QueryParams toParams() {
        return QueryParams.EMPTY;
    }
}
