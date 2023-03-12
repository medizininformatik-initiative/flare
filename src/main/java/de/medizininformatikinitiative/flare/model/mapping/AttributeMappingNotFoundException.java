package de.medizininformatikinitiative.flare.model.mapping;

import de.medizininformatikinitiative.flare.model.sq.TermCode;

public class AttributeMappingNotFoundException extends Exception {

    public AttributeMappingNotFoundException(TermCode mappingCode, TermCode attributeCode) {
        super("Attribute mapping for code `%s` in mapping with code `%s` was  not found."
                .formatted(mappingCode, attributeCode));
    }
}
