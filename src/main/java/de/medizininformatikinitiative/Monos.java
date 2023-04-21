package de.medizininformatikinitiative;

import de.medizininformatikinitiative.flare.Either;
import reactor.core.publisher.Mono;

public interface Monos {

    /**
     * Creates a {@code Mono} from an {@code Either}.
     *
     * @param either the {@code Either}
     * @param <T>    the type of the right value of the {@code Either} and the completed value of the {@code Mono}
     * @return either a {@code Mono} representing an error if the {@code either} is left or a {@code Mono} representing
     * a completed value if the {@code either} is right
     */
    static <T> Mono<T> ofEither(Either<Exception, T> either) {
        return either.either(Mono::error, Mono::just);
    }
}
