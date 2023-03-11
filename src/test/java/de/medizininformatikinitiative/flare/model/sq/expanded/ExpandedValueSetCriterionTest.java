package de.medizininformatikinitiative.flare.model.sq.expanded;

import de.medizininformatikinitiative.flare.model.Query;
import de.medizininformatikinitiative.flare.model.sq.QueryParams;
import de.numcodex.sq2cql.model.common.TermCode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExpandedValueSetCriterionTest {

    static final TermCode COVID = TermCode.of("http://loinc.org", "94500-6", "COVID");
    static final TermCode POSITIVE = TermCode.of("http://snomed.info/sct", "positive", "positive");
    static final TermCode VITAL_SIGNS = TermCode.of("http://terminology.hl7.org/CodeSystem/observation-category",
            "vital-signs", "Vital Signs");

    @Test
    void toQuery() {
        var criterion = new ExpandedValueSetCriterion("Observation", "code", COVID,
                new ConceptFilter("value-concept", POSITIVE), List.of());

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Observation", QueryParams.EMPTY
                .appendParam("code", COVID)
                .appendParam("value-concept", POSITIVE)));
    }

    @Test
    void toQuery_withOneAttributeFilter() {
        var criterion = new ExpandedValueSetCriterion("Observation", "code", COVID,
                new ConceptFilter("value-concept", POSITIVE), List.of(new CodeFilter("status", "final")));

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Observation", QueryParams.EMPTY
                .appendParam("code", COVID)
                .appendParam("value-concept", POSITIVE)
                .appendParam("status", "final")));
    }

    @Test
    void toQuery_withTwoAttributeFilters() {
        var criterion = new ExpandedValueSetCriterion("Observation", "code", COVID,
                new ConceptFilter("value-concept", POSITIVE), List.of(new CodeFilter("status", "final"),
                new ConceptFilter("category", VITAL_SIGNS)));

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Observation", QueryParams.EMPTY
                .appendParam("code", COVID)
                .appendParam("value-concept", POSITIVE)
                .appendParam("status", "final")
                .appendParam("category", VITAL_SIGNS)));
    }
}
