package de.medizininformatikinitiative.flare.model.mapping;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import de.medizininformatikinitiative.flare.Either;
import de.medizininformatikinitiative.flare.model.sq.TermCode;
import de.medizininformatikinitiative.flare.model.sq.expanded.ExpandedCodeFilter;
import de.medizininformatikinitiative.flare.model.sq.expanded.ExpandedCompositeConceptFilter;
import de.medizininformatikinitiative.flare.model.sq.expanded.ExpandedConceptFilter;
import de.medizininformatikinitiative.flare.model.sq.expanded.ExpandedFilter;

import java.util.List;
import java.util.Objects;

import static de.medizininformatikinitiative.flare.model.mapping.FilterType.COMPOSITE_QUANTITY_COMPARATOR;
import static de.medizininformatikinitiative.flare.model.mapping.FilterType.COMPOSITE_QUANTITY_RANGE;
import static java.util.Objects.requireNonNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FixedCriterion(FilterType type, String searchParameter, List<TermCode> concepts, TermCode compositeCode) {

    //TODO: welche Typen gibt es hier? Warum muessen wir den FilterType nehmen? Oder waere es besser einen eigenen Typ zu nehmen?

    public FixedCriterion {
        requireNonNull(type);
        requireNonNull(searchParameter);
        concepts = List.copyOf(concepts);
    }

    @JsonCreator
    static FixedCriterion of(@JsonProperty("type") FilterType type,
                             @JsonProperty("searchParameter") String searchParameter,
                             @JsonProperty("value") List<TermCode> concepts,
                             @JsonProperty("compositeCode") JsonNode compositeCode) {
        TermCode compCode = compositeCode == null ? null : TermCode.fromJsonNode(compositeCode);
        return new FixedCriterion(type, searchParameter, concepts, compCode);
    }

    public Either<Exception, List<ExpandedFilter>> expand() {
        //TODO: ein nicht passender Typ ist kein Expansionsproblem, sondern ein Parsing Problem
        return type == COMPOSITE_QUANTITY_COMPARATOR || type == COMPOSITE_QUANTITY_RANGE
                ? Either.left(new FixedCriterionTypeNotExpandableException(type))
                : Either.right(concepts.stream().map(concept -> switch (type) {
            case CODING -> (ExpandedFilter) new ExpandedConceptFilter(searchParameter, concept);
            case CODE -> new ExpandedCodeFilter(searchParameter, concept.code());
            case COMPOSITE_CONCEPT_COMPARATOR -> new ExpandedCompositeConceptFilter(searchParameter,
                    compositeCode, concept);
            default -> null;
        }).filter(Objects::nonNull).toList());

    }
}
