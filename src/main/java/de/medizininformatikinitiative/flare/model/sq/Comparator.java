package de.medizininformatikinitiative.flare.model.sq;

import static java.util.Objects.requireNonNull;

/**
 * Comparator constants used in Structured Queries.
 */
public enum Comparator {

    EQUAL("="),
    LESS_EQUAL("<="),
    LESS_THAN("<"),
    GREATER_EQUAL(">="),
    GREATER_THAN(">");

    private final String s;

    Comparator(String s) {
        this.s = requireNonNull(s);
    }

    public static Comparator fromJson(String s) {
        return switch (s) {
            case "eq" -> EQUAL;
            case "le" -> LESS_EQUAL;
            case "lt" -> LESS_THAN;
            case "ge" -> GREATER_EQUAL;
            case "gt" -> GREATER_THAN;
            default -> throw new IllegalArgumentException("unknown JSON comparator: " + s);
        };
    }

    public String toString() {
        return s;
    }
}
