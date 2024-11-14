#!/bin/bash -e

SCRIPT_DIR="$(dirname "$(readlink -f "$0")")"
. "$SCRIPT_DIR/util.sh"

BASE="http://localhost:8080"
EXPECTED_ARRAY_LENGTH=$2
EXPECTED_ID_LENGTH=$3
CURL_RESPONSE=$(curl -s "$BASE/query/execute-cohort" -H "Content-Type: application/sq+json" -d @"$SCRIPT_DIR/../integration-test/query-$1.json")
ACTUAL_PAT_ID_ARRAY=()

while IFS= read -r line; do
    ACTUAL_PAT_ID_ARRAY+=("$line")
done < <(echo "$CURL_RESPONSE" | jq -r '.[]')

test_array "cohort test" ACTUAL_PAT_ID_ARRAY "$EXPECTED_ID_LENGTH" "$EXPECTED_ARRAY_LENGTH"
