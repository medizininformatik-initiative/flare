package de.medizininformatikinitiative.flare.service;

public interface CachingService {

    CacheStats stats();

    record CacheStats(long estimatedEntryCount, long maxMemoryMiB, long usedMemoryMiB, long hitCount, long missCount, long evictionCount) {
    }
}
