package de.medizininformatikinitiative.flare.model;

public record Query(String type, String params) {

    public static Query ofType(String type) {
        return new Query(type, "");
    }
}
