package de.medizininformatikinitiative.flare.model.sq.expanded;

import de.medizininformatikinitiative.flare.model.fhir.Query;
import de.medizininformatikinitiative.flare.model.fhir.QueryParams;
import de.medizininformatikinitiative.flare.model.sq.Comparator;
import de.medizininformatikinitiative.flare.model.sq.TermCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;

import static de.medizininformatikinitiative.flare.model.sq.Comparator.GREATER_EQUAL;
import static de.medizininformatikinitiative.flare.model.sq.Comparator.LESS_EQUAL;
import static org.assertj.core.api.Assertions.assertThat;

class ExpandedCriterionTest {

    static final TermCode UNIT = new TermCode("http://unitsofmeasure.org", "ug/dL", "ug/dL");
    static final TermCode C71_1 = TermCode.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "C71.1", "Frontallappen");
    static final TermCode CONFIRMED = TermCode.of("http://terminology.hl7.org/CodeSystem/condition-ver-status",
            "confirmed", "Confirmed");
    static final TermCode SEVERE = TermCode.of("http://snomed.info/sct", "24484000", "Severe");
    static final TermCode CORTISOL = TermCode.of("http://loinc.org", "2143-6", "Cortisol");
    static final BigDecimal DECIMAL_LB = BigDecimal.valueOf(17.9);
    static final BigDecimal DECIMAL_UB = BigDecimal.valueOf(22);
    static final BigDecimal DECIMAL_LB_1 = BigDecimal.valueOf(23);
    static final BigDecimal DECIMAL_UB_1 = BigDecimal.valueOf(42);
    static final BigDecimal DECIMAL_LB_2 = BigDecimal.valueOf(30);
    static final BigDecimal DECIMAL_UB_2 = BigDecimal.valueOf(43.5);
    static final BigDecimal DECIMAL_1 = BigDecimal.valueOf(7.3);
    static final BigDecimal DECIMAL_2 = BigDecimal.valueOf(10);
    static final LocalDate LOCAL_DATE = LocalDate.of(1990, 10, 2);
    static final LocalDate LOCAL_DATE_1 = LocalDate.of(1990, 10, 2);
    static final LocalDate LOCAL_DATE_2 = LocalDate.of(2004, 4, 30);
    static final TermCode COMPOSITE_CODE = new TermCode("http://loing.org", "8480-6", "Sistolic Bloodpressure");
    public static final String COMPONENT_CODE_VALUE_QUANTITY = "component-code-value-quantity";
    public static final String COMPONENT_CODE_VALUE_CONCEPT = "component-code-value-concept";

    @Test
    void toQuery() {
        var criterion = ExpandedCriterion.of("Condition", "code", C71_1);

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Condition", QueryParams.of("code", C71_1)));
    }

    @Test
    void toQuery_withOneConceptFilter() {
        var criterion = ExpandedCriterion.of("Condition", "code", C71_1)
                .appendFilter(new ExpandedConceptFilter("verification-status", CONFIRMED, null));

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Condition", QueryParams.EMPTY
                .appendParam("code", C71_1)
                .appendParam("verification-status", CONFIRMED)));
    }

    @Test
    void toQuery_withTwoConceptFilters() {
        var criterion = ExpandedCriterion.of("Condition", "code", C71_1)
                .appendFilter(new ExpandedConceptFilter("verification-status", CONFIRMED, null))
                .appendFilter(new ExpandedConceptFilter("severity", SEVERE, null));

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Condition", QueryParams.EMPTY
                .appendParam("code", C71_1)
                .appendParam("verification-status", CONFIRMED)
                .appendParam("severity", SEVERE)));
    }

    @ParameterizedTest
    @EnumSource
    void toQuery_withOneComparatorFilter(Comparator comparator) {
        var criterion = ExpandedCriterion.of("Observation", "code", CORTISOL)
                .appendFilter(new ExpandedComparatorFilter("value-quantity", comparator, DECIMAL_1, UNIT, null));

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Observation", QueryParams.EMPTY
                .appendParam("code", CORTISOL)
                .appendParam("value-quantity", comparator, DECIMAL_1, UNIT, null)));
    }

    @ParameterizedTest
    @EnumSource
    void toQuery_withOneComparatorFilter_withoutUnit(Comparator comparator) {
        var criterion = ExpandedCriterion.of("Observation", "code", CORTISOL)
                .appendFilter(new ExpandedComparatorFilter("value-quantity", comparator, DECIMAL_1, null, null));

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Observation", QueryParams.EMPTY
                .appendParam("code", CORTISOL)
                .appendParam("value-quantity", comparator, DECIMAL_1, null, null)));
    }

    @ParameterizedTest
    @MethodSource("arityTwoComparatorArgumentProvider")
    void toQuery_withTwoComparatorFilters(Comparator comparator1, Comparator comparator2) {
        var criterion = ExpandedCriterion.of("Observation", "code", CORTISOL)
                .appendFilter(new ExpandedComparatorFilter("value-quantity", comparator1, DECIMAL_1, UNIT, null))
                .appendFilter(new ExpandedComparatorFilter("value-quantity", comparator2, DECIMAL_2, UNIT, null));

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Observation", QueryParams.EMPTY
                .appendParam("code", CORTISOL)
                .appendParam("value-quantity", comparator1, DECIMAL_1, UNIT, null)
                .appendParam("value-quantity", comparator2, DECIMAL_2, UNIT, null)));
    }

    @Test
    void toQuery_withOneRangeFilter() {
        var criterion = ExpandedCriterion.of("Observation", "code", CORTISOL)
                .appendFilter(new ExpandedRangeFilter("value-quantity", DECIMAL_LB, DECIMAL_UB, UNIT, null));

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Observation", QueryParams.EMPTY
                .appendParam("code", CORTISOL)
                .appendParam("value-quantity", GREATER_EQUAL, DECIMAL_LB, UNIT, null)
                .appendParam("value-quantity", LESS_EQUAL, DECIMAL_UB, UNIT, null)));
    }

    @Test
    void toQuery_withOneRangeFilter_withoutUnit() {
        var criterion = ExpandedCriterion.of("Observation", "code", CORTISOL)
                .appendFilter(new ExpandedRangeFilter("value-quantity", DECIMAL_LB, DECIMAL_UB, null, null));

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Observation", QueryParams.EMPTY
                .appendParam("code", CORTISOL)
                .appendParam("value-quantity", GREATER_EQUAL, DECIMAL_LB, null, null)
                .appendParam("value-quantity", LESS_EQUAL, DECIMAL_UB, null, null)));
    }

    @Test
    void toQuery_withTowRangeFilters() {
        var criterion = ExpandedCriterion.of("Observation", "code", CORTISOL)
                .appendFilter(new ExpandedRangeFilter("value-quantity", DECIMAL_LB_1, DECIMAL_UB_1, UNIT, null))
                .appendFilter(new ExpandedRangeFilter("value-quantity", DECIMAL_LB_2, DECIMAL_UB_2, UNIT, null));

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Observation", QueryParams.EMPTY
                .appendParam("code", CORTISOL)
                .appendParam("value-quantity", GREATER_EQUAL, DECIMAL_LB_1, UNIT, null)
                .appendParam("value-quantity", LESS_EQUAL, DECIMAL_UB_1, UNIT, null)
                .appendParam("value-quantity", GREATER_EQUAL, DECIMAL_LB_2, UNIT, null)
                .appendParam("value-quantity", LESS_EQUAL, DECIMAL_UB_2, UNIT, null)));
    }

    @ParameterizedTest
    @EnumSource
    void toQuery_withOneCompositeComparatorFilter(Comparator comparator) {
        var criterion = ExpandedCriterion.of("Observation", "code", CORTISOL)
                .appendFilter(new ExpandedComparatorFilter(COMPONENT_CODE_VALUE_QUANTITY, comparator, DECIMAL_1, UNIT, COMPOSITE_CODE));

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Observation", QueryParams.EMPTY
                .appendParam("code", CORTISOL)
                .appendParam(COMPONENT_CODE_VALUE_QUANTITY, comparator, DECIMAL_1, UNIT, COMPOSITE_CODE)));
    }

    @ParameterizedTest
    @EnumSource
    void toQuery_withTwoCompositeComparatorFilters(Comparator comparator) {
        var criterion = ExpandedCriterion.of("Observation", "code", CORTISOL)
                .appendFilter(new ExpandedComparatorFilter(COMPONENT_CODE_VALUE_QUANTITY, comparator, DECIMAL_1, UNIT, COMPOSITE_CODE))
                .appendFilter(new ExpandedComparatorFilter(COMPONENT_CODE_VALUE_QUANTITY, comparator, DECIMAL_2, UNIT, COMPOSITE_CODE));

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Observation", QueryParams.EMPTY
                .appendParam("code", CORTISOL)
                .appendParam(COMPONENT_CODE_VALUE_QUANTITY, comparator, DECIMAL_1, UNIT, COMPOSITE_CODE)
                .appendParam(COMPONENT_CODE_VALUE_QUANTITY, comparator, DECIMAL_2, UNIT, COMPOSITE_CODE)));
    }

    @Test
    void toQuery_withOneCompositeRangeFilter() {
        var criterion = ExpandedCriterion.of("Observation", "code", CORTISOL)
                .appendFilter(new ExpandedRangeFilter(COMPONENT_CODE_VALUE_QUANTITY, DECIMAL_1, DECIMAL_2, UNIT, COMPOSITE_CODE));

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Observation", QueryParams.EMPTY
                .appendParam("code", CORTISOL)
                .appendParam(COMPONENT_CODE_VALUE_QUANTITY, GREATER_EQUAL, DECIMAL_1, UNIT, COMPOSITE_CODE)
                .appendParam(COMPONENT_CODE_VALUE_QUANTITY, LESS_EQUAL, DECIMAL_2, UNIT, COMPOSITE_CODE)));
    }

    @Test
    void toQuery_withOneCompositeConceptFilter(){
        var criterion = ExpandedCriterion.of("Condition", "code", C71_1)
                .appendFilter(new ExpandedConceptFilter(COMPONENT_CODE_VALUE_CONCEPT, CONFIRMED, COMPOSITE_CODE));

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Condition", QueryParams.EMPTY
                .appendParam("code", C71_1)
                .appendParam(COMPONENT_CODE_VALUE_CONCEPT, CONFIRMED, COMPOSITE_CODE)));
    }

    @Test
    void toQuery_withTwoCompositeConceptFilters(){
        var criterion = ExpandedCriterion.of("Condition", "code", C71_1)
                .appendFilter(new ExpandedConceptFilter(COMPONENT_CODE_VALUE_CONCEPT, CONFIRMED, COMPOSITE_CODE))
                .appendFilter(new ExpandedConceptFilter(COMPONENT_CODE_VALUE_CONCEPT, SEVERE, COMPOSITE_CODE));

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Condition", QueryParams.EMPTY
                .appendParam("code", C71_1)
                .appendParam(COMPONENT_CODE_VALUE_CONCEPT, CONFIRMED, COMPOSITE_CODE)
                .appendParam(COMPONENT_CODE_VALUE_CONCEPT, SEVERE, COMPOSITE_CODE)));
    }

    @ParameterizedTest
    @EnumSource
    void toQuery_WithDateComparator(Comparator comparator) {
        var criterion = ExpandedCriterion.of("Observation", "code", CORTISOL)
                .appendFilter(new ExpandedDateComparatorFilter("birthdate", comparator, LOCAL_DATE));

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Observation", QueryParams.EMPTY
                .appendParam("code", CORTISOL)
                .appendParam("birthdate", comparator, LOCAL_DATE)));
    }

    @Test
    void toQuery_WithDateRange() {
        var criterion = ExpandedCriterion.of("Observation", "code", CORTISOL)
                .appendFilter(new ExpandedDateRangeFilter("birthdate", LOCAL_DATE_1, LOCAL_DATE_2));

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Observation", QueryParams.EMPTY
                .appendParam("code", CORTISOL)
                .appendParam("birthdate", GREATER_EQUAL, LOCAL_DATE_1)
                .appendParam("birthdate", LESS_EQUAL, LOCAL_DATE_2)));
    }

    @Test
    void toQuery_Patient_Gender() {
        var criterion = ExpandedCriterion.of("Patient")
                .appendFilter(new ExpandedCodeFilter("gender", "female"));

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Patient", QueryParams.EMPTY
                .appendParam("gender", "female")));
    }

    static Stream<Arguments> arityTwoComparatorArgumentProvider() {
        Stream.Builder<Arguments> argumentBuilder = Stream.builder();
        for (Comparator c1 : Comparator.values()) {
            for (Comparator c2 : Comparator.values()) {
                argumentBuilder.add(Arguments.of(c1, c2));
            }
        }
        return argumentBuilder.build();
    }
}
