package de.medizininformatikinitiative.flare.model.sq.expanded;

import de.medizininformatikinitiative.flare.model.fhir.QueryParams;
import de.medizininformatikinitiative.flare.model.sq.Comparator;

import java.math.BigDecimal;

public record ExpandedComparatorFilter(String searchParameter, Comparator comparator, BigDecimal value, String unit)
        implements ExpandedFilter {

    @Override
    public QueryParams toParams() {
        return QueryParams.EMPTY;
    }
}
