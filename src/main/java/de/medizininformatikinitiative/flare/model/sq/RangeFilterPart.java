package de.medizininformatikinitiative.flare.model.sq;

import de.medizininformatikinitiative.flare.Either;
import de.medizininformatikinitiative.flare.model.mapping.FilterMapping;
import de.medizininformatikinitiative.flare.model.sq.expanded.AgeUtils;
import de.medizininformatikinitiative.flare.model.sq.expanded.ExpandedFilter;
import de.medizininformatikinitiative.flare.model.sq.expanded.ExpandedRangeFilter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static java.util.Objects.requireNonNull;

public record RangeFilterPart(BigDecimal lowerBound, BigDecimal upperBound, TermCode unit) implements FilterPart {

    public RangeFilterPart {
        requireNonNull(lowerBound);
        requireNonNull(upperBound);
    }

    public static RangeFilterPart of(BigDecimal lowerBound, BigDecimal upperBound) {
        return new RangeFilterPart(lowerBound, upperBound, null);
    }

    public static RangeFilterPart of(BigDecimal lowerBound, BigDecimal upperBound, TermCode unit) {
        return new RangeFilterPart(lowerBound, upperBound, requireNonNull(unit));
    }

    @Override
    public Either<Exception, List<ExpandedFilter>> expand(LocalDate today, FilterMapping filterMapping) {
        if (filterMapping.isAge()) {
            return AgeUtils.expandedAgeFilterFromRange(today, lowerBound, upperBound, unit);
        }
        return Either.right(List.of(new ExpandedRangeFilter(filterMapping.searchParameter(), lowerBound, upperBound, unit)));
    }
}
