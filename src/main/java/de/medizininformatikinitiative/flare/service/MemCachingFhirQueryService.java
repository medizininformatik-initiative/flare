package de.medizininformatikinitiative.flare.service;

import com.github.benmanes.caffeine.cache.AsyncCacheLoader;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Weigher;
import de.medizininformatikinitiative.flare.model.Population;
import de.medizininformatikinitiative.flare.model.fhir.Query;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static java.util.Objects.requireNonNull;

public class MemCachingFhirQueryService implements CachingService, FhirQueryService {

    private static final Logger logger = LoggerFactory.getLogger(MemCachingFhirQueryService.class);

    private static final Weigher<Query, Population> WEIGHER = (key, value) ->
            key.toString().length() + value.memSize();

    private final FhirQueryService fhirQueryService;
    private final Config config;
    private AsyncLoadingCache<Query, Population> cache;

    public MemCachingFhirQueryService(FhirQueryService fhirQueryService, Config config) {
        this.fhirQueryService = requireNonNull(fhirQueryService);
        this.config = requireNonNull(config);
    }

    @PostConstruct
    public void init() {
        logger.info("Starting MemCachingFhirQueryService with: {}", config);
        cache = Caffeine.newBuilder()
                .weigher(WEIGHER)
                .maximumWeight(config.sizeInBytes)
                .expireAfterWrite(config.expire)
                .refreshAfterWrite(config.refresh)
                .recordStats()
                .buildAsync(new CacheLoader());
    }

    @Override
    public Mono<Population> execute(Query query, boolean ignoreCache) {
        logger.trace("Try loading population for query `{}` from memory.", query);
        return Mono.fromFuture(cache.get(query));
    }

    @Override
    public CacheStats stats() {
        var syncCache = cache.synchronous();
        return new CacheStats(syncCache.estimatedSize(),
                syncCache.stats().hitCount(),
                syncCache.stats().missCount());
    }

    public record Config(long sizeInBytes, Duration expire, Duration refresh) {
    }

    private class CacheLoader implements AsyncCacheLoader<Query, Population> {

        @Override
        public CompletableFuture<Population> asyncLoad(Query query, Executor executor) {
            return fhirQueryService.execute(query).toFuture();
        }

        @Override
        public CompletableFuture<Population> asyncReload(Query query, Population oldValue, Executor executor) {
            return fhirQueryService.execute(query, true).toFuture();
        }
    }
}
