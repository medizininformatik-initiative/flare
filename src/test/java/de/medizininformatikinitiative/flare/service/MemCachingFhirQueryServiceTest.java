package de.medizininformatikinitiative.flare.service;

import de.medizininformatikinitiative.flare.model.Population;
import de.medizininformatikinitiative.flare.model.fhir.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemCachingFhirQueryServiceTest {

    static final Query QUERY = Query.ofType("foo");
    static final String PATIENT_ID = "patient-id-113003";
    static final String ERROR_MSG = "error-msg-103632";

    @Mock
    private FhirQueryService queryService;

    private MemCachingFhirQueryService service;

    @BeforeEach
    void setUp() {
        service = new MemCachingFhirQueryService(queryService, new MemCachingFhirQueryService.Config(128,
                Duration.ofMinutes(1), Duration.ofMinutes(1)));
        service.init();
    }

    @Test
    void execute_error() {
        when(queryService.execute(QUERY)).thenReturn(Mono.error(new Exception(ERROR_MSG)));

        var result = service.execute(QUERY);

        StepVerifier.create(result).verifyErrorMessage(ERROR_MSG);
    }

    @Test
    void execute_success() {
        when(queryService.execute(QUERY)).thenReturn(Mono.just(Population.of(PATIENT_ID)));

        var result = service.execute(QUERY);

        StepVerifier.create(result).expectNext(Population.of(PATIENT_ID)).verifyComplete();
    }

    @Test
    void refresh() throws InterruptedException {
        service = new MemCachingFhirQueryService(queryService, new MemCachingFhirQueryService.Config(128,
                Duration.ofMinutes(1), Duration.ofMillis(100)));
        service.init();
        when(queryService.execute(QUERY)).thenReturn(Mono.just(Population.of()));
        service.execute(QUERY);
        when(queryService.execute(QUERY, true)).thenReturn(Mono.just(Population.of(PATIENT_ID)));
        Thread.sleep(200);

        var result = service.execute(QUERY);

        StepVerifier.create(result).expectNext(Population.of(PATIENT_ID)).verifyComplete();
    }
}
