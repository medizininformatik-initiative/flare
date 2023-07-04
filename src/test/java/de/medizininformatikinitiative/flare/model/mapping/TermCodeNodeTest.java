package de.medizininformatikinitiative.flare.model.mapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.medizininformatikinitiative.flare.model.sq.TermCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TermCodeNodeTest {

    static final TermCode ROOT = TermCode.of("foo", "root", "root");
    static final TermCode C1 = TermCode.of("foo", "c1", "c1");
    static final TermCode C2 = TermCode.of("foo", "c2", "c2");
    static final TermCode C11 = TermCode.of("foo", "c11", "c11");
    static final TermCode C12 = TermCode.of("foo", "c12", "c12");
    static final TermCode C111 = TermCode.of("foo", "c111", "c111");
    static final TermCode C112 = TermCode.of("foo", "c112", "c112");

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
        var mapper = new ObjectMapper();

        var conceptNode = mapper.readValue("""
                {"termCode": {
                   "system": "system-143705",
                   "code": "code-143708",
                   "display": "display-143716"
                 },
                 "children": []
                }
                """, TermCodeNode.class);

        assertThat(conceptNode.termCode().system()).isEqualTo("system-143705");
    }

    @Test
    void fromJson_AdditionalPropertyIsIgnored() throws Exception {
        var mapper = new ObjectMapper();

        var conceptNode = mapper.readValue("""
                {"foo-152133": "bar-152136",
                 "termCode": {
                   "system": "system-143705",
                   "code": "code-143708",
                   "display": "display-143716"
                 },
                 "children": []
                }
                """, TermCodeNode.class);

        assertThat(conceptNode.termCode().system()).isEqualTo("system-143705");
    }

    @Test
    void fromJson_WithChildren() throws Exception {
        var mapper = new ObjectMapper();

        var conceptNode = mapper.readValue("""
                {"termCode": {
                   "system": "system-143705",
                   "code": "code-143708",
                   "display": "display-143716"
                 },
                 "children": [
                  {"termCode": {
                     "system": "child-1-system-155856",
                     "code": "child-1-code-155858",
                     "display": "child-1-display-155900"
                  }},
                  {"termCode": {
                     "system": "child-2-system-155958",
                     "code": "child-2-code-160000",
                     "display": "child-2-display-160002"
                  }}
                 ]
                }
                """, TermCodeNode.class);

        assertThat(conceptNode.termCode().system()).isEqualTo("system-143705");
        assertThat(conceptNode.children().get(0).termCode().system()).isEqualTo("child-1-system-155856");
        assertThat(conceptNode.children().get(1).termCode().system()).isEqualTo("child-2-system-155958");
    }
}
