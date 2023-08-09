package de.medizininformatikinitiative.flare.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.medizininformatikinitiative.flare.FlareApplication;
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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

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

    private static final Logger logger = LoggerFactory.getLogger(StructuredQueryServiceIT.class);

    @Container
    @SuppressWarnings("resource")
    private static final GenericContainer<?> blaze = new GenericContainer<>("samply/blaze:0.22")
            .withImagePullPolicy(PullPolicy.alwaysPull())
            .withEnv("LOG_LEVEL", "debug")
            .withEnv("DB_SEARCH_PARAM_BUNDLE", "/app/custom-search-parameters.json")
            .withClasspathResourceMapping("de/medizininformatikinitiative/flare/service/custom-search-parameters.json", "/app/custom-search-parameters.json", BindMode.READ_ONLY)
            .withExposedPorts(8080)
            .waitingFor(Wait.forHttp("/health").forStatusCode(200))
            .withLogConsumer(new Slf4jLogConsumer(logger));

    private static boolean dataImported = false;

    @Autowired
    private WebClient dataStoreClient;

    @Autowired
    private StructuredQueryService service;
    @Autowired
    private StructuredQueryService service_Specimen;

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
            return MappingContext.of(mappings, conceptTree, CLOCK_2000);
        }

        @Bean
        public MappingContext mappingContext_Specimen() throws Exception {
            var mapper = new ObjectMapper();
            var mappings = Arrays.stream(mapper.readValue(slurp_ClassPath("mapping-specimen-test.json"), Mapping[].class))
                    .collect(Collectors.toMap(Mapping::key, identity()));
            var conceptTree = mapper.readValue(slurp_ClassPath("tree-specimen-test.json"), TermCodeNode.class);
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
        public Translator translator_Specimen(MappingContext mappingContext_Specimen) {
            return new Translator(mappingContext_Specimen);
        }

        @Bean
        public StructuredQueryService service(FhirQueryService fhirQueryService, Translator translator) {
            return new StructuredQueryService(fhirQueryService, translator);
        }

        @Bean
        public StructuredQueryService service_Specimen(FhirQueryService fhirQueryService, Translator translator_Specimen) {
            return new StructuredQueryService(fhirQueryService, translator_Specimen);
        }
    }

    private static String slurp_FlareApplication(String name) {
        try {
            return Files.readString(resourcePath_FlareApplication(name));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static String slurp_ClassPath(String name) {
        try {
            return Files.readString(resourcePath_ClassPath(name));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static Path resourcePath_ClassPath(String name) throws URISyntaxException {
        return Paths.get(Objects.requireNonNull(StructuredQueryServiceIT.class.getResource(name)).toURI());
    }

    private static Path resourcePath_FlareApplication(String name) throws URISyntaxException {
        return Paths.get(Objects.requireNonNull(FlareApplication.class.getResource(name)).toURI());
    }


    public static Stream<StructuredQuery> getTestQueriesReturningOnePatient() throws URISyntaxException, IOException {
        //not using try-with for zipFile here because the test would otherwise not work as it would state the error
        //that the zip file had been closed for some reason
        var zipFile = new ZipFile(resourcePath_FlareApplication("testCases").resolve("returningOnePatient.zip").toString());
        return zipFile.stream().map(s -> {
            try {
                return new ObjectMapper().readValue(zipFile.getInputStream(s), StructuredQuery.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @BeforeEach
    void setUp() {
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

    @Test
    void execute_genderTestCase() throws URISyntaxException, IOException {
        var query = parse(Files.readString(resourcePath_FlareApplication("testCases").resolve("returningOther").resolve("2-gender.json")));

        var result = service.execute(query).block();

        assertThat(result).isEqualTo(172);
    }

    @Test
    void execute_specimenTestCase() throws IOException {
        dataStoreClient.post()
                .contentType(APPLICATION_JSON)
                .bodyValue(slurp_ClassPath("specimen-diag-testbundle.json"))
                .retrieve()
                .toBodilessEntity()
                .block();

        var query = parse(slurp_ClassPath("sq-test-specimen-diag.json"));

        var result = service_Specimen.execute(query).block();

        assertThat(result).isOne();
    }

    @ParameterizedTest
    @MethodSource("getTestQueriesReturningOnePatient")
    void execute_casesReturningOne(StructuredQuery query) {
        var result = service.execute(query).block();

        assertThat(result).isOne();
    }

    @Test
    void execute_BloodPressureTestCase() throws Exception {
        var query = parse("""
                {
                  "version": "http://to_be_decided.com/draft-1/schema#",
                  "inclusionCriteria": [
                    [
                      {
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

        var result = service.execute(query).block();

        assertThat(result).isOne();
    }

    static StructuredQuery parse(String s) throws JsonProcessingException {
        return new ObjectMapper().readValue(s, StructuredQuery.class);
    }
}
