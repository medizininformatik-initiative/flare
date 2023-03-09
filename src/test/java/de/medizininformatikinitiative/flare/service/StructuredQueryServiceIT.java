package de.medizininformatikinitiative.flare.service;

import de.medizininformatikinitiative.flare.model.sq.ConceptCriterion;
import de.medizininformatikinitiative.flare.model.sq.StructuredQuery;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.structured_query.Concept;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.PullPolicy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
class StructuredQueryServiceIT {

    private static final TermCode C71 = TermCode.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "C71",
            "Malignant neoplasm of brain");

    private static final Logger logger = LoggerFactory.getLogger(StructuredQueryServiceIT.class);

    @Container
    @SuppressWarnings("resource")
    private static final GenericContainer<?> blaze = new GenericContainer<>("samply/blaze:0.20")
            .withImagePullPolicy(PullPolicy.alwaysPull())
            .withEnv("LOG_LEVEL", "debug")
            .withExposedPorts(8080)
            .waitingFor(Wait.forHttp("/health").forStatusCode(200))
            .withLogConsumer(new Slf4jLogConsumer(logger));

    @DynamicPropertySource
    static void registerBlazeProperties(DynamicPropertyRegistry registry) {
        registry.add("app.dataStore.baseUrl", () -> "http://%s:%d/fhir".formatted(blaze.getHost(), blaze.getFirstMappedPort()));
    }

    @Autowired
    private StructuredQueryService service;

    @Test
    void execute() {
        var query = StructuredQuery.of(List.of(List.of(ConceptCriterion.of(Concept.of(C71)))));

        var result = service.execute(query).block();

        assertThat(result).isZero();
    }
}
