package de.medizininformatikinitiative.flare.model.translate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.medizininformatikinitiative.flare.model.fhir.Query;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QueryExpressionTest {

    @Test
    void isNotEmpty() {
        var expression = new QueryExpression(Query.ofType("Condition"));

        assertThat(expression.isEmpty()).isFalse();
    }

    @Test
    void serializeJson() throws JsonProcessingException {
        var expression = new QueryExpression(Query.ofType("Condition"));

        var s = new ObjectMapper().writeValueAsString(expression);

        assertThat(s).isEqualTo("\"Condition\"");
    }
}
