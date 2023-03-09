package de.medizininformatikinitiative.flare.model.fhir;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Resource(String id, Reference patient, Reference subject) {

    public String patientId() {
        return patient != null ? patient.id() : subject != null ? subject.id() : id;
    }
}
