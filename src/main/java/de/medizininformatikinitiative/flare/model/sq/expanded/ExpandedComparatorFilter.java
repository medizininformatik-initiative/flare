package de.medizininformatikinitiative.flare.model.sq.expanded;

import de.medizininformatikinitiative.flare.model.fhir.QueryParams;
import de.medizininformatikinitiative.flare.model.sq.Comparator;
import de.medizininformatikinitiative.flare.model.sq.TermCode;

import java.math.BigDecimal;

public record ExpandedComparatorFilter(String searchParameter, Comparator comparator, BigDecimal value, TermCode unit)
        implements ExpandedFilter {

    @Override
    public QueryParams toParams() {
        var paramValue = comparator.toString() + value.toString();
        if (unit != null) {
            paramValue += unitAttachment();
        }
        return QueryParams.of(searchParameter, paramValue);
    }

    String unitAttachment() {
        return "|" + unit.system() + "|" + unit.code();
    }
}
