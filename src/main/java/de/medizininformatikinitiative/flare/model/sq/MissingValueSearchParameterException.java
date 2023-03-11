package de.medizininformatikinitiative.flare.model.sq;

import de.numcodex.sq2cql.model.common.TermCode;

public class MissingValueSearchParameterException extends QueryTranslationException {

    private final TermCode termCode;

    public MissingValueSearchParameterException(TermCode termCode) {
        super("Value search parameter for mapping with system `%s`, code `%s` and display `%s` not found."
                .formatted(termCode.system(), termCode.code(), termCode.display()));
        this.termCode = termCode;
    }

    public TermCode termCode() {
        return termCode;
    }
}
