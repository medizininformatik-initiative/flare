package de.medizininformatikinitiative.flare.model.sq.expanded;

import de.medizininformatikinitiative.flare.model.sq.QueryParams;
import de.numcodex.sq2cql.model.common.TermCode;

/**
 * A value filter using a concept as value.
 *
 * @param searchParameter the FHIR search parameter code to use for the value
 * @param termCode        the code to search for
 */
public record ConceptFilter(String searchParameter, TermCode termCode) implements Filter {

    @Override
    public QueryParams toParams() {
        return QueryParams.of(searchParameter, termCode);
    }
}
