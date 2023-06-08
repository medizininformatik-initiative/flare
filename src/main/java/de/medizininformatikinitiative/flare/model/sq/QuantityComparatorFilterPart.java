package de.medizininformatikinitiative.flare.model.sq;

import de.medizininformatikinitiative.flare.Either;
import de.medizininformatikinitiative.flare.model.mapping.FilterMapping;
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
    public Either<Exception, List<ExpandedFilter>> expand(LocalDate today, FilterMapping filterMapping, String referenceSearchParameter) {
        return filterMapping.expandComparatorFilterPart(today, comparator, value, referenceSearchParameter);
    }
}
