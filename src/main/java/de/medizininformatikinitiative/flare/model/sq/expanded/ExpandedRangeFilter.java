package de.medizininformatikinitiative.flare.model.sq.expanded;

import de.medizininformatikinitiative.flare.model.fhir.QueryParams;
import de.medizininformatikinitiative.flare.model.sq.Comparator;
import de.medizininformatikinitiative.flare.model.sq.TermCode;

import java.math.BigDecimal;

public record ExpandedRangeFilter(String searchParameter, BigDecimal lowerBound, BigDecimal upperBound, TermCode unit)
        implements ExpandedFilter {

    @Override
    public QueryParams toParams() {
        var lowerBoundParam = Comparator.GREATER_EQUAL + lowerBound.toString();
        if (unit != null) lowerBoundParam += unitAttachment();

        var upperBoundParam = Comparator.LESS_EQUAL + upperBound.toString();
        if (unit != null) upperBoundParam += unitAttachment();

        return QueryParams.of(searchParameter, lowerBoundParam).appendParam(searchParameter, upperBoundParam);
    }

    String unitAttachment() {
        return "|" + unit.system() + "|" + unit.code();
    }
}
