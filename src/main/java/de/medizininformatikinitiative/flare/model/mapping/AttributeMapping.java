package de.medizininformatikinitiative.flare.model.mapping;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import de.medizininformatikinitiative.flare.Either;
import de.medizininformatikinitiative.flare.model.sq.ConceptFilterTypeNotExpandableException;
import de.medizininformatikinitiative.flare.model.sq.Criterion;
import de.medizininformatikinitiative.flare.model.sq.TermCode;
import de.medizininformatikinitiative.flare.model.sq.expanded.*;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static de.medizininformatikinitiative.flare.model.mapping.AttributeMappingType.*;
import static java.util.Objects.requireNonNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AttributeMapping(AttributeMappingType type, TermCode key, String searchParameter,
                               Optional<TermCode> compositeCode)
        implements FilterMapping {

    public AttributeMapping {
        requireNonNull(type);
        requireNonNull(key);
        requireNonNull(searchParameter);
        requireNonNull(compositeCode);
    }

    @JsonCreator
    static AttributeMapping fromJson(@JsonProperty("attributeType") AttributeMappingType type,
                                     @JsonProperty("attributeKey") JsonNode key,
                                     @JsonProperty("attributeSearchParameter") String searchParameter,
                                     @JsonProperty("compositeCode") JsonNode compositeCode)
            throws CompositeCodeNotFoundException {
        requireNonNull(type, "missing JSON property: attributeType");
        requireNonNull(key, "missing JSON property: attributeKey");
        requireNonNull(searchParameter, "missing JSON property: attributeSearchParameter");
        if (type.isCompositeType()) {
            if (compositeCode == null) {
                throw new CompositeCodeNotFoundException(type);
            }
            return new AttributeMapping(type, TermCode.fromJsonNode(key), searchParameter,
                    Optional.of(TermCode.fromJsonNode(compositeCode)));
        }
        return new AttributeMapping(type, TermCode.fromJsonNode(key), searchParameter, Optional.empty());
    }

    public static AttributeMapping code(TermCode key, String searchParameter) {
        return new AttributeMapping(CODE, key, searchParameter, Optional.empty());
    }

    public static AttributeMapping coding(TermCode key, String searchParameter) {
        return new AttributeMapping(CODING, key, searchParameter, Optional.empty());
    }

    public static AttributeMapping compositeComparator(TermCode key, String searchParameter, TermCode compositeCode) {
        return new AttributeMapping(COMPOSITE_QUANTITY_COMPARATOR, key, searchParameter, Optional.of(compositeCode));
    }

    public static AttributeMapping compositeRange(TermCode key, String searchParameter, TermCode compositeCode) {
        return new AttributeMapping(COMPOSITE_QUANTITY_RANGE, key, searchParameter, Optional.of(compositeCode));
    }

    public static AttributeMapping compositeConcept(TermCode key, String searchParameter, TermCode compositeCode) {
        return new AttributeMapping(COMPOSITE_CONCEPT, key, searchParameter, Optional.of(compositeCode));
    }

    public static AttributeMapping reference(TermCode key, String searchParameter) {
        return new AttributeMapping(REFERENCE, key, searchParameter, Optional.empty());
    }

    @Override
    public boolean isAge() {
        return false;
    }

    @Override
    public Either<Exception, ExpandedFilter> expandConcept(TermCode concept) {
        return switch (type) {
            case CODE -> Either.right(new ExpandedCodeFilter(searchParameter, concept.code()));
            case CODING -> Either.right(new ExpandedConceptFilter(searchParameter, concept));
            case COMPOSITE_CONCEPT -> compositeCode
                    .map((Function<TermCode, Either<Exception, ExpandedFilter>>) compositeCode ->
                            Either.right(new ExpandedCompositeConceptFilter(searchParameter, compositeCode, concept)))
                    .orElse(Either.left(new ConceptFilterTypeNotExpandableException(COMPOSITE_CONCEPT)));
            default -> Either.left(new ConceptFilterTypeNotExpandableException(type));
        };
    }

    @Override
    public Either<Exception, List<ExpandedFilter>> expandReference(MappingContext mappingContext, Criterion criterion) {
        return type == REFERENCE
                ? criterion.expand(mappingContext)
                .map(expandedCriteria -> expandedCriteria.stream()
                        .map(ExpandedCriterion::filter)
                        .map(filter -> filter.chain(searchParameter))
                        .toList())
                : Either.left(new ConceptFilterTypeNotExpandableException(type));
    }
}
