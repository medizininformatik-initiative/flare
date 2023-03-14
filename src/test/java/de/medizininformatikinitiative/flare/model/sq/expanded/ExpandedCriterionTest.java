package de.medizininformatikinitiative.flare.model.sq.expanded;

import de.medizininformatikinitiative.flare.model.fhir.Query;
import de.medizininformatikinitiative.flare.model.fhir.QueryParams;
import de.medizininformatikinitiative.flare.model.sq.Comparator;
import de.medizininformatikinitiative.flare.model.sq.TermCode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static de.medizininformatikinitiative.flare.model.sq.Comparator.*;
import static org.assertj.core.api.Assertions.assertThat;

class ExpandedCriterionTest {

    public static final TermCode UNIT = new TermCode("http://loinc.org", "ug/dL", "ug/dL");
    static final TermCode C71_1 = TermCode.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "C71.1",
                                              "Frontallappen");
    static final TermCode CONFIRMED = TermCode.of("http://terminology.hl7.org/CodeSystem/condition-ver-status",
                                                  "confirmed", "Confirmed");
    static final TermCode SEVERE = TermCode.of("http://snomed.info/sct",
                                               "24484000", "Severe");
    static final TermCode CORTISOL = TermCode.of("http://loinc.org", "2143-6", "Cortisol");
    static final String VALE_FILTER_SEARCH_PARAMETER = "value-quantity";
    static final Comparator FIRST_COMPARATOR_FILTER_COMPARATOR = GREATER_THAN;
    static final BigDecimal FIRST_COMPARATOR_FILTER_VALUE = BigDecimal.valueOf(7.3);
    static final Comparator SECOND_COMPARATOR_FILTER_COMPARATOR = GREATER_THAN;
    static final BigDecimal SECOND_COMPARATOR_FILTER_VALUE = BigDecimal.valueOf(10);
    public static final ExpandedComparatorFilter SECOND_COMPARATOR_FILTER = new ExpandedComparatorFilter(
            VALE_FILTER_SEARCH_PARAMETER, SECOND_COMPARATOR_FILTER_COMPARATOR, SECOND_COMPARATOR_FILTER_VALUE, UNIT);
    static final BigDecimal FIRST_RANGE_FILTER_LOWER_BOUND = BigDecimal.valueOf(17.9);
    static final BigDecimal FIRST_RANGE_FILTER_UPPER_BOUND = BigDecimal.valueOf(22);
    static final BigDecimal SECOND_RANGE_FILTER_LOWER_BOUND = BigDecimal.valueOf(30);
    static final BigDecimal SECOND_RANGE_FILTER_UPPER_BOUND = BigDecimal.valueOf(43.5);

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
    void toQuery_withOneComparatorFilter() {
        var criterion = ExpandedCriterion.of("Observation", "code", CORTISOL)
                .appendFilter(new ExpandedComparatorFilter(
                        VALE_FILTER_SEARCH_PARAMETER, FIRST_COMPARATOR_FILTER_COMPARATOR, FIRST_COMPARATOR_FILTER_VALUE,
                        UNIT));

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Observation", QueryParams.EMPTY
                .appendParam("code", CORTISOL)
                .appendParam(VALE_FILTER_SEARCH_PARAMETER, FIRST_COMPARATOR_FILTER_COMPARATOR,
                             FIRST_COMPARATOR_FILTER_VALUE, UNIT)));
    }

    @Test
    void toQuery_withTwoComparatorFilters() {
        var criterion = ExpandedCriterion.of("Observation", "code", CORTISOL)
                .appendFilter(new ExpandedComparatorFilter(
                        VALE_FILTER_SEARCH_PARAMETER, FIRST_COMPARATOR_FILTER_COMPARATOR, FIRST_COMPARATOR_FILTER_VALUE,
                        UNIT))
                .appendFilter(SECOND_COMPARATOR_FILTER);

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Observation", QueryParams.EMPTY
                .appendParam("code", CORTISOL)
                .appendParam(VALE_FILTER_SEARCH_PARAMETER, FIRST_COMPARATOR_FILTER_COMPARATOR,
                             FIRST_COMPARATOR_FILTER_VALUE, UNIT)
                .appendParam(VALE_FILTER_SEARCH_PARAMETER, FIRST_COMPARATOR_FILTER_COMPARATOR,
                             SECOND_COMPARATOR_FILTER_VALUE, UNIT)));

    }


    @Test
    void toQuery_withOneRangeFilter() {
        var criterion = ExpandedCriterion.of("Observation", "code", CORTISOL)
                .appendFilter(new ExpandedRangeFilter(VALE_FILTER_SEARCH_PARAMETER, FIRST_RANGE_FILTER_LOWER_BOUND,
                                                      FIRST_RANGE_FILTER_UPPER_BOUND, UNIT));

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Observation", QueryParams.EMPTY
                .appendParam("code", CORTISOL)
                .appendParam(VALE_FILTER_SEARCH_PARAMETER, GREATER_EQUAL, FIRST_RANGE_FILTER_LOWER_BOUND, UNIT)
                .appendParam(VALE_FILTER_SEARCH_PARAMETER, LESS_EQUAL, FIRST_RANGE_FILTER_UPPER_BOUND, UNIT)));
    }


    @Test
    void toQuery_withTowRangeFilters() {
        var criterion = ExpandedCriterion.of("Observation", "code", CORTISOL)
                .appendFilter(new ExpandedRangeFilter(VALE_FILTER_SEARCH_PARAMETER, FIRST_RANGE_FILTER_LOWER_BOUND,
                                                      FIRST_RANGE_FILTER_UPPER_BOUND, UNIT))
                .appendFilter(new ExpandedRangeFilter(VALE_FILTER_SEARCH_PARAMETER, SECOND_RANGE_FILTER_LOWER_BOUND,
                                                      SECOND_RANGE_FILTER_UPPER_BOUND, UNIT));

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Observation", QueryParams.EMPTY
                .appendParam("code", CORTISOL)
                .appendParam(VALE_FILTER_SEARCH_PARAMETER, GREATER_EQUAL, FIRST_RANGE_FILTER_LOWER_BOUND, UNIT)
                .appendParam(VALE_FILTER_SEARCH_PARAMETER, LESS_EQUAL, FIRST_RANGE_FILTER_UPPER_BOUND, UNIT)
                .appendParam(VALE_FILTER_SEARCH_PARAMETER, GREATER_EQUAL, SECOND_RANGE_FILTER_LOWER_BOUND, UNIT)
                .appendParam(VALE_FILTER_SEARCH_PARAMETER, LESS_EQUAL, SECOND_RANGE_FILTER_UPPER_BOUND, UNIT)));
    }

}
