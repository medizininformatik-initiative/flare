package de.medizininformatikinitiative.flare.model.fhir;

import de.medizininformatikinitiative.flare.model.sq.TermCode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static de.medizininformatikinitiative.flare.model.sq.Comparator.GREATER_THAN;
import static org.assertj.core.api.Assertions.assertThat;

class QueryParamsTest {

    static final String UCUM_SYSTEM = "http://unitsofmeasure.org";
    static final String UNIT_CODE = "ug/dL";
    static final TermCode UNIT = new TermCode(UCUM_SYSTEM, UNIT_CODE, "ug/dL");
    static final BigDecimal VALUE = BigDecimal.valueOf(20.1);

    @Test
    void appendParam_withQuantityValue() {
        var queryParams = QueryParams.EMPTY.appendParam("value-quantity", GREATER_THAN, VALUE, UNIT);

        assertThat(queryParams)
                .hasToString("value-quantity=" + GREATER_THAN + VALUE + "|" + UCUM_SYSTEM + "|" + UNIT_CODE);
    }

    @Test
    void appendParam_withQuantityValue_withoutUnit() {
        var queryParams = QueryParams.EMPTY.appendParam("value-quantity", GREATER_THAN, VALUE, null);

        assertThat(queryParams).hasToString("value-quantity=" + GREATER_THAN + VALUE);
    }
}
