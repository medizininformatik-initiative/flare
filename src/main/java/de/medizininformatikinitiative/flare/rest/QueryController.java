package de.medizininformatikinitiative.flare.rest;

import de.medizininformatikinitiative.flare.model.sq.StructuredQuery;
import de.medizininformatikinitiative.flare.service.StructuredQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Objects;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Component
public class QueryController {

    private static final Logger logger = LoggerFactory.getLogger(QueryController.class);

    private final StructuredQueryService queryService;

    public QueryController(StructuredQueryService queryService) {
        this.queryService = Objects.requireNonNull(queryService);
    }

    @Bean
    public RouterFunction<ServerResponse> queryRouter() {
        return route(POST("query/execute").and(accept(MediaType.valueOf("application/sq+json"))), this::handle);
    }

    public Mono<ServerResponse> handle(ServerRequest request) {
        logger.debug("Execute query");
        return request.bodyToMono(StructuredQuery.class)
                .flatMap(queryService::execute)
                .flatMap(count -> ok().bodyValue(count));
    }
}
