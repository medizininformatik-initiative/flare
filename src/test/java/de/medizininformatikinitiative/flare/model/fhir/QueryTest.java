package de.medizininformatikinitiative.flare.model.fhir;

import org.junit.jupiter.api.Test;

import static de.medizininformatikinitiative.flare.model.fhir.QueryParams.stringValue;
import static org.assertj.core.api.Assertions.assertThat;

class QueryTest {

    @Test
    void testToString_TypeOnly() {
        var query = Query.ofType("Condition");

        var s = query.toString();

        assertThat(s).isEqualTo("Condition");
    }

    @Test
    void testToString_OneParam() {
        var query = new Query("Condition", QueryParams.of("name-152643", stringValue("value-152647"), null));

        var s = query.toString();

        assertThat(s).isEqualTo("Condition?name-152643=value-152647");
    }
}
