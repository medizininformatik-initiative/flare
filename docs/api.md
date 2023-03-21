# Flare REST API

## Cache Stats

Flare provides an endpoint to request cache statistics. If you call it,

```sh
curl -s http://localhost:8080/cache/stats | jq .
```

the result looks something like this:

```json
{
  "maxMemoryMib": 4096,
  "totalMemoryMib": 1994,
  "freeMemoryMib": 1135,
  "memory": {
    "estimatedEntryCount": 215,
    "maxMemoryMiB": 1024,
    "usedMemoryMiB": 92,
    "hitCount": 430,
    "missCount": 215,
    "evictionCount": 0
  },
  "disk": {
    "estimatedEntryCount": 0,
    "maxMemoryMiB": 0,
    "usedMemSizeInMiB": 0,
    "hitCount": 215,
    "missCount": 0,
    "evictionCount": 0
  }
}
```

In it the entries have the following meaning:

| Name                         | Description                                                                                                        |
|:-----------------------------|:-------------------------------------------------------------------------------------------------------------------|
| maxMemoryMib                 | The configured maximum JVM heap size in MiB. See env var `JAVA_TOOL_OPTIONS`.                                      | 
| totalMemoryMib               | The current total amount of memory used by the JVM in MiB. This can be lower as `maxMemoryMib`.                    | 
| freeMemoryMib                | The current free amount of memory in MiB.                                                                          | 
| memory / estimatedEntryCount | The estimated number of entries in the in-memory cache.                                                            | 
| memory / maxMemoryMiB        | The configured maximum JVM heap size to use for the in-memory cache in MiB. See env var `FLARE_CACHE_MEM_SIZE_MB`. | 
| memory / usedMemoryMiB       | The current amount of JVM heap size used for the in-memory cache in MiB.                                           | 
| memory / hitCount            | The total count of hits in the in-memory cache since the start of Flare.                                           | 
| memory / missCount           | The total count of misses in the in-memory cache since the start of Flare.                                         | 
| memory / evictionCount       | The total count of evictions from the in-memory cache since the start of Flare.                                    | 
| disk / estimatedEntryCount   | Not available.                                                                                                     | 
| disk / maxMemoryMiB          | Not available.                                                                                                     | 
| disk / usedMemoryMiB         | Not available.                                                                                                     | 
| disk / hitCount              | The total count of hits in the disk-based cache since the start of Flare.                                          | 
| disk / missCount             | The total count of misses in the disk-based cache since the start of Flare.                                        | 
| disk / evictionCount         | Not available.                                                                                                     | 
