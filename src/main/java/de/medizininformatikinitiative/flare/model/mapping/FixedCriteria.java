package de.medizininformatikinitiative.flare.model.mapping;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.medizininformatikinitiative.flare.model.sq.TermCode;

import java.util.List;

import static java.util.Objects.requireNonNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FixedCriteria(FilterType type, String searchParameter, List<TermCode> concepts) {

    public FixedCriteria {
        requireNonNull(type);
        requireNonNull(searchParameter);
        concepts = List.copyOf(concepts);
    }

    @JsonCreator
    static FixedCriteria of(@JsonProperty("type") FilterType type,
                            @JsonProperty("searchParameter") String searchParameter,
                            @JsonProperty("value") List<TermCode> concepts) {
        return new FixedCriteria(type, searchParameter, concepts);
    }
}
