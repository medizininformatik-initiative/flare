package de.medizininformatikinitiative.flare.model.mapping;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum AttributeMappingType implements JsonEnum {

    @JsonProperty("code") CODE(false),
    @JsonProperty("coding") CODING(false),
    @JsonProperty("composite-quantity-comparator") COMPOSITE_QUANTITY_COMPARATOR(true),
    @JsonProperty("composite-quantity-range") COMPOSITE_QUANTITY_RANGE(true),
    @JsonProperty("composite-concept") COMPOSITE_CONCEPT(true),
    @JsonProperty("reference") REFERENCE(false);

    private final boolean compositeType;

    AttributeMappingType(boolean compositeType) {
        this.compositeType = compositeType;
    }

    public boolean isCompositeType() {
        return compositeType;
    }

    @Override
    public String jsonName() {
        try {
            var field = AttributeMappingType.class.getField(name());
            return field.getAnnotation(JsonProperty.class).value();
        } catch (NoSuchFieldException e) {
            // can not happen
            throw new Error(e);
        }
    }
}
