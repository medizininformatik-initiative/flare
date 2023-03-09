package de.medizininformatikinitiative.flare.model.sq;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author Alexander Kiel
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record StructuredQuery(List<List<Criterion>> inclusionCriteria, List<List<Criterion>> exclusionCriteria) {

    public StructuredQuery {
        inclusionCriteria = inclusionCriteria.stream().map(List::copyOf).toList();
        exclusionCriteria = exclusionCriteria.stream().map(List::copyOf).toList();
    }

    public static StructuredQuery of(List<List<Criterion>> inclusionCriteria) {
        return new StructuredQuery(inclusionCriteria, List.of(List.of()));
    }

    @JsonCreator
    public static StructuredQuery of(@JsonProperty("inclusionCriteria") List<List<Criterion>> inclusionCriteria,
                                     @JsonProperty("exclusionCriteria") List<List<Criterion>> exclusionCriteria) {
        if (inclusionCriteria.isEmpty() || inclusionCriteria.stream().allMatch(List::isEmpty)) {
            throw new IllegalArgumentException("empty inclusion criteria");
        }
        return new StructuredQuery(inclusionCriteria,
                exclusionCriteria == null ? List.of(List.of()) : exclusionCriteria);
    }
}
