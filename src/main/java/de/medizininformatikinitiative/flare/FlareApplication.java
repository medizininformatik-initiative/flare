package de.medizininformatikinitiative.flare;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.medizininformatikinitiative.flare.model.mapping.Mapping;
import de.medizininformatikinitiative.flare.model.mapping.MappingContext;
import de.numcodex.sq2cql.model.TermCodeNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

@SpringBootApplication
public class FlareApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlareApplication.class, args);
    }

    @Bean
    public WebClient dataStoreClient(@Value("${app.dataStore.baseUrl}") String baseUrl) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Accept", "application/fhir+json")
                .build();
    }

    @Bean
    public MappingContext mappingContext() throws Exception {
        var mapper = new ObjectMapper();
        var mappings = Arrays.stream(mapper.readValue(slurp(
                        "codex-term-code-mapping.json"), Mapping[].class))
                .collect(Collectors.toMap(Mapping::key, v -> v));
        var conceptTree = mapper.readValue(slurp("codex-code-tree.json"), TermCodeNode.class);
        return MappingContext.of(mappings, conceptTree);
    }

    private static String slurp(String name) throws Exception {
        return Files.readString(resourcePath(name));
    }

    private static Path resourcePath(String name) throws URISyntaxException {
        return Paths.get(Objects.requireNonNull(FlareApplication.class.getResource(name)).toURI());
    }
}
