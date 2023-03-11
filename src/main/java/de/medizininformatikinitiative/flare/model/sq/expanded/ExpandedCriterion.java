package de.medizininformatikinitiative.flare.model.sq.expanded;

import de.medizininformatikinitiative.flare.model.Query;
import de.medizininformatikinitiative.flare.model.mapping.Mapping;
import de.medizininformatikinitiative.flare.model.sq.Criterion;

/**
 * A criterion that is already expanded from a {@link Criterion criterion} of the structured query.
 * <p>
 * Expanded criterion {@link #toQuery() translate} to exactly one {@link Query query} and contain already all
 * {@link Mapping mapping information} needed.
 */
public interface ExpandedCriterion {

    /**
     * Transforms this criterion into a {@link Query query}.
     *
     * @return the query of this criterion
     */
    Query toQuery();
}
