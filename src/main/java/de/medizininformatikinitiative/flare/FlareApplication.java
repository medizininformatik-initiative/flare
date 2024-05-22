package de.medizininformatikinitiative.flare;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.medizininformatikinitiative.flare.model.mapping.MappingContext;
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
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrations;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Clock;
import java.time.Duration;

import static org.springframework.security.oauth2.core.AuthorizationGrantType.CLIENT_CREDENTIALS;

@SpringBootApplication
public class FlareApplication {

    private static final String REGISTRATION_ID = "openid-connect";

    private static final Logger logger = LoggerFactory.getLogger(FlareApplication.class);

    private static final int EIGHT_MEGA_BYTE = 8 << 20;

    public static void main(String[] args) {
        SpringApplication.run(FlareApplication.class, args);
        logger.info("Maximum available memory: {} MiB", Runtime.getRuntime().maxMemory() >> 20);
        logger.info("Number of available processors: {}", Runtime.getRuntime().availableProcessors());
    }

    @Bean
    public WebClient dataStoreClient(@Value("${flare.fhir.server}") String baseUrl,
                                     @Value("${flare.fhir.user}") String user,
                                     @Value("${flare.fhir.password}") String password,
                                     @Value("${flare.fhir.maxConnections}") int maxConnections,
                                     @Value("${flare.fhir.maxQueueSize}") int maxQueueSize,
                                     ObjectMapper mapper,
                                     @Qualifier("oauth") ExchangeFilterFunction oauthExchangeFilterFunction) {
        logger.info("Create a HTTP connection pool to {} with a maximum of {} connections.", baseUrl, maxConnections);
        ConnectionProvider provider = ConnectionProvider.builder("data-store")
                .maxConnections(maxConnections)
                .pendingAcquireMaxCount(maxQueueSize)
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
                .filter(oauthExchangeFilterFunction)
                .codecs(configurer -> {
                    var codecs = configurer.defaultCodecs();
                    codecs.maxInMemorySize(EIGHT_MEGA_BYTE);
                    codecs.jackson2JsonEncoder(new Jackson2JsonEncoder(mapper));
                    codecs.jackson2JsonDecoder(new Jackson2JsonDecoder(mapper));
                })
                .build();
    }

    @Bean
    public MappingContext mappingContext() throws Exception {
        return Util.flareMappingContext(Clock.systemDefaultZone());
    }

    @Bean
    public MemCachingFhirQueryService memCachingFhirQueryService(
            @Qualifier("diskCachingFhirQueryService") FhirQueryService fhirQueryService,
            @Value("${flare.cache.mem.sizeMB}") int sizeInMebibytes,
            @Value("${flare.cache.mem.expire}") Duration expire,
            @Value("${flare.cache.mem.refresh}") Duration refresh) {
        return new MemCachingFhirQueryService(fhirQueryService, new MemCachingFhirQueryService.Config(sizeInMebibytes,
                expire, refresh));
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

    @Bean
    @Qualifier("oauth")
    ExchangeFilterFunction oauthExchangeFilterFunction(
            @Value("${flare.fhir.oauth.issuer.uri:}") String issuerUri,
            @Value("${flare.fhir.oauth.client.id:}") String clientId,
            @Value("${flare.fhir.oauth.client.secret:}") String clientSecret) {
        if (!issuerUri.isEmpty() && !clientId.isEmpty() && !clientSecret.isEmpty()) {
            logger.debug("Enabling OAuth2 authentication (issuer uri: '{}', client id: '{}').",
                    issuerUri, clientId);
            var clientRegistration = ClientRegistrations.fromIssuerLocation(issuerUri)
                    .registrationId(REGISTRATION_ID)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .authorizationGrantType(CLIENT_CREDENTIALS)
                    .build();
            var registrations = new InMemoryReactiveClientRegistrationRepository(clientRegistration);
            var clientService = new InMemoryReactiveOAuth2AuthorizedClientService(registrations);
            var authorizedClientManager = new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
                    registrations, clientService);
            var oAuthExchangeFilterFunction = new ServerOAuth2AuthorizedClientExchangeFilterFunction(
                    authorizedClientManager);
            oAuthExchangeFilterFunction.setDefaultClientRegistrationId(REGISTRATION_ID);

            return oAuthExchangeFilterFunction;
        } else {
            logger.debug("Skipping OAuth2 authentication.");
            return (request, next) -> next.exchange(request);
        }
    }
}
