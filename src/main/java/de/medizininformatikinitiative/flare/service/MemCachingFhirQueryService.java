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
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static java.util.Objects.requireNonNull;

public class MemCachingFhirQueryService implements FhirQueryService {

    private static final Logger logger = LoggerFactory.getLogger(MemCachingFhirQueryService.class);

    private static final Weigher<QueryWrapper, Population> WEIGHER = (key, value) ->
            key.query.toString().length() + value.memSize();

    private final FhirQueryService fhirQueryService;
    private final Config config;
    private AsyncLoadingCache<QueryWrapper, Population> cache;

    public MemCachingFhirQueryService(FhirQueryService fhirQueryService, Config config) {
        this.fhirQueryService = requireNonNull(fhirQueryService);
        this.config = requireNonNull(config);
    }

    @PostConstruct
    public void init() {
        logger.info("Starting MemCachingFhirQueryService with: {}", config);
        cache = Caffeine.newBuilder()
                .weigher(WEIGHER)
                .maximumWeight(config.sizeInMebibytes << 20)
                .expireAfterWrite(config.expire)
                .refreshAfterWrite(config.refresh)
                .recordStats()
                .buildAsync(new CacheLoader());
    }

    @Override
    public Mono<Population> execute(UUID id, Query query, boolean ignoreCache) {
        logger.trace("Try loading population for query `{}` part of query {} from memory.", query, id);
        return Mono.fromFuture(cache.get(new QueryWrapper(id, query)));
    }

    public CacheStats stats() {
        var syncCache = cache.synchronous();
        return new CacheStats(syncCache.estimatedSize(),
                config.sizeInMebibytes,
                syncCache.asMap().values().stream().mapToInt(Population::memSize).sum() >> 20,
                syncCache.stats().hitCount(),
                syncCache.stats().missCount(),
                syncCache.stats().evictionCount(),
                syncCache.stats().loadSuccessCount(),
                syncCache.stats().loadFailureCount(),
                syncCache.stats().totalLoadTime());
    }

    public record Config(long sizeInMebibytes, Duration expire, Duration refresh) {
    }

    public record CacheStats(long estimatedEntryCount, long maxMemoryMiB, long usedMemoryMiB, long hitCount,
                             long missCount, long evictionCount, long loadSuccessCount, long loadFailureCount,
                             long totalLoadTimeNanos) {
    }

    private record QueryWrapper(UUID id, Query query) {

        private QueryWrapper {
            requireNonNull(id);
            requireNonNull(query);
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            QueryWrapper that = (QueryWrapper) o;
            return Objects.equals(query, that.query);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(query);
        }
    }

    private class CacheLoader implements AsyncCacheLoader<QueryWrapper, Population> {

        @Override
        public CompletableFuture<Population> asyncLoad(QueryWrapper query, Executor executor) {
            logger.trace("Cache miss for query `{}` part of query {}.", query.query, query.id);
            return fhirQueryService.execute(query.id, query.query).toFuture();
        }

        @Override
        public CompletableFuture<Population> asyncReload(QueryWrapper query, Population oldValue, Executor executor) {
            logger.trace("Refresh query `{}`.", query.query);
            return fhirQueryService.execute(query.id, query.query, true).toFuture();
        }
    }
}
