package de.medizininformatikinitiative.flare.model.mapping;

import de.medizininformatikinitiative.flare.Either;
import de.medizininformatikinitiative.flare.model.sq.Concept;
import de.medizininformatikinitiative.flare.model.sq.TermCode;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * A context holding information to facilitate the mapping process.
 * <p>
 * Uses the default system clock to private the current instant for date/time calculations.
 *
 * @author Alexander Kiel
 */
public class MappingContext {

    private final Map<TermCode, Mapping> mappings;
    private final TermCodeNode conceptTree;
    private final Clock clock;

    private MappingContext(Map<TermCode, Mapping> mappings, TermCodeNode conceptTree, Clock clock) {
        this.mappings = Map.copyOf(mappings);
        this.conceptTree = requireNonNull(conceptTree);
        this.clock = requireNonNull(clock);
    }

    /**
     * Returns a mapping context.
     *
     * @param mappings    the mappings keyed by their term code
     * @param conceptTree a tree of concepts to expand (can be null)
     * @return the mapping context
     */
    public static MappingContext of(Map<TermCode, Mapping> mappings, TermCodeNode conceptTree) {
        return new MappingContext(mappings, conceptTree, Clock.systemDefaultZone());
    }

    /**
     * Returns a mapping context.
     *
     * @param mappings    the mappings keyed by their term code
     * @param conceptTree a tree of concepts to expand (can be null)
     * @param clock       a clock that is used for time-related calculations
     * @return the mapping context
     */
    public static MappingContext of(Map<TermCode, Mapping> mappings, TermCodeNode conceptTree, Clock clock) {
        return new MappingContext(mappings, conceptTree, clock);
    }

    /**
     * Tries to find the {@link Mapping} with the given {@code key}.
     *
     * @param key the TermCode of the mapping
     * @return either the Mapping or an exception
     */
    public Either<Exception, Mapping> findMapping(TermCode key) {
        var mapping = mappings.get(requireNonNull(key));
        return mapping == null ? Either.left(new MappingNotFoundException(key)) : Either.right(mapping);
    }

    /**
     * Expands {@code concept} into an {@link Either either} of {@link TermCode term codes}.
     *
     * @param concept the concept to expand
     * @return either a list of term codes or an exception
     */
    public Either<Exception, List<TermCode>> expandConcept(Concept concept) {
        var termCodes = concept.termCodes().stream().flatMap(conceptTree::expand).toList();
        return termCodes.isEmpty() ? Either.left(new ConceptNotExpandableException(concept)) : Either.right(termCodes);
    }

    /**
     * Returns the current day according to the clock of this mapping context.
     *
     * @return the current day
     */
    public LocalDate today() {
        return LocalDate.now(clock);
    }
}
