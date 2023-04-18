package de.medizininformatikinitiative.flare;

public interface Assertions {

    static <L, R> EitherAssert<L, R> assertThat(Either<L, R> actual) {
        return new EitherAssert<>(actual);
    }
}
