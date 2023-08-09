package de.medizininformatikinitiative.flare.model.sq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.medizininformatikinitiative.flare.Either;
import de.medizininformatikinitiative.flare.model.mapping.*;
import de.medizininformatikinitiative.flare.model.sq.expanded.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.medizininformatikinitiative.flare.Assertions.assertThat;
import static de.medizininformatikinitiative.flare.model.sq.Comparator.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CriterionTest {

    static final String COMPONENT_CODE_VALUE_QUANTITY = "component-code-value-quantity";
    static final TermCode VERIFICATION_STATUS = TermCode.of("http://hl7.org", "verification-status",
            "Verification Status");
    static final TermCode CONFIRMED = TermCode.of("http://terminology.hl7.org/CodeSystem/condition-ver-status",
            "confirmed", "Confirmed");
    static final TermCode UNCONFIRMED = TermCode.of("http://terminology.hl7.org/CodeSystem/condition-ver-status",
            "unconfirmed", "Unconfirmed");
    static final TermCode POSITIVE = TermCode.of("http://snomed.info/sct", "positive", "positive");
    static final TermCode MALE = TermCode.of("http://hl7.org/fhir/administrative-gender", "male", "Male");
    static final TermCode FEMALE = TermCode.of("http://hl7.org/fhir/administrative-gender", "female", "Female");
    static final TermCode OBSERVATION_STATUS = TermCode.of("http://hl7.org/fhir", "observation-status", "Observation Status");
    static final TermCode FINAL = TermCode.of("http://hl7.org/fhir/observation-status", "final", "Final");
    static final TermCode AGE = TermCode.of("http://snomed.info/sct", "424144002", "Gegenwärtiges chronologisches Alter");
    static final TermCode YEAR_UNIT = new TermCode("someSystem", "a", "a");
    static final TermCode WEEK_UNIT = new TermCode("someSystem", "wk", "mo");
    static final TermCode MONTH_UNIT = new TermCode("someSystem", "mo", "mo");
    static final BigDecimal AGE_OF_5 = BigDecimal.valueOf(5);
    static final BigDecimal DECIMAL = BigDecimal.valueOf(5);
    static final TermCode GRAM_PER_DECILITER = new TermCode("http://unitsofmeasure.org", "g/dL", "g/dL");
    static final TermCode COMPOSITE_CODE = new TermCode("http://loing.org", "8480-6", "Sistolic Bloodpressure");
    static final TermCode BLOOD_PRESSURE = new TermCode("http://loing.org", "8480-6", "Systolischer Blutdruck");

    @Mock
    MappingContext mappingContext;

    @Nested
    @DisplayName("from JSON")
    class FromJson {

        static final TermCode C71 = TermCode.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "C71",
                "Malignant neoplasm of brain");

        static Criterion parse(String s) throws JsonProcessingException {
            return new ObjectMapper().readValue(s, Criterion.class);
        }

        @Test
        @DisplayName("concept only")
        void conceptOnly() throws Exception {
            var criterion = parse("""
                    {
                        "termCodes": [
                            {
                                "system": "http://fhir.de/CodeSystem/bfarm/icd-10-gm",
                                "code": "C71",
                                "display": "Malignant neoplasm of brain"
                            }
                        ]
                    }
                    """);

            assertThat(criterion).isEqualTo(Criterion.of(Concept.of(C71)));
        }

        @Test
        @DisplayName("with time restriction")
        void withTimeRestriction() throws Exception {
            var criterion = parse("""
                    {
                        "termCodes": [
                            {
                                "system": "http://fhir.de/CodeSystem/bfarm/icd-10-gm",
                                "code": "C71",
                                "display": "Malignant neoplasm of brain"
                            }
                        ],
                        "timeRestriction": {
                          "afterDate": "2021-09-09",
                          "beforeDate": "2021-10-09"
                        }
                    }
                    """);

            assertThat(criterion).isEqualTo(Criterion.of(Concept.of(C71))
                    .appendTimeRestrictionFilter(new TimeRestriction.Interval(LocalDate.of(2021, 9, 9),
                            LocalDate.of(2021, 10, 9))));
        }
    }

    @Nested
    @DisplayName("expand")
    class Expand {

        static final TermCode TERM_CODE = TermCode.of("system-152138", "code-152142", "display-152148");

        @Test
        @DisplayName("not expandable")
        void notExpandable() {
            when(mappingContext.expandConcept(Concept.of(TERM_CODE))).thenReturn(Either.left(
                    new ConceptNotExpandableException(Concept.of(TERM_CODE))));

            var criteria = Criterion.of(Concept.of(TERM_CODE)).expand(mappingContext);

            assertThat(criteria).isLeftInstanceOf(ConceptNotExpandableException.class);
        }

        @Test
        @DisplayName("mapping not found")
        void mappingNotFound() {
            when(mappingContext.expandConcept(Concept.of(TERM_CODE))).thenReturn(Either.right(List.of(TERM_CODE)));
            when(mappingContext.findMapping(TERM_CODE)).thenReturn(Either.left(new MappingNotFoundException(TERM_CODE)));

            var criteria = Criterion.of(Concept.of(TERM_CODE)).expand(mappingContext);

            assertThat(criteria).isLeftInstanceOf(MappingNotFoundException.class);
        }

        @Nested
        @DisplayName("one concept expansion")
        class OneConceptExpansion {

            static final BigDecimal DECIMAL_1 = BigDecimal.valueOf(163017);
            static final BigDecimal DECIMAL_2 = BigDecimal.valueOf(163019);

            Mapping mapping = Mapping.of(TERM_CODE, "Condition", "code");
            ExpandedCriterion expandedCriterion = ExpandedCriterion.of("Condition", "code", TERM_CODE);

            static Stream<LocalDate> localDates() {
                return Stream.of(1973, 2000, 2023)
                        .flatMap(year -> Stream.of(1, 4, 12)
                                .flatMap(month -> Stream.of(1, 12, 23)
                                        .map(day -> LocalDate.of(year, month, 5))));
            }

            static Stream<Arguments> localDateIntervals() {
                return localDates().limit(5).flatMap(date1 -> localDates()
                        .filter(date1::isBefore)
                        .limit(5)
                        .map(date2 -> Arguments.of(date1, date2)));
            }

            @BeforeEach
            void setUp() {
                when(mappingContext.expandConcept(Concept.of(TERM_CODE))).thenReturn(Either.right(List.of(TERM_CODE)));
            }

            @Test
            @DisplayName("concept only")
            void conceptOnly() {
                when(mappingContext.findMapping(TERM_CODE)).thenReturn(Either.right(mapping));

                var criteria = Criterion.of(Concept.of(TERM_CODE)).expand(mappingContext);

                assertThat(criteria).isRightSatisfying(r -> assertThat(r).containsExactly(expandedCriterion));
            }

            @Test
            @DisplayName("one value filter with one concept")
            void oneValueFilter_OneConcept() {
                when(mappingContext.findMapping(TERM_CODE)).thenReturn(Either.right(mapping
                        .withValueFilterMapping(ValueMappingType.CODING, "value-concept")));

                var criteria = Criterion.of(Concept.of(TERM_CODE), ValueFilter.ofConcept(POSITIVE))
                        .expand(mappingContext);

                assertThat(criteria).isRightSatisfying(r -> assertThat(r).containsExactly(expandedCriterion
                        .appendFilter(new ExpandedConceptFilter("value-concept", POSITIVE))));
            }

            @Test
            @DisplayName("one value filter with one concept and one attribute filter with one concept")
            void oneValueFilter_OneConcept_OneAttributeFilter_OneConcept() {
                when(mappingContext.findMapping(TERM_CODE)).thenReturn(Either.right(mapping
                        .withValueFilterMapping(ValueMappingType.CODING, "value-concept")
                        .appendAttributeMapping(AttributeMapping.code(OBSERVATION_STATUS, "status"))));

                var criteria = Criterion.of(Concept.of(TERM_CODE), ValueFilter.ofConcept(POSITIVE))
                        .appendAttributeFilter(AttributeFilter.ofConcept(OBSERVATION_STATUS, FINAL))
                        .expand(mappingContext);

                assertThat(criteria).isRightSatisfying(r -> assertThat(r).containsExactly(expandedCriterion
                        .appendFilter(new ExpandedConceptFilter("value-concept", POSITIVE))
                        .appendFilter(new ExpandedCodeFilter("status", "final"))));
            }

            @Test
            @DisplayName("one value filter with two concepts")
            void oneValueFilter_TwoConcepts() {
                when(mappingContext.findMapping(TERM_CODE)).thenReturn(Either.right(mapping
                        .withValueFilterMapping(ValueMappingType.CODING, "value-concept")));

                var criteria = Criterion.of(Concept.of(TERM_CODE), ValueFilter.ofConcept(MALE, FEMALE))
                        .expand(mappingContext);

                assertThat(criteria).isRightSatisfying(r -> assertThat(r).containsExactly(
                        expandedCriterion.appendFilter(new ExpandedConceptFilter("value-concept", MALE)),
                        expandedCriterion.appendFilter(new ExpandedConceptFilter("value-concept", FEMALE))));
            }

            @Test
            @DisplayName("one value filter with two concepts and one fixed criterion")
            void oneValueFilter_TwoConcepts_OneFixedCriteria_OneConcept() {
                when(mappingContext.findMapping(TERM_CODE)).thenReturn(Either.right(mapping
                        .withValueFilterMapping(ValueMappingType.CODING, "value-concept")
                        .withFixedCriteria(new FixedCriterion(FixedCriterionType.CODE, "status", List.of(FINAL), null))));

                var criteria = Criterion.of(Concept.of(TERM_CODE), ValueFilter.ofConcept(MALE, FEMALE)).expand(mappingContext);

                assertThat(criteria).isRightSatisfying(r -> assertThat(r).containsExactly(
                        expandedCriterion
                                .appendFilter(new ExpandedCodeFilter("status", "final"))
                                .appendFilter(new ExpandedConceptFilter("value-concept", MALE)),
                        expandedCriterion
                                .appendFilter(new ExpandedCodeFilter("status", "final"))
                                .appendFilter(new ExpandedConceptFilter("value-concept", FEMALE))));
            }

            @Test
            @DisplayName("one comparator value filter")
            void oneComparatorValueFilter() {
                when(mappingContext.findMapping(TERM_CODE)).thenReturn(Either.right(mapping
                        .withValueFilterMapping(ValueMappingType.CODING, "value-quantity")));

                var criteria = Criterion.of(Concept.of(TERM_CODE), ValueFilter.ofComparator(LESS_THAN,
                        Quantity.of(DECIMAL, GRAM_PER_DECILITER))).expand(mappingContext);

                assertThat(criteria).isRightSatisfying(r -> assertThat(r).containsExactly(expandedCriterion
                        .appendFilter(new ExpandedQuantityComparatorFilter("value-quantity", LESS_THAN,
                                Quantity.of(DECIMAL, GRAM_PER_DECILITER)))));
            }

            @Test
            @DisplayName("one range value filter")
            void oneRangeValueFilter() {
                when(mappingContext.findMapping(TERM_CODE)).thenReturn(Either.right(mapping
                        .withValueFilterMapping(ValueMappingType.CODING, "value-quantity")));

                var criteria = Criterion.of(Concept.of(TERM_CODE), ValueFilter.ofRange(Quantity.of(DECIMAL_1,
                                GRAM_PER_DECILITER), Quantity.of(DECIMAL_2, GRAM_PER_DECILITER)))
                        .expand(mappingContext);

                assertThat(criteria).isRightSatisfying(r -> assertThat(r).containsExactly(expandedCriterion
                        .appendFilter(new ExpandedQuantityRangeFilter("value-quantity", Quantity.of(DECIMAL_1,
                                GRAM_PER_DECILITER), Quantity.of(DECIMAL_2, GRAM_PER_DECILITER)))));
            }

            @Test
            @DisplayName("one attribute filter with one concept")
            void oneAttributeFilter_OneConcept() {
                when(mappingContext.findMapping(TERM_CODE)).thenReturn(Either.right(mapping
                        .appendAttributeMapping(AttributeMapping.coding(VERIFICATION_STATUS, "verification-status"))));

                var criteria = Criterion.of(Concept.of(TERM_CODE)).appendAttributeFilter(AttributeFilter.ofConcept(
                        VERIFICATION_STATUS, CONFIRMED)).expand(mappingContext);

                assertThat(criteria).isRightSatisfying(r -> assertThat(r).containsExactly(expandedCriterion
                        .appendFilter(new ExpandedConceptFilter("verification-status", CONFIRMED))));
            }

            @Test
            @DisplayName("one attribute filter with two concepts")
            void oneAttributeFilter_TwoConcepts() {
                when(mappingContext.findMapping(TERM_CODE)).thenReturn(Either.right(mapping
                        .appendAttributeMapping(AttributeMapping.coding(VERIFICATION_STATUS, "verification-status"))));

                var criteria = Criterion.of(Concept.of(TERM_CODE)).appendAttributeFilter(AttributeFilter.ofConcept(
                        VERIFICATION_STATUS, CONFIRMED, UNCONFIRMED)).expand(mappingContext);

                assertThat(criteria).isRightSatisfying(r -> assertThat(r).containsExactly(
                        expandedCriterion.appendFilter(new ExpandedConceptFilter("verification-status", CONFIRMED)),
                        expandedCriterion.appendFilter(new ExpandedConceptFilter("verification-status", UNCONFIRMED))));
            }

            @Test
            @DisplayName("one fixed criterion with one concept")
            void oneFixedCriteria_OneConcept() {
                when(mappingContext.findMapping(TERM_CODE)).thenReturn(Either.right(mapping
                        .withFixedCriteria(new FixedCriterion(FixedCriterionType.CODING, "verification-status", List.of(CONFIRMED), null))));

                var criteria = Criterion.of(Concept.of(TERM_CODE)).expand(mappingContext);

                assertThat(criteria).isRightSatisfying(r -> assertThat(r).containsExactly(expandedCriterion
                        .appendFilter(new ExpandedConceptFilter("verification-status", CONFIRMED))));
            }

            @Test
            @DisplayName("one fixed criterion with two concepts")
            void oneFixedCriteria_TwoConcepts() {
                when(mappingContext.findMapping(TERM_CODE)).thenReturn(Either.right(mapping
                        .withFixedCriteria(new FixedCriterion(FixedCriterionType.CODING, "verification-status", List.of(CONFIRMED, UNCONFIRMED), null))));

                var criteria = Criterion.of(Concept.of(TERM_CODE)).expand(mappingContext);

                assertThat(criteria).isRightSatisfying(r -> assertThat(r).containsExactly(
                        expandedCriterion.appendFilter(new ExpandedConceptFilter("verification-status", CONFIRMED)),
                        expandedCriterion.appendFilter(new ExpandedConceptFilter("verification-status", UNCONFIRMED))));
            }

            @Test
            @DisplayName("one fixed criterion with one concept and a composite code")
            void oneFixedCriterion_OneConcept_withCompositeCode() {
                when(mappingContext.findMapping(TERM_CODE)).thenReturn(Either.right(mapping
                        .withFixedCriteria(new FixedCriterion(FixedCriterionType.COMPOSITE_CONCEPT, "verification-status", List.of(CONFIRMED), COMPOSITE_CODE))));

                var criteria = Criterion.of(Concept.of(TERM_CODE)).expand(mappingContext);

                assertThat(criteria).isRightSatisfying(r -> assertThat(r).containsExactly(expandedCriterion
                        .appendFilter(new ExpandedCompositeConceptFilter("verification-status", COMPOSITE_CODE, CONFIRMED))));
            }

            @Test
            @DisplayName("one composite-comparator filter")
            void oneCompositeComparatorFilter() {
                when(mappingContext.findMapping(TERM_CODE)).thenReturn(Either.right(mapping
                        .appendAttributeMapping(AttributeMapping.compositeComparator(BLOOD_PRESSURE,
                                COMPONENT_CODE_VALUE_QUANTITY, COMPOSITE_CODE))));

                var criteria = Criterion.of(Concept.of(TERM_CODE)).appendAttributeFilter(new AttributeFilter(
                        BLOOD_PRESSURE, new QuantityComparatorFilterPart(GREATER_THAN, Quantity.of(DECIMAL,
                        GRAM_PER_DECILITER)))).expand(mappingContext);

                assertThat(criteria).isRightSatisfying(r -> assertThat(r).containsExactly(expandedCriterion
                        .appendFilter(new ExpandedCompositeQuantityComparatorFilter(COMPONENT_CODE_VALUE_QUANTITY,
                                COMPOSITE_CODE, GREATER_THAN, Quantity.of(DECIMAL, GRAM_PER_DECILITER)))));
            }

            @Test
            @DisplayName("one composite-range filter")
            void oneCompositeRangeFilter() {
                when(mappingContext.findMapping(TERM_CODE)).thenReturn(Either.right(mapping
                        .appendAttributeMapping(AttributeMapping.compositeRange(BLOOD_PRESSURE,
                                COMPONENT_CODE_VALUE_QUANTITY, COMPOSITE_CODE))));

                var criteria = Criterion.of(Concept.of(TERM_CODE)).appendAttributeFilter(new AttributeFilter(
                        BLOOD_PRESSURE, new QuantityRangeFilterPart(Quantity.of(DECIMAL_1, GRAM_PER_DECILITER),
                        Quantity.of(DECIMAL_2, GRAM_PER_DECILITER)))).expand(mappingContext);

                assertThat(criteria).isRightSatisfying(r -> assertThat(r).containsExactly(expandedCriterion
                        .appendFilter(new ExpandedCompositeQuantityRangeFilter(COMPONENT_CODE_VALUE_QUANTITY,
                                COMPOSITE_CODE, Quantity.of(DECIMAL_1, GRAM_PER_DECILITER), Quantity.of(DECIMAL_2,
                                GRAM_PER_DECILITER)))));
            }

            @Test
            @DisplayName("one composite-concept filter")
            void oneCompositeConceptFilter() {
                when(mappingContext.findMapping(TERM_CODE)).thenReturn(Either.right(mapping
                        .appendAttributeMapping(AttributeMapping.compositeConcept(BLOOD_PRESSURE,
                                COMPONENT_CODE_VALUE_QUANTITY, COMPOSITE_CODE))));

                var criteria = Criterion.of(Concept.of(TERM_CODE)).appendAttributeFilter(new AttributeFilter(
                        BLOOD_PRESSURE, new ConceptFilterPart(List.of(TERM_CODE)))).expand(mappingContext);

                assertThat(criteria).isRightSatisfying(r -> assertThat(r).containsExactly(expandedCriterion
                        .appendFilter(new ExpandedCompositeConceptFilter(COMPONENT_CODE_VALUE_QUANTITY, COMPOSITE_CODE,
                                TERM_CODE))));
            }

            @Test
            @DisplayName("one composite-concept filter with wrong filter type in mapping")
            void oneCompositeConceptFilter_WithWrongFilterType() {
                when(mappingContext.findMapping(TERM_CODE)).thenReturn(Either.right(mapping
                        .appendAttributeMapping(AttributeMapping.compositeComparator(BLOOD_PRESSURE,
                                COMPONENT_CODE_VALUE_QUANTITY, COMPOSITE_CODE))));

                var criteria = Criterion.of(Concept.of(TERM_CODE)).appendAttributeFilter(new AttributeFilter(
                        BLOOD_PRESSURE, new ConceptFilterPart(List.of(TERM_CODE)))).expand(mappingContext);

                assertThat(criteria).isLeftInstanceOf(ConceptFilterTypeNotExpandableException.class);
            }

            @ParameterizedTest(name = "({0}]")
            @MethodSource("localDates")
            @DisplayName("one time restriction with open start point")
            void oneTimeRestriction_openStart(LocalDate end) {
                when(mappingContext.findMapping(TERM_CODE)).thenReturn(Either.right(mapping
                        .withTimeRestrictionParameter("time-restriction")));

                var criteria = Criterion.of(Concept.of(TERM_CODE))
                        .appendTimeRestrictionFilter(new TimeRestriction.OpenStart(end))
                        .expand(mappingContext);

                assertThat(criteria).isRightSatisfying(r -> assertThat(r).containsExactly(expandedCriterion
                        .appendFilter(new ExpandedDateComparatorFilter("time-restriction", LESS_EQUAL, end))));
            }

            @ParameterizedTest(name = "[{0})")
            @MethodSource("localDates")
            @DisplayName("one time restriction with open end point")
            void oneTimeRestriction_openEnd(LocalDate start) {
                when(mappingContext.findMapping(TERM_CODE)).thenReturn(Either.right(mapping
                        .withTimeRestrictionParameter("time-restriction")));

                var criteria = Criterion.of(Concept.of(TERM_CODE))
                        .appendTimeRestrictionFilter(new TimeRestriction.OpenEnd(start))
                        .expand(mappingContext);

                assertThat(criteria).isRightSatisfying(r -> assertThat(r).containsExactly(expandedCriterion
                        .appendFilter(new ExpandedDateComparatorFilter("time-restriction", GREATER_EQUAL, start))));
            }

            @ParameterizedTest(name = "[{0}, {1}]")
            @MethodSource("localDateIntervals")
            @DisplayName("one time restriction with start and end point")
            void oneTimeRestriction_openEnd(LocalDate start, LocalDate end) {
                when(mappingContext.findMapping(TERM_CODE)).thenReturn(Either.right(mapping
                        .withTimeRestrictionParameter("time-restriction")));

                var criteria = Criterion.of(Concept.of(TERM_CODE))
                        .appendTimeRestrictionFilter(new TimeRestriction.Interval(start, end))
                        .expand(mappingContext);

                assertThat(criteria).isRightSatisfying(r -> assertThat(r).containsExactly(expandedCriterion
                        .appendFilter(new ExpandedDateRangeFilter("time-restriction", start, end))));
            }
        }

        @Nested
        @DisplayName("two concept expansions")
        class TwoConceptExpansions {

            static final TermCode TERM_CODE_1 = TermCode.of("system-152138", "code-152142", "display-152148");
            static final TermCode TERM_CODE_2 = TermCode.of("system-160622", "code-160626", "display-160630");

            Mapping mapping1 = Mapping.of(TERM_CODE_1, "Condition", "code");
            Mapping mapping2 = Mapping.of(TERM_CODE_2, "Condition", "code");
            ExpandedCriterion expandedCriterion1 = ExpandedCriterion.of("Condition", "code", TERM_CODE_1);
            ExpandedCriterion expandedCriterion2 = ExpandedCriterion.of("Condition", "code", TERM_CODE_2);

            @BeforeEach
            void setUp() {
                when(mappingContext.expandConcept(Concept.of(TERM_CODE))).thenReturn(Either.right(List.of(TERM_CODE_1,
                        TERM_CODE_2)));
            }

            @Test
            @DisplayName("concept only")
            void conceptOnly() {
                when(mappingContext.findMapping(TERM_CODE_1)).thenReturn(Either.right(mapping1));
                when(mappingContext.findMapping(TERM_CODE_2)).thenReturn(Either.right(mapping2));

                var criteria = Criterion.of(Concept.of(TERM_CODE)).expand(mappingContext);

                assertThat(criteria).isRightSatisfying(r -> assertThat(r).containsExactly(expandedCriterion1, expandedCriterion2));
            }

            @Test
            @DisplayName("one attribute filter with two concepts")
            void oneAttributeFilter_TwoConcepts() {
                when(mappingContext.findMapping(TERM_CODE_1)).thenReturn(Either.right(mapping1
                        .appendAttributeMapping(AttributeMapping.coding(VERIFICATION_STATUS, "verification-status"))));
                when(mappingContext.findMapping(TERM_CODE_2)).thenReturn(Either.right(mapping2
                        .appendAttributeMapping(AttributeMapping.coding(VERIFICATION_STATUS, "verification-status"))));

                var criteria = Criterion.of(Concept.of(TERM_CODE)).appendAttributeFilter(AttributeFilter.ofConcept(
                        VERIFICATION_STATUS, CONFIRMED, UNCONFIRMED)).expand(mappingContext);

                assertThat(criteria).isRightSatisfying(r -> assertThat(r).containsExactly(
                        expandedCriterion1.appendFilter(new ExpandedConceptFilter("verification-status", CONFIRMED)),
                        expandedCriterion1.appendFilter(new ExpandedConceptFilter("verification-status", UNCONFIRMED)),
                        expandedCriterion2.appendFilter(new ExpandedConceptFilter("verification-status", CONFIRMED)),
                        expandedCriterion2.appendFilter(new ExpandedConceptFilter("verification-status", UNCONFIRMED))));
            }
        }

        @Nested
        @DisplayName("large mount of concept expansions")
        class LargeAmountOfConceptExpansions {

            static final int NUM_CODES = 1000;
            static final List<TermCode> TERM_CODES = IntStream.range(0, NUM_CODES)
                    .mapToObj(i -> TermCode.of("foo", "%d".formatted(i), "bar"))
                    .toList();

            List<Mapping> mappings = TERM_CODES.stream()
                    .map(termCode -> Mapping.of(termCode, "Condition", "code"))
                    .toList();
            List<ExpandedCriterion> expandedCriteria = TERM_CODES.stream()
                    .map(termCode -> ExpandedCriterion.of("Condition", "code", termCode))
                    .toList();

            @BeforeEach
            void setUp() {
                when(mappingContext.expandConcept(Concept.of(TERM_CODE))).thenReturn(Either.right(TERM_CODES));
            }

            @Test
            @DisplayName("concept only")
            void conceptOnly() {
                IntStream.range(0, NUM_CODES).forEach(i -> when(mappingContext.findMapping(TERM_CODES.get(i)))
                        .thenReturn(Either.right(mappings.get(i))));

                var criteria = Criterion.of(Concept.of(TERM_CODE)).expand(mappingContext);

                assertThat(criteria).isRightEqualTo(expandedCriteria);
            }
        }

        @Nested
        @DisplayName("with reference filers")
        class ReferenceFilters {
            public static final String RESSOURCE_TYPE_1 = "ressourceType1";
            public static final String TERM_CODE_SEARCHPARAM_1 = "term-code-searchparam-1";
            public static final String REF_CODE_SEARCHPARAM_1 = "ref-code-searchparam-1";
            public static final String TERM_CODE_SEARCHPARAM_2 = "term-code-searchparam-2";
            static final TermCode TERM_CODE_1 = new TermCode("sys", "termcode1", "disp");
            static final TermCode TERM_CODE_2 = new TermCode("sys", "termcode2", "disp");
            static final TermCode REF_FILTER_CODE_1 = new TermCode("sys", "ref-filter-code-1", "dips");

            @Test
            @DisplayName("One Criterion with one TermCode and one referenced Criterion")
            void singleParentCritTwoTermCodes_singleRefCritTwoTermCodes() {
                when(mappingContext.expandConcept(Concept.of(TERM_CODE_1))).thenReturn(Either.right(List.of(TERM_CODE_1)));
                when(mappingContext.expandConcept(Concept.of(TERM_CODE_2))).thenReturn(Either.right(List.of(TERM_CODE_2)));
                when(mappingContext.findMapping(TERM_CODE_1)).thenReturn(Either.right(Mapping.of(TERM_CODE_1, RESSOURCE_TYPE_1, TERM_CODE_SEARCHPARAM_1)
                        .appendAttributeMapping(AttributeMapping.reference(REF_FILTER_CODE_1, REF_CODE_SEARCHPARAM_1))));
                when(mappingContext.findMapping(TERM_CODE_2)).thenReturn(Either.right(Mapping.of(TERM_CODE_2, RESSOURCE_TYPE_1, TERM_CODE_SEARCHPARAM_2)));


                var criteria = Criterion.of(Concept.of(TERM_CODE_1))
                        .appendReferenceFiler(new ReferenceFilter(List.of(Criterion.of(Concept.of(TERM_CODE_2))), REF_FILTER_CODE_1))
                        .expand(mappingContext);

                var expandedCriteria = List.of(
                        new ExpandedCriterion(RESSOURCE_TYPE_1, TERM_CODE_SEARCHPARAM_1, TERM_CODE_1, List.of(
                                new ExpandedCodeFilter(REF_CODE_SEARCHPARAM_1 + "." + TERM_CODE_SEARCHPARAM_2, TERM_CODE_2.code()))));

                assertThat(criteria).isRightSatisfying(r -> assertThat(r).hasSameElementsAs(expandedCriteria));
            }


        }


        @Nested
        @DisplayName("special patient criteria")
        class PatientCriteria {

            static final TermCode GENDER = new TermCode("http://snomed.info/sct", "263495000", "Geschlecht");

            ExpandedCriterion expandedCriterion = ExpandedCriterion.of("Patient");

            @Test
            void gender() {
                when(mappingContext.expandConcept(Concept.of(GENDER))).thenReturn(Either.right(List.of(GENDER)));
                when(mappingContext.findMapping(GENDER)).thenReturn(Either.right(Mapping.of(GENDER, "Patient")
                        .withValueFilterMapping(ValueMappingType.CODE, "gender")));

                var criterion = Criterion.of(Concept.of(GENDER), ValueFilter.ofConcept(MALE)).expand(mappingContext);

                assertThat(criterion).isRightSatisfying(r -> assertThat(r).containsExactly(expandedCriterion
                        .appendFilter(new ExpandedCodeFilter("gender", "male"))));
            }

            @Nested
            @DisplayName("age")
            class Age {

                static final LocalDate YEAR_2000 = LocalDate.of(2000, 1, 1);
                static final LocalDate YEAR_1990 = LocalDate.of(1990, 1, 1);
                static final LocalDate YEAR_1995 = LocalDate.of(1995, 1, 1);
                static final LocalDate YEAR_1994 = LocalDate.of(1994, 1, 1);
                static final BigDecimal AGE_OF_10 = BigDecimal.valueOf(10);

                Mapping mapping = Mapping.of(AGE, "Patient").withValueFilterMapping(ValueMappingType.CODING, "birthdate");

                @BeforeEach
                void setUp() {
                    when(mappingContext.expandConcept(Concept.of(AGE))).thenReturn(Either.right(List.of(AGE)));
                }

                @Test
                @DisplayName("with comparator equal")
                void age_withComparatorEqual() {
                    when(mappingContext.findMapping(AGE)).thenReturn(Either.right(mapping));
                    when(mappingContext.today()).thenReturn(YEAR_2000);

                    var criterion = Criterion.of(Concept.of(AGE), ValueFilter.ofComparator(EQUAL, Quantity.of(AGE_OF_5,
                            YEAR_UNIT))).expand(mappingContext);

                    assertThat(criterion).isRightSatisfying(r -> assertThat(r).containsExactly(expandedCriterion
                            .appendFilter(new ExpandedDateRangeFilter("birthdate", YEAR_1994.plusDays(1), YEAR_1995))));
                }

                @ParameterizedTest
                @EnumSource(names = {"EQUAL"}, mode = EXCLUDE)
                @DisplayName("with other comparator")
                void age_withOtherComparator(Comparator comparator) {
                    when(mappingContext.findMapping(AGE)).thenReturn(Either.right(mapping));
                    when(mappingContext.today()).thenReturn(YEAR_2000);

                    var criterion = Criterion.of(Concept.of(AGE), ValueFilter.ofComparator(comparator,
                            Quantity.of(AGE_OF_5, YEAR_UNIT))).expand(mappingContext);

                    assertThat(criterion).isRightSatisfying(r -> assertThat(r).containsExactly(expandedCriterion
                            .appendFilter(new ExpandedDateComparatorFilter("birthdate", comparator.reverse(), YEAR_1995))));
                }

                @Test
                @DisplayName("with greater-than comparator and week unit")
                void age_withWeekUnit() {
                    when(mappingContext.findMapping(AGE)).thenReturn(Either.right(mapping));
                    when(mappingContext.today()).thenReturn(YEAR_2000);

                    var criterion = Criterion.of(Concept.of(AGE), ValueFilter.ofComparator(GREATER_THAN, Quantity.of(AGE_OF_5,
                            WEEK_UNIT))).expand(mappingContext);

                    assertThat(criterion).isRightSatisfying(r -> assertThat(r).containsExactly(expandedCriterion
                            .appendFilter(new ExpandedDateComparatorFilter("birthdate", LESS_THAN, YEAR_2000.minusWeeks(AGE_OF_5.longValue())))));
                }

                @Test
                @DisplayName("with greater-than comparator and month unit")
                void age_withMonthUnit() {
                    when(mappingContext.findMapping(AGE)).thenReturn(Either.right(mapping));
                    when(mappingContext.today()).thenReturn(YEAR_2000);

                    var criterion = Criterion.of(Concept.of(AGE), ValueFilter.ofComparator(GREATER_THAN, Quantity.of(AGE_OF_5,
                            MONTH_UNIT))).expand(mappingContext);

                    assertThat(criterion).isRightSatisfying(r -> assertThat(r).containsExactly(expandedCriterion
                            .appendFilter(new ExpandedDateComparatorFilter("birthdate", LESS_THAN, YEAR_2000.minusMonths(AGE_OF_5.longValue())))));
                }

                @Test
                @DisplayName("without unit in lower bound")
                void age_RangeWithoutLowerUnit() {
                    when(mappingContext.findMapping(AGE)).thenReturn(Either.right(mapping));
                    when(mappingContext.today()).thenReturn(YEAR_2000);

                    var criterion = Criterion.of(Concept.of(AGE), ValueFilter.ofRange(Quantity.of(AGE_OF_5),
                            Quantity.of(AGE_OF_10, YEAR_UNIT))).expand(mappingContext);

                    assertThat(criterion).isLeftInstanceOf(CalculationException.class);
                }

                @Test
                @DisplayName("without unit in upper bound")
                void age_RangeWithoutUpperUnit() {
                    when(mappingContext.findMapping(AGE)).thenReturn(Either.right(mapping));
                    when(mappingContext.today()).thenReturn(YEAR_2000);

                    var criterion = Criterion.of(Concept.of(AGE), ValueFilter.ofRange(Quantity.of(AGE_OF_5, YEAR_UNIT),
                            Quantity.of(AGE_OF_10))).expand(mappingContext);

                    assertThat(criterion).isLeftInstanceOf(CalculationException.class);
                }

                @Test
                @DisplayName("with comparator and unknown unit")
                void age_withComparator_UnknownUnit() {
                    when(mappingContext.findMapping(AGE)).thenReturn(Either.right(mapping));
                    when(mappingContext.today()).thenReturn(YEAR_2000);

                    var criterion = Criterion.of(Concept.of(AGE), ValueFilter.ofComparator(GREATER_THAN,
                            Quantity.of(AGE_OF_5, GRAM_PER_DECILITER))).expand(mappingContext);

                    assertThat(criterion).isLeftInstanceOf(CalculationException.class);
                }

                @Test
                @DisplayName("with comparator and without unit")
                void age_withComparator_WithoutUnit() {
                    when(mappingContext.findMapping(AGE)).thenReturn(Either.right(mapping));

                    var criterion = Criterion.of(Concept.of(AGE), ValueFilter.ofComparator(GREATER_THAN, Quantity.of(AGE_OF_5)))
                            .expand(mappingContext);

                    assertThat(criterion).isLeftInstanceOf(CalculationException.class);
                }

                @Test
                @DisplayName("with range")
                void age_withRange() {
                    when(mappingContext.findMapping(AGE)).thenReturn(Either.right(mapping));
                    when(mappingContext.today()).thenReturn(YEAR_2000);

                    var criterion = Criterion.of(Concept.of(AGE), ValueFilter.ofRange(Quantity.of(AGE_OF_5, YEAR_UNIT),
                                    Quantity.of(AGE_OF_10, YEAR_UNIT)))
                            .expand(mappingContext);

                    assertThat(criterion).isRightSatisfying(r -> assertThat(r).containsExactly(expandedCriterion
                            .appendFilter(new ExpandedDateRangeFilter("birthdate", YEAR_1990, YEAR_1995))));
                }
            }
        }
    }
}
