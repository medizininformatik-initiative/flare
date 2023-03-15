package de.medizininformatikinitiative.flare.service;

import de.medizininformatikinitiative.flare.model.Population;
import de.medizininformatikinitiative.flare.model.fhir.Query;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.TtlDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

public class DiskCachingFhirQueryService implements CachingService, FhirQueryService {

    private static final Logger logger = LoggerFactory.getLogger(DiskCachingFhirQueryService.class);

    private final FhirQueryService fhirQueryService;
    private final Config config;
    private final Executor executor;
    private Options options;
    private RocksDB db;
    private final AtomicLong hitCount = new AtomicLong();
    private final AtomicLong missCount = new AtomicLong();

    public DiskCachingFhirQueryService(FhirQueryService fhirQueryService, Config config, Executor executor) {
        this.fhirQueryService = requireNonNull(fhirQueryService);
        this.config = requireNonNull(config);
        this.executor = requireNonNull(executor);
    }

    @PostConstruct
    public void init() throws RocksDBException {
        options = new Options();
        options.setCreateIfMissing(true);
        db = TtlDB.open(options, config.path, (int) config.ttl.toSeconds(), false);
    }

    public CompletableFuture<Population> execute(Query query, boolean ignoreCache) {
        return (ignoreCache ? Optional.<Population>empty() : internalGet(query)).map(CompletableFuture::completedFuture)
                .orElseGet(() -> fhirQueryService.execute(query, ignoreCache).whenComplete((population, e) -> {
                    if (population != null) {
                        put(query, population);
                    }
                }));
    }

    @Override
    public CacheStats stats() {
        return new CacheStats(0, hitCount.get(), missCount.get());
    }

    private Optional<Population> internalGet(Query query) {
        try {
            return Optional.ofNullable(db.get(serializeQuery(query))).flatMap(bytes -> {
                try {
                    return Optional.of(Population.fromByteBuffer(ByteBuffer.wrap(bytes)));
                } catch (SerializerException e) {
                    logger.warn("Skip loading population because of: {}", e.getMessage());
                    return Optional.empty();
                } finally {
                    hitCount.incrementAndGet();
                }
            });
        } catch (RocksDBException e) {
            logger.warn("Skip loading population because of: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private void put(Query query, Population population) {
        executor.execute(() -> internalPut(query, population));
    }

    private void internalPut(Query query, Population population) {
        try {
            logger.trace("Store result of size {} for query: {}", population.size(), query);
            db.put(serializeQuery(query), population.toByteArray());
            missCount.incrementAndGet();
        } catch (RocksDBException e) {
            logger.warn("Skip caching population because of: {}", e.getMessage());
        }
    }

    private static byte[] serializeQuery(Query query) {
        return query.toString().getBytes(UTF_8);
    }

    @PreDestroy
    public void destroy() {
        logger.info("Shutting down the Disk Cache...");
        db.close();
        options.close();
        logger.info("Finished shutting down the Disk Cache.");
    }

    public record Config(String path, Duration ttl) {
    }
}
