#!/bin/bash -e

curl -sH 'Content-Type: application/fhir+json' \
  -d @src/test/resources/de/medizininformatikinitiative/flare/GeneratedBundle.json \
  -o /dev/null \
  -w 'Result: %{response_code}\n' \
  http://localhost:8082/fhir
