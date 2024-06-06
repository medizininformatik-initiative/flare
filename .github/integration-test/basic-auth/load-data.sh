#!/bin/bash -e

SCRIPT_DIR="$(dirname "$(readlink -f "$0")")"

blazectl --no-progress --server http://localhost:8082/fhir upload "$SCRIPT_DIR/../../test-data/synthea"
