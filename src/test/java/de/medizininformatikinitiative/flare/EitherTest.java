package de.medizininformatikinitiative.flare;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static de.medizininformatikinitiative.flare.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

class EitherTest {

    @Test
    @DisplayName("creating a left Either")
    void left() {
        assertThat(Either.left("foo")).isLeftEqualTo("foo");
    }

    @Test
    @DisplayName("creating a right Either")
    void right() {
        assertThat(Either.right("foo")).isRightEqualTo("foo");
    }

    @Nested
    @DisplayName("lift")
    class Lift {

        @Test
        @DisplayName("two right operands result in a right result")
        void bothRight() {
            var result = Either.lift(Integer::signum).apply(Either.right(1));

            assertThat(result).isRightEqualTo(1);
        }

        @Test
        @DisplayName("left operand results in a left result")
        void firstLeft() {
            var result = Either.lift(Integer::signum).apply(Either.left("error"));

            assertThat(result).isLeftEqualTo("error");
        }
    }

    @Nested
    @DisplayName("lift2")
    class Lift2 {

        @Test
        @DisplayName("two right operands result in a right result")
        void bothRight() {
            var result = Either.lift2(List::of).apply(Either.right(1), Either.right(2));

            assertThat(result).isRightEqualTo(List.of(1, 2));
        }

        @Test
        @DisplayName("first left operand results in a left result")
        void firstLeft() {
            var result = Either.lift2(List::of).apply(Either.left("error"), Either.right(2));

            assertThat(result).isLeftEqualTo("error");
        }

        @Test
        @DisplayName("second left operand results in a left result")
        void secondLeft() {
            var result = Either.lift2(List::of).apply(Either.right(List.of(1)), Either.left("error"));

            assertThat(result).isLeftEqualTo("error");
        }
    }

    @Nested
    @DisplayName("liftBinOp")
    class LiftBinOp {

        @Test
        @DisplayName("two right operands result in a right result")
        void bothRight() {
            var result = Either.liftBinOp(Integer::sum).apply(Either.right(1), Either.right(2));

            assertThat(result).isRightEqualTo(3);
        }

        @Test
        @DisplayName("first left operand results in a left result")
        void firstLeft() {
            var result = Either.liftBinOp(Integer::sum).apply(Either.left("error"), Either.right(2));

            assertThat(result).isLeftEqualTo("error");
        }

        @Test
        @DisplayName("second left operand results in a left result")
        void secondLeft() {
            var result = Either.liftBinOp(Integer::sum).apply(Either.right(1), Either.left("error"));

            assertThat(result).isLeftEqualTo("error");
        }
    }

    @Nested
    @DisplayName("map")
    class Map {

        @Test
        @DisplayName("a right value is mapped")
        void rightValue() {
            var result = Either.right(1).map(x -> x + 1);

            assertThat(result).isRightEqualTo(2);
        }

        @Test
        @DisplayName("a left value is not mapped")
        void leftValue() {
            var result = Either.<Integer, Integer>left(1).map(x -> x + 1);

            assertThat(result).isLeftEqualTo(1);
        }
    }

    @Nested
    @DisplayName("flatMap")
    class FlatMap {

        @Test
        @DisplayName("a right value is mapped by a function returning a right value")
        void rightValueRight() {
            var result = Either.right(1).flatMap(x -> Either.right(x + 1));

            assertThat(result).isRightEqualTo(2);
        }

        @Test
        @DisplayName("a right value is mapped by a function returning a left value")
        void rightValueLeft() {
            var result = Either.right(1).flatMap(x -> Either.left(x + 1));

            assertThat(result).isLeftEqualTo(2);
        }

        @Test
        @DisplayName("a left value is not mapped")
        void leftValue() {
            var result = Either.<Integer, Integer>left(1).flatMap(x -> Either.right(x + 1));

            assertThat(result).isLeftEqualTo(1);
        }
    }

    @Nested
    @DisplayName("either")
    class EitherMethod {

        @Test
        @DisplayName("a right value is applied to the right mapper")
        void right() {
            var result = Either.<Exception, String>right("a").either(Exception::getMessage, x -> x + "b");

            assertThat(result).isEqualTo("ab");
        }

        @Test
        @DisplayName("a left value is applied to the left mapper")
        void left() {
            var result = Either.<Exception, String>left(new Exception("a")).either(Exception::getMessage, x -> x + "b");

            assertThat(result).isEqualTo("a");
        }
    }
}
