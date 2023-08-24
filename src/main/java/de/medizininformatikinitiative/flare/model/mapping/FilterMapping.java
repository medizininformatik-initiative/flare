package de.medizininformatikinitiative.flare.model.mapping;

import de.medizininformatikinitiative.flare.Either;
import de.medizininformatikinitiative.flare.model.sq.*;
import de.medizininformatikinitiative.flare.model.sq.expanded.*;

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

    default Either<Exception, List<ExpandedFilter>> expandComparatorFilterPart(MappingContext mappingContext,
                                                                               Comparator comparator, Quantity value) {
        if (isAge()) {
            return (value instanceof Quantity.Unitless)
                    ? Either.left(new CalculationException("Missing unit in age calculation."))
                    : AgeUtils.expandedAgeFilterFromComparator(mappingContext.today(), comparator, (Quantity.WithUnit) value);
        }
        return Either.right(List.of(compositeCode()
                .map(compositeCode -> (ExpandedFilter) new ExpandedCompositeQuantityComparatorFilter(
                        searchParameter(), compositeCode, comparator, value))
                .orElse(new ExpandedQuantityComparatorFilter(searchParameter(), comparator, value))));
    }

    default Either<Exception, List<ExpandedFilter>> expandRangeFilterPart(MappingContext mappingContext,
                                                                          Quantity lowerBound, Quantity upperBound) {
        if (isAge()) {
            return (lowerBound instanceof Quantity.Unitless || upperBound instanceof Quantity.Unitless)
                    ? Either.left(new CalculationException("Missing unit in age calculation."))
                    : AgeUtils.expandedAgeFilterFromRange(mappingContext.today(), (Quantity.WithUnit) lowerBound,
                    (Quantity.WithUnit) upperBound);
        }
        return Either.right(List.of(compositeCode()
                .map(compositeCode -> (ExpandedFilter) new ExpandedCompositeQuantityRangeFilter(
                        searchParameter(), compositeCode, lowerBound, upperBound))
                .orElse(new ExpandedQuantityRangeFilter(searchParameter(), lowerBound, upperBound))));
    }

    /**
     * Expands the {@code criterion} into a list of {@code ExpandedFilter expanded filters} that should be combined with
     * logical {@literal OR}.
     *
     * @param mappingContext the context inside which the expansion should happen
     * @param criterion      the criterion to expand
     * @return either an error or a list of {@code ExpandedFilter expanded filters}
     */
    Either<Exception, List<ExpandedFilter>> expandReference(MappingContext mappingContext, Criterion criterion);
}
