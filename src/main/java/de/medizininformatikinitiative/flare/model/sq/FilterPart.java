package de.medizininformatikinitiative.flare.model.sq;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import de.medizininformatikinitiative.flare.model.mapping.FilterMapping;
import de.medizininformatikinitiative.flare.model.sq.expanded.ExpandedFilter;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.StreamSupport;

@JsonIgnoreProperties(ignoreUnknown = true)
public interface FilterPart {

    /**
     * Parses a value filterPart.
     *
     * @param node the JSON representation of the value filterPart
     * @return the parsed value filterPart
     * @throws IllegalArgumentException if the JSON isn't valid
     */
    static FilterPart fromJsonNode(JsonNode node) {
        var type = node.get("type").asText();

        return switch (type) {
            case "concept" -> {
                var selectedConcepts = node.get("selectedConcepts");
                if (selectedConcepts == null || selectedConcepts.isEmpty()) {
                    throw new IllegalArgumentException("empty selectedConcepts");
                } else {
                    yield new ConceptFilterPart(StreamSupport.stream(selectedConcepts.spliterator(), false)
                                                        .map(TermCode::fromJsonNode).toList());
                }
            }
            case "quantity-comparator" -> {
                var comparator = Comparator.fromJson(node.get("comparator").asText());
                var value = node.get("value").decimalValue();
                var unit = node.get("unit");
                if (unit == null) {
                    yield ComparatorFilterPart.of(comparator, value);
                } else {
                    yield ComparatorFilterPart.of(comparator, value, getUnitDetails(unit));
                }
            }
            case "quantity-range" -> {
                var lowerBound = node.get("minValue").decimalValue();
                var upperBound = node.get("maxValue").decimalValue();
                var unit = node.get("unit");
                if (unit == null) {
                    yield RangeFilterPart.of(lowerBound, upperBound);
                } else {
                    yield RangeFilterPart.of(lowerBound, upperBound, getUnitDetails(unit));
                }
            }
            default -> throw new IllegalArgumentException("unknown filterPart type: " + type);
        };
    }

    private static TermCode getUnitDetails(JsonNode unit) {
        var system = unit.get("system") == null ? "http://unitsofmeasure.org" : unit.get("system").asText();
        return new TermCode(system, unit.get("code").asText(), unit.get("display").asText());
    }

    Mono<List<ExpandedFilter>> expand(FilterMapping filterMapping);
}
