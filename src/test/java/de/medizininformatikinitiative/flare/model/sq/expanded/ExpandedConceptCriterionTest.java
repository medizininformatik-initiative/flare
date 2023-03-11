package de.medizininformatikinitiative.flare.model.sq.expanded;

import de.medizininformatikinitiative.flare.model.Query;
import de.medizininformatikinitiative.flare.model.sq.QueryParams;
import de.numcodex.sq2cql.model.common.TermCode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExpandedConceptCriterionTest {

    static final TermCode C71_1 = TermCode.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "C71.1",
            "Frontallappen");
    static final TermCode CONFIRMED = TermCode.of("http://terminology.hl7.org/CodeSystem/condition-ver-status",
            "confirmed", "Confirmed");
    static final TermCode SEVERE = TermCode.of("http://snomed.info/sct",
            "24484000", "Severe");

    @Test
    void toQuery() {
        var criterion = new ExpandedConceptCriterion("Condition", "code", C71_1, List.of());

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Condition", QueryParams.of("code", C71_1)));
    }

    @Test
    void toQuery_withOneAttributeFilter() {
        var criterion = new ExpandedConceptCriterion("Condition", "code", C71_1, List.of(new ConceptFilter(
                "verification-status", CONFIRMED)));

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Condition", QueryParams.EMPTY
                .appendParam("code", C71_1)
                .appendParam("verification-status", CONFIRMED)));
    }

    @Test
    void toQuery_withTwoAttributeFilters() {
        var criterion = new ExpandedConceptCriterion("Condition", "code", C71_1, List.of(new ConceptFilter(
                "verification-status", CONFIRMED), new ConceptFilter("severity", SEVERE)));

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Condition", QueryParams.EMPTY
                .appendParam("code", C71_1)
                .appendParam("verification-status", CONFIRMED)
                .appendParam("severity", SEVERE)));
    }
}
