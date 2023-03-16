package de.medizininformatikinitiative.flare.model.mapping;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import de.medizininformatikinitiative.flare.model.sq.TermCode;

import static de.medizininformatikinitiative.flare.model.mapping.FilterType.CODE;
import static de.medizininformatikinitiative.flare.model.mapping.FilterType.CODING;
import static java.util.Objects.requireNonNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AttributeMapping(FilterType type, TermCode key, String searchParameter) implements FilterMapping {

    public AttributeMapping {
        requireNonNull(type);
        requireNonNull(key);
        requireNonNull(searchParameter);
    }

    @JsonCreator
    static AttributeMapping of(@JsonProperty("attributeType") FilterType type,
                               @JsonProperty("attributeKey") JsonNode key,
                               @JsonProperty("attributeSearchParameter") String searchParameter) {
        return new AttributeMapping(type, TermCode.fromJsonNode(key), searchParameter);
    }

    public static AttributeMapping code(TermCode key, String searchParameter) {
        return new AttributeMapping(CODE, key, searchParameter);
    }

    public static AttributeMapping coding(TermCode key, String searchParameter) {
        return new AttributeMapping(CODING, key, searchParameter);
    }

    @Override
    public boolean isAge() {
        return false;
    }
}
