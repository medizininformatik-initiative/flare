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
                yield unit == null
                        ? ComparatorFilterPart.of(comparator, value)
                        : ComparatorFilterPart.of(comparator, value, unit.get("code").asText());
            }
            case "quantity-range" -> {
                var lowerBound = node.get("minValue").decimalValue();
                var upperBound = node.get("maxValue").decimalValue();
                var unit = node.get("unit");
                yield unit == null
                        ? RangeFilterPart.of(lowerBound, upperBound)
                        : RangeFilterPart.of(lowerBound, upperBound, unit.get("code").asText());
            }
            default -> throw new IllegalArgumentException("unknown filterPart type: " + type);
        };
    }

    Mono<List<ExpandedFilter>> expand(FilterMapping filterMapping);
}
