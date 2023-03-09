package de.medizininformatikinitiative.flare.model.sq;

import de.numcodex.sq2cql.model.structured_query.AttributeFilter;
import de.numcodex.sq2cql.model.structured_query.Concept;
import de.numcodex.sq2cql.model.structured_query.TimeRestriction;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Abstract criterion holding the concept, every non-static criterion has.
 */
abstract class AbstractCriterion implements Criterion {

    final Concept concept;
    final List<AttributeFilter> attributeFilters;
    final TimeRestriction timeRestriction;

    AbstractCriterion(Concept concept, List<AttributeFilter> attributeFilters, TimeRestriction timeRestriction) {
        this.concept = requireNonNull(concept);
        this.attributeFilters = List.copyOf(attributeFilters);
        this.timeRestriction = timeRestriction;
    }
}
