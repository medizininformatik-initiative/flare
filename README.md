# FLARE

**F**easibi**l**ity **A**nalysis **R**equest **E**xecutor

## Goal

The goal of this project is to provide a service that allows for execution of feasibility queries on a FHIR-server.

## Build

```sh
mvn clean install
```

## Run

```sh
docker-compose up
```

In order for flare to work, a FHIR server such as [Blaze](https://github.com/samply/blaze) must be running on port 8082 by default.

## Environment Variables

| Name                            | Default                               | Depr ¹ | Description                                                                                      |
|:--------------------------------|:--------------------------------------|--------|:-------------------------------------------------------------------------------------------------|
| FLARE_FHIR_SERVER               | http://localhost:8082/fhir            |        | The base URL of the FHIR server to use.                                                          |
| FLARE_FHIR_USER                 |                                       |        | The username to use for HTTP Basic Authentication.                                               |
| FLARE_FHIR_PASSWORD             |                                       |        | The password to use for HTTP Basic Authentication.                                               |
| FLARE_FHIR_OAUTH_ISSUER_URI     |                                       |        | The issuer URI of the OpenID Connect provider.                                                   |
| FLARE_FHIR_OAUTH_CLIENT_ID      |                                       |        | The client ID to use for authentication with OpenID Connect provider.                            |
| FLARE_FHIR_OAUTH_CLIENT_SECRET  |                                       |        | The client secret to use for authentication with OpenID Connect provider.                        |
| FLARE_FHIR_MAX_CONNECTIONS      | 4                                     |        | The maximum number of connections Flare opens towards the FHIR server.                           |
| FLARE_FHIR_MAX_QUEUE_SIZE       | 500                                   |        | The maximum number FHIR server requests Flare queues before returning an error.                  |
| FLARE_FHIR_PAGE_COUNT           | 1000                                  |        | The number of resources per page to request from the FHIR server.                                |
| FLARE_CACHE_MEM_SIZE_MB         | 1024                                  |        | The size of the in-memory cache in mebibytes.                                                    |
| FLARE_CACHE_MEM_EXPIRE          | PT48H                                 |        | The duration after which in-memory cache entries should expire in [ISO 8601 durations][1].       |
| FLARE_CACHE_MEM_REFRESH         | PT24H                                 |        | The duration after which in-memory cache entries should be refreshed in [ISO 8601 durations][1]. |
| FLARE_CACHE_DISK_PATH           | cache                                 |        | The name of the directory in which the on-disk cache should be written.                          |
| FLARE_CACHE_DISK_EXPIRE         | P7D                                   |        | The duration after which on-disk cache entries should expire in [ISO 8601 durations][1].         |
| FLARE_CACHE_DISK_THREADS        | 4                                     |        | The number of threads the disk cache should use for reading and writing entries.                 |
| FLARE_MAPPING_MAPPING_FILE      | ontology/codex-term-code-mapping.json | v2.4.0 | The mappings to use.                                                                             |
| FLARE_MAPPING_CONCEPT_TREE_FILE | ontology/codex-code-tree.json         | v2.4.0 | The code tree to use.                                                                            |
| FLARE_ENABLE_COHORT_ENDPOINT    | false                                 |        | Set to `true` to enable the cohort extraction endpoint, which returns the actual patient IDs.    |
| SERVER_PORT                     | 8080                                  |        | The port at which Flare provides its REST API.                                                   |
| JAVA_TOOL_OPTIONS               | -Xmx4g                                |        | JVM options \(Docker only\)                                                                      |
| LOG_LEVEL                       | info                                  |        | one of trace, debug, info, warn or error                                                         |

¹ Deprecated since

## Default Configuration

The default configuration assumes the following:

* there is about 8 GiB of memory available for Flare
  * 4 GiB JVM heap including 1 GiB in-memory Cache
  * about 1 GiB JVM off-heap memory especially for the disk-based cache
  * about 3 GiB for page cache and the rest of the operating system
* there is plenty of disk space
  * the disk space used by the disk-based cache is not constrained right now
  * cache entries life for at least 7 days per default
  * disk space is only reclaimed on a best-effort base
* the FHIR endpoint is capable of handling 4 requests in parallel
* the FHIR endpoint is capable of returning pages of size 1000 or constrain the page size itself

## Support for self-signed certificates

Flare supports the use of self-signed certificates from your own CAs.
On each startup FLARE will search through the folder /app/certs inside the container
, add all found CA *.pem files to a java truststore and start FLARE with this truststore.

In order to add your own CA files, add your own CA *.pem files to the /app/certs folder of the container.

Using docker-compose mount a folder from your host (e.g.: ./certs) to the /app/certs folder,
 add your *.pem files (one for each CA you would like to support) to the folder and ensure that they
have the .pem extension.

## Documentation

* [REST API](docs/api.md)

## License

Copyright ???

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

[1]: <https://en.wikipedia.org/wiki/ISO_8601>
