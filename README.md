# FLARE

**F**easibi**l**ity **A**nalysis **R**equest **E**xecutor

## Goal

The goal of this project is to provide a service that allows for execution of feasibility queries on a FHIR-server.

## Setting up the test-environment

Set up FHIR test server

```sh 
docker compose up
```

Load example data into FHIR server

```sh 
import-test-data.sh
```

## Build

```sh
mvn clean install
```

## License

Copyright ???

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
