package de.medizininformatikinitiative.flare.model.sq;

import de.numcodex.sq2cql.model.common.TermCode;

public class AttributeMappingNotFoundException extends QueryTranslationException {

    public AttributeMappingNotFoundException(TermCode mappingCode, TermCode attributeCode) {
        super("Attribute mapping for code `%s` in mapping with code `%s` was  not found."
                .formatted(mappingCode, attributeCode));
    }
}
