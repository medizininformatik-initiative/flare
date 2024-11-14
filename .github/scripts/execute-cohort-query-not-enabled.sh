#!/bin/bash -e

SCRIPT_DIR="$(dirname "$(readlink -f "$0")")"
. "util.sh"


BASE="http://localhost:8080"
curl_response=$(curl -s "$BASE/query/execute-cohort" -H "Content-Type: application/sq+json" -d @"../integration-test/query-$1.json")

status_code=$(curl -s -o /dev/null -w "%{http_code}" "$BASE/query/execute-cohort" -H "Content-Type: application/sq+json" -d @"../integration-test/query-$1.json")

test "cohort endpoint status code" $status_code 404
