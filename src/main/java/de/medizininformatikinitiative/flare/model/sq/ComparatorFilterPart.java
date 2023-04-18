package de.medizininformatikinitiative.flare.model.sq;

import de.medizininformatikinitiative.flare.Either;
import de.medizininformatikinitiative.flare.model.mapping.FilterMapping;
import de.medizininformatikinitiative.flare.model.sq.expanded.AgeUtils;
import de.medizininformatikinitiative.flare.model.sq.expanded.ExpandedComparatorFilter;
import de.medizininformatikinitiative.flare.model.sq.expanded.ExpandedFilter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static java.util.Objects.requireNonNull;

public record ComparatorFilterPart(Comparator comparator, BigDecimal value, TermCode unit) implements FilterPart {

    public ComparatorFilterPart {
        requireNonNull(comparator);
        requireNonNull(value);
    }

    /**
     * Returns a comparator filterPart without unit.
     *
     * @param comparator the comparator that should be used in the value comparison
     * @param value      the value that should be used in the value comparison
     * @return the comparator filterPart
     */
    public static ComparatorFilterPart of(Comparator comparator, BigDecimal value) {
        return new ComparatorFilterPart(comparator, value, null);
    }

    /**
     * Returns a comparator filterPart with unit.
     *
     * @param comparator the comparator that should be used in the value comparison
     * @param value      the value that should be used in the value comparison
     * @param unit       the unit of the value
     * @return the comparator filterPart
     */
    public static ComparatorFilterPart of(Comparator comparator, BigDecimal value, TermCode unit) {
        return new ComparatorFilterPart(comparator, value, requireNonNull(unit));
    }

    @Override
    public Either<Exception, List<ExpandedFilter>> expand(LocalDate today, FilterMapping filterMapping) {
        if (filterMapping.isAge()) {
            return unit == null
                    ? Either.left(new CalculationException("Missing unit in age calculation."))
                    : AgeUtils.expandedAgeFilterFromComparator(today, comparator, value, unit);
        }
        return Either.right(List.of(new ExpandedComparatorFilter(filterMapping.searchParameter(), comparator, value, unit)));
    }
}
