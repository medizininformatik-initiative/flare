package de.medizininformatikinitiative.flare.model.mapping;

public class FixedCriterionTypeNotExpandableException extends Exception{
    public FixedCriterionTypeNotExpandableException(FilterType type){
        super("A Fixed Criterion of Type %s is not allowed in the Mapping File.".formatted(type.name()));
    }
}
