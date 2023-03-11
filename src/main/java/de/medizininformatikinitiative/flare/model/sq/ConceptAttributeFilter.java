package de.medizininformatikinitiative.flare.model.sq;

import de.medizininformatikinitiative.flare.model.mapping.Mapping;
import de.medizininformatikinitiative.flare.model.sq.expanded.CodeFilter;
import de.medizininformatikinitiative.flare.model.sq.expanded.ConceptFilter;
import de.medizininformatikinitiative.flare.model.sq.expanded.Filter;
import de.numcodex.sq2cql.model.common.TermCode;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.LinkedList;
import java.util.List;

import static java.util.Objects.requireNonNull;

public record ConceptAttributeFilter(TermCode code, List<TermCode> concepts) implements AttributeFilter {

    public ConceptAttributeFilter {
        requireNonNull(code);
        concepts = List.copyOf(concepts);
    }

    /**
     * Creates a concept attribute filter.
     *
     * @param code    the code identifying the attribute
     * @param concept the first selected concept
     * @return the concept attribute filter
     * @throws NullPointerException if any of the arguments is {@code null}
     */
    public static ConceptAttributeFilter of(TermCode code, TermCode concept) {
        return new ConceptAttributeFilter(code, List.of(concept));
    }

    public ConceptAttributeFilter appendConcept(TermCode concept) {
        var concepts = new LinkedList<>(this.concepts);
        concepts.add(concept);
        return new ConceptAttributeFilter(code, concepts);
    }

    @Override
    public Flux<Filter> toFilter(Mapping mapping) {
        return mapping.findAttributeMapping(code).flux()
                .flatMap(attributeMapping -> Flux.fromIterable(concepts)
                        .flatMap(concept -> switch (attributeMapping.type()) {
                            case CODE -> Mono.just(new CodeFilter(attributeMapping.searchParameter(), concept.code()));
                            case CODING -> Mono.just(new ConceptFilter(attributeMapping.searchParameter(), concept));
                        }));
    }
}
