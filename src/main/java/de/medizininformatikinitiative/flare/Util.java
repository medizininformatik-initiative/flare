package de.medizininformatikinitiative.flare;

import reactor.core.publisher.Mono;

import java.util.LinkedList;
import java.util.List;

public interface Util {

    static <T> List<T> add(List<T> xs, T x) {
        LinkedList<T> rs = new LinkedList<>(xs);
        rs.add(x);
        return List.copyOf(rs);
    }

    static <T> List<List<T>> add(List<List<T>> xs, List<T> x) {
        LinkedList<List<T>> rs = new LinkedList<>(xs);
        rs.add(x);
        return List.copyOf(rs);
    }

    /**
     * In Clojure this is:
     * <pre>
     * (reduce
     *   (fn [result rows]
     *     (reduce
     *       (fn [new-result result-rows]
     *         (reduce
     *           (fn [new-result-row x]
     *             (conj new-result-row (conj result-rows x)))
     *           new-result
     *           rows))
     *       []
     *       result))
     *   [[]]
     *   matrix)
     * </pre>
     */
    static <T> List<List<T>> cartesianProduct(List<List<T>> matrix) {
        var result = List.of(List.<T>of());
        for (var rows : matrix) {
            result = cartesianProductHelper1(result, rows);
        }
        return result;
    }

    private static <T> List<List<T>> cartesianProductHelper1(List<List<T>> result, List<T> rows) {
        var newResult = List.<List<T>>of();
        for (var resultRows : result) {
            newResult = cartesianProductHelper2(newResult, resultRows, rows);
        }
        return newResult;
    }

    private static <T> List<List<T>> cartesianProductHelper2(List<List<T>> newResult, List<T> resultRows, List<T> rows) {
        for (var x : rows) {
            newResult = add(newResult, add(resultRows, x));
        }
        return newResult;
    }

    static <T> List<T> concat(List<T> as, List<T> bs) {
        LinkedList<T> rs = new LinkedList<>(as);
        rs.addAll(bs);
        return List.copyOf(rs);
    }

    static <T> Mono<List<List<T>>> add(Mono<List<List<T>>> monoR, Mono<List<T>> monoXs) {
        return monoR.flatMap(r -> monoXs.map(xs -> Util.add(r, xs)));
    }

    static <T> Mono<List<T>> concat(Mono<List<T>> mA, Mono<List<T>> mB) {
        return mA.flatMap(a -> mB.map(b -> Util.concat(a, b)));
    }
}
