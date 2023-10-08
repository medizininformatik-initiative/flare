package de.medizininformatikinitiative.flare.model.mapping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.medizininformatikinitiative.flare.model.sq.ContextualTermCode;
import de.medizininformatikinitiative.flare.model.sq.TermCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TermCodeNodeTest {

    static final TermCode CONTEXT = TermCode.of("context", "context", "context");
    static final ContextualTermCode ROOT = ContextualTermCode.of(CONTEXT, TermCode.of("foo", "root", "root"));
    static final ContextualTermCode C1 = ContextualTermCode.of(CONTEXT, TermCode.of("foo", "c1", "c1"));
    static final ContextualTermCode C2 = ContextualTermCode.of(CONTEXT, TermCode.of("foo", "c2", "c2"));
    static final ContextualTermCode C11 = ContextualTermCode.of(CONTEXT, TermCode.of("foo", "c11", "c11"));
    static final ContextualTermCode C12 = ContextualTermCode.of(CONTEXT, TermCode.of("foo", "c12", "c12"));
    static final ContextualTermCode C111 = ContextualTermCode.of(CONTEXT, TermCode.of("foo", "c111", "c111"));
    static final ContextualTermCode C112 = ContextualTermCode.of(CONTEXT, TermCode.of("foo", "c112", "c112"));

    @Test
    void noChildren() {
        var node = TermCodeNode.createNormal(ROOT);

        assertThat(node.children()).isEmpty();
    }

    @Test
    void expand_notFound() {
        var node = TermCodeNode.createNormal(ROOT);

        var result = node.expand(C1).toList();

        assertThat(result).isEmpty();
    }

    @Test
    void expand_itSelf_asLeaf() {
        var node = TermCodeNode.createNormal(ROOT);

        var result = node.expand(ROOT).toList();

        assertThat(result).containsExactly(ROOT);
    }

    @Test
    void expand_itSelf_withChildren_abstract() {
        var node = TermCodeNode.createAbstract(ROOT, TermCodeNode.createNormal(C1), TermCodeNode.createNormal(C2));

        var result = node.expand(ROOT).toList();

        assertThat(result).containsExactly(C1, C2);
    }

    @Test
    void expand_itSelf_withChildren_normal() {
        var node = TermCodeNode.createNormal(ROOT, TermCodeNode.createNormal(C1), TermCodeNode.createNormal(C2));

        var result = node.expand(ROOT).toList();

        assertThat(result).containsExactly(ROOT, C1, C2);
    }

    @Test
    void expand_child_withChildren() {
        var c1 = TermCodeNode.createNormal(C1, TermCodeNode.createNormal(C11), TermCodeNode.createNormal(C12));
        var node = TermCodeNode.createNormal(ROOT, c1, TermCodeNode.createNormal(C2));

        var result = node.expand(C1).toList();

        assertThat(result).containsExactly(C1, C11, C12);
    }

    @Test
    void expand_child_deep() {
        var c11 = TermCodeNode.createNormal(C11, TermCodeNode.createNormal(C111), TermCodeNode.createNormal(C112));
        var c1 = TermCodeNode.createNormal(C1, c11, TermCodeNode.createNormal(C12));
        var node = TermCodeNode.createNormal(ROOT, c1, TermCodeNode.createNormal(C2));

        var result = node.expand(C1).toList();

        assertThat(result).containsOnly(C1, C11, C12, C111, C112);
    }

    @Test
    void fromJson() throws Exception {
        var conceptNode = parse("""
                {
                  "context": {
                    "system": "context-system-142748",
                    "code": "context-code-142803",
                    "display": "context-display-142810"
                  },
                  "termCode": {
                    "system": "system-143705",
                    "code": "code-143708",
                    "display": "display-143716"
                  },
                  "children": []
                }
                """);

        assertThat(conceptNode.contextualTermCode())
                .isEqualTo(ContextualTermCode.of(
                        TermCode.of("context-system-142748", "context-code-142803", "context-display-142810"),
                        TermCode.of("system-143705", "code-143708", "display-143716")));
    }

    @Test
    void fromJson_AdditionalPropertyIsIgnored() throws Exception {
        var conceptNode = parse("""
                {
                  "foo-152133": "bar-152136",
                  "context": {
                    "system": "context-system-142748",
                    "code": "context-code-142803",
                    "display": "context-display-142810"
                  },
                  "termCode": {
                    "system": "system-143705",
                    "code": "code-143708",
                    "display": "display-143716"
                  },
                  "children": []
                }
                """);

        assertThat(conceptNode.contextualTermCode().context().system()).isEqualTo("context-system-142748");
    }

    @Test
    void fromJson_WithChildren() throws Exception {
        var conceptNode = parse("""
                {
                   "context": {
                     "system": "parent-context-system",
                     "code": "parent-context-code",
                     "display": "parent-context-display"
                   },
                   "termCode": {
                     "system": "parent-system",
                     "code": "parent-code",
                     "display": "parent-display"
                   },
                   "children": [
                     {
                       "context": {
                         "system": "child-1-context-system",
                         "code": "child-1-context-code",
                         "display": "child-1-context-display"
                       },
                       "termCode": {
                         "system": "child-1-system",
                         "code": "child-1-code",
                         "display": "child-1-display"
                       }
                     },
                     {
                       "context": {
                         "system": "child-2-context-system",
                         "code": "child-2-context-code",
                         "display": "child-2-context-display"
                       },
                       "termCode": {
                         "system": "child-2-system",
                         "code": "child-2-code",
                         "display": "child-2-display"
                       }
                     }
                   ]
                 }
                """);

        assertThat(conceptNode.contextualTermCode())
                .isEqualTo(ContextualTermCode.of(
                        TermCode.of("parent-context-system", "parent-context-code", "parent-context-display"),
                        TermCode.of("parent-system", "parent-code", "parent-display")));
        assertThat(conceptNode.children().get(0).contextualTermCode())
                .isEqualTo(ContextualTermCode.of(
                        TermCode.of("child-1-context-system", "child-1-context-code", "child-1-context-display"),
                        TermCode.of("child-1-system", "child-1-code", "child-1-display")));
        assertThat(conceptNode.children().get(1).contextualTermCode())
                .isEqualTo(ContextualTermCode.of(
                        TermCode.of("child-2-context-system", "child-2-context-code", "child-2-context-display"),
                        TermCode.of("child-2-system", "child-2-code", "child-2-display")));
    }

    static TermCodeNode parse(String s) throws JsonProcessingException {
        return new ObjectMapper().readValue(s, TermCodeNode.class);
    }
}
