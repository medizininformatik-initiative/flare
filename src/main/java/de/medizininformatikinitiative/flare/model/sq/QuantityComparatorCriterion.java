package de.medizininformatikinitiative.flare.model.sq;

import de.medizininformatikinitiative.flare.model.mapping.MappingContext;
import de.medizininformatikinitiative.flare.model.sq.expanded.ExpandedCriterion;
import de.numcodex.sq2cql.model.common.Comparator;
import de.numcodex.sq2cql.model.structured_query.Concept;
import de.numcodex.sq2cql.model.structured_query.TimeRestriction;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * A {@code QuantityComparatorCriterion} will select all patients that have at least one resource represented
 * by that concept and numeric value.
 * <p>
 * Examples are {@code Observation} resources representing the concept of a numeric laboratory
 * value.
 */
public record QuantityComparatorCriterion(Concept concept, List<AttributeFilter> attributeFilters,
                                          TimeRestriction timeRestriction, Comparator comparator,
                                          BigDecimal value, String unit) implements Criterion {

    /**
     * Returns a {@code QuantityComparatorCriterion}.
     *
     * @param concept          the concept the criterion represents
     * @param comparator       the comparator that should be used in the value comparison
     * @param value            the value that should be used in the value comparison
     * @param attributeFilters additional filters on particular attributes
     * @return the {@code QuantityComparatorCriterion}
     */
    public static QuantityComparatorCriterion of(Concept concept, Comparator comparator, BigDecimal value,
                                                 AttributeFilter... attributeFilters) {
        return new QuantityComparatorCriterion(concept, List.of(attributeFilters), null, requireNonNull(comparator),
                requireNonNull(value), null);
    }


    /**
     * Returns a {@code QuantityComparatorCriterion}.
     *
     * @param concept    the concept the criterion represents
     * @param comparator the comparator that should be used in the value comparison
     * @param value      the value that should be used in the value comparison
     * @param unit       the unit of the value
     * @return the {@code QuantityComparatorCriterion}
     */
    public static QuantityComparatorCriterion of(Concept concept, Comparator comparator, BigDecimal value, String unit) {
        return new QuantityComparatorCriterion(concept, List.of(), null, requireNonNull(comparator), requireNonNull(value),
                requireNonNull(unit));
    }


    /**
     * Returns a {@code QuantityComparatorCriterion}.
     *
     * @param concept          the concept the criterion represents
     * @param comparator       the comparator that should be used in the value comparison
     * @param value            the value that should be used in the value comparison
     * @param attributeFilters additional filters on particular attributes
     * @return the {@code QuantityComparatorCriterion}
     */
    public static QuantityComparatorCriterion of(Concept concept, Comparator comparator, BigDecimal value,
                                                 TimeRestriction timeRestriction,
                                                 AttributeFilter... attributeFilters) {
        return new QuantityComparatorCriterion(concept, List.of(attributeFilters), timeRestriction,
                requireNonNull(comparator),
                requireNonNull(value), null);
    }


    /**
     * Returns a {@code QuantityComparatorCriterion}.
     *
     * @param concept          the concept the criterion represents
     * @param comparator       the comparator that should be used in the value comparison
     * @param value            the value that should be used in the value comparison
     * @param attributeFilters additional filters on particular attributes
     * @param unit             the unit of the value
     * @return the {@code QuantityComparatorCriterion}
     */
    public static QuantityComparatorCriterion of(Concept concept, Comparator comparator, BigDecimal value,
                                                 String unit, TimeRestriction timeRestriction,
                                                 AttributeFilter... attributeFilters) {
        return new QuantityComparatorCriterion(concept, List.of(attributeFilters), timeRestriction,
                requireNonNull(comparator),
                requireNonNull(value), requireNonNull(unit));
    }

    public BigDecimal getValue() {
        return value;
    }

    @Override
    public Flux<ExpandedCriterion> expand(MappingContext mappingContext) {
        return Flux.empty();
    }
}
