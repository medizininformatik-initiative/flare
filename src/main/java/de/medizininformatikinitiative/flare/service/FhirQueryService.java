package de.medizininformatikinitiative.flare.service;

import de.medizininformatikinitiative.flare.model.Population;
import de.medizininformatikinitiative.flare.model.fhir.Query;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface FhirQueryService {

    /**
     * Executes {@code query}.
     *
     * @param id          the ID of the query used for tracing purposes
     * @param query       the query to execute
     * @param ignoreCache if the cache shouldn't be used
     * @return the population result
     */
    Mono<Population> execute(UUID id, Query query, boolean ignoreCache);

    /**
     * Executes {@code query} using a potentially cache.
     *
     * @param id          the ID of the query used for tracing purposes
     * @param query       the query to execute
     * @return the population result
     */
    default Mono<Population> execute(UUID id, Query query) {
        return execute(id, query, false);
    }
}
