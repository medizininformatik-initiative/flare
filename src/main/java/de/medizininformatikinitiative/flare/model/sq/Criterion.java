package de.medizininformatikinitiative.flare.model.sq;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.medizininformatikinitiative.flare.model.mapping.MappingContext;
import de.medizininformatikinitiative.flare.model.sq.expanded.ExpandedCriterion;
import de.numcodex.sq2cql.model.common.Comparator;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.structured_query.Concept;
import de.numcodex.sq2cql.model.structured_query.TimeRestriction;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static java.util.Objects.requireNonNull;

/**
 * A single, atomic criterion in Structured Query.
 *
 * @author Alexander Kiel
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public interface Criterion {

    @JsonCreator
    static Criterion create(@JsonProperty("termCodes") List<TermCode> termCodes,
                            @JsonProperty("valueFilter") ObjectNode valueFilter,
                            @JsonProperty("timeRestriction") TimeRestriction timeRestriction,
                            @JsonProperty("attributeFilters") List<ObjectNode> attributeFilters) {
        var concept = Concept.of(requireNonNull(termCodes, "missing JSON property: termCodes"));

        var attributes = (attributeFilters == null ? List.<ObjectNode>of() : attributeFilters).stream()
                .map(AttributeFilter::fromJsonNode)
                .flatMap(Optional::stream)
                .toList();

        if (valueFilter == null) {
            return new ConceptCriterion(concept, attributes, null);
        }

        var type = valueFilter.get("type").asText();
        if ("quantity-comparator".equals(type)) {
            var comparator = Comparator.fromJson(valueFilter.get("comparator").asText());
            var value = valueFilter.get("value").decimalValue();
            var unit = valueFilter.get("unit");
            if (unit == null) {
                return QuantityComparatorCriterion.of(concept, comparator, value, timeRestriction, attributes.toArray(AttributeFilter[]::new));
            } else {
                return QuantityComparatorCriterion.of(concept, comparator, value, unit.get("code").asText(),
                        timeRestriction, attributes.toArray(AttributeFilter[]::new));
            }
        }
        /*if ("quantity-range".equals(type)) {
            var lowerBound = valueFilter.get("minValue").decimalValue();
            var upperBound = valueFilter.get("maxValue").decimalValue();
            var unit = valueFilter.get("unit");
            if (unit == null) {
                return RangeCriterion.of(concept, lowerBound, upperBound, timeRestriction, attributes);
            } else {
                return RangeCriterion.of(concept, lowerBound, upperBound, unit.get("code").asText(), timeRestriction, attributes);
            }
        }*/
        if ("concept".equals(type)) {
            var selectedConcepts = valueFilter.get("selectedConcepts");
            if (selectedConcepts == null || selectedConcepts.isEmpty()) {
                throw new IllegalArgumentException("Missing or empty `selectedConcepts` key in concept criterion.");
            }
            return ValueSetCriterion.of(concept, StreamSupport.stream(selectedConcepts.spliterator(), false)
                    .map(TermCode::fromJsonNode).toList(), timeRestriction, attributes.toArray(AttributeFilter[]::new));
        }
        throw new IllegalArgumentException("unknown valueFilter type: " + type);
    }

    /**
     * Expands this criterion into a {@link Flux flux} of {@link ExpandedCriterion expanded criteria}.
     *
     * @param mappingContext contains the mappings needed to create the expanded criteria
     * @return a {@link Flux flux} of {@link ExpandedCriterion expanded criteria}
     */
    Flux<ExpandedCriterion> expand(MappingContext mappingContext);
}
