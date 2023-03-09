package de.medizininformatikinitiative.flare.model.sq;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.medizininformatikinitiative.flare.model.Query;
import de.medizininformatikinitiative.flare.model.mapping.MappingContext;
import de.numcodex.sq2cql.model.common.Comparator;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.structured_query.AttributeFilter;
import de.numcodex.sq2cql.model.structured_query.Concept;
import de.numcodex.sq2cql.model.structured_query.TimeRestriction;

import java.util.List;
import java.util.Optional;

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
                            @JsonProperty("timeRestriction") TimeRestriction conceptTimeRestriction,
                            @JsonProperty("attributeFilters") List<ObjectNode> attributeFilters) {
        var concept = Concept.of(requireNonNull(termCodes, "missing JSON property: termCodes"));

        var attributes = (attributeFilters == null ? List.<ObjectNode>of() : attributeFilters).stream()
                .map(AttributeFilter::fromJsonNode)
                .flatMap(Optional::stream)
                .toArray(AttributeFilter[]::new);

        if (valueFilter == null) {
            return ConceptCriterion.of(concept, conceptTimeRestriction, attributes);
        }

        var type = valueFilter.get("type").asText();
        if ("quantity-comparator".equals(type)) {
            var comparator = Comparator.fromJson(valueFilter.get("comparator").asText());
            var value = valueFilter.get("value").decimalValue();
            var unit = valueFilter.get("unit");
            if (unit == null) {
                return NumericCriterion.of(concept, comparator, value, conceptTimeRestriction, attributes);
            } else {
                return NumericCriterion.of(concept, comparator, value, unit.get("code").asText(),
                        conceptTimeRestriction, attributes);
            }
        }
        /*if ("quantity-range".equals(type)) {
            var lowerBound = valueFilter.get("minValue").decimalValue();
            var upperBound = valueFilter.get("maxValue").decimalValue();
            var unit = valueFilter.get("unit");
            if (unit == null) {
                return RangeCriterion.of(concept, lowerBound, upperBound, conceptTimeRestriction, attributes);
            } else {
                return RangeCriterion.of(concept, lowerBound, upperBound, unit.get("code").asText(), conceptTimeRestriction, attributes);
            }
        }
        if ("concept".equals(type)) {
            var selectedConcepts = valueFilter.get("selectedConcepts");
            if (selectedConcepts == null || selectedConcepts.isEmpty()) {
                throw new IllegalArgumentException("Missing or empty `selectedConcepts` key in concept criterion.");
            }
            return ValueSetCriterion.of(concept, StreamSupport.stream(selectedConcepts.spliterator(), false)
                    .map(TermCode::fromJsonNode).toList(), conceptTimeRestriction, attributes);
        }*/
        throw new IllegalArgumentException("unknown valueFilter type: " + type);
    }

    /**
     * Translates this criterion into a {@link Query}.
     *
     * @param mappingContext contains the mappings needed to create the CQL expression
     * @return a query
     */
    List<Query> toQuery(MappingContext mappingContext);
}
