package de.medizininformatikinitiative.flare.service;

import de.medizininformatikinitiative.flare.model.Population;
import de.medizininformatikinitiative.flare.model.fhir.Query;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.com.google.common.hash.Hashing;

import java.nio.file.Files;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiskCachingFhirQueryServiceTest {

    static final Query QUERY = Query.ofType("foo");
    static final String PATIENT_ID = "patient-id-113617";

    @Mock
    private FhirQueryService queryService;

    private DiskCachingFhirQueryService service;

    @BeforeEach
    void setUp() throws Exception {
        var path = Files.createTempDirectory("tmpDirPrefix").toFile().getAbsolutePath();
        service = new DiskCachingFhirQueryService(queryService, new DiskCachingFhirQueryService.Config(path,
                Duration.ofMinutes(1)), Executors.newFixedThreadPool(1));
        service.init();
    }

    @AfterEach
    void tearDown() {
        service.destroy();
    }

    @Test
    void execute_error() {
        when(queryService.execute(QUERY, false)).thenReturn(CompletableFuture.failedFuture(new Exception()));

        var result = service.execute(QUERY);

        assertThat(result).isCompletedExceptionally();
        assertThat(service.stats()).isEqualTo(new CachingService.CacheStats(0, 0, 0));
    }

    @Test
    void execute_miss() throws Exception {
        when(queryService.execute(QUERY, false)).thenReturn(CompletableFuture.completedFuture(Population.of()));

        var result = service.execute(QUERY);

        Thread.sleep(100);
        assertThat(result).isCompletedWithValue(Population.of());
        assertThat(service.stats()).isEqualTo(new CachingService.CacheStats(0, 0, 1));
    }

    @Test
    void execute_hit() throws Exception {
        ensureCacheContains(QUERY, Population.of());

        var result = service.execute(QUERY);

        Thread.sleep(100);
        assertThat(result).isCompletedWithValue(Population.of());
        assertThat(service.stats()).isEqualTo(new CachingService.CacheStats(0, 1, 1));
    }

    @Test
    void execute_ignoringCache() throws Exception {
        ensureCacheContains(QUERY, Population.of());
        when(queryService.execute(QUERY, true)).thenReturn(CompletableFuture.completedFuture(Population.of(PATIENT_ID)));

        var result = service.execute(QUERY, true);

        Thread.sleep(100);
        assertThat(result).isCompletedWithValue(Population.of(PATIENT_ID));
        assertThat(service.stats()).isEqualTo(new CachingService.CacheStats(0, 0, 2));
    }

    @ParameterizedTest
    @ValueSource(ints = {1_000, 10_000, 100_000, 1_000_000})
    void execute_hit_largePopulation(int n) throws InterruptedException {
        var population = populationOfSize(n);
        ensureCacheContains(QUERY, population);

        var result = service.execute(QUERY);

        Thread.sleep(100);
        assertThat(result).isCompletedWithValue(population);
        assertThat(service.stats()).isEqualTo(new CachingService.CacheStats(0, 1, 1));
    }

    private void ensureCacheContains(Query query, Population population) throws InterruptedException {
        when(queryService.execute(query, false)).thenReturn(CompletableFuture.completedFuture(population));
        service.execute(query);
        Thread.sleep(1000);
    }

    private static Population populationOfSize(int n) {
        return Population.copyOf(IntStream.range(0, n)
                .mapToObj("patient-id-%d"::formatted)
                .map(s -> Hashing.sha256().newHasher().putString(s, UTF_8).hash().toString())
                .collect(Collectors.toSet()));
    }
}
