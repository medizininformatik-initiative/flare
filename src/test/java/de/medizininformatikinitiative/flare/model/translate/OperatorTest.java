package de.medizininformatikinitiative.flare.model.translate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.medizininformatikinitiative.flare.model.fhir.Query;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OperatorTest {

    @Test
    void serializeJson() throws JsonProcessingException {
        var operator = Operator.union(new QueryExpression(Query.ofType("Condition")));

        var s = new ObjectMapper().writeValueAsString(operator);

        assertThat(s).isEqualTo("""
                {"name":"union","operands":["[base]/Condition"]}""");
    }
}
