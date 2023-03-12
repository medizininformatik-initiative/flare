package de.medizininformatikinitiative.flare.service;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.medizininformatikinitiative.flare.model.fhir.Query;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Service
public class CachingFhirQueryService implements FhirQueryService {

    private final AsyncLoadingCache<Query, Set<String>> cache;

    public CachingFhirQueryService(@Qualifier("dataStore") FhirQueryService fhirQueryService) {
        cache = Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(Duration.ofMinutes(60))
                .refreshAfterWrite(Duration.ofMinutes(10))
                .buildAsync((query, executor) -> fhirQueryService.execute(query));
    }

    public CompletableFuture<Set<String>> execute(Query query) {
        return cache.get(query);
    }
}
