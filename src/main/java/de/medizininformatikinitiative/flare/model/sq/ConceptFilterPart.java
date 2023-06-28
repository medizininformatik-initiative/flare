package de.medizininformatikinitiative.flare.model.sq;

import de.medizininformatikinitiative.flare.Either;
import de.medizininformatikinitiative.flare.model.mapping.FilterMapping;
import de.medizininformatikinitiative.flare.model.sq.expanded.ExpandedCodeFilter;
import de.medizininformatikinitiative.flare.model.sq.expanded.ExpandedConceptFilter;
import de.medizininformatikinitiative.flare.model.sq.expanded.ExpandedFilter;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static de.medizininformatikinitiative.flare.model.mapping.FilterType.*;

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
    public Either<Exception, List<ExpandedFilter>> expand(LocalDate today, FilterMapping filterMapping, String referenceSearchParam) {
        if(filterMapping.type() == COMPOSITE_QUANTITY_COMPARATOR || filterMapping.type() == COMPOSITE_QUANTITY_RANGE){
            return Either.left(new ConceptFilterTypeNotExpandableException(filterMapping.type()));
        }
        return Either.right(concepts.stream()
                .map(concept -> switch (filterMapping.type()) {
                    case CODE -> (ExpandedFilter) new ExpandedCodeFilter(filterMapping.searchParameter(),
                            concept.code(), referenceSearchParam);
                    case CODING -> new ExpandedConceptFilter(filterMapping.searchParameter(), concept, null,referenceSearchParam);
                    case COMPOSITE_CONCEPT_COMPARATOR -> new ExpandedConceptFilter(filterMapping.searchParameter(), concept, filterMapping.compositeCode(), referenceSearchParam);
                    case COMPOSITE_QUANTITY_RANGE, COMPOSITE_QUANTITY_COMPARATOR, REFERENCE -> null;
                }).filter(Objects::nonNull)
                .toList());
    }
}
