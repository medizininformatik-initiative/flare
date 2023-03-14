package de.medizininformatikinitiative.flare.model.sq.expanded;

import de.medizininformatikinitiative.flare.model.fhir.QueryParams;
import de.medizininformatikinitiative.flare.model.sq.Comparator;
import de.medizininformatikinitiative.flare.model.sq.TermCode;

import java.math.BigDecimal;

public record ExpandedComparatorFilter(String searchParameter, Comparator comparator, BigDecimal value, TermCode unit)
        implements ExpandedFilter {

    @Override
    public QueryParams toParams() {
        return QueryParams.EMPTY.appendParam(searchParameter, comparator, value, unit);
    }
}
