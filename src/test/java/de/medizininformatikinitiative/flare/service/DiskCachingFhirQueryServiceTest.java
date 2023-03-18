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
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.nio.file.Files;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
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
    static final String PATIENT_ID_1 = "patient-id-1-103010";
    static final String PATIENT_ID_2 = "patient-id-2-103014";
    static final String ERROR_MSG = "error-msg-102646";

    @Mock
    private FhirQueryService queryService;

    private final Scheduler scheduler = Schedulers.newParallel("foo", 2);

    private DiskCachingFhirQueryService service;

    @BeforeEach
    void setUp() throws Exception {
        var path = Files.createTempDirectory("tmpDirPrefix").toFile().getAbsolutePath();
        service = new DiskCachingFhirQueryService(queryService, new DiskCachingFhirQueryService.Config(path,
                Duration.ofMinutes(1)), scheduler, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
        service.init();
    }

    @AfterEach
    void tearDown() {
        service.destroy();
    }

    @Test
    void execute_error() {
        when(queryService.execute(QUERY, false)).thenReturn(Mono.error(new Exception(ERROR_MSG)));

        var result = service.execute(QUERY);

        StepVerifier.create(result).verifyErrorMessage(ERROR_MSG);
        waitForTasksToFinish();
        assertThat(service.stats()).isEqualTo(new CachingService.CacheStats(0, 0, 0));
    }

    @Test
    void execute_miss() {
        when(queryService.execute(QUERY, false)).thenReturn(Mono.just(Population.of(PATIENT_ID)));

        var result = service.execute(QUERY);

        StepVerifier.create(result).expectNext(Population.of(PATIENT_ID)).verifyComplete();
        waitForTasksToFinish();
        assertThat(service.stats()).isEqualTo(new CachingService.CacheStats(0, 0, 1));
    }

    @Test
    void execute_hit() {
        ensureCacheContains(QUERY, Population.of(PATIENT_ID));

        var result = service.execute(QUERY);

        StepVerifier.create(result).expectNext(Population.of(PATIENT_ID)).verifyComplete();
        waitForTasksToFinish();
        assertThat(service.stats()).isEqualTo(new CachingService.CacheStats(0, 1, 1));
    }

    @Test
    void execute_ignoringCache() {
        ensureCacheContains(QUERY, Population.of(PATIENT_ID_1));
        when(queryService.execute(QUERY, true)).thenReturn(Mono.just(Population.of(PATIENT_ID_2)));

        var result = service.execute(QUERY, true);

        StepVerifier.create(result).expectNext(Population.of(PATIENT_ID_2)).verifyComplete();
        waitForTasksToFinish();
        assertThat(service.stats()).isEqualTo(new CachingService.CacheStats(0, 0, 2));
    }

    @Test
    @DisplayName("expired populations will not be returned as hit")
    void execute_expiredHit() {
        ensureCacheContains(QUERY, Population.of(PATIENT_ID_1).withCreated(Instant.EPOCH.minus(2, MINUTES)));
        when(queryService.execute(QUERY, false)).thenReturn(Mono.just(Population.of(PATIENT_ID_2)));

        var result = service.execute(QUERY, false);

        StepVerifier.create(result).expectNext(Population.of(PATIENT_ID_2)).verifyComplete();
        waitForTasksToFinish();
        assertThat(service.stats()).isEqualTo(new CachingService.CacheStats(0, 0, 2));
    }

    @ParameterizedTest
    @ValueSource(ints = {1_000, 10_000, 100_000, 1_000_000})
    void execute_hit_largePopulation(int n) {
        var population = populationOfSize(n);
        ensureCacheContains(QUERY, population);

        var result = service.execute(QUERY);

        StepVerifier.create(result).expectNext(population).verifyComplete();
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

    private void waitForTasksToFinish() {
        StepVerifier.create(scheduler.disposeGracefully()).verifyComplete();
    }
}
