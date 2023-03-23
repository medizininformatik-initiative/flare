package de.medizininformatikinitiative.flare.rest;

import de.medizininformatikinitiative.flare.service.DiskCachingFhirQueryService;
import de.medizininformatikinitiative.flare.service.MemCachingFhirQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final MemCachingFhirQueryService memCache;
    private final DiskCachingFhirQueryService diskCache;

    public CacheController(MemCachingFhirQueryService memCache, DiskCachingFhirQueryService diskCache) {
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

    public record CacheStats(long maxMemoryMib, long totalMemoryMib, long freeMemoryMib,
                             MemCachingFhirQueryService.CacheStats memory,
                             DiskCachingFhirQueryService.CacheStats disk) {
    }
}
