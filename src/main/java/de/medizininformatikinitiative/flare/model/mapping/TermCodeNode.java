package de.medizininformatikinitiative.flare.model.mapping;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.medizininformatikinitiative.flare.model.sq.TermCode;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * @author Alexander Kiel
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public sealed interface TermCodeNode permits TermCodeNode.Abstract, TermCodeNode.Normal {

    static TermCodeNode createAbstract(TermCode termCode, TermCodeNode... children) {
        return new TermCodeNode.Abstract(termCode, children == null ? List.of() : List.of(children));
    }

    static TermCodeNode createNormal(TermCode termCode, TermCodeNode... children) {
        return new TermCodeNode.Normal(termCode, children == null ? List.of() : List.of(children));
    }

    @JsonCreator
    static TermCodeNode fromJson(@JsonProperty("termCode") TermCode termCode,
                                 @JsonProperty("abstract") boolean abstractJson,
                                 @JsonProperty("children") TermCodeNode... children) {
        requireNonNull(termCode, "missing JSON property: termCode");
        return abstractJson
                ? new Abstract(termCode, children == null ? List.of() : List.of(children))
                : new Normal(termCode, children == null ? List.of() : List.of(children));
    }

    Stream<TermCode> expand(TermCode termCode);

    Stream<TermCode> leafConcepts();

    TermCode termCode();

    List<TermCodeNode> children();

    record Abstract(TermCode termCode, List<TermCodeNode> children) implements TermCodeNode {

        public Abstract {
            requireNonNull(termCode);
            children = List.copyOf(children);
        }

        @Override
        public Stream<TermCode> expand(TermCode termCode) {
            if (requireNonNull(termCode).equals(this.termCode)) {
                return leafConcepts();
            } else if (children.isEmpty()) {
                return Stream.of();
            } else {
                return children.stream().flatMap(n -> n.expand(termCode));
            }
        }

        @Override
        public Stream<TermCode> leafConcepts() {
            return children.isEmpty() ? Stream.of() : children.stream().flatMap(TermCodeNode::leafConcepts);
        }
    }

    record Normal(TermCode termCode, List<TermCodeNode> children) implements TermCodeNode {

        public Normal {
            requireNonNull(termCode);
            children = List.copyOf(children);
        }

        @Override
        public Stream<TermCode> expand(TermCode termCode) {
            if (requireNonNull(termCode).equals(this.termCode)) {
                return leafConcepts();
            } else if (children.isEmpty()) {
                return Stream.of();
            } else {
                return children.stream().flatMap(n -> n.expand(termCode));
            }
        }

        @Override
        public Stream<TermCode> leafConcepts() {
            if (children.isEmpty()) {
                return Stream.of(termCode);
            } else {
                return Stream.concat(Stream.of(termCode), children.stream().flatMap(TermCodeNode::leafConcepts));
            }
        }
    }
}
