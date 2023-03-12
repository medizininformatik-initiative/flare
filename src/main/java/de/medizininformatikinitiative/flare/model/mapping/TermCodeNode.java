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
public record TermCodeNode(TermCode termCode, List<TermCodeNode> children) {

    public TermCodeNode {
        requireNonNull(termCode);
        children = List.copyOf(children);
    }

    public static TermCodeNode of(TermCode termCode) {
        return new TermCodeNode(termCode, List.of());
    }

    @JsonCreator
    public static TermCodeNode of(@JsonProperty("termCode") TermCode termCode,
                                  @JsonProperty("children") TermCodeNode... children) {
        return new TermCodeNode(requireNonNull(termCode, "missing JSON property: termCode"),
                children == null ? List.of() : List.of(children));
    }

    public Stream<TermCode> expand(TermCode termCode) {
        if (requireNonNull(termCode).equals(this.termCode)) {
            return leafConcepts();
        } else if (children.isEmpty()) {
            return Stream.of();
        } else {
            return children.stream().flatMap(n -> n.expand(termCode));
        }
    }

    private Stream<TermCode> leafConcepts() {
        if (children.isEmpty()) {
            return Stream.of(termCode);
        } else {
            return Stream.concat(Stream.of(termCode), children.stream().flatMap(TermCodeNode::leafConcepts));
        }
    }
}
