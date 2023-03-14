package de.medizininformatikinitiative.flare.model.fhir;

import de.medizininformatikinitiative.flare.model.sq.Comparator;
import de.medizininformatikinitiative.flare.model.sq.TermCode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;


class QueryParamsTest {

    public static final String UCUM_SYSTEM = "http://unitsofmeasure.org/";
    public static final String UNIT_CODE = "ug/dL";
    public static final TermCode UNIT = new TermCode(UCUM_SYSTEM, UNIT_CODE, "ug/dL");

    @Test
    void appendParam_withQuantityValue() {

        QueryParams queryParams = QueryParams.EMPTY.appendParam("value-quantity", Comparator.GREATER_THAN,
                                                                BigDecimal.valueOf(20.1), UNIT);

        assertThat(queryParams.toString()).isEqualTo("value-quantity" + "=" + Comparator.GREATER_THAN
                                                             + BigDecimal.valueOf(20.1) + "|" + UCUM_SYSTEM + "|"
                                                             + UNIT_CODE);
    }
}
