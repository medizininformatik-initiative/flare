package de.medizininformatikinitiative.flare.service;

import de.medizininformatikinitiative.flare.model.Population;
import de.medizininformatikinitiative.flare.model.fhir.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemCachingFhirQueryServiceTest {

    static final Query QUERY = Query.ofType("foo");
    public static final String PATIENT_ID = "patient-id-113003";

    @Mock
    private FhirQueryService queryService;

    private MemCachingFhirQueryService service;

    @BeforeEach
    void setUp() {
        service = new MemCachingFhirQueryService(queryService, new MemCachingFhirQueryService.Config(1024,
                Duration.ofMinutes(1), Duration.ofMinutes(1)));
        service.init();
    }

    @Test
    void execute_error() {
        when(queryService.execute(QUERY)).thenReturn(CompletableFuture.failedFuture(new Exception()));

        var result = service.execute(QUERY);

        assertThat(result).isCompletedExceptionally();
    }

    @Test
    void execute_success() {
        when(queryService.execute(QUERY)).thenReturn(CompletableFuture.completedFuture(Population.of()));

        var result = service.execute(QUERY);

        assertThat(result).isCompletedWithValue(Population.of());
    }

    @Test
    void refresh() throws InterruptedException {
        service = new MemCachingFhirQueryService(queryService, new MemCachingFhirQueryService.Config(1024,
                Duration.ofMinutes(1), Duration.ofMillis(100)));
        service.init();
        when(queryService.execute(QUERY)).thenReturn(CompletableFuture.completedFuture(Population.of()));
        service.execute(QUERY);
        when(queryService.execute(QUERY, true)).thenReturn(CompletableFuture.completedFuture(Population.of(PATIENT_ID)));
        Thread.sleep(200);

        var result = service.execute(QUERY);

        assertThat(result).isCompletedWithValue(Population.of(PATIENT_ID));
    }
}
