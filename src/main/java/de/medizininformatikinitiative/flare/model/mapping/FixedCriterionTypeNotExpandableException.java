package de.medizininformatikinitiative.flare.model.mapping;

public class FixedCriterionTypeNotExpandableException extends Exception {

    public FixedCriterionTypeNotExpandableException(FilterType type) {
        super("A fixed criterion of type %s is not allowed in the mapping file.".formatted(type.name()));
    }
}
