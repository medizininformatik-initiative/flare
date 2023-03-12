package de.medizininformatikinitiative.flare.model.sq;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.medizininformatikinitiative.flare.model.mapping.Mapping;
import de.medizininformatikinitiative.flare.model.mapping.MappingContext;
import de.medizininformatikinitiative.flare.model.sq.expanded.ExpandedCriterion;
import de.medizininformatikinitiative.flare.model.sq.expanded.ExpandedFilter;
import reactor.core.publisher.Flux;

import java.util.LinkedList;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * A single, atomic criterion in a {@link StructuredQuery structured query}.
 *
 * @param concept the concept the criterion is about
 * @param filters the filters applied to entities of {@code concept}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Criterion(Concept concept, List<Filter> filters, TimeRestriction timeRestriction) {

    public Criterion {
        requireNonNull(concept);
        filters = List.copyOf(filters);
    }

    public static Criterion of(Concept concept) {
        return new Criterion(concept, List.of(), null);
    }

    public static Criterion of(Concept concept, ValueFilter valueFilter) {
        return new Criterion(concept, List.of(valueFilter), null);
    }

    @JsonCreator
    static Criterion create(@JsonProperty("termCodes") List<TermCode> termCodes,
                            @JsonProperty("valueFilter") ObjectNode valueFilter,
                            @JsonProperty("timeRestriction") TimeRestriction timeRestriction,
                            @JsonProperty("attributeFilters") List<ObjectNode> attributeFilters) {
        var concept = Concept.of(requireNonNull(termCodes, "missing JSON property: termCodes"));

        var filters = new LinkedList<Filter>();
        if (valueFilter != null) {
            filters.add(ValueFilter.fromJsonNode(valueFilter));
        }
        for (ObjectNode attributeFilter : attributeFilters) {
            filters.add(AttributeFilter.fromJsonNode(attributeFilter));
        }

        return new Criterion(concept, filters, timeRestriction);
    }

    Criterion appendAttributeFilter(AttributeFilter attributeFilter) {
        var filters = new LinkedList<>(this.filters);
        filters.add(attributeFilter);
        return new Criterion(concept, filters, timeRestriction);
    }

    /**
     * Expands this criterion into a {@link Flux flux} of {@link ExpandedCriterion expanded criteria}.
     *
     * @param mappingContext contains the mappings needed to create the expanded criteria
     * @return a {@link Flux flux} of {@link ExpandedCriterion expanded criteria}
     */
    public Flux<ExpandedCriterion> expand(MappingContext mappingContext) {
        return mappingContext.expandConcept(concept).flatMap(termCode -> expandTermCode(mappingContext, termCode));
    }

    private Flux<ExpandedCriterion> expandTermCode(MappingContext mappingContext, TermCode termCode) {
        return mappingContext.findMapping(termCode).flux()
                .flatMap(mapping -> Flux.fromIterable(filters)
                        .flatMap(filter -> filter.expand(mapping)
                                .map(expandedFilter -> expandedCriterion(mapping, termCode, List.of(expandedFilter))))
                        .defaultIfEmpty(expandedCriterion(mapping, termCode, List.of())));
    }

    private static ExpandedCriterion expandedCriterion(Mapping mapping, TermCode termCode,
                                                       List<ExpandedFilter> attributeFilters) {
        return new ExpandedCriterion(mapping.resourceType(), mapping.termCodeSearchParameter(), termCode,
                attributeFilters);
    }
}
