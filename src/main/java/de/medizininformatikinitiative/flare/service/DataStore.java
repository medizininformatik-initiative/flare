package de.medizininformatikinitiative.flare.service;

import de.medizininformatikinitiative.flare.model.Population;
import de.medizininformatikinitiative.flare.model.fhir.Bundle;
import de.medizininformatikinitiative.flare.model.fhir.Query;
import de.medizininformatikinitiative.flare.model.fhir.QueryParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;

@Component
public class DataStore implements FhirQueryService {

    private static final Logger logger = LoggerFactory.getLogger(DataStore.class);

    private final WebClient client;
    private final Clock clock;
    private final int pageCount;

    public DataStore(@Qualifier("dataStoreClient") WebClient client,
                     @Qualifier("systemDefaultZone") Clock clock, @Value("${flare.fhir.pageCount}") int pageCount) {
        this.client = Objects.requireNonNull(client);
        this.clock = clock;
        this.pageCount = pageCount;
    }

    public Mono<Population> execute(Query query, boolean ignoreCache) {
        logger.debug("Execute query: {}", query);
        return client.post()
                .uri("/{type}/_search", query.type())
                .contentType(APPLICATION_FORM_URLENCODED)
                .bodyValue(query.params().appendParams(extraQueryParams(query.type())).toString())
                .retrieve()
                .bodyToFlux(Bundle.class)
                .expand(bundle -> bundle.linkWithRel("next")
                        .map(link -> fetchPage(link.url()))
                        .orElse(Mono.empty()))
                .flatMap(bundle -> Flux.fromStream(bundle.entry().stream().map(e -> e.resource().patientId())))
                .collectList()
                .map(patientIds -> Population.copyOf(patientIds).withCreated(clock.instant()));
    }

    private Mono<Bundle> fetchPage(String url) {
        logger.trace("fetch page {}", url);
        return client.get()
                .uri(url)
                .retrieve()
                .bodyToMono(Bundle.class);
    }

    private QueryParams extraQueryParams(String type) {
        return QueryParams.of("_elements", queryElements(type)).appendParam("_count", Integer.toString(pageCount));
    }

    /**
     * The elements the FHIR server should return in resources. For patients the id is sufficient and
     * for all other resource types, we need the subject reference.
     */
    private String queryElements(String type) {
        return switch (type) {
            case "Patient" -> "id";
            case "Immunization" -> "patient";
            case "Consent" -> "patient";
            default -> "subject";
        };
    }
}
