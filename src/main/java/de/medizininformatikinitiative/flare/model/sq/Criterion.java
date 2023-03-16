package de.medizininformatikinitiative.flare.model.sq;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.medizininformatikinitiative.flare.Util;
import de.medizininformatikinitiative.flare.model.mapping.FixedCriterion;
import de.medizininformatikinitiative.flare.model.mapping.Mapping;
import de.medizininformatikinitiative.flare.model.mapping.MappingContext;
import de.medizininformatikinitiative.flare.model.sq.expanded.ExpandedCriterion;
import de.medizininformatikinitiative.flare.model.sq.expanded.ExpandedFilter;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
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
public record Criterion(Concept concept, List<Filter> filters) {

    public Criterion {
        requireNonNull(concept);
        filters = List.copyOf(filters);
    }

    public static Criterion of(Concept concept) {
        return new Criterion(concept, List.of());
    }

    public static Criterion of(Concept concept, ValueFilter valueFilter) {
        return new Criterion(concept, List.of(valueFilter));
    }

    @JsonCreator
    static Criterion fromJson(@JsonProperty("termCodes") List<TermCode> termCodes,
                              @JsonProperty("valueFilter") ObjectNode valueFilter,
                              @JsonProperty("attributeFilters") List<ObjectNode> attributeFilters,
                              @JsonProperty("timeRestriction") TimeRestriction timeRestriction) {
        var concept = Concept.of(requireNonNull(termCodes, "missing JSON property: termCodes"));

        var filters = new LinkedList<Filter>();
        if (valueFilter != null) {
            filters.add(ValueFilter.fromJsonNode(valueFilter));
        }
        for (ObjectNode attributeFilter : (attributeFilters == null ? List.<ObjectNode>of() : attributeFilters)) {
            filters.add(AttributeFilter.fromJsonNode(attributeFilter));
        }
        if (timeRestriction != null) {
            filters.add(timeRestriction);
        }

        return new Criterion(concept, filters);
    }

    Criterion appendAttributeFilter(AttributeFilter attributeFilter) {
        var filters = new LinkedList<>(this.filters);
        filters.add(attributeFilter);
        return new Criterion(concept, filters);
    }

    Criterion appendTimeRestrictionFilter(TimeRestriction timeRestrictionFilter) {
        var filters = new LinkedList<>(this.filters);
        filters.add(timeRestrictionFilter);
        return new Criterion(concept, filters);
    }

    /**
     * Expands this criterion into a {@link Mono mono} of {@link ExpandedCriterion expanded criteria}.
     *
     * @param mappingContext contains the mappings needed to create the expanded criteria
     * @return a mono of expanded criteria
     */
    public Mono<List<ExpandedCriterion>> expand(MappingContext mappingContext) {
        return mappingContext.expandConcept(concept)
                .flatMap(termCodes -> termCodes.stream()
                        .map(termCode -> expandTermCode(mappingContext, termCode))
                        .reduce(Mono.just(List.of()), Util::concat));
    }

    private Mono<List<ExpandedCriterion>> expandTermCode(MappingContext mappingContext, TermCode termCode) {
        return mappingContext.findMapping(termCode)
                .flatMap(mapping -> expandFilters(mappingContext.today(), mapping)
                        .map(Util::cartesianProduct)
                        .map(expandedFilterMatrix -> expandedFilterMatrix.isEmpty()
                                ? List.of(expandedCriterion(mapping, termCode))
                                : expandedFilterMatrix.stream()
                                .map(expandedFilters -> expandedCriterion(mapping, termCode, expandedFilters))
                                .toList()));
    }

    private Mono<List<List<ExpandedFilter>>> expandFilters(LocalDate today, Mapping mapping) {
        return filters.stream()
                .map(filter -> filter.expand(today, mapping))
                .reduce(Mono.just(fixedCriterionFilters(mapping)), Util::add, Util::concat);
    }

    private List<List<ExpandedFilter>> fixedCriterionFilters(Mapping mapping) {
        return mapping.fixedCriteria().stream().map(FixedCriterion::expand).toList();
    }

    private static ExpandedCriterion expandedCriterion(Mapping mapping, TermCode termCode) {
        return ExpandedCriterion.of(mapping.resourceType(), mapping.termCodeSearchParameter(), termCode);
    }

    private static ExpandedCriterion expandedCriterion(Mapping mapping, TermCode termCode,
                                                       List<ExpandedFilter> filters) {
        return new ExpandedCriterion(mapping.resourceType(), mapping.termCodeSearchParameter(),
                mapping.termCodeSearchParameter() == null ? null : termCode, filters);
    }
}
