package de.medizininformatikinitiative.flare.model.sq.expanded;

import de.medizininformatikinitiative.flare.model.fhir.Query;
import de.medizininformatikinitiative.flare.model.fhir.QueryParams;
import de.medizininformatikinitiative.flare.model.sq.Comparator;
import de.medizininformatikinitiative.flare.model.sq.TermCode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ExpandedCriterionTest {

    static final TermCode C71_1 = TermCode.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "C71.1",
            "Frontallappen");
    static final TermCode CONFIRMED = TermCode.of("http://terminology.hl7.org/CodeSystem/condition-ver-status",
            "confirmed", "Confirmed");
    static final TermCode SEVERE = TermCode.of("http://snomed.info/sct",
            "24484000", "Severe");
    public static final TermCode UNIT = new TermCode("http://loinc.org", "ug/dL", "ug/dL");

    static final TermCode CORTISOL = TermCode.of("http://loinc.org", "2143-6", "Cortisol");
    public static final ExpandedComparatorFilter FIRST_COMPARATOR_FILTER = new ExpandedComparatorFilter(
            "value-quantity", Comparator.GREATER_THAN, BigDecimal.valueOf(7.3), UNIT);
    public static final ExpandedComparatorFilter SECOND_COMPARATOR_FILTER = new ExpandedComparatorFilter(
            "value-quantity", Comparator.LESS_EQUAL, BigDecimal.valueOf(10), UNIT);

    public static final ExpandedRangeFilter FIRST_RANGE_FILTER = new ExpandedRangeFilter("value-quantity",
            BigDecimal.valueOf(17.9), BigDecimal.valueOf(22), UNIT);
    public static final ExpandedRangeFilter SECOND_RANGE_FILTER = new ExpandedRangeFilter("value-quantity",
            BigDecimal.valueOf(30), BigDecimal.valueOf(43.5), UNIT);

    @Test
    void toQuery() {
        var criterion = ExpandedCriterion.of("Condition", "code", C71_1);

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Condition", QueryParams.of("code", C71_1)));
    }

    @Test
    void toQuery_withOneConceptFilter() {
        var criterion = ExpandedCriterion.of("Condition", "code", C71_1)
                .appendFilter(new ExpandedConceptFilter("verification-status", CONFIRMED));

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Condition", QueryParams.EMPTY
                .appendParam("code", C71_1)
                .appendParam("verification-status", CONFIRMED)));
    }

    @Test
    void toQuery_withTwoConceptFilters() {
        var criterion = ExpandedCriterion.of("Condition", "code", C71_1)
                .appendFilter(new ExpandedConceptFilter("verification-status", CONFIRMED))
                .appendFilter(new ExpandedConceptFilter("severity", SEVERE));

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Condition", QueryParams.EMPTY
                .appendParam("code", C71_1)
                .appendParam("verification-status", CONFIRMED)
                .appendParam("severity", SEVERE)));
    }

    @Test
    void toQuery_withOneComparatorFilter(){
        var criterion = ExpandedCriterion.of("Observation", "code", CORTISOL)
                .appendFilter(FIRST_COMPARATOR_FILTER);

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Observation", QueryParams.EMPTY
                .appendParam("code", CORTISOL)
                .appendParam(FIRST_COMPARATOR_FILTER.searchParameter(),
                        FIRST_COMPARATOR_FILTER.comparator().toString() + FIRST_COMPARATOR_FILTER.value()
                                + unitAttachment(UNIT))));
    }

    @Test
    void toQuery_withTwoComparatorFilters(){
        var criterion = ExpandedCriterion.of("Observation", "code", CORTISOL)
                .appendFilter(FIRST_COMPARATOR_FILTER)
                .appendFilter(SECOND_COMPARATOR_FILTER);

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Observation", QueryParams.EMPTY
                .appendParam("code", CORTISOL)
                .appendParam(FIRST_COMPARATOR_FILTER.searchParameter(),
                        FIRST_COMPARATOR_FILTER.comparator().toString() + FIRST_COMPARATOR_FILTER.value()
                                + unitAttachment(UNIT))
                .appendParam(FIRST_COMPARATOR_FILTER.searchParameter(),
                        FIRST_COMPARATOR_FILTER.comparator().toString() + SECOND_COMPARATOR_FILTER.value()
                                + unitAttachment(UNIT))));

    }


    @Test
    void toQuery_withOneRangeFilter(){
        var criterion = ExpandedCriterion.of("Observation", "code", CORTISOL)
                .appendFilter(FIRST_RANGE_FILTER);

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Observation", QueryParams.EMPTY
                .appendParam("code", CORTISOL)
                .appendParam(FIRST_RANGE_FILTER.searchParameter(),
                        Comparator.GREATER_EQUAL.toString() + FIRST_RANGE_FILTER.lowerBound()
                                + unitAttachment(UNIT))
                .appendParam(FIRST_RANGE_FILTER.searchParameter(),
                        Comparator.LESS_EQUAL.toString() + FIRST_RANGE_FILTER.lowerBound()
                                + unitAttachment(UNIT))));
    }


    @Test
    void toQuery_withTowRangeFilters(){
        var criterion = ExpandedCriterion.of("Observation", "code", CORTISOL)
                .appendFilter(FIRST_RANGE_FILTER)
                .appendFilter(SECOND_RANGE_FILTER);

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Observation", QueryParams.EMPTY
                .appendParam("code", CORTISOL)
                .appendParam(FIRST_RANGE_FILTER.searchParameter(),
                        Comparator.GREATER_EQUAL.toString() + FIRST_RANGE_FILTER.lowerBound()
                                + unitAttachment(UNIT))
                .appendParam(FIRST_RANGE_FILTER.searchParameter(),
                        Comparator.LESS_EQUAL.toString() + FIRST_RANGE_FILTER.lowerBound()
                                + unitAttachment(UNIT))
                .appendParam(SECOND_RANGE_FILTER.searchParameter(),
                        Comparator.GREATER_EQUAL.toString() + SECOND_RANGE_FILTER.lowerBound()
                                + unitAttachment(UNIT))
                .appendParam(SECOND_RANGE_FILTER.searchParameter(),
                        Comparator.LESS_EQUAL.toString() + SECOND_RANGE_FILTER.lowerBound()
                                + unitAttachment(UNIT))));
    }

    private String unitAttachment(TermCode unit){
        return "|" + unit.system() + "|" + unit.code();
    }
}
