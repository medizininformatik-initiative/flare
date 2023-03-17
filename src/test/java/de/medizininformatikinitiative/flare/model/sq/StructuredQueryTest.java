package de.medizininformatikinitiative.flare.model.sq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StructuredQueryTest {

    static final TermCode C71 = TermCode.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "C71",
            "Malignant neoplasm of brain");

    @Test
    void deserializeJson() throws JsonProcessingException {
        var s = """
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
                """;

        var query = new ObjectMapper().readValue(s, StructuredQuery.class);

        assertThat(query).isEqualTo(StructuredQuery.of(CriterionGroup.of(CriterionGroup.of(Criterion.of(Concept.of(C71))))));
    }
}
