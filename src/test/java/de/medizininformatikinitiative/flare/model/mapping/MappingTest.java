package de.medizininformatikinitiative.flare.model.mapping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.medizininformatikinitiative.flare.model.sq.TermCode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MappingTest {

    static final TermCode GENDER = new TermCode("http://snomed.info/sct", "263495000", "Geschlecht");
    static final TermCode CONCEPT = new TermCode("http://hl7.org/fhir/consent-state-codes", "active", "Active");

    @Test
    void fromJson_gender() throws Exception {
        var mapping = parse("""
                {
                    "fhirResourceType": "Patient",
                    "key": {
                        "code": "263495000",
                        "display": "Geschlecht",
                        "system": "http://snomed.info/sct"
                    },
                    "valueFhirPath": "gender",
                    "valueSearchParameter": "gender",
                    "valueType": "code",
                    "valueTypeFhir": "code"
                }
                """);

        assertThat(mapping.key()).isEqualTo(GENDER);
        assertThat(mapping.resourceType()).isEqualTo("Patient");
        assertThat(mapping.termCodeSearchParameter()).isNull();
    }

    @Test
    void fromJson_withFixedCriterion() throws Exception {
        var mapping = parse("""
                {
                    "fhirResourceType": "Consent",
                    "fixedCriteria": [
                        {
                          "fhirPath": "status",
                          "searchParameter": "status",
                          "type": "code",
                          "value": [
                              {
                                "code": "active",
                                "display": "Active",
                                "system": "http://hl7.org/fhir/consent-state-codes"
                              }
                          ]
                        }
                    ],
                    "key": {
                        "code": "combined-consent",
                        "display": "Einwilligung f\\u00fcr die zentrale Datenanalyse",
                        "system": "mii.abide"
                    }
                }
                """);

        assertThat(mapping.fixedCriteria()).containsExactly(new FixedCriterion(FixedCriterionType.CODE, "status",
                List.of(CONCEPT), null));
    }

    static Mapping parse(String s) throws JsonProcessingException {
        return new ObjectMapper().readValue(s, Mapping.class);
    }
}
