package de.medizininformatikinitiative.flare.model.sq;

import de.medizininformatikinitiative.flare.model.mapping.AttributeMappingType;

public class ConceptFilterTypeNotExpandableException extends Exception {
    public ConceptFilterTypeNotExpandableException(AttributeMappingType type) {
        super("Concept Filter of Mapping Filter Type %s could not be expanded.".formatted(type.name()));
    }
}
