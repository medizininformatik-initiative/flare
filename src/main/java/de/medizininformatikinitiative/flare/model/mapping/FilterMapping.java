package de.medizininformatikinitiative.flare.model.mapping;

import de.medizininformatikinitiative.flare.Either;
import de.medizininformatikinitiative.flare.model.sq.CalculationException;
import de.medizininformatikinitiative.flare.model.sq.Comparator;
import de.medizininformatikinitiative.flare.model.sq.Quantity;
import de.medizininformatikinitiative.flare.model.sq.TermCode;
import de.medizininformatikinitiative.flare.model.sq.expanded.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FilterMapping {

    String searchParameter();

    /**
     * Returns the optional composite code of this filter mapping.
     *
     * @return the composite code or {@link Optional#empty() empty}
     */
    Optional<TermCode> compositeCode();

    /**
     * Returns {@code true} iff the filter should be mapped with special age handling.
     *
     * @return {@code true} iff the filter should be mapped with special age handling
     */
    boolean isAge();

    Either<Exception, ExpandedFilter> expandConcept(TermCode concept);

    default Either<Exception, List<ExpandedFilter>> expandComparatorFilterPart(LocalDate today, Comparator comparator, Quantity value) {
        if (isAge()) {
            return (value instanceof Quantity.Unitless)
                    ? Either.left(new CalculationException("Missing unit in age calculation."))
                    : AgeUtils.expandedAgeFilterFromComparator(today, comparator, (Quantity.WithUnit) value);
        }
        return Either.right(List.of(compositeCode()
                .map(compositeCode -> (ExpandedFilter) new ExpandedCompositeQuantityComparatorFilter(
                        searchParameter(), compositeCode, comparator, value))
                .orElse(new ExpandedQuantityComparatorFilter(searchParameter(), comparator, value))));
    }

    default Either<Exception, List<ExpandedFilter>> expandRangeFilterPart(LocalDate today, Quantity lowerBound, Quantity upperBound) {
        if (isAge()) {
            return (lowerBound instanceof Quantity.Unitless || upperBound instanceof Quantity.Unitless)
                    ? Either.left(new CalculationException("Missing unit in age calculation."))
                    : AgeUtils.expandedAgeFilterFromRange(today, (Quantity.WithUnit) lowerBound,
                    (Quantity.WithUnit) upperBound);
        }
        return Either.right(List.of(compositeCode()
                .map(compositeCode -> (ExpandedFilter) new ExpandedCompositeQuantityRangeFilter(
                        searchParameter(), compositeCode, lowerBound, upperBound))
                .orElse(new ExpandedQuantityRangeFilter(searchParameter(), lowerBound, upperBound))));
    }
}
