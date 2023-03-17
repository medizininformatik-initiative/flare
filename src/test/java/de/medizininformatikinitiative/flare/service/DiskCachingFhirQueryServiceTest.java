package de.medizininformatikinitiative.flare.service;

import de.medizininformatikinitiative.flare.model.Population;
import de.medizininformatikinitiative.flare.model.fhir.Query;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.com.google.common.hash.Hashing;

import java.nio.file.Files;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.temporal.ChronoUnit.MINUTES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiskCachingFhirQueryServiceTest {

    static final Query QUERY = Query.ofType("foo");
    static final String PATIENT_ID = "patient-id-113617";
    public static final Duration READ_TIMEOUT = Duration.ofSeconds(1);

    @Mock
    private FhirQueryService queryService;

    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    private DiskCachingFhirQueryService service;

    @BeforeEach
    void setUp() throws Exception {
        var path = Files.createTempDirectory("tmpDirPrefix").toFile().getAbsolutePath();
        service = new DiskCachingFhirQueryService(queryService, new DiskCachingFhirQueryService.Config(path,
                Duration.ofMinutes(1)), executor, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
        service.init();
    }

    @AfterEach
    void tearDown() {
        service.destroy();
    }

    @Test
    void execute_error() throws InterruptedException {
        when(queryService.execute(QUERY, false)).thenReturn(CompletableFuture.failedFuture(new Exception()));

        var result = service.execute(QUERY);

        assertThat(result).failsWithin(READ_TIMEOUT);
        waitForTasksToFinish();
        assertThat(service.stats()).isEqualTo(new CachingService.CacheStats(0, 0, 0));
    }

    @Test
    void execute_miss() throws InterruptedException {
        when(queryService.execute(QUERY, false)).thenReturn(CompletableFuture.completedFuture(Population.of()));

        var result = service.execute(QUERY);

        assertThat(result).succeedsWithin(READ_TIMEOUT).isEqualTo(Population.of());
        waitForTasksToFinish();
        assertThat(service.stats()).isEqualTo(new CachingService.CacheStats(0, 0, 1));
    }

    @Test
    void execute_hit() throws InterruptedException {
        ensureCacheContains(QUERY, Population.of());

        var result = service.execute(QUERY);

        assertThat(result).succeedsWithin(READ_TIMEOUT).isEqualTo(Population.of());
        waitForTasksToFinish();
        assertThat(service.stats()).isEqualTo(new CachingService.CacheStats(0, 1, 1));
    }

    @Test
    void execute_ignoringCache() throws InterruptedException {
        ensureCacheContains(QUERY, Population.of());
        when(queryService.execute(QUERY, true)).thenReturn(CompletableFuture.completedFuture(Population.of(PATIENT_ID)));

        var result = service.execute(QUERY, true);

        assertThat(result).succeedsWithin(READ_TIMEOUT).isEqualTo(Population.of(PATIENT_ID));
        waitForTasksToFinish();
        assertThat(service.stats()).isEqualTo(new CachingService.CacheStats(0, 0, 2));
    }

    @Test
    @DisplayName("expired populations will not be returned as hit")
    void execute_expiredHit() throws InterruptedException {
        ensureCacheContains(QUERY, Population.of().withCreated(Instant.EPOCH.minus(2, MINUTES)));
        when(queryService.execute(QUERY, false)).thenReturn(CompletableFuture.completedFuture(Population.of(PATIENT_ID)));

        var result = service.execute(QUERY, false);

        assertThat(result).succeedsWithin(READ_TIMEOUT).isEqualTo(Population.of(PATIENT_ID));
        waitForTasksToFinish();
        assertThat(service.stats()).isEqualTo(new CachingService.CacheStats(0, 0, 2));
    }

    @ParameterizedTest
    @ValueSource(ints = {1_000, 10_000, 100_000, 1_000_000})
    void execute_hit_largePopulation(int n) throws InterruptedException {
        var population = populationOfSize(n);
        ensureCacheContains(QUERY, population);

        var result = service.execute(QUERY);

        assertThat(result).succeedsWithin(READ_TIMEOUT).isEqualTo(population);
        waitForTasksToFinish();
        assertThat(service.stats()).isEqualTo(new CachingService.CacheStats(0, 1, 1));
    }

    private void ensureCacheContains(Query query, Population population) {
        service.internalPut(query, population);
    }

    private static Population populationOfSize(int n) {
        return Population.copyOf(IntStream.range(0, n)
                .mapToObj("patient-id-%d"::formatted)
                .map(s -> Hashing.sha256().newHasher().putString(s, UTF_8).hash().toString())
                .collect(Collectors.toSet()));
    }

    private void waitForTasksToFinish() throws InterruptedException {
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);
    }
}
