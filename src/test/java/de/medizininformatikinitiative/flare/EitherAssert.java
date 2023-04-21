package de.medizininformatikinitiative.flare;

import org.assertj.core.api.AbstractAssert;

import java.util.function.Consumer;

public class EitherAssert<L, R> extends AbstractAssert<EitherAssert<L, R>, Either<L, R>> {

    protected EitherAssert(Either<L, R> actual) {
        super(actual, EitherAssert.class);
    }

    public EitherAssert<L, R> isLeftInstanceOf(Class<?> clazz) {
        isNotNull();
        if (actual instanceof Either.Left<L, R> l) {
            if (!clazz.isInstance(l.val())) {
                failWithMessage("Expected either to be left and instanceof `%s` but was a `%s`.", clazz.getName(),
                        l.val().getClass().getName());
            }
        } else {
            failWithMessage("Expected either to be left but was right.");
        }
        return myself;
    }

    public EitherAssert<L, R> isLeftEqualTo(L expected) {
        isNotNull();
        if (actual instanceof Either.Left<L, R> l) {
            if (!l.val().equals(expected)) {
                failWithMessage("Expected either to be left with value `%s` but was `%s`.", expected, l.val());
            }
        } else {
            failWithMessage("Expected either to be left but was left.");
        }
        return myself;
    }

    public EitherAssert<L, R> isRightEqualTo(R expected) {
        isNotNull();
        if (actual instanceof Either.Right<L, R> r) {
            if (!r.val().equals(expected)) {
                failWithMessage("Expected either to be right with value `%s` but was `%s`.", expected, r.val());
            }
        } else {
            failWithMessage("Expected either to be right but was left.");
        }
        return myself;
    }

    public EitherAssert<L, R> isRightSatisfying(Consumer<R> requirement) {
        isNotNull();
        if (actual instanceof Either.Right<L, R> r) {
            requirement.accept(r.val());
        } else {
            failWithMessage("Expected either to be right but was left.");
        }
        return myself;
    }
}
