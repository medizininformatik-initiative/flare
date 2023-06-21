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

## Determine the Distribution of Procedures

Generate the Procedure report:

```sh
blazectl evaluate-measure procedure-code.yml --server http://localhost:8082/fhir > procedure-report.json
```

Generate the term code tree:

```sh
cat procedure-report.json | jq -rf procedure-tree.jq > procedure-tree.json
```

Generate the mapping:

```sh
cat procedure-report.json | jq -rf procedure-mapping.jq > procedure-mapping.json
```

## Determine the Distribution of Observations

Generate the Observation report:

```sh
blazectl evaluate-measure observation-code.yml --server http://localhost:8082/fhir > observation-report.json
```

Generate the term code tree:

```sh
cat observation-report.json | jq -rf observation-tree.jq > observation-tree.json
```

Generate the mapping:

```sh
cat observation-report.json | jq -rf observation-mapping.jq > observation-mapping.json
```

## Concatenate Trees and Mappings

```sh
jq -sf tree.jq condition-tree.json procedure-tree.json observation-tree.json > tree.json
```

```sh
jq -s '[ .[0][], .[1][], .[2][] ]' condition-mapping.json procedure-mapping.json observation-mapping.json > mapping.json
```
