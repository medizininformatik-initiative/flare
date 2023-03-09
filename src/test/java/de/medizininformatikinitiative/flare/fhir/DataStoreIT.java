package de.medizininformatikinitiative.flare.fhir;

import de.medizininformatikinitiative.flare.model.Query;
import de.medizininformatikinitiative.flare.service.DataStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.PullPolicy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Testcontainers
class DataStoreIT {

    private static final Logger logger = LoggerFactory.getLogger(DataStoreIT.class);

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
        client = WebClient.builder()
                .baseUrl("http://%s/fhir".formatted(host))
                .defaultHeader("Accept", "application/fhir+json")
                .defaultHeader("X-Forwarded-Host", host)
                .build();
        dataStore = new DataStore(client, 1);
    }

    @Test
    void searchType_empty() {
        var result = dataStore.execute(Query.ofType("Observation")).join();

        assertThat(result).isEmpty();
    }

    @Test
    void searchType_oneObservation() {
        createPatient("0");
        createObservation("0");

        var result = dataStore.execute(Query.ofType("Observation")).join();

        assertThat(result).containsExactly("0");
    }

    @Test
    void searchType_twoObservationsFromOnePatient() {
        createPatient("0");
        createObservation("0");
        createObservation("0");

        var result = dataStore.execute(Query.ofType("Observation")).join();

        assertThat(result).containsExactly("0");
    }

    @Test
    void searchType_twoObservationsFromTwoPatients() {
        createPatient("0");
        createPatient("1");
        createObservation("0");
        createObservation("1");

        var result = dataStore.execute(Query.ofType("Observation")).join();

        assertThat(result).containsExactly("0", "1");
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
}
