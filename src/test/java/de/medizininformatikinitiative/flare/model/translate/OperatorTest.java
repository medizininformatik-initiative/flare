package de.medizininformatikinitiative.flare.model.translate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.medizininformatikinitiative.flare.model.fhir.Query;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OperatorTest {

    @Test
    void isEmpty_NoOperands() {
        var operator = Operator.union();

        assertThat(operator.isEmpty()).isTrue();
    }

    @Test
    void isEmpty_OneEmptyOperand() {
        var operator = Operator.union(Operator.intersection());

        assertThat(operator.isEmpty()).isTrue();
    }

    @Test
    void isEmpty_OneNonEmptyOperand() {
        var operator = Operator.union(new QueryExpression(Query.ofType("Condition")));

        assertThat(operator.isEmpty()).isFalse();
    }

    @Test
    void isEmpty_OneEmptyFollowedByOneNonEmptyOperand() {
        var operator = Operator.union(Operator.intersection(), new QueryExpression(Query.ofType("Condition")));

        assertThat(operator.isEmpty()).isFalse();
    }

    @Test
    void serializeJson() throws JsonProcessingException {
        var operator = Operator.union(new QueryExpression(Query.ofType("Condition")));

        var s = new ObjectMapper().writeValueAsString(operator);

        assertThat(s).isEqualTo("""
                {"name":"union","operands":["[base]/Condition"]}""");
    }
}
