package de.medizininformatikinitiative.flare.model.translate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Operator expression that consists of a {@code name} and a list of {@code operands}.
 * <p>
 * There are currently the different kind of operators defined in the {@link Name} enum. If new kinds will be added,
 * there should be also new constructor methods.
 *
 * @param name     the name of the operator
 * @param operands the operands which are itself expressions
 */
@JsonIgnoreProperties({"empty"})
public record Operator(Name name, List<? extends Expression> operands) implements Expression {

    public Operator {
        operands = List.copyOf(operands);
    }

    /**
     * Creates a new {@code difference} operator.
     *
     * @param operands the operands
     * @return the new difference operator
     */
    public static Operator difference(Expression... operands) {
        return new Operator(Name.DIFFERENCE, List.of(operands));
    }

    /**
     * Creates a new {@code intersection} operator.
     *
     * @param operands the operands
     * @return the new intersection operator
     */
    public static Operator intersection(Expression... operands) {
        return new Operator(Name.INTERSECTION, List.of(operands));
    }

    /**
     * Creates a new {@code union} operator.
     *
     * @param operands the operands
     * @return the new union operator
     */
    public static Operator union(Expression... operands) {
        return new Operator(Name.UNION, List.of(operands));
    }

    /**
     * Returns {@code true} iff all {@code operands} are empty or there are no operands at all.
     *
     * @return {@code true} iff all {@code operands} are empty or there are no operands at all
     */
    public boolean isEmpty() {
        return operands.stream().allMatch(Expression::isEmpty);
    }

    public Operator concat(Operator operator) {
        var queries = new ArrayList<Expression>(this.operands);
        queries.addAll(operator.operands);
        return new Operator(this.name, queries);
    }

    public Operator add(Expression element) {
        var queries = new ArrayList<Expression>(this.operands);
        queries.add(element);
        return new Operator(name, queries);
    }

    public Operator map(Function<? super List<? extends Expression>, ? extends List<Expression>> mapper) {
        return new Operator(name, mapper.apply(operands));
    }

    public enum Name {
        @JsonProperty("difference") DIFFERENCE,
        @JsonProperty("intersection") INTERSECTION,
        @JsonProperty("union") UNION
    }
}
