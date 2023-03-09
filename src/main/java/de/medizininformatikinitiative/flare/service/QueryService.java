package de.medizininformatikinitiative.flare.service;

import de.medizininformatikinitiative.flare.model.Query;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface QueryService {

    CompletableFuture<Set<String>> execute(Query query);
}
