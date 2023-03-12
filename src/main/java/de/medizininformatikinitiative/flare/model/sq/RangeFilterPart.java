package de.medizininformatikinitiative.flare.model.sq;

import de.medizininformatikinitiative.flare.model.mapping.FilterMapping;
import de.medizininformatikinitiative.flare.model.sq.expanded.ExpandedFilter;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

import static java.util.Objects.requireNonNull;

public record RangeFilterPart(BigDecimal lowerBound, BigDecimal upperBound, String unit) implements FilterPart {

    public RangeFilterPart {
        requireNonNull(lowerBound);
        requireNonNull(upperBound);
    }

    public static RangeFilterPart of(BigDecimal lowerBound, BigDecimal upperBound) {
        return new RangeFilterPart(lowerBound, upperBound, null);
    }

    public static RangeFilterPart of(BigDecimal lowerBound, BigDecimal upperBound, String unit) {
        return new RangeFilterPart(lowerBound, upperBound, requireNonNull(unit));
    }

    @Override
    public Mono<List<ExpandedFilter>> expand(FilterMapping filterMapping) {
        return Mono.just(List.of());
    }
}
