{
  "inclusionCriteria": [
    [
      [ .group[0].stratifier[0].stratum[] ] |
        unique_by(.value.coding[0].code) |
        sort_by(.population[0].count) |
        reverse |
        limit(10; .[]) |
        .value.coding[0] |
        { "termCodes": [ . ] }
    ]
  ]
}
