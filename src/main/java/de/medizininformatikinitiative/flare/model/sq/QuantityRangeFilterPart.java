package de.medizininformatikinitiative.flare.model.sq;

import de.medizininformatikinitiative.flare.Either;
import de.medizininformatikinitiative.flare.model.mapping.FilterMapping;
import de.medizininformatikinitiative.flare.model.sq.expanded.AgeUtils;
import de.medizininformatikinitiative.flare.model.sq.expanded.ExpandedCompositeQuantityRangeFilter;
import de.medizininformatikinitiative.flare.model.sq.expanded.ExpandedFilter;
import de.medizininformatikinitiative.flare.model.sq.expanded.ExpandedQuantityRangeFilter;

import java.time.LocalDate;
import java.util.List;

import static java.util.Objects.requireNonNull;

public record QuantityRangeFilterPart(Quantity lowerBound, Quantity upperBound) implements FilterPart {

    public QuantityRangeFilterPart {
        requireNonNull(lowerBound);
        requireNonNull(upperBound);
    }

    public static QuantityRangeFilterPart of(Quantity lowerBound, Quantity upperBound) {
        return new QuantityRangeFilterPart(lowerBound, upperBound);
    }

    @Override
    public Either<Exception, List<ExpandedFilter>> expand(LocalDate today, FilterMapping filterMapping) {
        if (filterMapping.isAge()) {
            return (lowerBound instanceof Quantity.Unitless || upperBound instanceof Quantity.Unitless)
                    ? Either.left(new CalculationException("Missing unit in age calculation."))
                    : AgeUtils.expandedAgeFilterFromRange(today, (Quantity.WithUnit) lowerBound,
                    (Quantity.WithUnit) upperBound);
        }
        return Either.right(List.of(filterMapping.compositeCode()
                .map(compositeCode -> (ExpandedFilter) new ExpandedCompositeQuantityRangeFilter(
                        filterMapping.searchParameter(), compositeCode, lowerBound, upperBound))
                .orElse(new ExpandedQuantityRangeFilter(filterMapping.searchParameter(), lowerBound, upperBound))));
    }
}
