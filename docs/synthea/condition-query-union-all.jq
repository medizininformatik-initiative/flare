{
  "inclusionCriteria": [
    [
      [ .group[0].stratifier[0].stratum[].value.coding[0] ] | unique_by(.code) [] |
        { "termCodes": [ . ] }
    ]
  ]
}
