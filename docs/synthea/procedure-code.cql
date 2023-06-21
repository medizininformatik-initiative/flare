library "procedure-code"
using FHIR version '4.0.0'
include FHIRHelpers version '4.0.0'

context Patient

define InInitialPopulation:
  [Procedure]

define function Code(procedure FHIR.Procedure):
  procedure.code
