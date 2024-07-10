package de.medizininformatikinitiative.flare.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.medizininformatikinitiative.flare.FlareApplication;
import de.medizininformatikinitiative.flare.Util;
import de.medizininformatikinitiative.flare.model.Population;
import de.medizininformatikinitiative.flare.model.mapping.Mapping;
import de.medizininformatikinitiative.flare.model.mapping.MappingContext;
import de.medizininformatikinitiative.flare.model.mapping.TermCodeNode;
import de.medizininformatikinitiative.flare.model.sq.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.PullPolicy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Testcontainers
@SpringBootTest
class StructuredQueryServiceIT {

    private static final Clock CLOCK_2000 = Clock.fixed(LocalDate.of(2000, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
    private static final TermCode I08 = TermCode.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "I08", "");
    private static final TermCode COVID = TermCode.of("http://loinc.org", "94500-6", "");
    private static final TermCode INVALID = TermCode.of("http://loinc.org", "LA15841-2", "Invalid");
    private static final TermCode DIAGNOSIS = TermCode.of("fdpg.mii.cds", "Diagnose", "Diagnose");
    private static final TermCode OBSERVATION = TermCode.of("fdpg.mii.cds", "Laboruntersuchung", "Laboruntersuchung");

    private static final Logger logger = LoggerFactory.getLogger(StructuredQueryServiceIT.class);

    @Container
    @SuppressWarnings("resource")
    private static final GenericContainer<?> blaze = new GenericContainer<>("samply/blaze:0.28")
            .withImagePullPolicy(PullPolicy.alwaysPull())
            .withEnv("LOG_LEVEL", "debug")
            .withEnv("DB_SEARCH_PARAM_BUNDLE", "/app/custom-search-parameters.json")
            .withClasspathResourceMapping("de/medizininformatikinitiative/flare/service/referencedCriteria/custom-search-parameters.json", "/app/custom-search-parameters.json", BindMode.READ_ONLY)
            .withExposedPorts(8080)
            .waitingFor(Wait.forHttp("/health").forStatusCode(200))
            .withLogConsumer(new Slf4jLogConsumer(logger));

    private static boolean dataImported = false;

    @Autowired
    private WebClient dataStoreClient;

    @Autowired
    private StructuredQueryService service;

    @Autowired
    private StructuredQueryService service_BloodPressure;

    private static String slurp_FlareApplication(String name) throws URISyntaxException, IOException {
        return Files.readString(resourcePathFlareApplication(name));
    }

    private static String slurpStructuredQueryService(String name) throws URISyntaxException, IOException {
        return Files.readString(resourcePathStructuredQueryService(name));
    }

    private static Path resourcePathStructuredQueryService(String name) throws URISyntaxException {
        return Paths.get(Objects.requireNonNull(StructuredQueryServiceIT.class.getResource(name)).toURI());
    }

    private static Path resourcePathFlareApplication(String name) throws URISyntaxException {
        return Paths.get(Objects.requireNonNull(FlareApplication.class.getResource(name)).toURI());
    }

    private int getExecutionResult(StructuredQuery query){
        return service.execute(query).block().size();
    }

    public static List<StructuredQuery> getTestQueriesReturningOnePatient() throws URISyntaxException, IOException {
        Path directoryPath = Paths.get(resourcePathFlareApplication("testCases").resolve("returningOnePatient").toString());

        try (Stream<Path> paths = Files.list(directoryPath)) {
            return paths.map(path -> {
                try {
                    return new ObjectMapper().readValue(Files.readString(path), StructuredQuery.class);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).toList();
        }
    }

    static StructuredQuery parseSq(String s) throws JsonProcessingException {
        return new ObjectMapper().readValue(s, StructuredQuery.class);
    }

    @BeforeEach
    void setUp() throws URISyntaxException, IOException {
        if (!dataImported) {
            dataStoreClient.post()
                    .contentType(APPLICATION_JSON)
                    .bodyValue(slurp_FlareApplication("GeneratedBundle.json"))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            dataImported = true;
        }
    }

    @Test
    void execute_Criterion() {
        var query = StructuredQuery.of(CriterionGroup.of(CriterionGroup.of(Criterion.of(ContextualConcept.of(DIAGNOSIS, Concept.of(I08))))));

        var result = service.execute(query);
        StepVerifier.create(result).expectNext(Population.of("id-pat-diag-I08.0")).verifyComplete();
    }

    @Test
    void execute_genderTestCase() throws URISyntaxException, IOException {
        var query = parseSq(Files.readString(resourcePathFlareApplication("testCases").resolve("returningOther").resolve("2-gender.json")));
        var result = getExecutionResult(query);

        assertThat(result).isEqualTo(172);
    }

    @Test
    void execute_consentTestCase() throws URISyntaxException, IOException {
        var query = parseSq(Files.readString(resourcePathFlareApplication("testCases").resolve("returningOther").resolve("consent.json")));

        var result = service.execute(query);

        StepVerifier.create(result).expectNext(Population.of("id-pat-consent-test")).verifyComplete();
    }

    @Test
    void execute_specimenTestCase() throws IOException, URISyntaxException {
        dataStoreClient.post()
                .contentType(APPLICATION_JSON)
                .bodyValue(slurpStructuredQueryService("referencedCriteria/specimen-diag-testbundle.json"))
                .retrieve()
                .toBodilessEntity()
                .block();

        var query = parseSq(slurpStructuredQueryService("referencedCriteria/sq-test-specimen-diag.json"));

        var result = service.execute(query);

        StepVerifier.create(result).expectNext(Population.of("id-pat-diab-test-1")).verifyComplete();

    }

    @ParameterizedTest
    @MethodSource("getTestQueriesReturningOnePatient")
    void execute_casesReturningOne(StructuredQuery query) {
        var result = getExecutionResult(query);

        assertThat(result).isOne();
    }

    @Test
    void execute_BloodPressureTestCase() throws Exception {
        var query = parseSq("""
                {
                  "version": "https://medizininformatik-initiative.de/fdpg/StructuredQuery/v3/schema",
                  "inclusionCriteria": [
                    [
                      {
                        "context": {
                              "code": "Laboruntersuchung",
                              "display": "Laboruntersuchung",
                              "system": "fdpg.mii.cds",
                              "version": "1.0.0"
                         },
                        "termCodes": [
                          {
                            "code": "85354-9",
                            "system": "http://loinc.org",
                            "display": "Blutdruck"
                          }
                        ],
                        "attributeFilters": [
                          {
                            "type": "quantity-comparator",
                            "unit": {
                              "code": "mm[Hg]",
                              "display": "mm[Hg]"
                            },
                            "value": 1,
                            "comparator": "gt",
                            "attributeCode": {
                              "code": "8480-6",
                              "system": "http://loinc.org",
                              "display": "Sistolic Bloodpressure"
                            }
                          }
                        ]
                      }
                    ]
                  ]
                }
                """);

        var result = service_BloodPressure.execute(query);

        StepVerifier.create(result).expectNext(Population.of("id-pat-bloodpressure-test")).verifyComplete();

    }

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
            return Util.flareMappingContext(CLOCK_2000);
        }

        @Bean
        public MappingContext mappingContext_BloodPressure() throws Exception {
            var mapper = new ObjectMapper();
            var mappings = Arrays.stream(mapper.readValue(slurpStructuredQueryService("compositeSearchParams/mapping-bloodPressure.json"), Mapping[].class))
                    .collect(Collectors.toMap(Mapping::key, identity()));
            var conceptTree = mapper.readValue(slurpStructuredQueryService("compositeSearchParams/tree-bloodPressure.json"), TermCodeNode.class);
            return MappingContext.of(mappings, conceptTree, CLOCK_2000);
        }

        @Bean
        public FhirQueryService fhirQueryService(WebClient dataStoreClient) {
            return new DataStore(dataStoreClient, Clock.systemDefaultZone(), 1);
        }

        @Bean
        public Translator translator(MappingContext mappingContext) {
            return new Translator(mappingContext);
        }

        @Bean
        public Translator translator_BloodPressure(MappingContext mappingContext_BloodPressure) {
            return new Translator(mappingContext_BloodPressure);
        }

        @Bean
        public StructuredQueryService service(FhirQueryService fhirQueryService, Translator translator) {
            return new StructuredQueryService(fhirQueryService, translator);
        }

        @Bean
        public StructuredQueryService service_BloodPressure(FhirQueryService fhirQueryService, Translator translator_BloodPressure) {
            return new StructuredQueryService(fhirQueryService, translator_BloodPressure);
        }
    }
}
