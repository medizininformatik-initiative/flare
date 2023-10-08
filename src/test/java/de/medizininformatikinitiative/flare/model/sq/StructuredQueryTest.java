package de.medizininformatikinitiative.flare.model.sq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static de.medizininformatikinitiative.flare.model.sq.TestUtil.cc;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StructuredQueryTest {

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
                        "context": {
                          "system": "context-system",
                          "code": "context-code",
                          "display": "context-display"
                        },
                        "termCodes": [
                          {
                            "system": "system-172743",
                            "code": "code",
                            "display": "display"
                          }
                        ]
                      }
                    ]
                  ]
                }
                """);

        assertThat(query).isEqualTo(StructuredQuery.of(CriterionGroup.of(CriterionGroup.of(Criterion.of(
                cc("system-172743"))))));
    }

    @Test
    void deserializeJson_TwoInclusionCriteria() throws JsonProcessingException {
        var query = parse("""
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
                            "system": "system-172743",
                            "code": "code-1",
                            "display": "display"
                          }
                        ]
                      },
                      {
                        "context": {
                          "system": "context-system",
                          "code": "context-code",
                          "display": "context-display"
                        },
                        "termCodes": [
                          {
                            "system": "system-172743",
                            "code": "code-2",
                            "display": "display"
                          }
                        ]
                      }
                    ]
                  ]
                }
                """);

        assertThat(query).isEqualTo(StructuredQuery.of(CriterionGroup.of(CriterionGroup.of(
                Criterion.of(cc("system-172743", 1)),
                Criterion.of(cc("system-172743", 2))
        ))));
    }

    @Test
    void deserializeJson_TwoInclusionCriteria2() throws JsonProcessingException {
        var query = parse("""
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
                            "system": "system-172743",
                            "code": "code-1",
                            "display": "display"
                          }
                        ]
                      }
                    ],
                    [
                      {
                        "context": {
                          "system": "context-system",
                          "code": "context-code",
                          "display": "context-display"
                        },
                        "termCodes": [
                          {
                            "system": "system-172743",
                            "code": "code-2",
                            "display": "display"
                          }
                        ]
                      }
                    ]
                  ]
                }
                """);

        assertThat(query).isEqualTo(StructuredQuery.of(CriterionGroup.of(CriterionGroup.of(
                        Criterion.of(cc("system-172743", 1))),
                CriterionGroup.of(Criterion.of(cc("system-172743", 2))))));
    }

    @Test
    void deserializeJson_EmptyInclusionCriteriaAreIgnored() throws JsonProcessingException {
        var query = parse("""
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
                            "system": "system-172743",
                            "code": "code",
                            "display": "display"
                          }
                        ]
                      }
                    ],
                    [
                    ]
                  ]
                }
                """);

        assertThat(query).isEqualTo(StructuredQuery.of(CriterionGroup.of(CriterionGroup.of(Criterion.of(
                cc("system-172743"))))));
    }

    @Test
    void deserializeJson_OneExclusionCriteria() throws JsonProcessingException {
        var query = parse("""
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
                            "system": "system-172743",
                            "code": "code-1",
                            "display": "display"
                          }
                        ]
                      }
                    ]
                  ],
                  "exclusionCriteria": [
                    [
                      {
                        "context": {
                          "system": "context-system",
                          "code": "context-code",
                          "display": "context-display"
                        },
                        "termCodes": [
                          {
                            "system": "system-172743",
                            "code": "code-2",
                            "display": "display"
                          }
                        ]
                      }
                    ]
                  ]
                }
                """);

        assertThat(query).isEqualTo(StructuredQuery.of(
                CriterionGroup.of(CriterionGroup.of(Criterion.of(cc("system-172743", 1)))),
                CriterionGroup.of(CriterionGroup.of(Criterion.of(cc("system-172743", 2))))
        ));
    }

    @Test
    void deserializeJson_TwoExclusionCriteria() throws JsonProcessingException {
        var query = parse("""
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
                            "system": "system-172743",
                            "code": "code-1",
                            "display": "display"
                          }
                        ]
                      }
                    ]
                  ],
                  "exclusionCriteria": [
                    [
                      {
                        "context": {
                          "system": "context-system",
                          "code": "context-code",
                          "display": "context-display"
                        },
                        "termCodes": [
                          {
                            "system": "system-172743",
                            "code": "code-2",
                            "display": "display"
                          }
                        ]
                      },
                      {
                        "context": {
                          "system": "context-system",
                          "code": "context-code",
                          "display": "context-display"
                        },
                        "termCodes": [
                          {
                            "system": "system-172743",
                            "code": "code-3",
                            "display": "display"
                          }
                        ]
                      }
                    ]
                  ]
                }
                """);

        assertThat(query).isEqualTo(StructuredQuery.of(
                CriterionGroup.of(CriterionGroup.of(Criterion.of(cc("system-172743", 1)))),
                CriterionGroup.of(CriterionGroup.of(Criterion.of(cc("system-172743", 2)),
                        Criterion.of(cc("system-172743", 3))))
        ));
    }

    @Test
    void deserializeJson_TwoExclusionCriteria2() throws JsonProcessingException {
        var query = parse("""
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
                            "system": "system-172743",
                            "code": "code-1",
                            "display": "display"
                          }
                        ]
                      }
                    ]
                  ],
                  "exclusionCriteria": [
                    [
                      {
                        "context": {
                          "system": "context-system",
                          "code": "context-code",
                          "display": "context-display"
                        },
                        "termCodes": [
                          {
                            "system": "system-172743",
                            "code": "code-2",
                            "display": "display"
                          }
                        ]
                      }
                    ],
                    [
                      {
                        "context": {
                          "system": "context-system",
                          "code": "context-code",
                          "display": "context-display"
                        },
                        "termCodes": [
                          {
                            "system": "system-172743",
                            "code": "code-3",
                            "display": "display"
                          }
                        ]
                      }
                    ]
                  ]
                }
                """);

        assertThat(query).isEqualTo(StructuredQuery.of(
                CriterionGroup.of(CriterionGroup.of(Criterion.of(cc("system-172743", 1)))),
                CriterionGroup.of(CriterionGroup.of(Criterion.of(cc("system-172743", 2))),
                        CriterionGroup.of(Criterion.of(cc("system-172743", 3))))
        ));
    }

    static StructuredQuery parse(String s) throws JsonProcessingException {
        return new ObjectMapper().readValue(s, StructuredQuery.class);
    }
}
