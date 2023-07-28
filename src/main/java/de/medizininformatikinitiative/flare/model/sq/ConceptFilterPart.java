package de.medizininformatikinitiative.flare.model.sq;

import de.medizininformatikinitiative.flare.Either;
import de.medizininformatikinitiative.flare.Util;
import de.medizininformatikinitiative.flare.model.mapping.FilterMapping;
import de.medizininformatikinitiative.flare.model.sq.expanded.ExpandedFilter;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

public record ConceptFilterPart(List<TermCode> concepts) implements FilterPart {

    public ConceptFilterPart {
        concepts = List.copyOf(concepts);
    }

    /**
     * Creates a concept attribute filter part.
     *
     * @param concept the first selected concept
     * @return the concept attribute filter part
     * @throws NullPointerException if {@code concept} is {@code null}
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
        return concepts.stream().map(filterMapping::expandConcept)
                .reduce(Either.right(List.of()), Either.lift2(Util::add), Either.liftBinOp(Util::concat));
    }
}
