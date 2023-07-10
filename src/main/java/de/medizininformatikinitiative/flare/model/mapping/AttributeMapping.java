package de.medizininformatikinitiative.flare.model.mapping;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import de.medizininformatikinitiative.flare.model.sq.TermCode;

import java.util.Optional;

import static de.medizininformatikinitiative.flare.model.mapping.FilterType.*;
import static java.util.Objects.requireNonNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AttributeMapping(FilterType type, TermCode key, String searchParameter, Optional<TermCode> compositeCode)
        implements FilterMapping {

    public AttributeMapping {
        requireNonNull(type);
        requireNonNull(key);
        requireNonNull(searchParameter);
        requireNonNull(compositeCode);
    }

    //TODO: this is also not the normal FilterType
    @JsonCreator
    static AttributeMapping of(@JsonProperty("attributeType") FilterType type,
                               @JsonProperty("attributeKey") JsonNode key,
                               @JsonProperty("attributeSearchParameter") String searchParameter,
                               @JsonProperty("compositeCode") JsonNode compositeCode) {
        //TODO: check that type is one of the composite types before reading compositeCode
        //TODO: thow exception of type and composite code doesn't match
        return new AttributeMapping(type, TermCode.fromJsonNode(key), searchParameter,
                Optional.ofNullable(compositeCode).map(TermCode::fromJsonNode));
    }

    public static AttributeMapping code(TermCode key, String searchParameter) {
        return new AttributeMapping(CODE, key, searchParameter, Optional.empty());
    }

    public static AttributeMapping coding(TermCode key, String searchParameter) {
        return new AttributeMapping(CODING, key, searchParameter, Optional.empty());
    }

    public static AttributeMapping compositeComparator(TermCode key, String searchParameter, TermCode compositeCode) {
        return new AttributeMapping(COMPOSITE_QUANTITY_COMPARATOR, key, searchParameter, Optional.of(compositeCode));
    }

    public static AttributeMapping compositeRange(TermCode key, String searchParameter, TermCode compositeCode) {
        return new AttributeMapping(COMPOSITE_QUANTITY_RANGE, key, searchParameter, Optional.of(compositeCode));
    }

    public static AttributeMapping compositeConcept(TermCode key, String searchParameter, TermCode compositeCode) {
        return new AttributeMapping(COMPOSITE_CONCEPT_COMPARATOR, key, searchParameter, Optional.of(compositeCode));
    }

    @Override
    public boolean isAge() {
        return false;
    }
}
