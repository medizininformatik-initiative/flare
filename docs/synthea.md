# The Synthea Dataset

## Determine the Distribution of Conditions

Generate the Condition report:

```sh
blazectl evaluate-measure condition-code.yml --server http://localhost:8082/fhir > condition-report.json
```

Generate the term code tree:

```sh
cat condition-report.json | jq -rf condition-tree.jq > condition-tree.json
```

Generate the mapping:

```sh
cat condition-report.json | jq -rf condition-mapping.jq > condition-mapping.json
```

Generate the union-all query:

```sh
cat condition-report.json | jq -rf condition-query-union-all.jq > condition-query-union-all.json
```

Generate the union-top-10 query:

```sh
cat condition-report.json | jq -rf condition-query-union-top-10.jq > condition-query-union-top-10.json
```

Generate the intersection-top-10 query:

```sh
cat condition-report.json | jq -rf condition-query-intersection-top-10.jq > condition-query-intersection-top-10.json
```
