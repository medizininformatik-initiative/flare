package de.medizininformatikinitiative.flare.model.sq;

import de.medizininformatikinitiative.flare.model.mapping.Mapping;
import de.medizininformatikinitiative.flare.model.sq.expanded.Filter;
import de.numcodex.sq2cql.model.common.TermCode;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;

import static java.util.Objects.requireNonNull;

public record RangeAttributeFilter(TermCode attributeCode,
                                   BigDecimal lowerBound,
                                   BigDecimal upperBound,
                                   String unit) implements AttributeFilter {

    public RangeAttributeFilter {
        requireNonNull(attributeCode);
        requireNonNull(lowerBound);
        requireNonNull(upperBound);
    }

    public static RangeAttributeFilter of(TermCode attributeCode, BigDecimal lowerBound, BigDecimal upperBound) {
        return new RangeAttributeFilter(attributeCode, lowerBound, upperBound, null);
    }

    public static RangeAttributeFilter of(TermCode attributeCode, BigDecimal lowerBound, BigDecimal upperBound, String unit) {
        return new RangeAttributeFilter(attributeCode, lowerBound, upperBound, requireNonNull(unit));
    }

    @Override
    public Flux<Filter> toFilter(Mapping mapping) {
        return Flux.empty();
    }
}
