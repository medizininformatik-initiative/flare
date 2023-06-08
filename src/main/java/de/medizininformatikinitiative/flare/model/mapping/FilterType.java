package de.medizininformatikinitiative.flare.model.mapping;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum FilterType {
    @JsonProperty("code") CODE,
    @JsonProperty("coding") CODING,
    @JsonProperty("composite-quantity-comparator") COMPOSITE_QUANTITY_COMPARATOR,
    @JsonProperty("composite-quantity-range") COMPOSITE_QUANTITY_RANGE,
    @JsonProperty("composite-quantity-concept") COMPOSITE_CONCEPT_COMPARATOR,
}
