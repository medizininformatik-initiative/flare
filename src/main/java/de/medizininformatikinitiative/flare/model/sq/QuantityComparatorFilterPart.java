package de.medizininformatikinitiative.flare.model.sq;

import de.medizininformatikinitiative.flare.Either;
import de.medizininformatikinitiative.flare.model.mapping.FilterMapping;
import de.medizininformatikinitiative.flare.model.sq.expanded.AgeUtils;
import de.medizininformatikinitiative.flare.model.sq.expanded.ExpandedQuantityComparatorFilter;
import de.medizininformatikinitiative.flare.model.sq.expanded.ExpandedCompositeQuantityComparatorFilter;
import de.medizininformatikinitiative.flare.model.sq.expanded.ExpandedFilter;

import java.time.LocalDate;
import java.util.List;

import static java.util.Objects.requireNonNull;

public record QuantityComparatorFilterPart(Comparator comparator, Quantity value) implements FilterPart {

    public QuantityComparatorFilterPart {
        requireNonNull(comparator);
        requireNonNull(value);
    }

    /**
     * Returns a comparator filter part.
     *
     * @param comparator the comparator that should be used in the value comparison
     * @param value      the value that should be used in the value comparison
     * @return the comparator filter part
     */
    public static QuantityComparatorFilterPart of(Comparator comparator, Quantity value) {
        return new QuantityComparatorFilterPart(comparator, value);
    }

    @Override
    public Either<Exception, List<ExpandedFilter>> expand(LocalDate today, FilterMapping filterMapping) {
        if (filterMapping.isAge()) {
            return (value instanceof Quantity.Unitless)
                    ? Either.left(new CalculationException("Missing unit in age calculation."))
                    : AgeUtils.expandedAgeFilterFromComparator(today, comparator, (Quantity.WithUnit) value);
        }
        return Either.right(List.of(filterMapping.compositeCode()
                .map(compositeCode -> (ExpandedFilter) new ExpandedCompositeQuantityComparatorFilter(
                        filterMapping.searchParameter(), compositeCode, comparator, value))
                .orElse(new ExpandedQuantityComparatorFilter(filterMapping.searchParameter(), comparator, value))));
    }
}
