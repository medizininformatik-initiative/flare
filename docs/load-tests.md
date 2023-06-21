# Load Tests

## Union Top 10 Conditions

This test uses a query about the [the union of the top 10 most frequent conditions](synthea/condition-query-union-top-10.json) and runs it with a rate of ten times per second for a duration of 60 seconds.

```sh
jq -scM '.[0].body = (.[1] | @base64) | .[0]' load-tests/execute.json synthea/condition-query-union-top-10.json |\
vegeta attack -rate=10 -format=json -duration=60s |\
vegeta report
```

Result:

```text
Requests      [total, rate, throughput]         600, 10.02, 10.00
Duration      [total, attack, wait]             59.991s, 59.9s, 90.809ms
Latencies     [min, mean, 50, 90, 95, 99, max]  64.906ms, 74.969ms, 72.419ms, 82.132ms, 93.32ms, 101.689ms, 189.035ms
Bytes In      [total, mean]                     3600, 6.00
Bytes Out     [total, mean]                     649200, 1082.00
Success       [ratio]                           100.00%
Status Codes  [code:count]                      200:600
```

## Intersection Top 10 Conditions

This test uses a query about the [the intersection of the top 10 most frequent conditions](synthea/condition-query-intersection-top-10.json) and runs it with a rate of ten times per second for a duration of 60 seconds.

```sh
jq -scM '.[0].body = (.[1] | @base64) | .[0]' load-tests/execute.json synthea/condition-query-intersection-top-10.json |\
vegeta attack -rate=10 -format=json -duration=60s | \
vegeta report
```

Result:

```text
Requests      [total, rate, throughput]         600, 10.02, 10.00
Duration      [total, attack, wait]             59.999s, 59.9s, 99.469ms
Latencies     [min, mean, 50, 90, 95, 99, max]  46.537ms, 87.491ms, 85.925ms, 96.824ms, 99.819ms, 186.572ms, 200.036ms
Bytes In      [total, mean]                     2400, 4.00
Bytes Out     [total, mean]                     660000, 1100.00
Success       [ratio]                           100.00%
Status Codes  [code:count]                      200:600
```
