package de.medizininformatikinitiative.flare.model.sq.expanded;

import de.medizininformatikinitiative.flare.model.fhir.QueryParams;
import de.medizininformatikinitiative.flare.model.sq.TermCode;

import java.math.BigDecimal;

import static de.medizininformatikinitiative.flare.model.sq.Comparator.GREATER_EQUAL;
import static de.medizininformatikinitiative.flare.model.sq.Comparator.LESS_EQUAL;

public record ExpandedRangeFilter(String searchParameter, BigDecimal lowerBound, BigDecimal upperBound, TermCode unit)
        implements ExpandedFilter {

    @Override
    public QueryParams toParams() {
        return QueryParams.EMPTY
                .appendParam(searchParameter, GREATER_EQUAL, lowerBound, unit)
                .appendParam(searchParameter, LESS_EQUAL, upperBound, unit);
    }
}
