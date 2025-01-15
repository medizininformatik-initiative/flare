package de.medizininformatikinitiative.flare.service;

import de.medizininformatikinitiative.flare.Util;
import de.medizininformatikinitiative.flare.model.Population;
import de.medizininformatikinitiative.flare.model.fhir.Bundle;
import de.medizininformatikinitiative.flare.model.fhir.Query;
import de.medizininformatikinitiative.flare.model.fhir.QueryParams;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Clock;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static de.medizininformatikinitiative.flare.model.fhir.QueryParams.stringValue;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;

@Component
public class DataStore implements FhirQueryService {

    private static final Logger logger = LoggerFactory.getLogger(DataStore.class);

    private final WebClient client;
    private final Clock clock;
    private final int pageCount;

    public DataStore(@Qualifier("dataStoreClient") WebClient client, @Qualifier("systemDefaultZone") Clock clock,
                     @Value("${flare.fhir.pageCount}") int pageCount) {
        this.client = Objects.requireNonNull(client);
        this.clock = clock;
        this.pageCount = pageCount;
    }

    @PostConstruct
    public void init() {
        logger.info("Start DataStore with pageCount: {}", pageCount);
    }

    @Override
    public Mono<Population> execute(UUID id, Query query, boolean ignoreCache) {
        var startNanoTime = System.nanoTime();
        logger.debug("Execute query as part of query {}: {}", id, query);
        return client.post()
                .uri("/{type}/_search", query.type())
                .contentType(APPLICATION_FORM_URLENCODED)
                .bodyValue(query.params().appendParams(extraQueryParams(query.type())).toString())
                .retrieve()
                .bodyToFlux(Bundle.class)
                .expand(bundle -> bundle.linkWithRel("next")
                        .map(link -> fetchPage(link.url()))
                        .orElse(Mono.empty()))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(e -> e instanceof WebClientResponseException &&
                                shouldRetry(((WebClientResponseException) e).getStatusCode())))
                .flatMap(bundle -> Flux.fromStream(bundle.entry().stream().flatMap(e -> e.resource().patientId().stream())))
                .collect(Collectors.toSet())
                .map(patientIds -> Population.copyOf(patientIds).withCreated(clock.instant()))
                .doOnNext(p -> logger.debug("Finished query `{}` as part of query {} returning {} patients in {} seconds.", query, id, p.size(),
                        "%.1f".formatted(Util.durationSecondsSince(startNanoTime))))
                .doOnError(e -> logger.error("Error while executing query `{}` as part of query {}: {}", query, id, e.getMessage()));
    }

    private static boolean shouldRetry(HttpStatusCode code) {
        return code.is5xxServerError() || code.value() == 404;
    }

    private Mono<Bundle> fetchPage(String url) {

        logger.trace("fetch page {}", url);
        return client.get()
                .uri(url)
                .retrieve()
                .bodyToMono(Bundle.class);
    }

    private QueryParams extraQueryParams(String type) {
        return QueryParams.of("_elements", stringValue(queryElements(type)))
                .appendParam("_count", stringValue(Integer.toString(pageCount)));
    }

    /**
     * The elements the FHIR server should return in resources. For patients the id is sufficient and
     * for all other resource types, we need the subject reference.
     */
    private static String queryElements(String type) {
        return switch (type) {
            case "Patient" -> "id";
            case "Immunization" -> "patient";
            case "Consent" -> "patient";
            default -> "subject";
        };
    }
}
