package de.medizininformatikinitiative.flare.model.mapping;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.medizininformatikinitiative.flare.model.sq.TermCode;
import de.medizininformatikinitiative.flare.model.sq.expanded.ExpandedCodeFilter;
import de.medizininformatikinitiative.flare.model.sq.expanded.ExpandedConceptFilter;
import de.medizininformatikinitiative.flare.model.sq.expanded.ExpandedFilter;

import java.util.List;

import static java.util.Objects.requireNonNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FixedCriterion(FilterType type, String searchParameter, List<TermCode> concepts) {

    public FixedCriterion {
        requireNonNull(type);
        requireNonNull(searchParameter);
        concepts = List.copyOf(concepts);
    }

    @JsonCreator
    static FixedCriterion of(@JsonProperty("type") FilterType type,
                             @JsonProperty("searchParameter") String searchParameter,
                             @JsonProperty("value") List<TermCode> concepts) {
        return new FixedCriterion(type, searchParameter, concepts);
    }

    public List<ExpandedFilter> expand() {
        return concepts.stream().map(concept -> switch (type) {
            case CODING -> (ExpandedFilter) new ExpandedConceptFilter(searchParameter, concept);
            case CODE -> new ExpandedCodeFilter(searchParameter, concept.code());
        }).toList();
    }
}
