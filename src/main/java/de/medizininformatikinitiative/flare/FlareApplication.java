package de.medizininformatikinitiative.flare;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.medizininformatikinitiative.flare.model.mapping.Mapping;
import de.medizininformatikinitiative.flare.model.mapping.MappingContext;
import de.medizininformatikinitiative.flare.model.mapping.TermCodeNode;
import de.medizininformatikinitiative.flare.service.DiskCachingFhirQueryService;
import de.medizininformatikinitiative.flare.service.FhirQueryService;
import de.medizininformatikinitiative.flare.service.MemCachingFhirQueryService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;

@SpringBootApplication
public class FlareApplication {

    private static final int TWO_MEGA_BYTE = 2 * 1024 * 1024;

    public static void main(String[] args) {
        SpringApplication.run(FlareApplication.class, args);
    }

    @Bean
    public WebClient dataStoreClient(@Value("${flare.fhir.server}") String baseUrl, ObjectMapper mapper) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Accept", "application/fhir+json")
                .codecs(configurer -> {
                    var codecs = configurer.defaultCodecs();
                    codecs.maxInMemorySize(TWO_MEGA_BYTE);
                    codecs.jackson2JsonEncoder(new Jackson2JsonEncoder(mapper));
                    codecs.jackson2JsonDecoder(new Jackson2JsonDecoder(mapper));
                })
                .build();
    }

    @Bean
    public MappingContext mappingContext() throws Exception {
        var mapper = new ObjectMapper();
        var mappings = Arrays.stream(mapper.readValue(slurp("codex-term-code-mapping.json"), Mapping[].class))
                .collect(Collectors.toMap(Mapping::key, identity()));
        var conceptTree = mapper.readValue(slurp("codex-code-tree.json"), TermCodeNode.class);
        return MappingContext.of(mappings, conceptTree);
    }

    @Bean
    public MemCachingFhirQueryService memCachingFhirQueryService(
            @Qualifier("diskCachingFhirQueryService") FhirQueryService fhirQueryService,
            @Value("${flare.cache.memSizeMB}") int sizeMB,
            @Value("${flare.cache.memExpiryHours}") int expiryHours,
            @Value("${flare.cache.memRefreshHours}") int refreshHours) {
        return new MemCachingFhirQueryService(fhirQueryService, new MemCachingFhirQueryService.Config(
                ((long) sizeMB) * 1024 * 1024, Duration.ofHours(expiryHours), Duration.ofHours(refreshHours)));
    }

    @Bean
    public DiskCachingFhirQueryService diskCachingFhirQueryService(
            @Qualifier("dataStore") FhirQueryService fhirQueryService,
            @Value("${flare.cache.diskPath}") String path,
            @Value("${flare.cache.diskExpiryHours}") int ttlHours) {
        return new DiskCachingFhirQueryService(fhirQueryService, new DiskCachingFhirQueryService.Config(path,
                Duration.ofHours(ttlHours)), Executors.newFixedThreadPool(4));
    }

    private static String slurp(String name) throws Exception {
        return Files.readString(resourcePath(name));
    }

    private static Path resourcePath(String name) throws URISyntaxException {
        return Paths.get(Objects.requireNonNull(FlareApplication.class.getResource(name)).toURI());
    }
}
