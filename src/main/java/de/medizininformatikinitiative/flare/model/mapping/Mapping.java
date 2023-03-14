package de.medizininformatikinitiative.flare.model.mapping;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.medizininformatikinitiative.flare.model.sq.TermCode;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class Mapping {

    private final TermCode key;
    private final String resourceType;
    private final String termCodeSearchParameter;
    private final ValueFilterMapping valueFilterMapping;
    private final List<FixedCriterion> fixedCriteria;
    private final Map<TermCode, AttributeMapping> attributeMappings;
    private final String timeRestrictionPath;

    private Mapping(TermCode key, String resourceType, String termCodeSearchParameter,
                    ValueFilterMapping valueFilterMapping, List<FixedCriterion> fixedCriteria,
                    Map<TermCode, AttributeMapping> attributeMappings, String timeRestrictionPath) {
        this.key = requireNonNull(key);
        this.resourceType = requireNonNull(resourceType);
        this.termCodeSearchParameter = termCodeSearchParameter;
        this.valueFilterMapping = valueFilterMapping;
        this.fixedCriteria = List.copyOf(fixedCriteria);
        this.attributeMappings = Map.copyOf(attributeMappings);
        this.timeRestrictionPath = timeRestrictionPath;
    }

    public static Mapping of(TermCode concept, String resourceType) {
        return new Mapping(concept, resourceType, null, null, List.of(), Map.of(), null);
    }

    public static Mapping of(TermCode concept, String resourceType, String termCodeSearchParameter) {
        return new Mapping(concept, resourceType, requireNonNull(termCodeSearchParameter), null, List.of(), Map.of(),
                null);
    }

    @JsonCreator
    public static Mapping of(@JsonProperty("key") TermCode key,
                             @JsonProperty("fhirResourceType") String resourceType,
                             @JsonProperty("termCodeSearchParameter") String termCodeSearchParameter,
                             @JsonProperty("valueSearchParameter") String valueSearchParameter,
                             @JsonProperty("valueTypeFhir") FilterType valueTypeFhir,
                             @JsonProperty("fixedCriteria") List<FixedCriterion> fixedCriteria,
                             @JsonProperty("attributeSearchParameters") List<AttributeMapping> attributeMappings,
                             @JsonProperty("timeRestrictionPath") String timeRestrictionPath) {
        return new Mapping(key, resourceType, termCodeSearchParameter,
                valueSearchParameter == null
                        ? null
                        : new ValueFilterMapping(valueTypeFhir == null ? FilterType.CODING : valueTypeFhir,
                        valueSearchParameter, key),
                fixedCriteria == null ? List.of() : List.copyOf(fixedCriteria),
                (attributeMappings == null ? Map.of() : attributeMappings.stream()
                        .collect(Collectors.toMap(AttributeMapping::key, Function.identity()))),
                timeRestrictionPath);
    }

    public Mapping withValueFilterMapping(FilterType type, String searchParameter) {
        return new Mapping(key, resourceType, termCodeSearchParameter,
                new ValueFilterMapping(type, searchParameter, key),
                fixedCriteria, attributeMappings, timeRestrictionPath);
    }

    public Mapping withFixedCriteria(FixedCriterion fixedCriterion) {
        var fixedCriteria = new LinkedList<>(this.fixedCriteria);
        fixedCriteria.add(fixedCriterion);
        return new Mapping(key, resourceType, termCodeSearchParameter, valueFilterMapping,
                fixedCriteria, attributeMappings, timeRestrictionPath);
    }

    public Mapping appendAttributeMapping(AttributeMapping attributeMapping) {
        var attributeMappings = new HashMap<>(this.attributeMappings);
        attributeMappings.put(attributeMapping.key(), attributeMapping);
        return new Mapping(key, resourceType, termCodeSearchParameter, valueFilterMapping,
                fixedCriteria, attributeMappings, timeRestrictionPath);
    }

    public TermCode key() {
        return key;
    }

    public String resourceType() {
        return resourceType;
    }

    public String termCodeSearchParameter() {
        return termCodeSearchParameter;
    }

    public Optional<FilterMapping> valueFilterMapping() {
        return Optional.ofNullable(valueFilterMapping);
    }

    public List<FixedCriterion> fixedCriteria() {
        return fixedCriteria;
    }

    public Mono<AttributeMapping> findAttributeMapping(TermCode code) {
        AttributeMapping mapping = attributeMappings.get(code);
        return mapping == null ? Mono.error(new AttributeMappingNotFoundException(key, code)) : Mono.just(mapping);
    }

    private record ValueFilterMapping(FilterType type, String searchParameter, TermCode key) implements FilterMapping {

        private static final TermCode AGE = TermCode.of("http://snomed.info/sct", "424144002", "age");

        private ValueFilterMapping {
            requireNonNull(type);
            requireNonNull(searchParameter);
            requireNonNull(key);
        }

        @Override
        public boolean isAge() {
            return key.equals(AGE);
        }
    }
}
