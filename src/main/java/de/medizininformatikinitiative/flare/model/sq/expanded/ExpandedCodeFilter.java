package de.medizininformatikinitiative.flare.model.sq.expanded;

import de.medizininformatikinitiative.flare.model.fhir.QueryParams;

/**
 * A value filterPart using a single code as value.
 *
 * @param searchParameter the FHIR search parameter code to use for the value
 * @param code            the code to search for
 */
public record ExpandedCodeFilter(String searchParameter, String code) implements ExpandedFilter {

    @Override
    public QueryParams toParams() {
        return QueryParams.of(searchParameter, code);
    }
}
