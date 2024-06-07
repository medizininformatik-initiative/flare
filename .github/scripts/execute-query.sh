#!/bin/bash -e

SCRIPT_DIR="$(dirname "$(readlink -f "$0")")"
. "$SCRIPT_DIR/util.sh"

BASE="http://localhost:8080"
EXPECTED_COUNT=$2
ACTUAL_EXPECTED_COUNT=$( curl -s "$BASE/query/execute" -H "Content-Type: application/sq+json" -d @"$SCRIPT_DIR/../integration-test/query-$1.json")

test "count" "$ACTUAL_EXPECTED_COUNT" "$EXPECTED_COUNT"
