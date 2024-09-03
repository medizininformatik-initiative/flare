package de.medizininformatikinitiative.flare.model.mapping;

import de.medizininformatikinitiative.flare.model.sq.ContextualConcept;
import de.medizininformatikinitiative.flare.model.sq.ContextualTermCode;
import de.medizininformatikinitiative.flare.model.sq.TermCode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static de.medizininformatikinitiative.flare.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

class MappingContextTest {

    static final TermCode CONTEXT = TermCode.of("context", "context", "context");
    static final String SYSTEM = "sys";
    static final ContextualTermCode ROOT = ContextualTermCode.of(CONTEXT, TermCode.of("root", "root", "root"));
    static final String C1 = "c1";
    static final String C2 = "c2";

    private ContextualTermCode contextualTermCodeOf(String code) {
        return new ContextualTermCode(MappingContextTest.CONTEXT, new TermCode(MappingContextTest.SYSTEM, code, "display"));
    }

    @Test
    void expandConcept_ConceptNotExpandable() {
        var context = MappingContext.of(Map.of(), new MappingTreeBase(List.of(new MappingTreeModuleRoot(CONTEXT, SYSTEM, Map.of()))));

        var result = context.expandConcept(ContextualConcept.of(contextualTermCodeOf(C1)));

        assertThat(result).isLeftInstanceOf(ContextualConceptNotExpandableException.class);
    }

    @Test
    void expandConcept_OneConcept() {
        var context = MappingContext.of(Map.of(), new MappingTreeBase(List.of(
                new MappingTreeModuleRoot(CONTEXT, SYSTEM, Map.of(C1, new MappingTreeModuleEntry(C1, List.of()))))));

        var result = context.expandConcept(ContextualConcept.of(contextualTermCodeOf(C1)));

        assertThat(result).isRightSatisfying(r -> assertThat(r).containsExactly(contextualTermCodeOf(C1)));
    }

    @Test
    void expandConcept_TwoConcepts() {
        var context = MappingContext.of(Map.of(), new MappingTreeBase(List.of(
                new MappingTreeModuleRoot(CONTEXT, SYSTEM, Map.of(C1, new MappingTreeModuleEntry(C1, List.of(C2)),
                        C2, new MappingTreeModuleEntry(C2, List.of()))))));

        var result = context.expandConcept(ContextualConcept.of(contextualTermCodeOf(C1)));

        assertThat(result).isRightSatisfying(r -> {
            assertThat(r.get(0)).isEqualTo(contextualTermCodeOf(C1));
            assertThat(r.get(1)).isEqualTo(contextualTermCodeOf(C2));
        });
    }
}
