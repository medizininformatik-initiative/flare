package de.medizininformatikinitiative.flare.rest;

import de.medizininformatikinitiative.Monos;
import de.medizininformatikinitiative.flare.Util;
import de.medizininformatikinitiative.flare.model.mapping.MappingException;
import de.medizininformatikinitiative.flare.model.sq.StructuredQuery;
import de.medizininformatikinitiative.flare.service.StructuredQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Set;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.*;

@Component
public class QueryController {

    private static final MediaType MEDIA_TYPE_SQ = MediaType.valueOf("application/sq+json");

    private static final Logger logger = LoggerFactory.getLogger(QueryController.class);

    private final StructuredQueryService queryService;

    public QueryController(StructuredQueryService queryService) {
        this.queryService = Objects.requireNonNull(queryService);
    }

    @Bean
    public RouterFunction<ServerResponse> queryRouter(@Value("${flare.cohort.enabled}") boolean cohortEnabled) {

        var route = route(POST("query/execute").and(accept(MEDIA_TYPE_SQ)), this::execute)
                .andRoute(POST("query/translate").and(accept(MEDIA_TYPE_SQ)), this::translate);

        if (cohortEnabled) {
            route = route.andRoute(POST("query/execute-cohort").and(accept(MEDIA_TYPE_SQ)), this::executeCohort);
        }

        return route;
    }

    public Mono<ServerResponse> execute(ServerRequest request) {
        var startNanoTime = System.nanoTime();
        logger.debug("Execute feasibility query");
        return request.bodyToMono(StructuredQuery.class)
                .flatMap(queryService::execute).map(Set::size)
                .flatMap(count -> {
                    logger.debug("Finished feasibility query returning cohort size {} in {} seconds.", count,
                            "%.1f".formatted(Util.durationSecondsSince(startNanoTime)));
                    return ok().bodyValue(count);
                })
                .onErrorResume(MappingException.class, e -> {
                    logger.warn("Mapping error: {}", e.getMessage());
                    return badRequest().bodyValue(new Error(e.getMessage()));
                })
                .onErrorResume(WebClientRequestException.class, e -> {
                    logger.error("Service not available because of downstream web client errors: {}", e.getMessage());
                    return status(503).bodyValue(new Error(e.getMessage()));
                });
    }

    public Mono<ServerResponse> executeCohort(ServerRequest request) {
        var startNanoTime = System.nanoTime();
        logger.debug("Execute cohort query");
        return request.bodyToMono(StructuredQuery.class)
                .flatMap(queryService::execute)
                .flatMap(population -> {
                    logger.debug("Finished cohort query returning cohort of {} patient IDs in {} seconds.", population.size(),
                            "%.1f".formatted(Util.durationSecondsSince(startNanoTime)));
                    return ok().bodyValue(population);
                })
                .onErrorResume(MappingException.class, e -> {
                    logger.warn("Mapping error: {}", e.getMessage());
                    return badRequest().bodyValue(new Error(e.getMessage()));
                })
                .onErrorResume(WebClientRequestException.class, e -> {
                    logger.error("Service not available because of downstream web client errors: {}", e.getMessage());
                    return status(503).bodyValue(new Error(e.getMessage()));
                });
    }

    public Mono<ServerResponse> translate(ServerRequest request) {
        logger.debug("Translate query");
        return request.bodyToMono(StructuredQuery.class)
                .map(queryService::translate)
                .flatMap(Monos::ofEither)
                .flatMap(queryExpression -> ok().bodyValue(queryExpression))
                .onErrorResume(MappingException.class, e -> badRequest().bodyValue(new Error(e.getMessage())));
    }

    public record Error(String error) {
    }
}
