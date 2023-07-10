package de.medizininformatikinitiative.flare.model.sq;

import de.medizininformatikinitiative.flare.Either;
import de.medizininformatikinitiative.flare.Util;
import de.medizininformatikinitiative.flare.model.mapping.FilterMapping;
import de.medizininformatikinitiative.flare.model.sq.expanded.ExpandedCodeFilter;
import de.medizininformatikinitiative.flare.model.sq.expanded.ExpandedCompositeConceptFilter;
import de.medizininformatikinitiative.flare.model.sq.expanded.ExpandedConceptFilter;
import de.medizininformatikinitiative.flare.model.sq.expanded.ExpandedFilter;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import static de.medizininformatikinitiative.flare.model.mapping.FilterType.COMPOSITE_CONCEPT_COMPARATOR;

public record ConceptFilterPart(List<TermCode> concepts) implements FilterPart {

    public ConceptFilterPart {
        concepts = List.copyOf(concepts);
    }

    /**
     * Creates a concept attribute filterPart.
     *
     * @param concept the first selected concept
     * @return the concept attribute filterPart
     * @throws NullPointerException if any of the arguments is {@code null}
     */
    static ConceptFilterPart of(TermCode concept) {
        return new ConceptFilterPart(List.of(concept));
    }

    ConceptFilterPart appendConcept(TermCode concept) {
        var concepts = new LinkedList<>(this.concepts);
        concepts.add(concept);
        return new ConceptFilterPart(concepts);
    }

    @Override
    public Either<Exception, List<ExpandedFilter>> expand(LocalDate today, FilterMapping filterMapping) {
        //TODO: more expansion of a single concept into FilterMapping
        return concepts.stream()
                .map((Function<TermCode, Either<Exception, ExpandedFilter>>) concept -> switch (filterMapping.type()) {
                    case CODE -> Either.right(new ExpandedCodeFilter(filterMapping.searchParameter(), concept.code()));
                    case CODING -> Either.right(new ExpandedConceptFilter(filterMapping.searchParameter(), concept));
                    case COMPOSITE_QUANTITY_COMPARATOR, COMPOSITE_QUANTITY_RANGE ->
                            Either.left(new ConceptFilterTypeNotExpandableException(filterMapping.type()));
                    case COMPOSITE_CONCEPT_COMPARATOR -> filterMapping.compositeCode()
                            .map((Function<TermCode, Either<Exception, ExpandedFilter>>) compositeCode ->
                                    Either.right(new ExpandedCompositeConceptFilter(filterMapping.searchParameter(),
                                            compositeCode, concept)))
                            .orElse(Either.left(new ConceptFilterTypeNotExpandableException(COMPOSITE_CONCEPT_COMPARATOR)));
                })
                .reduce(Either.right(List.of()), Either.lift2(Util::add), Either.liftBinOp(Util::concat));
    }
}
