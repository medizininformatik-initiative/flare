package de.medizininformatikinitiative.flare.service;

import de.medizininformatikinitiative.flare.model.fhir.Query;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface FhirQueryService {

    CompletableFuture<Set<String>> execute(Query query);
}
