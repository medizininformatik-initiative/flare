package de.medizininformatikinitiative.flare.model.sq;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.medizininformatikinitiative.flare.Either;
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
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * A single, atomic criterion in a {@link StructuredQuery structured query}.
 *
 * @param concept the concept the criterion is about
 * @param filters the filters applied to entities of {@code concept}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Criterion(Concept concept, List<Filter> filters, List<ReferenceFilter> referenceFilters) {

    public Criterion {
        requireNonNull(concept);
        filters = List.copyOf(filters);
    }

    public static Criterion of(Concept concept) {
        return new Criterion(concept, List.of(), List.of());
    }

    public static Criterion of(Concept concept, ValueFilter valueFilter) {
        return new Criterion(concept, List.of(valueFilter), List.of());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonCreator
    static Criterion fromJson(@JsonProperty("termCodes") List<TermCode> termCodes,
                              @JsonProperty("valueFilter") ObjectNode valueFilter,
                              @JsonProperty("attributeFilters") List<ObjectNode> attributeFilters,
                              @JsonProperty("timeRestriction") TimeRestriction timeRestriction) throws JsonProcessingException {
        var concept = Concept.of(requireNonNull(termCodes, "missing JSON property: termCodes"));

        var filters = new LinkedList<Filter>();
        var referenceFilters = new LinkedList<ReferenceFilter>();
        if (valueFilter != null) {
            filters.add(ValueFilter.fromJsonNode(valueFilter));
        }
        for (ObjectNode attributeFilter : (attributeFilters == null ? List.<ObjectNode>of() : attributeFilters)) {
            var type = attributeFilter.get("type").asText();
            if (type.equals("reference")) {
                referenceFilters.add(new ObjectMapper().readValue(attributeFilter.toString(), ReferenceFilter.class));


            } else {
                filters.add(AttributeFilter.fromJsonNode(attributeFilter));
            }
        }
        if (timeRestriction != null) {
            filters.add(timeRestriction);
        }

        return new Criterion(concept, filters, referenceFilters);
    }

    Criterion appendAttributeFilter(AttributeFilter attributeFilter) {
        var filters = new LinkedList<>(this.filters);
        filters.add(attributeFilter);
        return new Criterion(concept, filters, referenceFilters);
    }

    Criterion appendTimeRestrictionFilter(TimeRestriction timeRestrictionFilter) {
        var filters = new LinkedList<>(this.filters);
        filters.add(timeRestrictionFilter);
        return new Criterion(concept, filters, referenceFilters);
    }

    Criterion appendReferenceFiler(ReferenceFilter referenceFilter) {
        var referenceFilters = new LinkedList<>(this.referenceFilters);
        referenceFilters.add(referenceFilter);
        return new Criterion(concept, filters, referenceFilters);
    }

    /**
     * Expands this criterion into a {@link Mono mono} of {@link ExpandedCriterion expanded criteria}.
     *
     * @param mappingContext contains the mappings needed to create the expanded criteria
     * @return a mono of expanded criteria
     */
    public Either<Exception, List<ExpandedCriterion>> expand(MappingContext mappingContext) {
        return mappingContext.expandConcept(concept)
                .flatMap(termCodes -> termCodes.stream()
                        .map(termCode -> expandTermCode(mappingContext, termCode))
                        .reduce(Either.right(List.of()), Either.liftBinOp(Util::concat)));
    }

    public Either<Exception, List<TermCode>> expandConcept(MappingContext mappingContext) {
        return mappingContext.expandConcept(concept);
    }

    private Either<Exception, List<ExpandedCriterion>> expandTermCode(MappingContext mappingContext, TermCode termCode) {
        return mappingContext.findMapping(termCode)
                .flatMap(mapping -> expandReferenceFilters(mapping, mappingContext, expandNonReferenceFilters(mappingContext.today(), mapping, null), 0)
                        .flatMap(expRefFilters -> expRefFilters.stream().map(filterList ->
                                        filterList.map(Util::cartesianProduct)).reduce(Either.right(List.of()), Either.liftBinOp(Util::concat))
                                .map(expandedFilterMatrix ->
                                        expandedFilterMatrix.isEmpty()
                                                ? List.of(expandedCriterion(mapping, termCode))
                                                : expandedFilterMatrix.stream()
                                                .map(expandedFilters -> expandedCriterion(mapping, termCode, expandedFilters))
                                                .toList())));
    }

    private Either<Exception, List<Either<Exception, List<List<ExpandedFilter>>>>> expandReferenceFilters(Mapping outerTermCodeMapping,
                                                                                                          MappingContext mappingContext,
                                                                                                          Either<Exception,
                                                                                                                  List<List<ExpandedFilter>>> listA,
                                                                                                          int refFilterIndex) {
        if (referenceFilters.size() == 0) {
            return Either.right(List.of(listA));
        }
        return referenceFilters.get(refFilterIndex).expand(outerTermCodeMapping, mappingContext).flatMap(allCritFilters -> {
            Either<Exception, List<Either<Exception, List<List<ExpandedFilter>>>>> resultList = Either.right(new LinkedList<>());
            for (Either<Exception, List<List<ExpandedFilter>>> singleCritFilters : allCritFilters) {
                var concatenatedFilters = Stream.of(listA).reduce(singleCritFilters, Either.liftBinOp(Util::concat));
                if (refFilterIndex + 1 < referenceFilters.size()) {
                    resultList = Stream.of(resultList).reduce(expandReferenceFilters(outerTermCodeMapping, mappingContext,
                            concatenatedFilters, refFilterIndex + 1), Either.liftBinOp(Util::concat));
                } else {
                    resultList = Stream.of(resultList).reduce(Either.right(List.of(concatenatedFilters)), Either.liftBinOp(Util::concat));
                }
            }
            return resultList;
        });

    }

    public Either<Exception, List<List<ExpandedFilter>>> expandNonReferenceFilters(LocalDate today, Mapping mapping, String referenceSearchParam) {
        return filters.stream()
                .map(filter -> filter.expand(today, mapping, referenceSearchParam))
                .reduce(Either.right(fixedCriterionFilters(mapping)), Either.lift2(Util::add), Either.liftBinOp(Util::concat));
    }

    private static List<List<ExpandedFilter>> fixedCriterionFilters(Mapping mapping) {
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
