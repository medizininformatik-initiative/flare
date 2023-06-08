package de.medizininformatikinitiative.flare.model.sq.expanded;

import de.medizininformatikinitiative.flare.model.fhir.QueryParams;
import de.medizininformatikinitiative.flare.model.sq.Comparator;
import de.medizininformatikinitiative.flare.model.sq.TermCode;

import java.math.BigDecimal;

import static java.util.Objects.requireNonNull;

public record ExpandedComparatorFilter(String searchParameter, Comparator comparator, BigDecimal value, TermCode unit, TermCode compositeCode)
        implements ExpandedFilter {

    public ExpandedComparatorFilter {
        requireNonNull(searchParameter);
        requireNonNull(comparator);
        requireNonNull(value);
    }

    @Override
    public QueryParams toParams() {
        return QueryParams.EMPTY.appendParam(searchParameter, comparator, value, unit, compositeCode);
    }
}
