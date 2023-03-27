package de.medizininformatikinitiative.flare.model.mapping;

import de.medizininformatikinitiative.flare.model.sq.Concept;

public class ConceptNotExpandableException extends MappingException {

    public ConceptNotExpandableException(Concept concept) {
        super("None of the following term codes %s was found in the code tree. Please check the code tree configured by the env var FLARE_MAPPING_CONCEPTTREEFILE.".formatted(concept));
    }
}
