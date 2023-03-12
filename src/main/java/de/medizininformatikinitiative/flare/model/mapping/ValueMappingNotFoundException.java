package de.medizininformatikinitiative.flare.model.mapping;

import de.medizininformatikinitiative.flare.model.sq.TermCode;

public class ValueMappingNotFoundException extends Exception {

    public ValueMappingNotFoundException(TermCode termCode) {
        super("Value mapping for mapping with code `%s` was not found.".formatted(termCode));
    }
}
