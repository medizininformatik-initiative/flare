package de.medizininformatikinitiative.flare.model.mapping;

public class FilterTypeNotAllowedException extends MappingException {

    public FilterTypeNotAllowedException(AttributeMappingType type) {
        super("A fixed criterion of type %s is not allowed in the mapping file.".formatted(type));
    }
}
