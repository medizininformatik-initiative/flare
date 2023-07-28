package de.medizininformatikinitiative.flare.model.sq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StructuredQueryTest {

    static final TermCode C71 = TermCode.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "C71",
            "Malignant neoplasm of brain");

    static final TermCode C72 = TermCode.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "C72",
            "Malignant neoplasm of brain");

    static final TermCode C73 = TermCode.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "C73",
            "Malignant neoplasm of brain");

    @Test
    void deserializeJson_Empty() {
        assertThatThrownBy(() -> parse("{}"))
                .hasRootCauseMessage("empty inclusion criteria");
    }

    @Test
    void deserializeJson_EmptyInclusionCriteria() {
        assertThatThrownBy(() -> parse("""
                {
                  "inclusionCriteria": [
                  ]
                }
                """))
                .hasRootCauseMessage("empty inclusion criteria");
    }

    @Test
    void deserializeJson_EmptyInclusionCriteria2() {
        assertThatThrownBy(() -> parse("""
                {
                  "inclusionCriteria": [
                    [
                    ]
                  ]
                }
                """))
                .hasRootCauseMessage("empty inclusion criteria");
    }

    @Test
    void deserializeJson_OneInclusionCriteria() throws JsonProcessingException {
        var query = parse("""
                {
                  "inclusionCriteria": [
                    [
                      {
                        "termCodes": [
                          {
                            "system": "http://fhir.de/CodeSystem/bfarm/icd-10-gm",
                            "code": "C71",
                            "display": "Malignant neoplasm of brain"
                          }
                        ]
                      }
                    ]
                  ]
                }
                """);

        assertThat(query).isEqualTo(StructuredQuery.of(CriterionGroup.of(CriterionGroup.of(Criterion.of(Concept.of(C71))))));
    }

    @Test
    void deserializeJson_TwoInclusionCriteria() throws JsonProcessingException {
        var query = parse("""
                {
                  "inclusionCriteria": [
                    [
                      {
                        "termCodes": [
                          {
                            "system": "http://fhir.de/CodeSystem/bfarm/icd-10-gm",
                            "code": "C71",
                            "display": "Malignant neoplasm of brain"
                          }
                        ]
                      },
                      {
                        "termCodes": [
                          {
                            "system": "http://fhir.de/CodeSystem/bfarm/icd-10-gm",
                            "code": "C72",
                            "display": "Malignant neoplasm of brain"
                          }
                        ]
                      }
                    ]
                  ]
                }
                """);

        assertThat(query).isEqualTo(StructuredQuery.of(CriterionGroup.of(CriterionGroup.of(Criterion.of(Concept.of(C71)),
                Criterion.of(Concept.of(C72))))));
    }

    @Test
    void deserializeJson_TwoInclusionCriteria2() throws JsonProcessingException {
        var query = parse("""
                {
                  "inclusionCriteria": [
                    [
                      {
                        "termCodes": [
                          {
                            "system": "http://fhir.de/CodeSystem/bfarm/icd-10-gm",
                            "code": "C71",
                            "display": "Malignant neoplasm of brain"
                          }
                        ]
                      }
                    ],
                    [
                      {
                        "termCodes": [
                          {
                            "system": "http://fhir.de/CodeSystem/bfarm/icd-10-gm",
                            "code": "C72",
                            "display": "Malignant neoplasm of brain"
                          }
                        ]
                      }
                    ]
                  ]
                }
                """);

        assertThat(query).isEqualTo(StructuredQuery.of(CriterionGroup.of(CriterionGroup.of(Criterion.of(Concept.of(C71))),
                CriterionGroup.of(Criterion.of(Concept.of(C72))))));
    }

    @Test
    void deserializeJson_EmptyInclusionCriteriaAreIgnored() throws JsonProcessingException {
        var query = parse("""
                {
                  "inclusionCriteria": [
                    [
                      {
                        "termCodes": [
                          {
                            "system": "http://fhir.de/CodeSystem/bfarm/icd-10-gm",
                            "code": "C71",
                            "display": "Malignant neoplasm of brain"
                          }
                        ]
                      }
                    ],
                    [
                    ]
                  ]
                }
                """);

        assertThat(query).isEqualTo(StructuredQuery.of(CriterionGroup.of(CriterionGroup.of(Criterion.of(Concept.of(C71))))));
    }

    @Test
    void deserializeJson_OneExclusionCriteria() throws JsonProcessingException {
        var query = parse("""
                {
                  "inclusionCriteria": [
                    [
                      {
                        "termCodes": [
                          {
                            "system": "http://fhir.de/CodeSystem/bfarm/icd-10-gm",
                            "code": "C71",
                            "display": "Malignant neoplasm of brain"
                          }
                        ]
                      }
                    ]
                  ],
                  "exclusionCriteria": [
                    [
                      {
                        "termCodes": [
                          {
                            "system": "http://fhir.de/CodeSystem/bfarm/icd-10-gm",
                            "code": "C72",
                            "display": "Malignant neoplasm of brain"
                          }
                        ]
                      }
                    ]
                  ]
                }
                """);

        assertThat(query).isEqualTo(StructuredQuery.of(
                CriterionGroup.of(CriterionGroup.of(Criterion.of(Concept.of(C71)))),
                CriterionGroup.of(CriterionGroup.of(Criterion.of(Concept.of(C72))))
        ));
    }

    @Test
    void deserializeJson_TwoExclusionCriteria() throws JsonProcessingException {
        var query = parse("""
                {
                  "inclusionCriteria": [
                    [
                      {
                        "termCodes": [
                          {
                            "system": "http://fhir.de/CodeSystem/bfarm/icd-10-gm",
                            "code": "C71",
                            "display": "Malignant neoplasm of brain"
                          }
                        ]
                      }
                    ]
                  ],
                  "exclusionCriteria": [
                    [
                      {
                        "termCodes": [
                          {
                            "system": "http://fhir.de/CodeSystem/bfarm/icd-10-gm",
                            "code": "C72",
                            "display": "Malignant neoplasm of brain"
                          }
                        ]
                      },
                      {
                        "termCodes": [
                          {
                            "system": "http://fhir.de/CodeSystem/bfarm/icd-10-gm",
                            "code": "C73",
                            "display": "Malignant neoplasm of brain"
                          }
                        ]
                      }
                    ]
                  ]
                }
                """);

        assertThat(query).isEqualTo(StructuredQuery.of(
                CriterionGroup.of(CriterionGroup.of(Criterion.of(Concept.of(C71)))),
                CriterionGroup.of(CriterionGroup.of(Criterion.of(Concept.of(C72)), Criterion.of(Concept.of(C73))))
        ));
    }

    @Test
    void deserializeJson_TwoExclusionCriteria2() throws JsonProcessingException {
        var query = parse("""
                {
                  "inclusionCriteria": [
                    [
                      {
                        "termCodes": [
                          {
                            "system": "http://fhir.de/CodeSystem/bfarm/icd-10-gm",
                            "code": "C71",
                            "display": "Malignant neoplasm of brain"
                          }
                        ]
                      }
                    ]
                  ],
                  "exclusionCriteria": [
                    [
                      {
                        "termCodes": [
                          {
                            "system": "http://fhir.de/CodeSystem/bfarm/icd-10-gm",
                            "code": "C72",
                            "display": "Malignant neoplasm of brain"
                          }
                        ]
                      }
                    ],
                    [
                      {
                        "termCodes": [
                          {
                            "system": "http://fhir.de/CodeSystem/bfarm/icd-10-gm",
                            "code": "C73",
                            "display": "Malignant neoplasm of brain"
                          }
                        ]
                      }
                    ]
                  ]
                }
                """);

        assertThat(query).isEqualTo(StructuredQuery.of(
                CriterionGroup.of(CriterionGroup.of(Criterion.of(Concept.of(C71)))),
                CriterionGroup.of(CriterionGroup.of(Criterion.of(Concept.of(C72))),
                        CriterionGroup.of(Criterion.of(Concept.of(C73))))
        ));
    }

    static StructuredQuery parse(String s) throws JsonProcessingException {
        return new ObjectMapper().readValue(s, StructuredQuery.class);
    }
}
