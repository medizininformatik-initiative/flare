#!/bin/bash -e

SCRIPT_DIR="$(dirname "$(readlink -f "$0")")"
. "$SCRIPT_DIR/util.sh"

BASE="http://localhost:8080"
STATUS_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE/query/execute-cohort" -H "Content-Type: application/sq+json" -d @"$SCRIPT_DIR/../integration-test/query-$1.json")
EXPECTED_STATUS_CODE=404

test "cohort endpoint status code" "$STATUS_CODE" $EXPECTED_STATUS_CODE
