package de.medizininformatikinitiative.flare.service;

import de.medizininformatikinitiative.flare.model.Population;
import de.medizininformatikinitiative.flare.model.fhir.Query;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

class DataStoreTest {

    private static final Instant FIXED_INSTANT = Instant.ofEpochSecond(104152);
    private static MockWebServer mockStore;

    private DataStore dataStore;

    @BeforeAll
    static void setUp() throws IOException {
        mockStore = new MockWebServer();
        mockStore.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockStore.shutdown();
    }

    @BeforeEach
    void initialize() {
        WebClient client = WebClient.builder()
                .baseUrl("http://localhost:%d/fhir".formatted(mockStore.getPort()))
                .defaultHeader("Accept", "application/fhir+json")
                .build();
        dataStore = new DataStore(client, Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC), 1000);
    }

    @ParameterizedTest
    @DisplayName("retires the request")
    @ValueSource(ints = {404, 500, 503, 504})
    void execute_retry(int statusCode) {
        mockStore.enqueue(new MockResponse().setResponseCode(statusCode));
        mockStore.enqueue(new MockResponse().setResponseCode(200));

        var result = dataStore.execute(Query.ofType("Observation"));

        StepVerifier.create(result).expectNext(Population.of().withCreated(FIXED_INSTANT)).verifyComplete();
    }

    @Test
    @DisplayName("fails after 3 unsuccessful retires")
    void execute_retry_fails() {
        mockStore.enqueue(new MockResponse().setResponseCode(500));
        mockStore.enqueue(new MockResponse().setResponseCode(500));
        mockStore.enqueue(new MockResponse().setResponseCode(500));
        mockStore.enqueue(new MockResponse().setResponseCode(500));
        mockStore.enqueue(new MockResponse().setResponseCode(200));

        var result = dataStore.execute(Query.ofType("Observation"));

        StepVerifier.create(result).verifyErrorMessage("Retries exhausted: 3/3");
    }

    @Test
    @DisplayName("doesn't retry a 400")
    void execute_retry_400() {
        mockStore.enqueue(new MockResponse().setResponseCode(400));

        var result = dataStore.execute(Query.ofType("Observation"));

        StepVerifier.create(result).verifyError(WebClientResponseException.BadRequest.class);
    }
}
