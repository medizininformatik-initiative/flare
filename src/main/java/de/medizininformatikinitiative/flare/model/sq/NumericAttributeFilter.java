package de.medizininformatikinitiative.flare.model.sq;

import de.medizininformatikinitiative.flare.model.mapping.Mapping;
import de.medizininformatikinitiative.flare.model.sq.expanded.Filter;
import de.numcodex.sq2cql.model.common.Comparator;
import de.numcodex.sq2cql.model.common.TermCode;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;

import static java.util.Objects.requireNonNull;

public record NumericAttributeFilter(TermCode attributeCode,
                                     Comparator comparator,
                                     BigDecimal value,
                                     String unit) implements AttributeFilter {

    public NumericAttributeFilter {
        requireNonNull(attributeCode);
        requireNonNull(comparator);
        requireNonNull(value);
    }

    /**
     * Returns a {@code NumericAttributeFilter}.
     *
     * @param attributeCode the code identifying the attribute
     * @param comparator    the comparator that should be used in the value comparison
     * @param value         the value that should be used in the value comparison
     * @return the {@code NumericAttributeFilter}
     */
    public static NumericAttributeFilter of(TermCode attributeCode, Comparator comparator, BigDecimal value) {
        return new NumericAttributeFilter(attributeCode, comparator, value, null);
    }

    /**
     * Returns a {@code NumericAttributeFilter}.
     *
     * @param attributeCode the code identifying the attribute
     * @param comparator    the comparator that should be used in the value comparison
     * @param value         the value that should be used in the value comparison
     * @param unit          the unit of the value
     * @return the {@code NumericAttributeFilter}
     */
    public static NumericAttributeFilter of(TermCode attributeCode, Comparator comparator, BigDecimal value, String unit) {
        return new NumericAttributeFilter(attributeCode, comparator, value, requireNonNull(unit));
    }

    @Override
    public Flux<Filter> toFilter(Mapping mapping) {
        return Flux.empty();
    }
}
