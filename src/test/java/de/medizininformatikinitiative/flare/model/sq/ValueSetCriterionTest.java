package de.medizininformatikinitiative.flare.model.sq;

import de.medizininformatikinitiative.flare.model.mapping.Mapping;
import de.medizininformatikinitiative.flare.model.mapping.MappingContext;
import de.medizininformatikinitiative.flare.model.sq.expanded.ConceptFilter;
import de.medizininformatikinitiative.flare.model.sq.expanded.ExpandedValueSetCriterion;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.structured_query.Concept;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValueSetCriterionTest {

    static final TermCode COVID = TermCode.of("http://loinc.org", "94500-6", "COVID");
    static final TermCode SEX = TermCode.of("http://loinc.org", "76689-9", "Sex assigned at birth");
    static final TermCode POSITIVE = TermCode.of("http://snomed.info/sct", "positive", "positive");
    static final TermCode MALE = TermCode.of("http://hl7.org/fhir/administrative-gender", "male", "Male");
    static final TermCode FEMALE = TermCode.of("http://hl7.org/fhir/administrative-gender", "female", "Female");

    @Mock
    MappingContext mappingContext;

    @Test
    void toQuery_EmptyExpansion() {
        when(mappingContext.expandConcept(Concept.of(COVID))).thenReturn(Flux.error(new ConceptNotExpandableException(
                Concept.of(COVID))));

        var criteria = ConceptCriterion.of(Concept.of(COVID)).expand(mappingContext);

        StepVerifier.create(criteria).expectError(ConceptNotExpandableException.class).verify();
    }

    @Test
    void toQuery_MappingNotFound() {
        when(mappingContext.expandConcept(Concept.of(COVID))).thenReturn(Flux.just(COVID));
        when(mappingContext.findMapping(COVID)).thenReturn(Mono.error(new MappingNotFoundException(COVID)));

        var criteria = ConceptCriterion.of(Concept.of(COVID)).expand(mappingContext);

        StepVerifier.create(criteria).expectError(MappingNotFoundException.class).verify();
    }

    @Test
    void toQuery_WithOneSelectedConcept() {
        when(mappingContext.expandConcept(Concept.of(COVID))).thenReturn(Flux.just(COVID));
        when(mappingContext.findMapping(COVID)).thenReturn(Mono.just(Mapping.of(COVID, "Observation", "code")
                .withValueSearchParameter("value-concept")));

        var criteria = ValueSetCriterion.of(Concept.of(COVID), POSITIVE).expand(mappingContext);

        StepVerifier.create(criteria)
                .expectNext(new ExpandedValueSetCriterion("Observation", "code", COVID, new ConceptFilter("value-concept", POSITIVE), List.of()))
                .expectComplete()
                .verify();
    }

    @Test
    void toQuery_WithTwoSelectedConcepts() {
        when(mappingContext.expandConcept(Concept.of(SEX))).thenReturn(Flux.just(SEX));
        when(mappingContext.findMapping(SEX)).thenReturn(Mono.just(Mapping.of(SEX, "Observation", "code")
                .withValueSearchParameter("value-concept")));

        var criteria = ValueSetCriterion.of(Concept.of(SEX), MALE, FEMALE).expand(mappingContext);

        StepVerifier.create(criteria)
                .expectNext(new ExpandedValueSetCriterion("Observation", "code", SEX, new ConceptFilter("value-concept", MALE), List.of()))
                .expectNext(new ExpandedValueSetCriterion("Observation", "code", SEX, new ConceptFilter("value-concept", FEMALE), List.of()))
                .expectComplete()
                .verify();
    }
}
