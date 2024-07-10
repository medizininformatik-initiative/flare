package de.medizininformatikinitiative.flare.service;

import de.medizininformatikinitiative.flare.Either;
import de.medizininformatikinitiative.flare.model.Population;
import de.medizininformatikinitiative.flare.model.fhir.Query;
import de.medizininformatikinitiative.flare.model.fhir.QueryParams;
import de.medizininformatikinitiative.flare.model.mapping.MappingNotFoundException;
import de.medizininformatikinitiative.flare.model.sq.*;
import de.medizininformatikinitiative.flare.model.translate.Operator;
import de.medizininformatikinitiative.flare.model.translate.QueryExpression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static de.medizininformatikinitiative.flare.Assertions.assertThat;
import static de.medizininformatikinitiative.flare.model.fhir.QueryParams.conceptValue;
import static de.medizininformatikinitiative.flare.model.sq.TestUtil.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SuppressWarnings("SameParameterValue")
@ExtendWith(MockitoExtension.class)
class StructuredQueryServiceTest {

    static final String BFARM = "http://fhir.de/CodeSystem/bfarm/icd-10-gm";
    static final Criterion CONCEPT_CRITERION = Criterion.of(cc(1));
    static final Population PATIENT = Population.of("patient-id-140857");
    static final Population PATIENT_1 = Population.of("patient-id-144725");
    static final Population PATIENT_2 = Population.of("patient-id-144727");

    @Mock
    private FhirQueryService fhirQueryService;

    @Mock
    private Translator translator;

    @InjectMocks
    private StructuredQueryService service;

    private int getExecutionResult(StructuredQuery query){
        return service.execute(query).block().size();
    }

    @Nested
    class SingleInclusion {

        StructuredQuery query = StructuredQuery.of(CriterionGroup.of(CriterionGroup.of(CONCEPT_CRITERION)));

        @Nested
        @DisplayName("translator mapping error")
        class MappingError {

            @BeforeEach
            void setUp() {
                when(translator.toQuery(CONCEPT_CRITERION)).thenReturn(Either.left(new MappingNotFoundException(ctc(1))));
            }

            @Test
            void execute() {
                var result = service.execute(query);

                StepVerifier.create(result).expectError(MappingNotFoundException.class).verify();
            }

            @Test
            void translate() {
                var result = service.translate(query);

                assertThat(result).isLeftInstanceOf(MappingNotFoundException.class);
            }
        }

        @Nested
        @DisplayName("single criterion expansion")
        class SingleExpansion {

            @Test
            @DisplayName("execute: returns 1")
            void execute() {
                var query = query(incl(PATIENT));

                var result = getExecutionResult(query);

                assertThat(result).isOne();
            }

            @Test
            @DisplayName("translate: returns CODE_1")
            void translate() {
                var query = query(incl(tc(1)));

                var result = service.translate(query);

                assertThat(result).isRightEqualTo(Operator.intersection(Operator.union(queryExpr(tc(1)))));
            }
        }

        @Nested
        @DisplayName("multiple criterion expansion")
        class MultipleExpansion {

            @Test
            @DisplayName("execute: multiple patients → returns 2")
            void execute_MultiplePatients() {
                var query = query(inclExpand(PATIENT_1, PATIENT_2));

                var result = getExecutionResult(query);

                assertThat(result).isEqualTo(2);
            }

            @Test
            @DisplayName("execute: same patient → returns 1")
            void execute_SamePatient() {
                var query = query(inclExpand(PATIENT, PATIENT));

                var result = getExecutionResult(query);

                assertThat(result).isOne();
            }

            @Test
            @DisplayName("translate: returns CODE_1 ∪ CODE_2")
            void translate() {
                var query = query(inclExpand(tc(1), tc(2)));

                var result = service.translate(query);

                assertThat(result).isRightEqualTo(Operator.intersection(Operator.union(queryExpr(tc(1)),
                        queryExpr(tc(2)))));
            }
        }
    }

    @Nested
    class ConjunctionInclusion {

        @Test
        @DisplayName("execute: multiple patients → returns 0")
        void execute_MultiplePatients() {
            var query = query(inclAnd(PATIENT_1, PATIENT_2));

            var result = getExecutionResult(query);

            assertThat(result).isZero();
        }

        @Test
        @DisplayName("execute: same patient → returns 1")
        void execute_SamePatient() {
            var query = query(inclAnd(PATIENT, PATIENT));

            var result = getExecutionResult(query);

            assertThat(result).isOne();
        }

        @Test
        @DisplayName("translate: returns CODE_1 ∩ CODE_2")
        void translate() {
            var query = query(inclAnd(tc(1), tc(2)));

            var result = service.translate(query);

            assertThat(result).isRightEqualTo(Operator.intersection(Operator.union(queryExpr(tc(1))),
                    Operator.union(queryExpr(tc(2)))));
        }
    }

    @Nested
    class DisjunctionInclusion {

        @Test
        @DisplayName("execute: multiple patients → returns 2")
        void execute_MultiplePatients() {
            var query = query(inclOr(PATIENT_1, PATIENT_2));

            var result = getExecutionResult(query);

            assertThat(result).isEqualTo(2);
        }

        @Test
        @DisplayName("execute: same patient → returns 1")
        void execute_SamePatient() {
            var query = query(inclOr(PATIENT, PATIENT));

            var result = getExecutionResult(query);

            assertThat(result).isOne();
        }

        @Test
        @DisplayName("translate: returns CODE_1 ∪ CODE_2")
        void translate() {
            var query = query(inclOr(tc(1), tc(2)));

            var result = service.translate(query);

            assertThat(result).isRightEqualTo(Operator.intersection(Operator.union(queryExpr(tc(1)),
                    queryExpr(tc(2)))));
        }
    }

    @Nested
    class SingleInclusionAndExclusion {

        @Test
        @DisplayName("execute: PATIENT is not excluded → returns 1")
        void execute_PatientNotExcluded() {
            var query = query(incl(PATIENT), excl(PATIENT_1));
            var result = getExecutionResult(query);
            assertThat(result).isOne();
        }

        @Test
        @DisplayName("execute: PATIENT is excluded → returns 0")
        void execute_PatientExcluded() {
            var query = query(incl(PATIENT), excl(PATIENT));

            var result = getExecutionResult(query);

            assertThat(result).isZero();
        }

        @Test
        @DisplayName("translate: returns CODE_1 ∖ CODE_2")
        void translate() {
            var query = query(incl(tc(1)), excl(tc(2)));

            var result = service.translate(query);

            assertThat(result).isRightEqualTo(Operator.difference(
                    Operator.intersection(Operator.union(queryExpr(tc(1)))),
                    Operator.union(Operator.intersection(Operator.union(queryExpr(tc(2)))))));
        }
    }

    @Nested
    class SingleInclusionAndDisjunctionExclusion {

        @Test
        @DisplayName("execute: PATIENT is not excluded → returns 1")
        void execute_PatientNotExcluded() {
            var query = query(incl(PATIENT), exclOr(PATIENT_1, PATIENT_2));

            var result = getExecutionResult(query);

            assertThat(result).isOne();
        }

        @Test
        @DisplayName("execute: PATIENT is excluded → returns 0")
        void execute_PatientExcluded() {
            var query = query(incl(PATIENT), exclOr(PATIENT, PATIENT_1));

            var result = getExecutionResult(query);

            assertThat(result).isZero();
        }

        @Test
        @DisplayName("translate: returns CODE_1 ∖ (CODE_2 ∪ CODE_3)")
        void translate() {
            var query = query(incl(tc(1)), exclOr(tc(2), tc(3)));

            var result = service.translate(query);

            assertThat(result).isRightEqualTo(Operator.difference(
                    Operator.intersection(Operator.union(queryExpr(tc(1)))),
                    Operator.union(Operator.intersection(Operator.union(queryExpr(tc(2)))),
                            Operator.intersection(Operator.union(queryExpr(tc(3)))))));
        }
    }

    @Nested
    class SingleInclusionAndConjunctionExclusion {

        @Test
        @DisplayName("execute: PATIENT is not excluded → returns 1")
        void execute_PatientNotExcluded() {
            var query = query(incl(PATIENT), exclAnd(PATIENT, PATIENT_1));

            var result = getExecutionResult(query);

            assertThat(result).isOne();
        }

        @Test
        @DisplayName("execute: PATIENT is excluded → returns 0")
        void execute_PatientExcluded() {
            var query = query(incl(PATIENT), exclAnd(PATIENT, PATIENT));

            var result = getExecutionResult(query);

            assertThat(result).isZero();
        }

        @Test
        @DisplayName("translate: returns CODE_1 ∖ (CODE_2 ∩ CODE_3)")
        void translate() {
            var query = query(incl(tc(1)), exclAnd(tc(2), tc(3)));

            var result = service.translate(query);

            assertThat(result).isRightEqualTo(Operator.difference(
                    Operator.intersection(Operator.union(queryExpr(tc(1)))),
                    Operator.union(Operator.intersection(Operator.union(queryExpr(tc(2))),
                            Operator.union(queryExpr(tc(3)))))));
        }
    }

    CriterionGroup<Criterion> group(Population population) {
        return CriterionGroup.of(whenQuery(population));
    }

    CriterionGroup<Criterion> group(Population p1, Population p2) {
        return CriterionGroup.of(whenQuery(p1), whenQuery(p2));
    }

    CriterionGroup<Criterion> groupExpand(Population p1, Population p2) {
        return CriterionGroup.of(whenQueryExpand(p1, p2));
    }

    CriterionGroup<Criterion> groupExpand(TermCode c1, TermCode c2) {
        return CriterionGroup.of(whenCriterionExpand(c1, c2).criterion);
    }

    CriterionGroup<Criterion> group(TermCode code) {
        return CriterionGroup.of(whenCriterion(code).criterion);
    }

    CriterionGroup<Criterion> group(TermCode c1, TermCode c2) {
        return CriterionGroup.of(whenCriterion(c1).criterion, whenCriterion(c2).criterion);
    }

    Criterion whenQuery(Population population) {
        var criterionQuery = whenCriterion(TermCode.of(BFARM, UUID.randomUUID().toString(), ""));
        when(fhirQueryService.execute(criterionQuery.query)).thenReturn(Mono.just(population));
        return criterionQuery.criterion;
    }

    Criterion whenQueryExpand(Population p1, Population p2) {
        var criterionQuery = whenCriterionExpand(TermCode.of(BFARM, UUID.randomUUID().toString(), ""),
                TermCode.of(BFARM, UUID.randomUUID().toString(), ""));
        when(fhirQueryService.execute(criterionQuery.query1)).thenReturn(Mono.just(p1));
        when(fhirQueryService.execute(criterionQuery.query2)).thenReturn(Mono.just(p2));
        return criterionQuery.criterion;
    }

    CriterionQuery whenCriterion(TermCode code) {
        var criterion = Criterion.of(ContextualConcept.of(CONTEXT, Concept.of(code)));
        var query = Query.of("Condition", QueryParams.of("code", conceptValue(code)));
        when(translator.toQuery(criterion)).thenReturn(Either.right(List.of(query)));
        return new CriterionQuery(criterion, query);
    }

    CriterionQuery2 whenCriterionExpand(TermCode code1, TermCode code2) {
        var criterion = Criterion.of(ContextualConcept.of(CONTEXT, Concept.of(code1)));
        var query1 = Query.of("Condition", QueryParams.of("code", conceptValue(code1)));
        var query2 = Query.of("Condition", QueryParams.of("code", conceptValue(code2)));
        when(translator.toQuery(criterion)).thenReturn(Either.right(List.of(query1, query2)));
        return new CriterionQuery2(criterion, query1, query2);
    }

    record CriterionQuery(Criterion criterion, Query query) {
    }

    record CriterionQuery2(Criterion criterion, Query query1, Query query2) {
    }

    CriterionGroup<CriterionGroup<Criterion>> incl(Population population) {
        return CriterionGroup.of(group(population));
    }

    CriterionGroup<CriterionGroup<Criterion>> inclExpand(Population p1, Population p2) {
        return CriterionGroup.of(groupExpand(p1, p2));
    }

    CriterionGroup<CriterionGroup<Criterion>> inclExpand(TermCode c1, TermCode c2) {
        return CriterionGroup.of(groupExpand(c1, c2));
    }

    CriterionGroup<CriterionGroup<Criterion>> inclOr(Population p1, Population p2) {
        return CriterionGroup.of(group(p1, p2));
    }

    CriterionGroup<CriterionGroup<Criterion>> inclAnd(Population p1, Population p2) {
        return CriterionGroup.of(group(p1), group(p2));
    }

    CriterionGroup<CriterionGroup<Criterion>> inclOr(TermCode c1, TermCode c2) {
        return CriterionGroup.of(group(c1, c2));
    }

    CriterionGroup<CriterionGroup<Criterion>> inclAnd(TermCode c1, TermCode c2) {
        return CriterionGroup.of(group(c1), group(c2));
    }

    CriterionGroup<CriterionGroup<Criterion>> excl(Population population) {
        return CriterionGroup.of(group(population));
    }

    CriterionGroup<CriterionGroup<Criterion>> incl(TermCode code) {
        return CriterionGroup.of(group(code));
    }

    CriterionGroup<CriterionGroup<Criterion>> excl(TermCode code) {
        return CriterionGroup.of(group(code));
    }

    CriterionGroup<CriterionGroup<Criterion>> exclOr(Population p1, Population p2) {
        return CriterionGroup.of(group(p1), group(p2));
    }

    CriterionGroup<CriterionGroup<Criterion>> exclAnd(Population p1, Population p2) {
        return CriterionGroup.of(group(p1, p2));
    }

    CriterionGroup<CriterionGroup<Criterion>> exclOr(TermCode c1, TermCode c2) {
        return CriterionGroup.of(group(c1), group(c2));
    }

    CriterionGroup<CriterionGroup<Criterion>> exclAnd(TermCode c1, TermCode c2) {
        return CriterionGroup.of(group(c1, c2));
    }

    static StructuredQuery query(CriterionGroup<CriterionGroup<Criterion>> inclusionCriteria) {
        return StructuredQuery.of(inclusionCriteria);
    }

    static StructuredQuery query(CriterionGroup<CriterionGroup<Criterion>> inclusionCriteria,
                                 CriterionGroup<CriterionGroup<Criterion>> exclusionCriteria) {
        return StructuredQuery.of(inclusionCriteria, exclusionCriteria);
    }

    static QueryExpression queryExpr(TermCode code) {
        return new QueryExpression(Query.of("Condition", QueryParams.of("code", conceptValue(code))));
    }
}
