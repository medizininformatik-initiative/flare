package de.medizininformatikinitiative.flare.model.fhir;

import de.medizininformatikinitiative.flare.model.sq.Comparator;
import de.medizininformatikinitiative.flare.model.sq.TermCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * An immutable list of query params.
 * <p>
 * In order to build a list of query params, start either with {@link #EMPTY} or one of the {@link #of(String, String)
 * of-creator methods} and continue with one of the {@link #appendParam(String, String, String) appendParam} functions.
 */
public record QueryParams(List<Param> params) {

    public static QueryParams EMPTY = new QueryParams(List.of());

    public QueryParams {
        params = List.copyOf(params);
    }

    public static QueryParams of(String name, String value) {
        return EMPTY.appendParam(name, value, null);
    }

    public static QueryParams of(String name, String value, String referenceSearchParam) {
        return EMPTY.appendParam(name, value, referenceSearchParam);
    }

    public static QueryParams of(String name, TermCode termCode) {
        return EMPTY.appendParam(requireNonNull(name), requireNonNull(termCode));
    }

    public static QueryParams of(String name, TermCode termCode,TermCode compositeCode) {
        return EMPTY.appendParam(requireNonNull(name), requireNonNull(termCode), compositeCode, null);
    }

    public static QueryParams of(String name, TermCode termCode,TermCode compositeCode, String referenceSearchParam) {
        return EMPTY.appendParam(requireNonNull(name), requireNonNull(termCode), compositeCode, referenceSearchParam);
    }

    /**
     * Appends a param with {@code name} and {@code value}.
     *
     * @param                       name  the name of the query parameter
     * @param                       value the value of the query parameter
     * @param referenceSearchParam  the Search Parameter of the Reference Filter, if it comes from a reference
     * @return the {@code QueryParams} resulting in appending the param
     */
    public QueryParams appendParam(String name, String value, String referenceSearchParam) {
        var sb = new LinkedList<>(this.params);
        sb.add(new Param(name, value, referenceSearchParam));
        return new QueryParams(sb);
    }

    /**
     * Appends a param with {@code name} and {@code value}.
     *
     * @param name                  the name of the query parameter
     * @param value                 the value of the query parameter
     * @param compositeCode         the compositeCode that should be prepended to the query
     * @param referenceSearchParam  the Search Parameter of the Reference Filter, if it comes from a reference
     * @return the {@code QueryParams} resulting in appending the param
     */
    public QueryParams appendParam(String name, String value, TermCode compositeCode, String referenceSearchParam) {
        String compCodeAttachment = compositeCode == null ? "" : compositeCode.system() + "|" + compositeCode.code() + "$";
        return appendParam(name, compCodeAttachment + value, referenceSearchParam);
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
        return appendParam(name, termCode.system() + "|" + termCode.code(), null);
    }

    /**
     * Appends a param with {@code name} and a token generated from {@code termCode}.
     * <p>
     * The token is build by joining the {@link TermCode#system() system} and the {@link TermCode#code() code} from
     * {@code termCode} with a {@code |} character.
     *
     * @param name                  the name of the query parameter
     * @param termCode              the {@link TermCode} to use as value of the query parameter
     * @param compositeCode         the Composite Code that should be prepended to the query
     * @param referenceSearchParam  the Search Parameter of the Reference Filter, if it comes from a reference
     * @return the {@code QueryParams} resulting in appending the param
     */
    public QueryParams appendParam(String name, TermCode termCode, TermCode compositeCode, String referenceSearchParam) {
        return appendParam(name, termCode.system() + "|" + termCode.code(), compositeCode, referenceSearchParam);
    }

    /**
     * Appends a param with {@code name} and a value that the resources should be compared with in the end.
     *
     * @param name                  the name of the query parameter
     * @param comparator            the {@link Comparator} to use as prefix
     * @param value                 the value that should be compared
     * @param unit                  the unit of the {@code value}
     * @param compositeCode         the Composite Code that should be prepended to the query
     * @param referenceSearchParam  the Search Parameter of the Reference Filter, if it comes from a reference
     * @return the {@code QueryParams} resulting in appending the param
     */
    public QueryParams appendParam(String name, Comparator comparator, BigDecimal value, TermCode unit, TermCode compositeCode, String referenceSearchParam) {
        String unitAttachment = unit == null ? "" : "|" + unit.system() + "|" + unit.code();
        return appendParam(name, comparator.toString() + requireNonNull(value) + unitAttachment, compositeCode, referenceSearchParam);
    }

    /**
     * Appends a param with {@code name} and a date value that the resources should be compared with in the end.
     *
     * @param name                  the name of the query parameter
     * @param comparator            the {@link Comparator} to use as prefix
     * @param value                 the date that should be compared
     * @param referenceSearchParam  the Search Parameter of the Reference Filter, if it comes from a reference
     * @return the {@code QueryParams} resulting in appending the param
     */
    public QueryParams appendParam(String name, Comparator comparator, LocalDate value, String referenceSearchParam) {
        return appendParam(name, comparator.toString() + requireNonNull(value), referenceSearchParam);
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

    public record Param(String name, String value, String referenceSearchParam) {

        @Override
        public String toString() {
            String referenceAttachment = referenceSearchParam == null ? "" : referenceSearchParam + ".";
            return referenceAttachment + name + "=" + value;
        }
    }
}
