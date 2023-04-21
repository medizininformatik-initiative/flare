package de.medizininformatikinitiative.flare.model.sq;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import de.medizininformatikinitiative.flare.Either;
import de.medizininformatikinitiative.flare.model.mapping.Mapping;
import de.medizininformatikinitiative.flare.model.mapping.ValueMappingNotFoundException;
import de.medizininformatikinitiative.flare.model.sq.expanded.ExpandedFilter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static java.util.Objects.requireNonNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ValueFilter(FilterPart filterPart) implements Filter {

    public ValueFilter {
        requireNonNull(filterPart);
    }

    public static ValueFilter ofConcept(TermCode firstConcept, TermCode... otherConcepts) {
        var filterPart = ConceptFilterPart.of(firstConcept);
        for (TermCode concept : otherConcepts) {
            filterPart = filterPart.appendConcept(concept);
        }
        return new ValueFilter(filterPart);
    }

    public static ValueFilter ofComparator(Comparator comparator, BigDecimal value) {
        return new ValueFilter(ComparatorFilterPart.of(comparator, value));
    }

    public static ValueFilter ofComparator(Comparator comparator, BigDecimal value, TermCode unit) {
        return new ValueFilter(ComparatorFilterPart.of(comparator, value, unit));
    }

    public static ValueFilter ofRange(BigDecimal lowerBound, BigDecimal upperBound, TermCode unit) {
        return new ValueFilter(RangeFilterPart.of(lowerBound, upperBound, unit));
    }

    /**
     * Parses an attribute filterPart.
     *
     * @param node the JSON representation of the attribute filterPart
     * @return the parsed attribute filterPart
     * @throws IllegalArgumentException if the JSON isn't valid
     */
    public static ValueFilter fromJsonNode(JsonNode node) {
        return new ValueFilter(FilterPart.fromJsonNode(node));
    }

    @Override
    public Either<Exception, List<ExpandedFilter>> expand(LocalDate today, Mapping mapping) {
        return mapping.valueFilterMapping()
                .map(filterMapping -> filterPart.expand(today, filterMapping))
                .orElseGet(() -> Either.left(new ValueMappingNotFoundException(mapping.key())));
    }
}
