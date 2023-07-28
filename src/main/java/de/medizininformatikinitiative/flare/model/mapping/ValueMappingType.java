package de.medizininformatikinitiative.flare.model.mapping;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ValueMappingType {
    @JsonProperty("code") CODE,
    @JsonProperty("coding") CODING
}
