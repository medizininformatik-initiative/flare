package de.medizininformatikinitiative.flare.service;

import com.github.benmanes.caffeine.cache.AsyncCacheLoader;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Weigher;
import de.medizininformatikinitiative.flare.model.Population;
import de.medizininformatikinitiative.flare.model.fhir.Query;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static java.util.Objects.requireNonNull;

public class MemCachingFhirQueryService implements CachingService, FhirQueryService {

    private static final Weigher<Query, Population> WEIGHER = (key, value) ->
            key.toString().length() + value.memSize();

    private final FhirQueryService fhirQueryService;
    private final AsyncLoadingCache<Query, Population> cache;

    public MemCachingFhirQueryService(FhirQueryService fhirQueryService, Config config) {
        this.fhirQueryService = requireNonNull(fhirQueryService);
        cache = Caffeine.newBuilder()
                .weigher(WEIGHER)
                .maximumWeight(config.sizeInBytes)
                .expireAfterWrite(config.expireDuration)
                .refreshAfterWrite(config.refreshDuration)
                .recordStats()
                .buildAsync(new CacheLoader());
    }

    public CompletableFuture<Population> execute(Query query, boolean ignoreCache) {
        return cache.get(query);
    }

    public CacheStats stats() {
        var syncCache = cache.synchronous();
        return new CacheStats(syncCache.estimatedSize(),
                syncCache.stats().hitCount(),
                syncCache.stats().missCount());
    }

    public record Config(long sizeInBytes, Duration expireDuration, Duration refreshDuration) {
    }

    private class CacheLoader implements AsyncCacheLoader<Query, Population> {

        @Override
        public CompletableFuture<? extends Population> asyncLoad(Query query, Executor executor) {
            return fhirQueryService.execute(query);
        }

        @Override
        public CompletableFuture<? extends Population> asyncReload(Query query, Population oldValue, Executor executor) {
            return fhirQueryService.execute(query, true);
        }
    }
}
