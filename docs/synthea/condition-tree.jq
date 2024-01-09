{
  "termCode": {
    "code": "",
    "display": "",
    "system": ""
  },
  "children": [
    [ .group[0].stratifier[0].stratum[].value.coding[0] ] | unique_by(.code) [] | {"termCode": ., "context": { "code": "Diagnose", "system": "fdpg.mii.cds", "version": "1.0.0", "display": "Diagnose"}}
  ]
}
