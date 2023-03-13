package de.medizininformatikinitiative.flare.model.fhir;

import de.medizininformatikinitiative.flare.model.sq.Comparator;
import de.medizininformatikinitiative.flare.model.sq.TermCode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;


class QueryParamsTest {

    @Test
    void appendParam_withValue() {
        var name = "value-quantity";
        var comparator = Comparator.GREATER_THAN;
        var value = BigDecimal.valueOf(20.1);
        var unit = new TermCode("http://loinc.org", "ug/dL", "ug/dL");

        QueryParams queryParams = QueryParams.EMPTY.appendParam(name, comparator, value, unit);

        assertThat(queryParams.toString()).isEqualTo(name + "=" + comparator + value + "|" + unit.system()
                + "|" + unit.code());
    }
}