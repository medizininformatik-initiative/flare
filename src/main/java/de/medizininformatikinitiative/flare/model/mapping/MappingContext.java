package de.medizininformatikinitiative.flare.model.mapping;

import de.medizininformatikinitiative.flare.model.sq.ConceptNotExpandableException;
import de.medizininformatikinitiative.flare.model.sq.MappingNotFoundException;
import de.numcodex.sq2cql.model.TermCodeNode;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.structured_query.Concept;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * A context holding information to facilitate the mapping process.
 *
 * @author Alexander Kiel
 */
public class MappingContext {

    private final Map<TermCode, Mapping> mappings;
    private final TermCodeNode conceptTree;

    private MappingContext(Map<TermCode, Mapping> mappings, TermCodeNode conceptTree) {
        this.mappings = Map.copyOf(mappings);
        this.conceptTree = requireNonNull(conceptTree);
    }

    /**
     * Returns a mapping context.
     *
     * @param mappings    the mappings keyed by their term code
     * @param conceptTree a tree of concepts to expand (can be null)
     * @return the mapping context
     */
    public static MappingContext of(Map<TermCode, Mapping> mappings, TermCodeNode conceptTree) {
        return new MappingContext(mappings, conceptTree);
    }

    /**
     * Tries to find the {@link Mapping} with the given {@code key}.
     *
     * @param key the TermCode of the mapping
     * @return either the Mapping or {@code Optional#empty() nothing}
     */
    public Mono<Mapping> findMapping(TermCode key) {
        var mapping = mappings.get(requireNonNull(key));
        return mapping == null ? Mono.error(new MappingNotFoundException(key)) : Mono.just(mapping);
    }

    /**
     * Expands {@code concept} into a stream of {@link TermCode TermCodes}.
     *
     * @param concept the concept to expand
     * @return the stream of TermCodes
     */
    public Flux<TermCode> expandConcept(Concept concept) {
        return Flux.fromStream(concept.termCodes().stream().flatMap(conceptTree::expand))
                .switchIfEmpty(Mono.error(new ConceptNotExpandableException(concept)));
    }
}
