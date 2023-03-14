package de.medizininformatikinitiative.flare.model.mapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.medizininformatikinitiative.flare.model.sq.TermCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MappingTest {

    static final TermCode GENDER = new TermCode("http://snomed.info/sct", "263495000", "Geschlecht");

    @Test
    void fromJson() throws Exception {
        var mapper = new ObjectMapper();

        var mapping = mapper.readValue("""
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
                """, Mapping.class);

        assertThat(mapping.key()).isEqualTo(GENDER);
        assertThat(mapping.resourceType()).isEqualTo("Patient");
        assertThat(mapping.termCodeSearchParameter()).isNull();
    }
}
