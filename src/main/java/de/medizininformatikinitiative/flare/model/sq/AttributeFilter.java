package de.medizininformatikinitiative.flare.model.sq;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import de.medizininformatikinitiative.flare.model.mapping.Mapping;
import de.medizininformatikinitiative.flare.model.sq.expanded.Filter;
import de.numcodex.sq2cql.model.common.Comparator;
import de.numcodex.sq2cql.model.common.TermCode;
import reactor.core.publisher.Flux;

import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public interface AttributeFilter {

    /**
     * Tries to parse an {@code AttributeFilter}.
     * <p>
     * Returns {@link Optional#empty() nothing} if the filter is of type concept and there are no concepts given.
     *
     * @param node the JSON representation of the {@code AttributeFilter}
     * @return either the {@code AttributeFilter} or {@link Optional#empty() nothing} if the {@code AttributeFilter} is empty
     */
    static Optional<AttributeFilter> fromJsonNode(JsonNode node) {
        var code = TermCode.fromJsonNode(node.get("attributeCode"));
        var type = node.get("type").asText();
        if ("quantity-comparator".equals(type)) {
            var comparator = Comparator.fromJson(node.get("comparator").asText());
            var value = node.get("value").decimalValue();
            var unit = node.get("unit");
            if (unit == null) {
                return Optional.of(NumericAttributeFilter.of(code, comparator, value));
            } else {
                return Optional.of(NumericAttributeFilter.of(code, comparator, value, unit.get("code").asText()));
            }
        }
        if ("quantity-range".equals(type)) {
            var lowerBound = node.get("minValue").decimalValue();
            var upperBound = node.get("maxValue").decimalValue();
            var unit = node.get("unit");
            if (unit == null) {
                return Optional.of(RangeAttributeFilter.of(code, lowerBound, upperBound));
            } else {
                return Optional.of(RangeAttributeFilter.of(code, lowerBound, upperBound, unit.get("code").asText()));
            }
        }
        if ("concept".equals(type)) {
            var selectedConcepts = node.get("selectedConcepts");
            if (selectedConcepts == null || selectedConcepts.isEmpty()) {
                return Optional.empty();
            } else {
                return Optional.of(ConceptAttributeFilter.of(code, TermCode.fromJsonNode(selectedConcepts.get(0))));
            }
        }
        throw new IllegalArgumentException("unknown valueFilter type: " + type);
    }

    Flux<Filter> toFilter(Mapping mapping);
}
