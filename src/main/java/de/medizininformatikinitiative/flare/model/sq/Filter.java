package de.medizininformatikinitiative.flare.model.sq;

import de.medizininformatikinitiative.flare.model.mapping.Mapping;
import de.medizininformatikinitiative.flare.model.sq.expanded.ExpandedFilter;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

public interface Filter {

    /**
     * Expands this filter into possible multiple {@link ExpandedFilter expanded filters} using the {@code mapping}.
     * <p>
     * Filter expansion creates one expanded filter for each concept of a {@link ConceptFilterPart concept filter part}.
     * Filters with other filter parts expand to one expanded filter.
     *
     * @param today   the current day used for date/time calculations
     * @param mapping the mapping to use for expansion
     * @return possibly multiple expanded filters
     */
    Mono<List<ExpandedFilter>> expand(LocalDate today, Mapping mapping);
}
