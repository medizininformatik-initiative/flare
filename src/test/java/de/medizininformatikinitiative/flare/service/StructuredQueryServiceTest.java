package de.medizininformatikinitiative.flare.service;

import de.medizininformatikinitiative.flare.model.Query;
import de.medizininformatikinitiative.flare.model.sq.ConceptCriterion;
import de.medizininformatikinitiative.flare.model.sq.StructuredQuery;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.structured_query.Concept;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StructuredQueryServiceTest {

    private static final TermCode C71 = TermCode.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "C71",
            "Malignant neoplasm of brain");
    private static final TermCode C72 = TermCode.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "C72",
            "Malignant neoplasm of brain");
    private static final TermCode C73 = TermCode.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "C73",
            "Malignant neoplasm of brain");
    public static final ConceptCriterion CONCEPT_CRITERION = ConceptCriterion.of(Concept.of(C71));
    public static final ConceptCriterion CONCEPT_CRITERION_1 = ConceptCriterion.of(Concept.of(C72));
    public static final ConceptCriterion CONCEPT_CRITERION_2 = ConceptCriterion.of(Concept.of(C73));
    private static final String PATIENT_ID = "patient-id-140857";
    private static final String PATIENT_ID_1 = "patient-id-144725";
    private static final String PATIENT_ID_2 = "patient-id-144727";
    public static final Query CONCEPT_QUERY = Query.of("Condition", "code=C71");
    public static final Query CONCEPT_QUERY_1 = Query.of("Condition", "code=C72");
    public static final Query CONCEPT_QUERY_2 = Query.of("Condition", "code=C73");

    @Mock
    private FhirQueryService fhirQueryService;

    @Mock
    private Translator translator;

    @InjectMocks
    private StructuredQueryService service;

    @Test
    void execute_emptyQuery() {
        var query = StructuredQuery.of(List.of(List.of()));

        var result = service.execute(query).block();

        assertThat(result).isZero();
    }

    @Test
    void execute_singleIncludeConceptCriterion() {
        var query = StructuredQuery.of(List.of(List.of(CONCEPT_CRITERION)));
        when(translator.toQuery(CONCEPT_CRITERION)).thenReturn(List.of(CONCEPT_QUERY));
        when(fhirQueryService.execute(CONCEPT_QUERY)).thenReturn(CompletableFuture.completedFuture(Set.of(PATIENT_ID)));

        var result = service.execute(query).block();

        assertThat(result).isOne();
    }

    @Test
    void execute_singleIncludeConceptCriterion_Expanding() {
        var query = StructuredQuery.of(List.of(List.of(CONCEPT_CRITERION)));
        when(translator.toQuery(CONCEPT_CRITERION)).thenReturn(List.of(CONCEPT_QUERY_1, CONCEPT_QUERY_2));
        when(fhirQueryService.execute(CONCEPT_QUERY_1)).thenReturn(CompletableFuture.completedFuture(Set.of(PATIENT_ID_1)));
        when(fhirQueryService.execute(CONCEPT_QUERY_2)).thenReturn(CompletableFuture.completedFuture(Set.of(PATIENT_ID_2)));

        var result = service.execute(query).block();

        assertThat(result).isEqualTo(2);
    }

    @Test
    void execute_singleIncludeConceptCriterion_Expanding_SamePatient() {
        var query = StructuredQuery.of(List.of(List.of(CONCEPT_CRITERION)));
        when(translator.toQuery(CONCEPT_CRITERION)).thenReturn(List.of(CONCEPT_QUERY_1, CONCEPT_QUERY_2));
        when(fhirQueryService.execute(CONCEPT_QUERY_1)).thenReturn(CompletableFuture.completedFuture(Set.of(PATIENT_ID)));
        when(fhirQueryService.execute(CONCEPT_QUERY_2)).thenReturn(CompletableFuture.completedFuture(Set.of(PATIENT_ID)));

        var result = service.execute(query).block();

        assertThat(result).isOne();
    }

    @Test
    void execute_same_singleIncludeConceptCriterion_singleExcludeConceptCriterion() {
        var query = StructuredQuery.of(List.of(List.of(CONCEPT_CRITERION)), List.of(List.of(CONCEPT_CRITERION)));
        when(translator.toQuery(CONCEPT_CRITERION)).thenReturn(List.of(CONCEPT_QUERY));
        when(fhirQueryService.execute(CONCEPT_QUERY)).thenReturn(CompletableFuture.completedFuture(Set.of(PATIENT_ID)));

        var result = service.execute(query).block();

        assertThat(result).isZero();
    }

    @Test
    void execute_singleIncludeConceptCriterion_singleExcludeConceptCriterion_SamePatient() {
        var query = StructuredQuery.of(List.of(List.of(CONCEPT_CRITERION_1)), List.of(List.of(CONCEPT_CRITERION_2)));
        when(translator.toQuery(CONCEPT_CRITERION_1)).thenReturn(List.of(CONCEPT_QUERY_1));
        when(translator.toQuery(CONCEPT_CRITERION_2)).thenReturn(List.of(CONCEPT_QUERY_2));
        when(fhirQueryService.execute(CONCEPT_QUERY_1)).thenReturn(CompletableFuture.completedFuture(Set.of(PATIENT_ID)));
        when(fhirQueryService.execute(CONCEPT_QUERY_2)).thenReturn(CompletableFuture.completedFuture(Set.of(PATIENT_ID)));

        var result = service.execute(query).block();

        assertThat(result).isZero();
    }

    @Test
    void execute_twoIncludeConceptCriteria_orLevel() {
        var query = StructuredQuery.of(List.of(List.of(CONCEPT_CRITERION_1, CONCEPT_CRITERION_2)));
        when(translator.toQuery(CONCEPT_CRITERION_1)).thenReturn(List.of(CONCEPT_QUERY_1));
        when(translator.toQuery(CONCEPT_CRITERION_2)).thenReturn(List.of(CONCEPT_QUERY_2));
        when(fhirQueryService.execute(CONCEPT_QUERY_1)).thenReturn(CompletableFuture.completedFuture(Set.of(PATIENT_ID_1)));
        when(fhirQueryService.execute(CONCEPT_QUERY_2)).thenReturn(CompletableFuture.completedFuture(Set.of(PATIENT_ID_2)));

        var result = service.execute(query).block();

        assertThat(result).isEqualTo(2);
    }

    @Test
    void execute_twoIncludeConceptCriteria_andLevel_samePatient() {
        var query = StructuredQuery.of(List.of(List.of(CONCEPT_CRITERION_1), List.of(CONCEPT_CRITERION_2)));
        when(translator.toQuery(CONCEPT_CRITERION_1)).thenReturn(List.of(CONCEPT_QUERY_1));
        when(translator.toQuery(CONCEPT_CRITERION_2)).thenReturn(List.of(CONCEPT_QUERY_2));
        when(fhirQueryService.execute(CONCEPT_QUERY_1)).thenReturn(CompletableFuture.completedFuture(Set.of(PATIENT_ID)));
        when(fhirQueryService.execute(CONCEPT_QUERY_2)).thenReturn(CompletableFuture.completedFuture(Set.of(PATIENT_ID)));

        var result = service.execute(query).block();

        assertThat(result).isOne();
    }

    @Test
    void execute_twoIncludeConceptCriteria_andLevel_differentPatients() {
        var query = StructuredQuery.of(List.of(List.of(CONCEPT_CRITERION_1), List.of(CONCEPT_CRITERION_2)));
        when(translator.toQuery(CONCEPT_CRITERION_1)).thenReturn(List.of(CONCEPT_QUERY_1));
        when(translator.toQuery(CONCEPT_CRITERION_2)).thenReturn(List.of(CONCEPT_QUERY_2));
        when(fhirQueryService.execute(CONCEPT_QUERY_1)).thenReturn(CompletableFuture.completedFuture(Set.of(PATIENT_ID_1)));
        when(fhirQueryService.execute(CONCEPT_QUERY_2)).thenReturn(CompletableFuture.completedFuture(Set.of(PATIENT_ID_2)));

        var result = service.execute(query).block();

        assertThat(result).isZero();
    }
}
