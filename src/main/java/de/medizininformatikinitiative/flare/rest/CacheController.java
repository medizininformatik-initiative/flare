package de.medizininformatikinitiative.flare.rest;

import de.medizininformatikinitiative.flare.service.CachingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static java.util.Objects.requireNonNull;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Component
public class CacheController {

    private static final Logger logger = LoggerFactory.getLogger(CacheController.class);

    private final CachingService memCache;
    private final CachingService diskCache;

    public CacheController(@Qualifier("memCachingFhirQueryService") CachingService memCache,
                           @Qualifier("diskCachingFhirQueryService") CachingService diskCache) {
        this.memCache = requireNonNull(memCache);
        this.diskCache = requireNonNull(diskCache);
    }

    @Bean
    public RouterFunction<ServerResponse> cacheRouter() {
        return route(GET("cache/stats"), this::handle);
    }

    public Mono<ServerResponse> handle(ServerRequest request) {
        logger.debug("Return cache infos");
        return ok().bodyValue(new CacheStats(Runtime.getRuntime().maxMemory() >> 20,
                Runtime.getRuntime().totalMemory() >> 20,
                Runtime.getRuntime().freeMemory() >> 20,
                memCache.stats(), diskCache.stats()));
    }

    public record CacheStats(long maxMemoryMib, long totalMemoryMib, long freeMemoryMib, CachingService.CacheStats memory,
                             CachingService.CacheStats disk) {
    }
}
