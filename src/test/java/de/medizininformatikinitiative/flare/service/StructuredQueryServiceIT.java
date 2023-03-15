package de.medizininformatikinitiative.flare.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.medizininformatikinitiative.flare.FlareApplication;
import de.medizininformatikinitiative.flare.model.mapping.Mapping;
import de.medizininformatikinitiative.flare.model.mapping.MappingContext;
import de.medizininformatikinitiative.flare.model.mapping.TermCodeNode;
import de.medizininformatikinitiative.flare.model.sq.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.PullPolicy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Testcontainers
@SpringBootTest
class StructuredQueryServiceIT {

    private static final TermCode I08 = TermCode.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "I08", "");
    private static final TermCode COVID = TermCode.of("http://loinc.org", "94500-6", "");
    private static final TermCode INVALID = TermCode.of("http://loinc.org", "LA15841-2", "Invalid");

    private static final Logger logger = LoggerFactory.getLogger(StructuredQueryServiceIT.class);

    @Container
    @SuppressWarnings("resource")
    private static final GenericContainer<?> blaze = new GenericContainer<>("samply/blaze:0.20")
            .withImagePullPolicy(PullPolicy.alwaysPull())
            .withEnv("LOG_LEVEL", "debug")
            .withExposedPorts(8080)
            .waitingFor(Wait.forHttp("/health").forStatusCode(200))
            .withLogConsumer(new Slf4jLogConsumer(logger));

    @Configuration
    static class Config {

        @Bean
        public WebClient dataStoreClient() {
            var host = "%s:%d".formatted(blaze.getHost(), blaze.getFirstMappedPort());
            return WebClient.builder()
                    .baseUrl("http://%s/fhir".formatted(host))
                    .defaultHeader("Accept", "application/fhir+json")
                    .defaultHeader("X-Forwarded-Host", host)
                    .build();
        }

        @Bean
        public MappingContext mappingContext() throws Exception {
            var mapper = new ObjectMapper();
            var mappings = Arrays.stream(mapper.readValue(new File("ontology/codex-term-code-mapping.json"), Mapping[].class))
                    .collect(Collectors.toMap(Mapping::key, identity()));
            var conceptTree = mapper.readValue(new File("ontology/codex-code-tree.json"), TermCodeNode.class);
            return MappingContext.of(mappings, conceptTree);
        }

        @Bean
        public FhirQueryService fhirQueryService(WebClient dataStoreClient) {
            return new DataStore(dataStoreClient, 1);
        }

        @Bean
        public Translator translator(MappingContext mappingContext) {
            return new Translator(mappingContext);
        }

        @Bean
        public StructuredQueryService service(FhirQueryService fhirQueryService, Translator translator) {
            return new StructuredQueryService(fhirQueryService, translator);
        }
    }

    private static boolean dataImported = false;

    @Autowired
    private WebClient dataStoreClient;

    @Autowired
    private StructuredQueryService service;

    @BeforeEach
    void setUp() {
        if (!dataImported) {
            dataStoreClient.post()
                    .contentType(APPLICATION_JSON)
                    .bodyValue(slurp("GeneratedBundle.json"))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            dataImported = true;
        }
    }

    @Test
    void execute_Criterion() {
        var query = StructuredQuery.of(CriterionGroup.of(CriterionGroup.of(Criterion.of(Concept.of(I08)))));

        var result = service.execute(query).block();

        assertThat(result).isOne();
    }

    @Test
    void execute_Criterion_WithValueFilter() {
        var query = StructuredQuery.of(CriterionGroup.of(CriterionGroup.of(Criterion.of(Concept.of(COVID), ValueFilter.ofConcept(INVALID)))));

        var result = service.execute(query).block();

        assertThat(result).isOne();
    }

    private static String slurp(String name) {
        try {
            return Files.readString(resourcePath(name));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static Path resourcePath(String name) throws URISyntaxException {
        return Paths.get(Objects.requireNonNull(FlareApplication.class.getResource(name)).toURI());
    }
}
