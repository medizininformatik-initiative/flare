package de.medizininformatikinitiative.flare.model.mapping;

import de.numcodex.sq2cql.model.TermCodeNode;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.structured_query.Concept;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

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
        this.mappings = mappings;
        this.conceptTree = conceptTree;
    }

    /**
     * Returns an empty mapping context.
     *
     * @return the mapping context
     */
    public static MappingContext of() {
        return new MappingContext(Map.of(), null);
    }

    /**
     * Returns a mapping context.
     *
     * @param mappings    the mappings keyed by their term code
     * @param conceptTree a tree of concepts to expand (can be null)
     * @return the mapping context
     */
    public static MappingContext of(Map<TermCode, Mapping> mappings, TermCodeNode conceptTree) {
        return new MappingContext(Map.copyOf(mappings), conceptTree);
    }

    /**
     * Tries to find the {@link Mapping} with the given {@code key}.
     *
     * @param key the TermCode of the mapping
     * @return either the Mapping or {@code Optional#empty() nothing}
     */
    public Optional<Mapping> findMapping(TermCode key) {
        return Optional.ofNullable(mappings.get(requireNonNull(key)));
    }

    /**
     * Expands {@code concept} into a stream of {@link TermCode TermCodes}.
     *
     * @param concept the concept to expand
     * @return the stream of TermCodes
     */
    public Stream<TermCode> expandConcept(Concept concept) {
        var expandedCodes = conceptTree == null ? List.<TermCode>of() : expandCodes(concept);
        return (expandedCodes.isEmpty() ? concept.termCodes() : expandedCodes).stream().filter(mappings::containsKey);
    }

    private List<TermCode> expandCodes(Concept concept) {
        return concept.termCodes().stream().flatMap(conceptTree::expand).toList();
    }
}
