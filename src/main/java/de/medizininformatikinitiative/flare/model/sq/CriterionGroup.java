package de.medizininformatikinitiative.flare.model.sq;

import de.medizininformatikinitiative.flare.model.Population;
import de.medizininformatikinitiative.flare.model.translate.Expression;
import de.medizininformatikinitiative.flare.model.translate.Operator;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.function.Function;

/**
 * A group of {@link Criterion criterion} providing operations to execute and translate them.
 *
 * @param criteria a list of criterion
 * @param <T>      the type of the criterion that can be either {@link Criterion} itself or an additional layer of groups
 */
public record CriterionGroup<T>(List<T> criteria) {

    private static final Scheduler SCHEDULER = Schedulers.parallel();

    public CriterionGroup {
        criteria = List.copyOf(criteria);
    }

    @SafeVarargs
    public static <T> CriterionGroup<T> of(T... criteria) {
        return new CriterionGroup<>(List.of(criteria));
    }

    /**
     * Returns a group consisting of the results of applying the {@code mapper} to its {@code criteria}.
     *
     * @param mapper the function to apply on all {@code criteria} of this group
     * @param <R>    the function result type
     * @return a group consisting of the results of applying the {@code mapper} to its {@code criteria}
     */
    public <R> CriterionGroup<R> map(Function<? super T, R> mapper) {
        return new CriterionGroup<>(criteria.stream().map(mapper).toList());
    }

    /**
     * Wraps all {@code criteria} of this group into an additional layer of groups.
     *
     * @return a criterion group of criterion groups of the original criteria
     */
    public CriterionGroup<CriterionGroup<T>> wrapCriteria() {
        return map(CriterionGroup::of);
    }

    /**
     * Executes all {@code criteria} of this group using the {@code executor} and performs a set
     * {@link Population#intersection(Population) intersection} on the results.
     */
    public Mono<Population> executeAndIntersection(Function<T, Publisher<? extends Population>> executor) {
        return parallelCriteriaFlux().flatMap(executor).reduce(Population::intersection);
    }

    /**
     * Executes all {@code criteria} of this group using the {@code executor} and performs a set
     * {@link Population#union(Population) union} on the results.
     */
    public Mono<Population> executeAndUnion(Function<T, Publisher<? extends Population>> executor) {
        return parallelCriteriaFlux().flatMap(executor).reduce(Population::union);
    }

    /**
     * Translates all {@code criteria} of this group using the {@code translator} and wraps all resulting expressions
     * into an {@link Operator#intersection(Expression...) intersection} operator.
     */
    public Mono<Operator> translateAndIntersection(Function<T, Mono<? extends Expression>> translator) {
        return Flux.fromIterable(criteria).flatMap(translator).reduce(Operator.intersection(), Operator::add);
    }

    /**
     * Translates all {@code criteria} of this group using the {@code translator} and wraps all resulting expressions
     * into an {@link Operator#union(Expression...) union} operator.
     */
    public Mono<Operator> translateAndUnion(Function<T, Mono<? extends Expression>> translator) {
        return Flux.fromIterable(criteria).flatMap(translator).reduce(Operator.union(), Operator::add);
    }

    /**
     * Translates all {@code criteria} of this group using the {@code translator} and {@link Operator#concat(Operator)
     * concats} all resulting expressions together.
     */
    public Mono<Operator> translateAndConcat(Function<T, Mono<Operator>> translator) {
        return Flux.fromIterable(criteria).flatMap(translator).reduce(Operator::concat);
    }

    private ParallelFlux<T> parallelCriteriaFlux() {
        return Flux.fromIterable(criteria).parallel().runOn(SCHEDULER);
    }
}
