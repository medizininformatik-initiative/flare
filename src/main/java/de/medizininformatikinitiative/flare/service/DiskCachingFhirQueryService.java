package de.medizininformatikinitiative.flare.service;

import de.medizininformatikinitiative.flare.model.Population;
import de.medizininformatikinitiative.flare.model.fhir.Query;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.rocksdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.nio.ByteBuffer;
import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static org.rocksdb.CompressionType.LZ4_COMPRESSION;
import static org.rocksdb.CompressionType.ZSTD_COMPRESSION;

public class DiskCachingFhirQueryService implements CachingService, FhirQueryService {

    private static final Logger logger = LoggerFactory.getLogger(DiskCachingFhirQueryService.class);

    private final FhirQueryService fhirQueryService;
    private final Config config;
    private final Scheduler scheduler;
    private final Clock clock;
    private Options options;
    private RocksDB db;
    private final AtomicLong hitCount = new AtomicLong();
    private final AtomicLong missCount = new AtomicLong();

    public DiskCachingFhirQueryService(FhirQueryService fhirQueryService, Config config, Scheduler scheduler,
                                       Clock clock) {
        this.fhirQueryService = requireNonNull(fhirQueryService);
        this.config = requireNonNull(config);
        this.scheduler = requireNonNull(scheduler);
        this.clock = requireNonNull(clock);
    }

    @PostConstruct
    public void init() throws RocksDBException {
        logger.info("Starting DiskCachingFhirQueryService with: {}", config);
        options = new Options();
        options.setCreateIfMissing(true);
        options.setCompressionType(LZ4_COMPRESSION);
        options.setBottommostCompressionType(ZSTD_COMPRESSION);
        options.setWriteBufferSize(256 << 20);
        options.setTableFormatConfig(new BlockBasedTableConfig().setBlockSize(16384));
        db = TtlDB.open(options, config.path, (int) config.expire.toSeconds(), false);
    }

    @Override
    public Mono<Population> execute(Query query, boolean ignoreCache) {
        if (ignoreCache) {
            return executeQuery(query, true);
        } else {
            return internalGet(query).switchIfEmpty(Mono.defer(() -> executeQuery(query, false)));
        }
    }

    private Mono<Population> executeQuery(Query query, boolean ignoreCache) {
        return fhirQueryService.execute(query, ignoreCache).doOnNext(population -> put(query, population));
    }

    private Mono<Population> internalGet(Query query) {
        return Mono.fromSupplier(() -> internalBlockingGet(query)).publishOn(scheduler);
    }

    private Population internalBlockingGet(Query query) {
        logger.trace("Try loading population for query `{}` from disk.", query);
        byte[] value = internalGetValue(serializeQuery(query));
        if (value == null) {
            return null;
        }
        var population = deserializePopulation(value);
        if (population == null || isExpired(population)) {
            return null;
        }
        hitCount.incrementAndGet();
        return population;
    }

    private byte[] internalGetValue(byte[] key) {
        try {
            return db.get(key);
        } catch (RocksDBException e) {
            logger.warn("Skip loading population because of: {}", e.getMessage());
            return null;
        }
    }

    private static Population deserializePopulation(byte[] bytes) {
        try {
            return Population.fromByteBuffer(ByteBuffer.wrap(bytes));
        } catch (SerializerException e) {
            logger.warn("Skip loading population because of: {}", e.getMessage());
            return null;
        }
    }

    private boolean isExpired(Population p) {
        return p.created().plus(config.expire).isBefore(clock.instant());
    }

    @Override
    public CacheStats stats() {
        return new CacheStats(0, 0, 0, hitCount.get(), missCount.get(), 0);
    }

    private void put(Query query, Population population) {
        scheduler.schedule(() -> internalPut(query, population));
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

    public record Config(String path, Duration expire) {
    }
}
