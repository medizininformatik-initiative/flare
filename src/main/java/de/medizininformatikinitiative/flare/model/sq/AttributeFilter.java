package de.medizininformatikinitiative.flare.model.sq;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import de.medizininformatikinitiative.flare.Either;
import de.medizininformatikinitiative.flare.model.mapping.Mapping;
import de.medizininformatikinitiative.flare.model.sq.expanded.ExpandedFilter;

import java.time.LocalDate;
import java.util.List;

import static java.util.Objects.requireNonNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AttributeFilter(TermCode code, FilterPart filterPart) implements Filter {

    public AttributeFilter {
        requireNonNull(code);
        requireNonNull(filterPart);
    }

    public static AttributeFilter ofConcept(TermCode code, TermCode firstConcept, TermCode... otherConcepts) {
        var filterPart = ConceptFilterPart.of(firstConcept);
        for (TermCode concept : otherConcepts) {
            filterPart = filterPart.appendConcept(concept);
        }
        return new AttributeFilter(code, filterPart);
    }

    /**
     * Parses an attribute filterPart.
     *
     * @param node the JSON representation of the attribute filterPart
     * @return the parsed attribute filterPart
     * @throws IllegalArgumentException if the JSON isn't valid
     */
    public static AttributeFilter fromJsonNode(JsonNode node) {
        var code = TermCode.fromJsonNode(node.get("attributeCode"));
        return new AttributeFilter(code, FilterPart.fromJsonNode(node));
    }

    @Override
    public Either<Exception, List<ExpandedFilter>> expand(LocalDate today, Mapping mapping) {
        return mapping.findAttributeMapping(code).flatMap(filterMapping -> filterPart.expand(today, filterMapping));
    }
}
