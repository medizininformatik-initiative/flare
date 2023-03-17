package de.medizininformatikinitiative.flare.model.mapping;

import de.medizininformatikinitiative.flare.model.sq.Concept;

public class ConceptNotExpandableException extends MappingException {

    public ConceptNotExpandableException(Concept concept) {
        super("The concept `%s` is not expandable.".formatted(concept));
    }
}
