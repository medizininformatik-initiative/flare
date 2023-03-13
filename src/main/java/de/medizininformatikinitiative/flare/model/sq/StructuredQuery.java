package de.medizininformatikinitiative.flare.model.sq;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import static java.util.Objects.requireNonNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StructuredQuery(CriterionGroup<CriterionGroup<Criterion>> inclusionCriteria,
                              CriterionGroup<CriterionGroup<Criterion>> exclusionCriteria) {

    public StructuredQuery {
        requireNonNull(inclusionCriteria);
        requireNonNull(exclusionCriteria);
    }

    public static StructuredQuery of(CriterionGroup<CriterionGroup<Criterion>> inclusionCriteria) {
        return new StructuredQuery(inclusionCriteria, CriterionGroup.of(CriterionGroup.of()));
    }

    public static StructuredQuery of(CriterionGroup<CriterionGroup<Criterion>> inclusionCriteria,
                                     CriterionGroup<CriterionGroup<Criterion>> exclusionCriteria) {
        return new StructuredQuery(inclusionCriteria, exclusionCriteria);
    }

    @JsonCreator
    public static StructuredQuery fromJson(@JsonProperty("inclusionCriteria") List<List<Criterion>> inclusionCriteria,
                                           @JsonProperty("exclusionCriteria") List<List<Criterion>> exclusionCriteria) {
        if (inclusionCriteria.isEmpty() || inclusionCriteria.stream().allMatch(List::isEmpty)) {
            throw new IllegalArgumentException("empty inclusion criteria");
        }
        return new StructuredQuery(new CriterionGroup<>(inclusionCriteria.stream().map(CriterionGroup::new).toList()),
                exclusionCriteria == null
                        ? CriterionGroup.of(CriterionGroup.of())
                        : new CriterionGroup<>(exclusionCriteria.stream().map(CriterionGroup::new).toList()));
    }
}
