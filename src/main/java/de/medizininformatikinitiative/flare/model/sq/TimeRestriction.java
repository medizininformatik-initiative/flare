package de.medizininformatikinitiative.flare.model.sq;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import static java.util.Objects.requireNonNull;

public record TimeRestriction(String afterDate, String beforeDate) {

    public TimeRestriction {
        requireNonNull(afterDate);
        requireNonNull(beforeDate);
    }

    public static TimeRestriction of(String afterDate, String beforeDate) {
        return new TimeRestriction(afterDate, beforeDate);
    }

    @JsonCreator
    public static TimeRestriction create(@JsonProperty("afterDate") String afterDate,
                                         @JsonProperty("beforeDate") String beforeDate) {
        //FIXME: quick and dirty for empty timeRestriction
        if (afterDate == null && beforeDate == null) {
            return null;
        }
        return TimeRestriction.of(afterDate, beforeDate);
    }
}
