package de.medizininformatikinitiative.flare.service;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.medizininformatikinitiative.flare.model.Query;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Service
public class CachingQueryService implements QueryService {

    private final AsyncLoadingCache<Query, Set<String>> cache;

    public CachingQueryService(@Qualifier("dataStore") QueryService queryService) {
        cache = Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(Duration.ofMinutes(5))
                .refreshAfterWrite(Duration.ofMinutes(1))
                .buildAsync((query, executor) -> queryService.execute(query));
    }

    public CompletableFuture<Set<String>> execute(Query query) {
        return cache.get(query);
    }
}
