package de.medizininformatikinitiative.flare.model.mapping;

import de.medizininformatikinitiative.flare.model.sq.ContextualConcept;
import de.medizininformatikinitiative.flare.model.sq.ContextualTermCode;
import de.medizininformatikinitiative.flare.model.sq.TermCode;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static de.medizininformatikinitiative.flare.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

class MappingContextTest {

    static final TermCode CONTEXT = TermCode.of("context", "context", "context");
    static final ContextualTermCode ROOT = ContextualTermCode.of(CONTEXT, TermCode.of("root", "root", "root"));
    static final ContextualTermCode C1 = ContextualTermCode.of(CONTEXT, TermCode.of("foo", "c1", "c1"));
    static final ContextualTermCode C11 = ContextualTermCode.of(CONTEXT, TermCode.of("foo", "c11", "c11"));
    static final ContextualTermCode C12 = ContextualTermCode.of(CONTEXT, TermCode.of("foo", "c12", "c12"));

    @Test
    void expandConcept_ConceptNotExpandable() {
        var context = MappingContext.of(Map.of(), TermCodeNode.createNormal(ROOT));

        var result = context.expandConcept(ContextualConcept.of(C1));

        assertThat(result).isLeftInstanceOf(ContextualConceptNotExpandableException.class);
    }

    @Test
    void expandConcept_OneConcept() {
        var context = MappingContext.of(Map.of(), TermCodeNode.createNormal(ROOT, TermCodeNode.createNormal(C1)));

        var result = context.expandConcept(ContextualConcept.of(C1));

        assertThat(result).isRightSatisfying(r -> assertThat(r).containsExactly(C1));
    }

    @Test
    void expandConcept_TwoConcepts() {
        var context = MappingContext.of(Map.of(), TermCodeNode.createAbstract(C1, TermCodeNode.createNormal(C11),
                TermCodeNode.createNormal(C12)));

        var result = context.expandConcept(ContextualConcept.of(C1));

        assertThat(result).isRightSatisfying(r -> assertThat(r).containsExactly(C11, C12));
    }
}
