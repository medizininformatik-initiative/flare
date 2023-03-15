package de.medizininformatikinitiative.flare.service;

import de.medizininformatikinitiative.flare.model.Population;
import de.medizininformatikinitiative.flare.model.fhir.Query;

import java.util.concurrent.CompletableFuture;

public interface FhirQueryService {

    CompletableFuture<Population> execute(Query query, boolean ignoreCache);

    default CompletableFuture<Population> execute(Query query) {
        return execute(query, false);
    }
}
