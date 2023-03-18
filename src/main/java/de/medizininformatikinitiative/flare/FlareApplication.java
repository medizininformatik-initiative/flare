package de.medizininformatikinitiative.flare;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.medizininformatikinitiative.flare.model.mapping.Mapping;
import de.medizininformatikinitiative.flare.model.mapping.MappingContext;
import de.medizininformatikinitiative.flare.model.mapping.TermCodeNode;
import de.medizininformatikinitiative.flare.service.DiskCachingFhirQueryService;
import de.medizininformatikinitiative.flare.service.FhirQueryService;
import de.medizininformatikinitiative.flare.service.MemCachingFhirQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.io.File;
import java.time.Clock;
import java.time.Duration;
import java.util.Arrays;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;

@SpringBootApplication
public class FlareApplication {

    private static final Logger logger = LoggerFactory.getLogger(FlareApplication.class);

    private static final int TWO_MEGA_BYTE = 2 * 1024 * 1024;

    public static void main(String[] args) {
        SpringApplication.run(FlareApplication.class, args);
    }

    @Bean
    public WebClient dataStoreClient(@Value("${flare.fhir.server}") String baseUrl,
                                     @Value("${flare.fhir.user}") String user,
                                     @Value("${flare.fhir.password}") String password,
                                     @Value("${flare.fhir.maxConnections}") int maxConnections,
                                     ObjectMapper mapper) {
        logger.info("Create a HTTP connection pool to {} with a maximum of {} connections.", baseUrl, maxConnections);
        ConnectionProvider provider = ConnectionProvider.builder("data-store")
                .maxConnections(maxConnections)
                .pendingAcquireMaxCount(500)
                .build();
        HttpClient httpClient = HttpClient.create(provider);
        WebClient.Builder builder = WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader("Accept", "application/fhir+json");
        if (!user.isEmpty() && !password.isEmpty()) {
            builder = builder.filter(ExchangeFilterFunctions.basicAuthentication(user, password));
        }
        return builder
                .codecs(configurer -> {
                    var codecs = configurer.defaultCodecs();
                    codecs.maxInMemorySize(TWO_MEGA_BYTE);
                    codecs.jackson2JsonEncoder(new Jackson2JsonEncoder(mapper));
                    codecs.jackson2JsonDecoder(new Jackson2JsonDecoder(mapper));
                })
                .build();
    }

    @Bean
    public MappingContext mappingContext(@Value("${flare.mapping.mappingsFile}") String mappingsFile,
                                         @Value("${flare.mapping.conceptTreeFile}") String conceptTreeFile) throws Exception {
        var mapper = new ObjectMapper();
        var mappings = Arrays.stream(mapper.readValue(new File(mappingsFile), Mapping[].class))
                .collect(Collectors.toMap(Mapping::key, identity()));
        var conceptTree = mapper.readValue(new File(conceptTreeFile), TermCodeNode.class);
        return MappingContext.of(mappings, conceptTree);
    }

    @Bean
    public MemCachingFhirQueryService memCachingFhirQueryService(
            @Qualifier("diskCachingFhirQueryService") FhirQueryService fhirQueryService,
            @Value("${flare.cache.mem.sizeMB}") int sizeMB,
            @Value("${flare.cache.mem.expire}") Duration expire,
            @Value("${flare.cache.mem.refresh}") Duration refresh) {
        return new MemCachingFhirQueryService(fhirQueryService, new MemCachingFhirQueryService.Config(
                ((long) sizeMB) * 1024 * 1024, expire, refresh));
    }

    @Bean
    public DiskCachingFhirQueryService diskCachingFhirQueryService(
            @Qualifier("dataStore") FhirQueryService fhirQueryService,
            @Qualifier("systemDefaultZone") Clock clock,
            @Value("${flare.cache.disk.path}") String path,
            @Value("${flare.cache.disk.expire}") Duration expire,
            @Value("${flare.cache.disk.threads}") int numThreads) {
        return new DiskCachingFhirQueryService(fhirQueryService, new DiskCachingFhirQueryService.Config(path, expire),
                Schedulers.newParallel("disk-cache", numThreads), clock);
    }

    @Bean
    public Clock systemDefaultZone() {
        return Clock.systemDefaultZone();
    }
}
