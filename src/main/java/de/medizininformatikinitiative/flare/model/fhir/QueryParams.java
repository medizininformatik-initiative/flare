package de.medizininformatikinitiative.flare.model.fhir;

import de.medizininformatikinitiative.flare.model.sq.Comparator;
import de.medizininformatikinitiative.flare.model.sq.TermCode;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An immutable list of query params.
 * <p>
 * In order to build a list of query params, start either with {@link #EMPTY} or one of the {@link #of(String, String)
 * of-creator methods} and continue with one of the {@link #appendParam(String, String) appendParam} functions.
 */
public record QueryParams(List<Param> params) {

    public static QueryParams EMPTY = new QueryParams(List.of());

    public QueryParams {
        params = List.copyOf(params);
    }

    public static QueryParams of(String name, String value) {
        return EMPTY.appendParam(name, value);
    }

    public static QueryParams of(String name, TermCode termCode) {
        return EMPTY.appendParam(name, termCode);
    }

    /**
     * Appends a param with {@code name} and {@code value}.
     *
     * @param name  the name of the query parameter
     * @param value the value of the query parameter
     * @return the {@code QueryParams} resulting in appending the param
     */
    public QueryParams appendParam(String name, String value) {
        var sb = new LinkedList<>(this.params);
        sb.add(new Param(name, value));
        return new QueryParams(sb);
    }

    /**
     * Appends a param with {@code name} and a token generated from {@code termCode}.
     * <p>
     * The token is build by joining the {@link TermCode#system() system} and the {@link TermCode#code() code} from
     * {@code termCode} with a {@code |} character.
     *
     * @param name     the name of the query parameter
     * @param termCode the {@link TermCode} to use as value of the query parameter
     * @return the {@code QueryParams} resulting in appending the param
     */
    public QueryParams appendParam(String name, TermCode termCode) {
        return appendParam(name, termCode.system() + "|" + termCode.code());
    }

    /**
     * Appends a param with {@code name} and a value that the resources should be compared with in the end.
     *
     * @param name       the name of the query parameter
     * @param comparator the {@link Comparator} to use as prefix
     * @param value      the value that should be compared
     * @param unit       the unit of the {@code value}
     * @return the {@code QueryParams} resulting in appending the param
     */
    public QueryParams appendParam(String name, Comparator comparator, BigDecimal value, TermCode unit) {
        String unitAttachment = unit == null ? "" :  "|" + unit.system() + "|" + unit.code();
        return appendParam(name, comparator.toString() + value + unitAttachment);
    }

    /**
     * Appends a params by calling the function {@code appendTo}.
     *
     * @param params a function that takes a {@code QueryParams}, appends some params returning the resulting {@code QueryParams}
     * @return the {@code QueryParams} resulting in appending the params
     */
    public QueryParams appendParams(QueryParams params) {
        var sb = new LinkedList<>(this.params);
        sb.addAll(params.params);
        return new QueryParams(sb);
    }

    @Override
    public String toString() {
        return params.stream().map(Param::toString).collect(Collectors.joining("&"));
    }

    private record Param(String name, String value) {

        @Override
        public String toString() {
            return name + "=" + value;
        }
    }
}
