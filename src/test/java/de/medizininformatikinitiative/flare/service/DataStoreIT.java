package de.medizininformatikinitiative.flare.service;

import de.medizininformatikinitiative.flare.model.Population;
import de.medizininformatikinitiative.flare.model.fhir.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.PullPolicy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.test.StepVerifier;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Testcontainers
class DataStoreIT {

    private static final Logger logger = LoggerFactory.getLogger(DataStoreIT.class);

    static final Instant FIXED_INSTANT = Instant.ofEpochSecond(104152);

    @Container
    @SuppressWarnings("resource")
    private final GenericContainer<?> blaze = new GenericContainer<>("samply/blaze:0.20")
            .withImagePullPolicy(PullPolicy.alwaysPull())
            .withEnv("LOG_LEVEL", "debug")
            .withExposedPorts(8080)
            .waitingFor(Wait.forHttp("/health").forStatusCode(200))
            .withLogConsumer(new Slf4jLogConsumer(logger));

    private WebClient client;
    private DataStore dataStore;

    @SuppressWarnings("HttpUrlsUsage")
    @BeforeEach
    void setUp() {
        var host = "%s:%d".formatted(blaze.getHost(), blaze.getFirstMappedPort());
        ConnectionProvider provider = ConnectionProvider.builder("custom")
                .maxConnections(4)
                .build();
        HttpClient httpClient = HttpClient.create(provider);
        client = WebClient.builder()
                .baseUrl("http://%s/fhir".formatted(host))
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader("Accept", "application/fhir+json")
                .defaultHeader("X-Forwarded-Host", host)
                .build();
        dataStore = new DataStore(client, Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC), 1000);
    }

    @Test
    void execute_empty() {
        var result = dataStore.execute(Query.ofType("Observation"));

        StepVerifier.create(result).expectNext(Population.of().withCreated(FIXED_INSTANT)).verifyComplete();
    }

    @Test
    void execute_oneObservation() {
        createPatient("0");
        createObservation("0");

        var result = dataStore.execute(Query.ofType("Observation"));

        StepVerifier.create(result).expectNext(Population.of("0").withCreated(FIXED_INSTANT)).verifyComplete();
    }

    @Test
    void execute_twoObservationsFromOnePatient() {
        createPatient("0");
        createObservation("0");
        createObservation("0");

        var result = dataStore.execute(Query.ofType("Observation"));

        StepVerifier.create(result).expectNext(Population.of("0").withCreated(FIXED_INSTANT)).verifyComplete();
    }

    @Test
    void execute_twoObservationsFromTwoPatients() {
        createPatient("0");
        createPatient("1");
        createObservation("0");
        createObservation("1");

        var result = dataStore.execute(Query.ofType("Observation"));

        StepVerifier.create(result).expectNext(Population.of("0", "1").withCreated(FIXED_INSTANT)).verifyComplete();
    }

    @Test
    @DisplayName("A single Resource without a Reference to a Patient is invalid and should not be counted")
    void execute_OneObsWithoutReference_FromOnePat(){
        createObservation_withoutReference();

        var result = dataStore.execute(Query.ofType("Observation"));

        StepVerifier.create(result).expectNext(Population.of().withCreated(FIXED_INSTANT)).verifyComplete();
    }

    @Test
    @DisplayName("There is one Resource with a valid and one with an invalid Reference. Only the one with the valid" +
                " Reference should be counted")
    void execute_OneObsWithoutReferenceOneObsWithReference_FromTwoPats(){
        createPatient("1");
        createObservation_withoutReference();
        createObservation("1");

        var result = dataStore.execute(Query.ofType("Observation"));

        StepVerifier.create(result).expectNext(Population.of("1").withCreated(FIXED_INSTANT)).verifyComplete();
    }

    @Test
    @DisplayName("1000 concurrent requests will fill up the pending acquire queue because of constraint max connections")
    void pendingAcquireQueueReachedMaximum() {
        createPatient("0");

        var result = Flux.range(1, 1000).flatMap(i -> dataStore.execute(Query.ofType("Patient"))).collectList();

        StepVerifier.create(result).verifyErrorMessage("Pending acquire queue has reached its maximum size of 8");
    }

    private void createPatient(String id) {
        client.put()
                .uri("/Patient/{id}", id)
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        { "resourceType": "Patient",
                          "id": "%s"
                        }
                        """.formatted(id))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    private void createObservation(String patientId) {
        client.post()
                .uri("/Observation")
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        { "resourceType": "Observation",
                          "subject": { "reference": "Patient/%s" }
                        }
                        """.formatted(patientId))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    private void createObservation_withoutReference() {
        client.post()
                .uri("/Observation")
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        { "resourceType": "Observation",
                          "subject": {}
                        }
                        """)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
