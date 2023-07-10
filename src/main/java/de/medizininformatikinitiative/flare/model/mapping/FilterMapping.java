package de.medizininformatikinitiative.flare.model.mapping;

import de.medizininformatikinitiative.flare.model.sq.TermCode;

import java.util.Optional;

public interface FilterMapping {

    FilterType type();

    String searchParameter();

    /**
     * Returns the optional composite code of this filter mapping.
     *
     * @return the composite code or {@link Optional#empty() empty}
     */
    Optional<TermCode> compositeCode();

    /**
     * Returns {@code true} iff the filter should be mapped with special age handling.
     *
     * @return {@code true} iff the filter should be mapped with special age handling
     */
    boolean isAge();
}
