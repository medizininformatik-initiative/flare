package de.medizininformatikinitiative.flare.model.mapping;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import de.medizininformatikinitiative.flare.model.sq.TermCode;

import static de.medizininformatikinitiative.flare.model.mapping.FilterType.*;
import static java.util.Objects.requireNonNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AttributeMapping(FilterType type, TermCode key, String searchParameter, TermCode compositeCode) implements FilterMapping {

    public AttributeMapping {
        requireNonNull(type);
        requireNonNull(key);
        requireNonNull(searchParameter);
    }

    @JsonCreator
    static AttributeMapping of(@JsonProperty("attributeType") FilterType type,
                               @JsonProperty("attributeKey") JsonNode key,
                               @JsonProperty("attributeSearchParameter") String searchParameter,
                               @JsonProperty("compositeCode") JsonNode compositeCode) {
        TermCode compCode = compositeCode == null ? null : TermCode.fromJsonNode(compositeCode);
        return new AttributeMapping(type, TermCode.fromJsonNode(key), searchParameter, compCode);
    }

    public static AttributeMapping code(TermCode key, String searchParameter) {
        return new AttributeMapping(CODE, key, searchParameter, null);
    }

    public static AttributeMapping coding(TermCode key, String searchParameter) {
        return new AttributeMapping(CODING, key, searchParameter, null);
    }

    public static AttributeMapping compositeComparator(TermCode key, String searchParameter, TermCode compositeCode) {
        return new AttributeMapping(COMPOSITE_QUANTITY_COMPARATOR, key, searchParameter, compositeCode);
    }

    public static AttributeMapping compositeRange(TermCode key, String searchParameter, TermCode compositeCode) {
        return new AttributeMapping(COMPOSITE_QUANTITY_RANGE, key, searchParameter, compositeCode);
    }

    public static AttributeMapping compositeConcept(TermCode key, String searchParameter, TermCode compositeCode) {
        return new AttributeMapping(COMPOSITE_CONCEPT_COMPARATOR, key, searchParameter, compositeCode);
    }

    public static AttributeMapping reference(TermCode key, String searchParameter) {
        return new AttributeMapping(REFERENCE, key, searchParameter, null);
    }

    @Override
    public boolean isAge() {
        return false;
    }
}
