package de.medizininformatikinitiative.flare.service;

public interface CachingService {

    CacheStats stats();

    record CacheStats(long estimatedSize, long hitCount, long missCount) {
    }
}
