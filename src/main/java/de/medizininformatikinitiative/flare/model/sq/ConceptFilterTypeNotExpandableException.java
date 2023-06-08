package de.medizininformatikinitiative.flare.model.sq;

import de.medizininformatikinitiative.flare.model.mapping.FilterType;

public class ConceptFilterTypeNotExpandableException  extends  Exception{
    public ConceptFilterTypeNotExpandableException(FilterType type){
        super("A Concept Filter of Type %s should not be possible and cannot be expanded.".formatted(type.name()));
    }
}
