package de.medizininformatikinitiative;

import de.medizininformatikinitiative.flare.Either;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class MonosTest {

    public static final String MESSAGE = "msg-150857";
    public static final String VALUE = "val-151510";

    @Nested
    @DisplayName("ofEither")
    class OfEither {

        @Test
        @DisplayName("a Left Either results in an Error Mono ")
        void ofLeft() {
            var result = Monos.ofEither(Either.left(new Exception(MESSAGE)));

            StepVerifier.create(result).verifyErrorMessage(MESSAGE);
        }

        @Test
        @DisplayName("a Right Either results in an Success Mono ")
        void ofRight() {
            var result = Monos.ofEither(Either.right(VALUE));

            StepVerifier.create(result).expectNext(VALUE).verifyComplete();
        }
    }
}
