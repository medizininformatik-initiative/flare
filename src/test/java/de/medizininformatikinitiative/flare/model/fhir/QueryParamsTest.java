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
    static final TermCode COMPOSITE_CODE = new TermCode("http://loing.org", "8480-6", "Sistolic Bloodpressure");
    static final TermCode TERM_CODE = TermCode.of("system-152138", "code-152142", "display-152148");

    @Test
    void appendParam_withQuantityValue() {
        var queryParams = QueryParams.EMPTY.appendParam("value-quantity", GREATER_THAN, VALUE, UNIT, null, null);

        assertThat(queryParams)
                .hasToString("value-quantity=" + GREATER_THAN + VALUE + "|" + UCUM_SYSTEM + "|" + UNIT_CODE);
    }

    @Test
    void appendParam_withQuantityValue_withoutUnit() {
        var queryParams = QueryParams.EMPTY.appendParam("value-quantity", GREATER_THAN, VALUE, null, null, null);

        assertThat(queryParams).hasToString("value-quantity=" + GREATER_THAN + VALUE);
    }

    @Test
    void appendParam_withComparator_withCompositeCode() {
        var queryParams = QueryParams.EMPTY.appendParam("component-code-value-quantity", GREATER_THAN, VALUE, null, COMPOSITE_CODE, null);

        assertThat(queryParams).hasToString("component-code-value-quantity=" + COMPOSITE_CODE.system() + "|" +
                                            COMPOSITE_CODE.code() + "$" + GREATER_THAN + VALUE);
    }

    @Test
    void appendParam_withConcept_withCompositeCode() {
        var queryParams = QueryParams.EMPTY.appendParam("component-code-value-concept", TERM_CODE, COMPOSITE_CODE, null);

        assertThat(queryParams).hasToString("component-code-value-concept=" + COMPOSITE_CODE.system() + "|" +
                COMPOSITE_CODE.code() + "$" + TERM_CODE.system() + "|" + TERM_CODE.code());
    }

    @Test
    void appendParam_withReferenceSearchParam() {
        var queryParams = QueryParams.EMPTY.appendParam("code", TERM_CODE, null, "diagnosis");

        assertThat(queryParams).hasToString("diagnosis.code=" +  TERM_CODE.system() + "|" + TERM_CODE.code());
    }

    @Test
    void appendParam_withQuantityValue_withReferenceSearchParam() {
        var queryParams = QueryParams.EMPTY.appendParam("value-quantity", GREATER_THAN, VALUE, UNIT, null, "diagnosis");

        assertThat(queryParams)
                .hasToString("diagnosis.value-quantity=" + GREATER_THAN + VALUE + "|" + UCUM_SYSTEM + "|" + UNIT_CODE);
    }
}
