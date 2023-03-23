# Flare REST API

## Execute

The Execute-Endpoint returns a single number representing the number of patients fitting the criteria of the structured query that is passed in the body of a POST-Request. Also, the POST-Request must contain `application/sq+json` as Content-Type. It can be called like this:

```sh
curl -s http://localhost:8080/query/execute -H "Content-Type: application/sq+json" -d '<query>'
```


An example `<query>` is:
```json
{
  "version": "http://to_be_decided.com/draft-1/schema#",
  "display": "",
  "inclusionCriteria": [
    [
      {
        "termCodes": [
          {
            "code": "263495000",
            "system": "http://snomed.info/sct",
            "display": "Geschlecht"
          }
        ],
        "valueFilter": {
          "selectedConcepts": [
            {
              "code": "female",
              "system": "http://hl7.org/fhir/administrative-gender",
              "display": "Female"
            }
          ],
          "type": "concept"
        }
      }
    ]
  ]
}
```

The result of that query could be:
```json
31910
```

## Translate

Flare also provides a Translate-Endpoint, which returns the separate FHIR Search queries wrapped in a json format that shows each set operation that is necessary to fit the original inclusion/ exclusion structure.

```sh
curl -s http://localhost:8080/query/translate -H "Content-Type: application/sq+json" -d '<query>' | jq .
```

The translate output explains how Flare will execute the Structured Query. Each JSON Object describes a operator, with a name and operands. The name can be one of union, intersection and difference and describes the set operation carried out by the operator. The operands are FHIR Search queries that will be executed. The result of executing a FHIR Search query is a set of patient ids also called population. For each operator, its operation is applied to its populations and the result is returned recursively as a new population to the next outer operator, where it is again operated upon. The result of the whole calculation will be one single population. The execute endpoint would output the size of that final population.

Here is an example for a translation result:
```json
{
  "name": "difference",
  "operands": [
    {
      "name": "intersection",
      "operands": [
        {
          "name": "union",
          "operands": [
            "[base]/Patient?birthdate=lt2018-03-22",
            "[base]/Patient?gender=female"
          ]
        }
      ]
    },
    {
      "name": "union",
      "operands": [
        {
          "name": "intersection",
          "operands": [
            {
              "name": "union",
              "operands": [
                "[base]/Patient?gender=male"
              ]
            }
          ]
        }
      ]
    }
  ]
}
```
The example query is:
```json
{
  "version": "http://to_be_decided.com/draft-1/schema#",
  "display": "",
  "inclusionCriteria": [
    [
      {
        "termCodes": [
          {
            "code": "424144002",
            "system": "http://snomed.info/sct",
            "display": "Gegenwärtiges chronologisches Alter"
          }
        ],
        "valueFilter": {
          "type": "quantity-comparator",
          "unit": {
            "code": "a",
            "display": "a"
          },
          "value": 5,
          "comparator": "gt"
        }
      },
      {
        "termCodes": [
          {
            "code": "263495000",
            "system": "http://snomed.info/sct",
            "display": "Geschlecht"
          }
        ],
        "valueFilter": {
          "selectedConcepts": [
            {
              "code": "female",
              "system": "http://hl7.org/fhir/administrative-gender",
              "display": "Female"
            }
          ],
          "type": "concept"
        }
      }
    ]
  ],
  "exclusionCriteria": [
    [
      {
        "termCodes": [
          {
            "code": "263495000",
            "system": "http://snomed.info/sct",
            "display": "Geschlecht"
          }
        ],
        "valueFilter": {
          "selectedConcepts": [
            {
              "code": "male",
              "system": "http://hl7.org/fhir/administrative-gender",
              "display": "Male"
            }
          ],
          "type": "concept"
        }
      }
    ]
  ]
}
```

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
