# Load Tests

## Union Top 10 Conditions

This test uses a query about the [the union of the top 10 most frequent conditions](synthea/condition-query-union-top-10.json) and runs it with a rate of two times per second for a duration of 60 seconds.

```sh
jq -scM '.[0].body = (.[1] | @base64) | .[0]' load-tests/execute.json synthea/condition-query-union-top-10.json |\
vegeta attack -rate=2 -format=json -duration=60s | \
vegeta report
```

Result:

```text
Requests      [total, rate, throughput]         120, 2.02, 1.95
Duration      [total, attack, wait]             1m2s, 59.499s, 2.183s
Latencies     [min, mean, 50, 90, 95, 99, max]  1.832s, 2.747s, 2.582s, 3.28s, 4.047s, 4.324s, 4.351s
Bytes In      [total, mean]                     720, 6.00
Bytes Out     [total, mean]                     129840, 1082.00
Success       [ratio]                           100.00%
Status Codes  [code:count]                      200:120
```

## Intersection Top 10 Conditions

This test uses a query about the [the intersection of the top 10 most frequent conditions](synthea/condition-query-intersection-top-10.json) and runs it with a rate of two times per second for a duration of 60 seconds.

```sh
jq -scM '.[0].body = (.[1] | @base64) | .[0]' load-tests/execute.json synthea/condition-query-intersection-top-10.json |\
vegeta attack -rate=2 -format=json -duration=60s | \
vegeta report
```

Result:

```text
Requests      [total, rate, throughput]         120, 2.02, 1.80
Duration      [total, attack, wait]             1m6s, 59.5s, 6.989s
Latencies     [min, mean, 50, 90, 95, 99, max]  1.453s, 11.769s, 11.388s, 16.688s, 17.48s, 22.138s, 22.487s
Bytes In      [total, mean]                     480, 4.00
Bytes Out     [total, mean]                     132000, 1100.00
Success       [ratio]                           100.00%
Status Codes  [code:count]                      200:120
```
