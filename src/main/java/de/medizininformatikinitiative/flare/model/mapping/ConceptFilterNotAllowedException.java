package de.medizininformatikinitiative.flare.model.mapping;

public class ConceptFilterNotAllowedException extends Exception {
    public ConceptFilterNotAllowedException() {
        super("A single concept filter part that has a mapping of type reference is not allowed");
    }
}
