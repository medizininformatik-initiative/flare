package de.medizininformatikinitiative.flare.model.sq;

import de.numcodex.sq2cql.model.structured_query.Concept;

public class ConceptNotExpandableException extends QueryTranslationException {

    public ConceptNotExpandableException(Concept concept) {
        super("The concept `%s` is not expandable.".formatted(concept));
    }
}
