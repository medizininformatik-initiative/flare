package de.medizininformatikinitiative.flare.model.sq.expanded;

import de.medizininformatikinitiative.flare.model.sq.QueryParams;

/**
 * A value filter using a single code as value.
 *
 * @param searchParameter the FHIR search parameter code to use for the value
 * @param code            the code to search for
 */
public record CodeFilter(String searchParameter, String code) implements Filter {

    public QueryParams toParams() {
        return QueryParams.of(searchParameter, code);
    }
}
