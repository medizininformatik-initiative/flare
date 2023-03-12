package de.medizininformatikinitiative.flare.model.sq.expanded;

import de.medizininformatikinitiative.flare.model.fhir.QueryParams;
import de.medizininformatikinitiative.flare.model.sq.TermCode;

/**
 * A value filterPart using a concept as value.
 *
 * @param searchParameter the FHIR search parameter code to use for the value
 * @param termCode        the code to search for
 */
public record ExpandedConceptFilter(String searchParameter, TermCode termCode) implements ExpandedFilter {

    @Override
    public QueryParams toParams() {
        return QueryParams.of(searchParameter, termCode);
    }
}
