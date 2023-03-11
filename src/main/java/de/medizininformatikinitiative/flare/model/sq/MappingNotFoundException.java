package de.medizininformatikinitiative.flare.model.sq;

import de.numcodex.sq2cql.model.common.TermCode;

public class MappingNotFoundException extends QueryTranslationException {

    public MappingNotFoundException(TermCode termCode) {
        super("Mapping for code `%s` not found.".formatted(termCode));
    }
}
