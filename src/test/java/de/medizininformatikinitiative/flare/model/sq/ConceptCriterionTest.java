package de.medizininformatikinitiative.flare.model.sq;

import de.medizininformatikinitiative.flare.model.Query;
import de.medizininformatikinitiative.flare.model.mapping.Mapping;
import de.medizininformatikinitiative.flare.model.mapping.MappingContext;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.structured_query.Concept;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConceptCriterionTest {

    private static final TermCode C71 = TermCode.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "C71",
            "Malignant neoplasm of brain");
    private static final TermCode C71_1 = TermCode.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "C71.1",
            "Frontallappen");
    private static final TermCode C71_2 = TermCode.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "C71.2",
            "Temporallappen");

    @Mock
    private MappingContext mappingContext;

    @Test
    void toQuery() {
        when(mappingContext.expandConcept(Concept.of(C71))).thenReturn(Stream.of(C71));
        when(mappingContext.findMapping(C71)).thenReturn(Optional.of(Mapping.of(C71, "Condition", "code")));

        List<Query> queries = ConceptCriterion.of(Concept.of(C71)).toQuery(mappingContext);

        assertThat(queries).containsExactly(Query.of("Condition", "code=http://fhir.de/CodeSystem/bfarm/icd-10-gm|C71"));
    }

    @Test
    void toQuery_Expanding() {
        when(mappingContext.expandConcept(Concept.of(C71))).thenReturn(Stream.of(C71_1, C71_2));
        when(mappingContext.findMapping(C71_1)).thenReturn(Optional.of(Mapping.of(C71_1, "Condition", "code")));
        when(mappingContext.findMapping(C71_2)).thenReturn(Optional.of(Mapping.of(C71_2, "Condition", "code")));

        List<Query> queries = ConceptCriterion.of(Concept.of(C71)).toQuery(mappingContext);

        assertThat(queries).containsExactly(Query.of("Condition", "code=http://fhir.de/CodeSystem/bfarm/icd-10-gm|C71.1"),
                Query.of("Condition", "code=http://fhir.de/CodeSystem/bfarm/icd-10-gm|C71.2"));
    }
}
