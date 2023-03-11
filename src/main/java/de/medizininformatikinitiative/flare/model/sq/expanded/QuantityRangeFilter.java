package de.medizininformatikinitiative.flare.model.sq.expanded;

import de.medizininformatikinitiative.flare.model.sq.QueryParams;
import de.numcodex.sq2cql.model.common.Comparator;

import java.math.BigDecimal;

public record QuantityRangeFilter(Comparator comparator, BigDecimal value, String unit) implements Filter {

    @Override
    public QueryParams toParams() {
        return QueryParams.EMPTY;
    }
}
