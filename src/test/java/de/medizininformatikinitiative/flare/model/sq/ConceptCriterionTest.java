package de.medizininformatikinitiative.flare.model.sq;

import de.medizininformatikinitiative.flare.model.mapping.AttributeMapping;
import de.medizininformatikinitiative.flare.model.mapping.Mapping;
import de.medizininformatikinitiative.flare.model.mapping.MappingContext;
import de.medizininformatikinitiative.flare.model.sq.expanded.ConceptFilter;
import de.medizininformatikinitiative.flare.model.sq.expanded.ExpandedCriterion;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.structured_query.Concept;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConceptCriterionTest {

    static final TermCode C71 = TermCode.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "C71",
            "Malignant neoplasm of brain");
    static final TermCode C71_1 = TermCode.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "C71.1",
            "Frontallappen");
    static final TermCode C71_2 = TermCode.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "C71.2",
            "Temporallappen");
    static final TermCode VERIFICATION_STATUS = TermCode.of("hl7.org", "verificationStatus",
            "verificationStatus");
    static final TermCode CONFIRMED = TermCode.of("http://terminology.hl7.org/CodeSystem/condition-ver-status",
            "confirmed", "Confirmed");
    static final TermCode UNCONFIRMED = TermCode.of("http://terminology.hl7.org/CodeSystem/condition-ver-status",
            "unconfirmed", "Unconfirmed");

    @Mock
    MappingContext mappingContext;

    @Test
    void expand_NotExpandable() {
        when(mappingContext.expandConcept(Concept.of(C71))).thenReturn(Flux.error(new ConceptNotExpandableException(
                Concept.of(C71))));

        var criteria = ConceptCriterion.of(Concept.of(C71)).expand(mappingContext);

        StepVerifier.create(criteria).expectError(ConceptNotExpandableException.class).verify();
    }

    @Test
    void expand_MappingNotFound() {
        when(mappingContext.expandConcept(Concept.of(C71))).thenReturn(Flux.just(C71));
        when(mappingContext.findMapping(C71)).thenReturn(Mono.error(new MappingNotFoundException(C71)));

        var criteria = ConceptCriterion.of(Concept.of(C71)).expand(mappingContext);

        StepVerifier.create(criteria).expectError(MappingNotFoundException.class).verify();
    }

    @Test
    void expand_OneExpansion() {
        when(mappingContext.expandConcept(Concept.of(C71))).thenReturn(Flux.just(C71));
        when(mappingContext.findMapping(C71)).thenReturn(Mono.just(Mapping.of(C71, "Condition", "code")));

        var criteria = ConceptCriterion.of(Concept.of(C71)).expand(mappingContext);

        StepVerifier.create(criteria)
                .expectNext(ExpandedCriterion.of("Condition", "code", C71))
                .expectComplete()
                .verify();
    }

    @Test
    void expand_TwoExpansions() {
        when(mappingContext.expandConcept(Concept.of(C71))).thenReturn(Flux.just(C71_1, C71_2));
        when(mappingContext.findMapping(C71_1)).thenReturn(Mono.just(Mapping.of(C71_1, "Condition", "code")));
        when(mappingContext.findMapping(C71_2)).thenReturn(Mono.just(Mapping.of(C71_2, "Condition", "code")));

        var criteria = ConceptCriterion.of(Concept.of(C71)).expand(mappingContext);

        StepVerifier.create(criteria)
                .expectNext(ExpandedCriterion.of("Condition", "code", C71_1))
                .expectNext(ExpandedCriterion.of("Condition", "code", C71_2))
                .expectComplete()
                .verify();
    }

    @Test
    void expand_OneExpansion_OneAttributeFilter_OneConcept() {
        when(mappingContext.expandConcept(Concept.of(C71))).thenReturn(Flux.just(C71));
        when(mappingContext.findMapping(C71)).thenReturn(Mono.just(Mapping.of(C71, "Condition", "code")
                .appendAttributeMapping(AttributeMapping.coding(VERIFICATION_STATUS, "verification-status"))));

        var criteria = ConceptCriterion.of(Concept.of(C71)).appendAttributeFilter(ConceptAttributeFilter.of(
                VERIFICATION_STATUS, CONFIRMED)).expand(mappingContext);

        StepVerifier.create(criteria)
                .expectNext(ExpandedCriterion.of("Condition", "code", C71)
                        .appendFilter(new ConceptFilter("verification-status", CONFIRMED)))
                .expectComplete()
                .verify();
    }

    @Test
    void expand_OneExpansion_OneAttributeFilter_TwoConcepts() {
        when(mappingContext.expandConcept(Concept.of(C71))).thenReturn(Flux.just(C71));
        when(mappingContext.findMapping(C71)).thenReturn(Mono.just(Mapping.of(C71, "Condition", "code")
                .appendAttributeMapping(AttributeMapping.coding(VERIFICATION_STATUS, "verification-status"))));

        var criteria = ConceptCriterion.of(Concept.of(C71)).appendAttributeFilter(ConceptAttributeFilter.of(
                VERIFICATION_STATUS, CONFIRMED).appendConcept(UNCONFIRMED)).expand(mappingContext);

        StepVerifier.create(criteria)
                .expectNext(ExpandedCriterion.of("Condition", "code", C71)
                        .appendFilter(new ConceptFilter("verification-status", CONFIRMED)))
                .expectNext(ExpandedCriterion.of("Condition", "code", C71)
                        .appendFilter(new ConceptFilter("verification-status", UNCONFIRMED)))
                .expectComplete()
                .verify();
    }

    @Test
    void expand_TwoExpansions_OneAttributeFilter_TwoConcepts() {
        when(mappingContext.expandConcept(Concept.of(C71))).thenReturn(Flux.just(C71_1, C71_2));
        when(mappingContext.findMapping(C71_1)).thenReturn(Mono.just(Mapping.of(C71_1, "Condition", "code")
                .appendAttributeMapping(AttributeMapping.coding(VERIFICATION_STATUS, "verification-status"))));
        when(mappingContext.findMapping(C71_2)).thenReturn(Mono.just(Mapping.of(C71_2, "Condition", "code")
                .appendAttributeMapping(AttributeMapping.coding(VERIFICATION_STATUS, "verification-status"))));

        var criteria = ConceptCriterion.of(Concept.of(C71)).appendAttributeFilter(ConceptAttributeFilter.of(
                VERIFICATION_STATUS, CONFIRMED).appendConcept(UNCONFIRMED)).expand(mappingContext);

        StepVerifier.create(criteria)
                .expectNext(ExpandedCriterion.of("Condition", "code", C71_1)
                        .appendFilter(new ConceptFilter("verification-status", CONFIRMED)))
                .expectNext(ExpandedCriterion.of("Condition", "code", C71_1)
                        .appendFilter(new ConceptFilter("verification-status", UNCONFIRMED)))
                .expectNext(ExpandedCriterion.of("Condition", "code", C71_2)
                        .appendFilter(new ConceptFilter("verification-status", CONFIRMED)))
                .expectNext(ExpandedCriterion.of("Condition", "code", C71_2)
                        .appendFilter(new ConceptFilter("verification-status", UNCONFIRMED)))
                .expectComplete()
                .verify();
    }
}
