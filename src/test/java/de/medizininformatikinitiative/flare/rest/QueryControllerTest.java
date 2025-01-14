package de.medizininformatikinitiative.flare.rest;

import de.medizininformatikinitiative.flare.Either;
import de.medizininformatikinitiative.flare.model.Population;
import de.medizininformatikinitiative.flare.model.fhir.Query;
import de.medizininformatikinitiative.flare.model.mapping.MappingNotFoundException;
import de.medizininformatikinitiative.flare.model.sq.*;
import de.medizininformatikinitiative.flare.model.translate.Operator;
import de.medizininformatikinitiative.flare.model.translate.QueryExpression;
import de.medizininformatikinitiative.flare.service.StructuredQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueryControllerTest {

    static final UUID ID = UUID.randomUUID();
    static final String PATIENT_ID = "patient-id-211701";
    static final String PATIENT_ID_1 = "patient-id-1-131300";
    static final MediaType MEDIA_TYPE_SQ = MediaType.valueOf("application/sq+json");
    static final TermCode FEVER = TermCode.of("http://snomed.info/sct", "386661006", "Fever (finding)");
    static final StructuredQuery STRUCTURED_QUERY = StructuredQuery.of(CriterionGroup.of(CriterionGroup.of(
            Criterion.of(ContextualConcept.of(TestUtil.CONTEXT, Concept.of(FEVER))))));
    static final QueryExpression QUERY_EXPRESSION = new QueryExpression(Query.ofType("Condition"));

    @Mock
    StructuredQueryService queryService;

    @Mock
    IdGenerator queryIdGenerator;

    @InjectMocks
    private QueryController controller;

    private WebTestClient client;

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToRouterFunction(controller.queryRouter(true)).build();
    }

    @Test
    void execute() {
        when(queryIdGenerator.generateRandom()).thenReturn(ID);
        when(queryService.execute(ID, STRUCTURED_QUERY)).thenReturn(Mono.just(Population.of(PATIENT_ID)));

        client.post()
                .uri("/query/execute")
                .contentType(MEDIA_TYPE_SQ)
                .bodyValue("""
                        {
                          "inclusionCriteria": [
                            [
                              {
                                "context": {
                                  "system": "context-system",
                                  "code": "context-code",
                                  "display": "context-display"
                                },
                                "termCodes": [
                                  {
                                    "system": "http://snomed.info/sct",
                                    "code": "386661006",
                                    "display": "Fever (finding)"
                                  }
                                ]
                              }
                            ]
                          ]
                        }
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody().json("1");
    }

    @Test
    void executeCohort() throws JsonProcessingException {
        when(queryIdGenerator.generateRandom()).thenReturn(ID);
        when(queryService.execute(ID, STRUCTURED_QUERY)).thenReturn(Mono.just(Population.of(PATIENT_ID, PATIENT_ID_1)));

        ObjectMapper objectMapper = new ObjectMapper();

        client.post()
                .uri("/query/execute-cohort")
                .contentType(MEDIA_TYPE_SQ)
                .bodyValue("""
                        {
                          "inclusionCriteria": [
                            [
                              {
                                "context": {
                                  "system": "context-system",
                                  "code": "context-code",
                                  "display": "context-display"
                                },
                                "termCodes": [
                                  {
                                    "system": "http://snomed.info/sct",
                                    "code": "386661006",
                                    "display": "Fever (finding)"
                                  }
                                ]
                              }
                            ]
                          ]
                        }
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody().json(objectMapper.writeValueAsString(List.of(PATIENT_ID, PATIENT_ID_1)));
    }

    @Test
    void executeCohortDisabled() {
        client = WebTestClient.bindToRouterFunction(controller.queryRouter(false)).build();

        client.post()
                .uri("/query/execute-cohort")
                .contentType(MEDIA_TYPE_SQ)
                .bodyValue("""
                        {
                          "inclusionCriteria": [
                            [
                              {
                                "context": {
                                  "system": "context-system",
                                  "code": "context-code",
                                  "display": "context-display"
                                },
                                "termCodes": [
                                  {
                                    "system": "http://snomed.info/sct",
                                    "code": "386661006",
                                    "display": "Fever (finding)"
                                  }
                                ]
                              }
                            ]
                          ]
                        }
                        """)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void execute_error() {
        when(queryIdGenerator.generateRandom()).thenReturn(ID);
        when(queryService.execute(ID, STRUCTURED_QUERY)).thenReturn(Mono.error(new MappingNotFoundException(ContextualTermCode.of(TestUtil.CONTEXT, FEVER))));

        client.post()
                .uri("/query/execute")
                .contentType(MEDIA_TYPE_SQ)
                .bodyValue("""
                        {
                          "inclusionCriteria": [
                            [
                              {
                                "context": {
                                  "system": "context-system",
                                  "code": "context-code",
                                  "display": "context-display"
                                },
                                "termCodes": [
                                  {
                                    "system": "http://snomed.info/sct",
                                    "code": "386661006",
                                    "display": "Fever (finding)"
                                  }
                                ]
                              }
                            ]
                          ]
                        }
                        """)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("Mapping for the contextual term code ContextualTermCode[context=TermCode[system=context-system, code=context-code, display=context-display], termCode=TermCode[system=http://snomed.info/sct, code=386661006, display=Fever (finding)]] not found.");
    }

    @Test
    void translate() {
        when(queryService.translate(STRUCTURED_QUERY)).thenReturn(Either.right(Operator.union(QUERY_EXPRESSION)));

        client.post()
                .uri("/query/translate")
                .contentType(MEDIA_TYPE_SQ)
                .bodyValue("""
                        {
                          "inclusionCriteria": [
                            [
                              {
                                "context": {
                                  "system": "context-system",
                                  "code": "context-code",
                                  "display": "context-display"
                                },
                                "termCodes": [
                                  {
                                    "system": "http://snomed.info/sct",
                                    "code": "386661006",
                                    "display": "Fever (finding)"
                                  }
                                ]
                              }
                            ]
                          ]
                        }
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("union")
                .jsonPath("$.operands[0]").isEqualTo("[base]/Condition");
    }

    @Test
    void translate_error() {
        when(queryService.translate(STRUCTURED_QUERY)).thenReturn(Either.left(new MappingNotFoundException(ContextualTermCode.of(TestUtil.CONTEXT, FEVER))));

        client.post()
                .uri("/query/translate")
                .contentType(MEDIA_TYPE_SQ)
                .bodyValue("""
                        {
                          "inclusionCriteria": [
                            [
                              {
                                "context": {
                                  "system": "context-system",
                                  "code": "context-code",
                                  "display": "context-display"
                                },
                                "termCodes": [
                                  {
                                    "system": "http://snomed.info/sct",
                                    "code": "386661006",
                                    "display": "Fever (finding)"
                                  }
                                ]
                              }
                            ]
                          ]
                        }
                        """)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("Mapping for the contextual term code ContextualTermCode[context=TermCode[system=context-system, code=context-code, display=context-display], termCode=TermCode[system=http://snomed.info/sct, code=386661006, display=Fever (finding)]] not found.");
    }
}
