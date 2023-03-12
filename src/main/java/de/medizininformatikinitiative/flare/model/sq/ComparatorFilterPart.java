package de.medizininformatikinitiative.flare.model.sq;

import de.medizininformatikinitiative.flare.model.mapping.FilterMapping;
import de.medizininformatikinitiative.flare.model.sq.expanded.ExpandedFilter;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;

import static java.util.Objects.requireNonNull;

public record ComparatorFilterPart(Comparator comparator, BigDecimal value, String unit) implements FilterPart {

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
    public static ComparatorFilterPart of(Comparator comparator, BigDecimal value, String unit) {
        return new ComparatorFilterPart(comparator, value, requireNonNull(unit));
    }

    @Override
    public Flux<ExpandedFilter> expand(FilterMapping filterMapping) {
        return Flux.empty();
    }
}
