{
  "inclusionCriteria": [
    [
      [ .group[0].stratifier[0].stratum[].value.coding[0] ] | unique_by(.code) [] |
        { "termCodes": [ . ], "context": { "code": "Diagnose", "system": "fdpg.mii.cds", "version": "1.0.0", "display": "Diagnose"} }
    ]
  ]
}
