package de.medizininformatikinitiative.flare.model.sq.expanded;

import de.medizininformatikinitiative.flare.model.fhir.QueryParams;
import de.medizininformatikinitiative.flare.model.sq.Comparator;
import de.medizininformatikinitiative.flare.model.sq.TermCode;

import java.math.BigDecimal;

public record ExpandedRangeFilter(String searchParameter, BigDecimal lowerBound, BigDecimal upperBound, TermCode unit)
        implements ExpandedFilter {

    @Override
    public QueryParams toParams() {
        return QueryParams.EMPTY.appendParam(searchParameter, Comparator.GREATER_EQUAL, lowerBound, unit)
                                .appendParam(searchParameter, Comparator.LESS_EQUAL, upperBound, unit);
    }
}
