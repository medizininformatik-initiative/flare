package de.medizininformatikinitiative.flare.model.fhir;

public record Query(String type, QueryParams params) {

    public static Query of(String type, QueryParams params) {
        return new Query(type, params);
    }

    public static Query ofType(String type) {
        return new Query(type, QueryParams.EMPTY);
    }
}
