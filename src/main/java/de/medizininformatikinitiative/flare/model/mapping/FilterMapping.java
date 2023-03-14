package de.medizininformatikinitiative.flare.model.mapping;

public interface FilterMapping {

    FilterType type();

    String searchParameter();

    /**
     * Returns {@code true} iff the filter should be mapped with special age handling.
     *
     * @return {@code true} iff the filter should be mapped with special age handling
     */
    boolean isAge();
}
