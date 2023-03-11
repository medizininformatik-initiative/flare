package de.medizininformatikinitiative.flare.model.mapping;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import de.numcodex.sq2cql.model.common.TermCode;

import static de.medizininformatikinitiative.flare.model.mapping.AttributeMapping.Type.CODE;
import static de.medizininformatikinitiative.flare.model.mapping.AttributeMapping.Type.CODING;
import static java.util.Objects.requireNonNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AttributeMapping(Type type, TermCode key, String searchParameter) {

    public AttributeMapping {
        requireNonNull(type);
        requireNonNull(key);
        requireNonNull(searchParameter);
    }

    @JsonCreator
    public static AttributeMapping of(@JsonProperty("attributeType") Type type,
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

    public enum Type {
        @JsonProperty("code") CODE,
        @JsonProperty("coding") CODING
    }
}
