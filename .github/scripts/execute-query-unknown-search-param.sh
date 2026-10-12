#!/bin/bash -e

SCRIPT_DIR="$(dirname "$(readlink -f "$0")")"
. "$SCRIPT_DIR/util.sh"

# Strict handling enabled?
if [ "$1" == "true" ]; then
  EXPECTED_RESPONSE_STATUS_CODE="500"
else
  EXPECTED_RESPONSE_STATUS_CODE="200"
fi

BASE="http://localhost:8080"
RESPONSE_STATUS_CODE=$( curl -s -o /dev/null -w "%{http_code}" "$BASE/query/execute" -H "Content-Type: application/sq+json" -d @"$SCRIPT_DIR/../integration-test/query-unknown-search-param.json" )

test "query endpoint status code" "$RESPONSE_STATUS_CODE" "$EXPECTED_RESPONSE_STATUS_CODE"