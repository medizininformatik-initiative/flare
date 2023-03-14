package de.medizininformatikinitiative.flare.model.sq;

import de.medizininformatikinitiative.flare.model.mapping.*;
import de.medizininformatikinitiative.flare.model.sq.expanded.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static de.medizininformatikinitiative.flare.model.mapping.FilterType.CODE;
import static de.medizininformatikinitiative.flare.model.mapping.FilterType.CODING;
import static de.medizininformatikinitiative.flare.model.sq.Comparator.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CriterionTest {

    static final TermCode C71 = TermCode.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "C71",
            "Malignant neoplasm of brain");
    static final TermCode C71_1 = TermCode.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "C71.1",
            "Frontallappen");
    static final TermCode C71_2 = TermCode.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "C71.2",
            "Temporallappen");
    static final TermCode VERIFICATION_STATUS = TermCode.of("http://hl7.org", "verification-status",
            "Verification Status");
    static final TermCode CONFIRMED = TermCode.of("http://terminology.hl7.org/CodeSystem/condition-ver-status",
            "confirmed", "Confirmed");
    static final TermCode UNCONFIRMED = TermCode.of("http://terminology.hl7.org/CodeSystem/condition-ver-status",
            "unconfirmed", "Unconfirmed");
    static final TermCode COVID = TermCode.of("http://loinc.org", "94500-6", "COVID");
    static final TermCode SEX = TermCode.of("http://loinc.org", "76689-9", "Sex assigned at birth");
    static final TermCode POSITIVE = TermCode.of("http://snomed.info/sct", "positive", "positive");
    static final TermCode MALE = TermCode.of("http://hl7.org/fhir/administrative-gender", "male", "Male");
    static final TermCode FEMALE = TermCode.of("http://hl7.org/fhir/administrative-gender", "female", "Female");
    static final TermCode OBSERVATION_STATUS = TermCode.of("http://hl7.org/fhir", "observation-status", "Observation Status");
    static final TermCode FINAL = TermCode.of("http://hl7.org/fhir/observation-status", "final", "Final");
    static final TermCode AGE = TermCode.of("http://snomed.info/sct", "424144002", "Gegenwärtiges chronologisches Alter");
    static final TermCode YEAR_UNIT = new TermCode("someSystem", "a", "a");
    static final BigDecimal AGE_OF_5 = BigDecimal.valueOf(5);
    static final BigDecimal DECIMAL = BigDecimal.valueOf(5);
    static final BigDecimal DECIMAL_1 = BigDecimal.valueOf(5);
    static final BigDecimal DECIMAL_2 = BigDecimal.valueOf(10);
    static final TermCode GRAM_PER_DECILITER = new TermCode("http://unitsofmeasure.org", "g/dL", "g/dL");
    static final LocalDate YEAR_2000 = LocalDate.of(2000, 1, 1);
    static final LocalDate YEAR_1990 = LocalDate.of(1990, 1, 1);
    static final LocalDate YEAR_1995 = LocalDate.of(1995, 1, 1);
    static final LocalDate YEAR_1994 = LocalDate.of(1994, 1, 1);
    static final BigDecimal AGE_OF_10 = BigDecimal.valueOf(10);
    static final TermCode GENDER = new TermCode("http://snomed.info/sct", "263495000", "Geschlecht");

    @Mock
    MappingContext mappingContext;

    @Test
    void expand_NotExpandable() {
        when(mappingContext.expandConcept(Concept.of(C71))).thenReturn(Mono.error(new ConceptNotExpandableException(
                Concept.of(C71))));

        var criteria = Criterion.of(Concept.of(C71)).expand(mappingContext);

        StepVerifier.create(criteria).expectError(ConceptNotExpandableException.class).verify();
    }

    @Test
    void expand_MappingNotFound() {
        when(mappingContext.expandConcept(Concept.of(C71))).thenReturn(Mono.just(List.of(C71)));
        when(mappingContext.findMapping(C71)).thenReturn(Mono.error(new MappingNotFoundException(C71)));

        var criteria = Criterion.of(Concept.of(C71)).expand(mappingContext);

        StepVerifier.create(criteria).expectError(MappingNotFoundException.class).verify();
    }

    @Test
    void expand_OneConceptExpansion() {
        when(mappingContext.expandConcept(Concept.of(C71))).thenReturn(Mono.just(List.of(C71)));
        when(mappingContext.findMapping(C71)).thenReturn(Mono.just(Mapping.of(C71, "Condition", "code")));

        var criteria = Criterion.of(Concept.of(C71)).expand(mappingContext).block();

        assertThat(criteria).containsExactly(ExpandedCriterion.of("Condition", "code", C71));
    }

    @Test
    void expand_TwoConceptExpansions() {
        when(mappingContext.expandConcept(Concept.of(C71))).thenReturn(Mono.just(List.of(C71_1, C71_2)));
        when(mappingContext.findMapping(C71_1)).thenReturn(Mono.just(Mapping.of(C71_1, "Condition", "code")));
        when(mappingContext.findMapping(C71_2)).thenReturn(Mono.just(Mapping.of(C71_2, "Condition", "code")));

        var criteria = Criterion.of(Concept.of(C71)).expand(mappingContext).block();

        assertThat(criteria).containsExactly(
                ExpandedCriterion.of("Condition", "code", C71_1),
                ExpandedCriterion.of("Condition", "code", C71_2));
    }

    @Test
    void toQuery_OneConceptExpansion_OneValueFilter_OneConcept() {
        when(mappingContext.expandConcept(Concept.of(COVID))).thenReturn(Mono.just(List.of(COVID)));
        when(mappingContext.findMapping(COVID)).thenReturn(Mono.just(Mapping.of(COVID, "Observation", "code")
                .withValueFilterMapping(CODING, "value-concept")));

        var criteria = Criterion.of(Concept.of(COVID), ValueFilter.ofConcept(POSITIVE)).expand(mappingContext).block();

        assertThat(criteria).containsExactly(ExpandedCriterion.of("Observation", "code", COVID)
                .appendFilter(new ExpandedConceptFilter("value-concept", POSITIVE)));
    }

    @Test
    void toQuery_OneConceptExpansion_OneValueFilter_OneConcept_OneAttributeFilter_OneConcept() {
        when(mappingContext.expandConcept(Concept.of(COVID))).thenReturn(Mono.just(List.of(COVID)));
        when(mappingContext.findMapping(COVID)).thenReturn(Mono.just(Mapping.of(COVID, "Observation", "code")
                .withValueFilterMapping(CODING, "value-concept")
                .appendAttributeMapping(AttributeMapping.code(OBSERVATION_STATUS, "status"))));

        var criteria = Criterion.of(Concept.of(COVID), ValueFilter.ofConcept(POSITIVE))
                .appendAttributeFilter(AttributeFilter.ofConcept(OBSERVATION_STATUS, FINAL))
                .expand(mappingContext).block();

        assertThat(criteria).containsExactly(ExpandedCriterion.of("Observation", "code", COVID)
                .appendFilter(new ExpandedConceptFilter("value-concept", POSITIVE))
                .appendFilter(new ExpandedCodeFilter("status", "final")));
    }

    @Test
    void toQuery_OneConceptExpansion_OneValueFilter_TwoConcepts() {
        when(mappingContext.expandConcept(Concept.of(SEX))).thenReturn(Mono.just(List.of(SEX)));
        when(mappingContext.findMapping(SEX)).thenReturn(Mono.just(Mapping.of(SEX, "Observation", "code")
                .withValueFilterMapping(CODING, "value-concept")));

        var criteria = Criterion.of(Concept.of(SEX), ValueFilter.ofConcept(MALE, FEMALE)).expand(mappingContext).block();

        assertThat(criteria).containsExactly(
                ExpandedCriterion.of("Observation", "code", SEX)
                        .appendFilter(new ExpandedConceptFilter("value-concept", MALE)),
                ExpandedCriterion.of("Observation", "code", SEX)
                        .appendFilter(new ExpandedConceptFilter("value-concept", FEMALE)));
    }

    @Test
    void expand_OneConceptExpansion_OneAttributeFilter_OneConcept() {
        when(mappingContext.expandConcept(Concept.of(C71))).thenReturn(Mono.just(List.of(C71)));
        when(mappingContext.findMapping(C71)).thenReturn(Mono.just(Mapping.of(C71, "Condition", "code")
                .appendAttributeMapping(AttributeMapping.coding(VERIFICATION_STATUS, "verification-status"))));

        var criteria = Criterion.of(Concept.of(C71)).appendAttributeFilter(AttributeFilter.ofConcept(
                VERIFICATION_STATUS, CONFIRMED)).expand(mappingContext).block();

        assertThat(criteria).containsExactly(ExpandedCriterion.of("Condition", "code", C71)
                .appendFilter(new ExpandedConceptFilter("verification-status", CONFIRMED)));
    }

    @Test
    void expand_OneConceptExpansion_OneAttributeFilter_TwoConcepts() {
        when(mappingContext.expandConcept(Concept.of(C71))).thenReturn(Mono.just(List.of(C71)));
        when(mappingContext.findMapping(C71)).thenReturn(Mono.just(Mapping.of(C71, "Condition", "code")
                .appendAttributeMapping(AttributeMapping.coding(VERIFICATION_STATUS, "verification-status"))));

        var criteria = Criterion.of(Concept.of(C71)).appendAttributeFilter(AttributeFilter.ofConcept(
                VERIFICATION_STATUS, CONFIRMED, UNCONFIRMED)).expand(mappingContext).block();

        assertThat(criteria).containsExactly(
                ExpandedCriterion.of("Condition", "code", C71)
                        .appendFilter(new ExpandedConceptFilter("verification-status", CONFIRMED)),
                ExpandedCriterion.of("Condition", "code", C71)
                        .appendFilter(new ExpandedConceptFilter("verification-status", UNCONFIRMED)));
    }

    @Test
    void expand_TwoConceptExpansions_OneAttributeFilter_TwoConcepts() {
        when(mappingContext.expandConcept(Concept.of(C71))).thenReturn(Mono.just(List.of(C71_1, C71_2)));
        when(mappingContext.findMapping(C71_1)).thenReturn(Mono.just(Mapping.of(C71_1, "Condition", "code")
                .appendAttributeMapping(AttributeMapping.coding(VERIFICATION_STATUS, "verification-status"))));
        when(mappingContext.findMapping(C71_2)).thenReturn(Mono.just(Mapping.of(C71_2, "Condition", "code")
                .appendAttributeMapping(AttributeMapping.coding(VERIFICATION_STATUS, "verification-status"))));

        var criteria = Criterion.of(Concept.of(C71)).appendAttributeFilter(AttributeFilter.ofConcept(
                VERIFICATION_STATUS, CONFIRMED, UNCONFIRMED)).expand(mappingContext).block();

        assertThat(criteria).containsExactly(
                ExpandedCriterion.of("Condition", "code", C71_1)
                        .appendFilter(new ExpandedConceptFilter("verification-status", CONFIRMED)),
                ExpandedCriterion.of("Condition", "code", C71_1)
                        .appendFilter(new ExpandedConceptFilter("verification-status", UNCONFIRMED)),
                ExpandedCriterion.of("Condition", "code", C71_2)
                        .appendFilter(new ExpandedConceptFilter("verification-status", CONFIRMED)),
                ExpandedCriterion.of("Condition", "code", C71_2)
                        .appendFilter(new ExpandedConceptFilter("verification-status", UNCONFIRMED)));
    }

    @Test
    void expand_OneConceptExpansion_OneFixedCriteria_OneConcept() {
        when(mappingContext.expandConcept(Concept.of(C71))).thenReturn(Mono.just(List.of(C71)));
        when(mappingContext.findMapping(C71)).thenReturn(Mono.just(Mapping.of(C71, "Condition", "code")
                .withFixedCriteria(new FixedCriterion(CODING, "verification-status", List.of(CONFIRMED)))));

        var criteria = Criterion.of(Concept.of(C71)).expand(mappingContext).block();

        assertThat(criteria).containsExactly(ExpandedCriterion.of("Condition", "code", C71)
                .appendFilter(new ExpandedConceptFilter("verification-status", CONFIRMED)));
    }

    @Test
    void expand_OneConceptExpansion_OneFixedCriteria_TwoConcepts() {
        when(mappingContext.expandConcept(Concept.of(C71))).thenReturn(Mono.just(List.of(C71)));
        when(mappingContext.findMapping(C71)).thenReturn(Mono.just(Mapping.of(C71, "Condition", "code")
                .withFixedCriteria(new FixedCriterion(CODING, "verification-status", List.of(CONFIRMED,
                        UNCONFIRMED)))));

        var criteria = Criterion.of(Concept.of(C71)).expand(mappingContext).block();

        assertThat(criteria).containsExactly(ExpandedCriterion.of("Condition", "code", C71)
                        .appendFilter(new ExpandedConceptFilter("verification-status", CONFIRMED)),
                ExpandedCriterion.of("Condition", "code", C71)
                        .appendFilter(new ExpandedConceptFilter("verification-status", UNCONFIRMED)));
    }

    @Test
    void toQuery_OneConceptExpansion_OneValueFilter_TwoConcepts_OneFixedCriteria_OneConcept() {
        when(mappingContext.expandConcept(Concept.of(SEX))).thenReturn(Mono.just(List.of(SEX)));
        when(mappingContext.findMapping(SEX)).thenReturn(Mono.just(Mapping.of(SEX, "Observation", "code")
                .withValueFilterMapping(CODING, "value-concept")
                .withFixedCriteria(new FixedCriterion(FilterType.CODE, "status", List.of(FINAL)))));

        var criteria = Criterion.of(Concept.of(SEX), ValueFilter.ofConcept(MALE, FEMALE)).expand(mappingContext).block();

        assertThat(criteria).containsExactly(
                ExpandedCriterion.of("Observation", "code", SEX)
                        .appendFilter(new ExpandedCodeFilter("status", "final"))
                        .appendFilter(new ExpandedConceptFilter("value-concept", MALE)),
                ExpandedCriterion.of("Observation", "code", SEX)
                        .appendFilter(new ExpandedCodeFilter("status", "final"))
                        .appendFilter(new ExpandedConceptFilter("value-concept", FEMALE)));
    }

    @Test
    void toQuery_OneComparatorFilter() {
        when(mappingContext.expandConcept(Concept.of(SEX))).thenReturn(Mono.just(List.of(SEX)));
        when(mappingContext.findMapping(SEX)).thenReturn(Mono.just(Mapping.of(SEX, "Observation", "code")
                .withValueFilterMapping(CODING, "value-quantity")));

        var criteria = Criterion.of(Concept.of(SEX), ValueFilter.ofComparator(LESS_THAN, DECIMAL, GRAM_PER_DECILITER))
                .expand(mappingContext).block();

        assertThat(criteria).containsExactly(
                ExpandedCriterion.of("Observation", "code", SEX)
                        .appendFilter(new ExpandedComparatorFilter("value-quantity", LESS_THAN, DECIMAL,
                                GRAM_PER_DECILITER)));
    }

    @Test
    void toQuery_OneRangeFilter() {
        when(mappingContext.expandConcept(Concept.of(SEX))).thenReturn(Mono.just(List.of(SEX)));
        when(mappingContext.findMapping(SEX)).thenReturn(Mono.just(Mapping.of(SEX, "Observation", "code")
                .withValueFilterMapping(CODING, "value-quantity")));

        var criteria = Criterion.of(Concept.of(SEX), ValueFilter.ofRange(DECIMAL_1, DECIMAL_2, GRAM_PER_DECILITER))
                .expand(mappingContext).block();

        assertThat(criteria).containsExactly(
                ExpandedCriterion.of("Observation", "code", SEX)
                        .appendFilter(new ExpandedRangeFilter("value-quantity", DECIMAL_1, DECIMAL_2,
                                GRAM_PER_DECILITER)));
    }


    @Test
    void toQuery_Age_ComparatorEqual() {
        when(mappingContext.expandConcept(Concept.of(AGE))).thenReturn(Mono.just(List.of(AGE)));
        when(mappingContext.findMapping(AGE)).thenReturn(Mono.just(Mapping.of(AGE, "Patient")
                .withValueFilterMapping(CODING, "birthDate")));
        when(mappingContext.today()).thenReturn(YEAR_2000);

        var criterion = Criterion.of(Concept.of(AGE), ValueFilter.ofComparator(EQUAL, AGE_OF_5, YEAR_UNIT))
                .expand(mappingContext).block();

        assertThat(criterion).containsExactly(
                ExpandedCriterion.of("Patient")
                        .appendFilter(new ExpandedDateRangeFilter("birthDate", YEAR_1994.plusDays(1), YEAR_1995)));
    }

    @Test
    void toQuery_Age_ComparatorOther() {
        when(mappingContext.expandConcept(Concept.of(AGE))).thenReturn(Mono.just(List.of(AGE)));
        when(mappingContext.findMapping(AGE)).thenReturn(Mono.just(Mapping.of(AGE, "Patient")
                .withValueFilterMapping(CODING, "birthDate")));
        when(mappingContext.today()).thenReturn(YEAR_2000);

        var criterion = Criterion.of(Concept.of(AGE), ValueFilter.ofComparator(GREATER_THAN, AGE_OF_5,
                YEAR_UNIT)).expand(mappingContext).block();

        assertThat(criterion).containsExactly(
                ExpandedCriterion.of("Patient")
                        .appendFilter(new ExpandedDateComparatorFilter("birthDate", LESS_THAN, YEAR_1995)));
    }

    @Test
    void toQuery_Age_Comparator_UnknownUnit() {
        when(mappingContext.expandConcept(Concept.of(AGE))).thenReturn(Mono.just(List.of(AGE)));
        when(mappingContext.findMapping(AGE)).thenReturn(Mono.just(Mapping.of(AGE, "Patient")
                .withValueFilterMapping(CODING, "birthDate")));
        when(mappingContext.today()).thenReturn(YEAR_2000);

        var criterion = Criterion.of(Concept.of(AGE), ValueFilter.ofComparator(GREATER_THAN, AGE_OF_5,
                GRAM_PER_DECILITER)).expand(mappingContext);

        StepVerifier.create(criterion).expectError(CalculationException.class).verify();
    }

    @Test
    void toQuery_Age_Comparator_WithoutUnit() {
        when(mappingContext.expandConcept(Concept.of(AGE))).thenReturn(Mono.just(List.of(AGE)));
        when(mappingContext.findMapping(AGE)).thenReturn(Mono.just(Mapping.of(AGE, "Patient")
                .withValueFilterMapping(CODING, "birthDate")));

        var criterion = Criterion.of(Concept.of(AGE), ValueFilter.ofComparator(GREATER_THAN, AGE_OF_5))
                .expand(mappingContext);

        StepVerifier.create(criterion).expectError(CalculationException.class).verify();
    }

    @Test
    void toQuery_AgeRange() {
        when(mappingContext.expandConcept(Concept.of(AGE))).thenReturn(Mono.just(List.of(AGE)));
        when(mappingContext.findMapping(AGE)).thenReturn(Mono.just(Mapping.of(AGE, "Patient")
                .withValueFilterMapping(CODING, "birthDate")));
        when(mappingContext.today()).thenReturn(YEAR_2000);

        var criterion = Criterion.of(Concept.of(AGE), ValueFilter.ofRange(AGE_OF_5, AGE_OF_10, YEAR_UNIT))
                .expand(mappingContext).block();

        assertThat(criterion).containsExactly(
                ExpandedCriterion.of("Patient")
                        .appendFilter(new ExpandedDateRangeFilter("birthDate", YEAR_1990, YEAR_1995)));
    }

    @Test
    void toQuery_Patient_Gender() {
        when(mappingContext.expandConcept(Concept.of(GENDER))).thenReturn(Mono.just(List.of(GENDER)));
        when(mappingContext.findMapping(GENDER)).thenReturn(Mono.just(Mapping.of(GENDER, "Patient")
                .withValueFilterMapping(CODE, "gender")));

        var criterion = Criterion.of(Concept.of(GENDER), ValueFilter.ofConcept(MALE)).expand(mappingContext).block();

        assertThat(criterion).containsExactly(
                ExpandedCriterion.of("Patient")
                        .appendFilter(new ExpandedCodeFilter("gender", "male")));
    }
}
