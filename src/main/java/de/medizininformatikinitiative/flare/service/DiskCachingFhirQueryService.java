package de.medizininformatikinitiative.flare.service;

import de.medizininformatikinitiative.flare.model.Population;
import de.medizininformatikinitiative.flare.model.fhir.Query;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.rocksdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.time.Clock;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;
import static org.rocksdb.CompressionType.LZ4_COMPRESSION;
import static org.rocksdb.CompressionType.ZSTD_COMPRESSION;

public class DiskCachingFhirQueryService implements CachingService, FhirQueryService {

    private static final Logger logger = LoggerFactory.getLogger(DiskCachingFhirQueryService.class);

    private final FhirQueryService fhirQueryService;
    private final Config config;
    private final Executor executor;
    private final Clock clock;
    private Options options;
    private RocksDB db;
    private final AtomicLong hitCount = new AtomicLong();
    private final AtomicLong missCount = new AtomicLong();

    public DiskCachingFhirQueryService(FhirQueryService fhirQueryService, Config config, Executor executor,
                                       Clock clock) {
        this.fhirQueryService = requireNonNull(fhirQueryService);
        this.config = requireNonNull(config);
        this.executor = requireNonNull(executor);
        this.clock = requireNonNull(clock);
    }

    @PostConstruct
    public void init() throws RocksDBException {
        options = new Options();
        options.setCreateIfMissing(true);
        options.setCompressionType(LZ4_COMPRESSION);
        options.setBottommostCompressionType(ZSTD_COMPRESSION);
        options.setWriteBufferSize(256 << 20);
        options.setTableFormatConfig(new BlockBasedTableConfig().setBlockSize(16384));
        db = TtlDB.open(options, config.path, (int) config.expireDuration.toSeconds(), false);
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
            return Optional.ofNullable(db.get(serializeQuery(query)))
                    .flatMap(this::deserializePopulation)
                    .filter(not(this::isExpired))
                    .map(p -> {
                        hitCount.incrementAndGet();
                        return p;
                    });
        } catch (RocksDBException e) {
            logger.warn("Skip loading population because of: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<Population> deserializePopulation(byte[] bytes) {
        try {
            return Optional.of(Population.fromByteBuffer(ByteBuffer.wrap(bytes)));
        } catch (SerializerException e) {
            logger.warn("Skip loading population because of: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private boolean isExpired(Population p) {
        return p.created().plus(config.expireDuration).isBefore(clock.instant());
    }

    private void put(Query query, Population population) {
        executor.execute(() -> internalPut(query, population));
    }

    void internalPut(Query query, Population population) {
        try {
            logger.trace("Store result of size {} for query: {}", population.size(), query);
            db.put(new WriteOptions(), serializeQueryBuffer(query), population.toByteBuffer());
            missCount.incrementAndGet();
        } catch (RocksDBException e) {
            logger.warn("Skip caching population because of: {}", e.getMessage());
        }
    }

    private static byte[] serializeQuery(Query query) {
        return query.toString().getBytes(UTF_8);
    }

    private static ByteBuffer serializeQueryBuffer(Query query) {
        byte[] bytes = serializeQuery(query);
        ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length);
        buffer.put(bytes);
        return buffer.flip();
    }

    @PreDestroy
    public void destroy() {
        logger.info("Shutting down the Disk Cache...");
        db.close();
        options.close();
        logger.info("Finished shutting down the Disk Cache.");
    }

    public record Config(String path, Duration expireDuration) {
    }
}
