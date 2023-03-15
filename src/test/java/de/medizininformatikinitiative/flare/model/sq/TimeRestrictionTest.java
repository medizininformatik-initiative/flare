package de.medizininformatikinitiative.flare.model.sq;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TimeRestrictionTest {

    @Test
    void fromJson_withInvalidAfterDate() {
        var mapper = new ObjectMapper();

        assertThatThrownBy(() -> mapper.readValue("""
                {
                  "afterDate": "foo",
                  "beforeDate": "2021-10-09"
                }
                """, TimeRestriction.class))
                .hasRootCauseMessage("Invalid value `foo` in time restriction property `afterDate`.");
    }

    @Test
    void fromJson_withInvalidBeforeDate() {
        var mapper = new ObjectMapper();

        assertThatThrownBy(() -> mapper.readValue("""
                {
                  "afterDate": "2021-10-09",
                  "beforeDate": "bar"
                }
                """, TimeRestriction.class))
                .hasRootCauseMessage("Invalid value `bar` in time restriction property `beforeDate`.");
    }

    @Test
    void fromJson_withBothDatesMissing() {
        var mapper = new ObjectMapper();

        assertThatThrownBy(() -> mapper.readValue("{}", TimeRestriction.class))
                .hasRootCauseMessage("Missing properties expect at least one of `afterDate` or `beforeDate`.");
    }

    @Test
    void fromJson_withoutAfterDate() throws Exception {
        var mapper = new ObjectMapper();

        var result = mapper.readValue("""
                {
                  "beforeDate": "2021-10-09"
                }
                """, TimeRestriction.class);

        assertThat(result).isEqualTo(new TimeRestriction.OpenStart(LocalDate.of(2021, 10, 9)));
    }

    @Test
    void fromJson_withoutBeforeDate() throws Exception {
        var mapper = new ObjectMapper();

        var result = mapper.readValue("""
                {
                  "afterDate": "2023-03-16"
                }
                """, TimeRestriction.class);

        assertThat(result).isEqualTo(new TimeRestriction.OpenEnd(LocalDate.of(2023, 3, 16)));
    }
}
