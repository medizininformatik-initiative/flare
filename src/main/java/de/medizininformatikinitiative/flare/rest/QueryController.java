package de.medizininformatikinitiative.flare.rest;

import de.medizininformatikinitiative.flare.model.sq.StructuredQuery;
import de.medizininformatikinitiative.flare.service.StructuredQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
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
        return route(GET("query"), this::handle);
    }

    public Mono<ServerResponse> handle(ServerRequest request) {
        logger.debug("Request query page");
        return queryService.execute(StructuredQuery.of(List.of(List.of())))
                .flatMap(count -> ok().bodyValue(count));
    }
}
