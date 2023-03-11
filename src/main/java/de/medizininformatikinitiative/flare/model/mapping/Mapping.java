package de.medizininformatikinitiative.flare.model.mapping;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.medizininformatikinitiative.flare.model.sq.AttributeMappingNotFoundException;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.structured_query.Modifier;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * @author Alexander Kiel
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Mapping {

    private final TermCode key;
    private final String resourceType;
    private final String termCodeSearchParameter;
    private final String valueSearchParameter;
    private final String valueTypeFhir;
    private final List<Modifier> fixedCriteria;
    private final Map<TermCode, AttributeMapping> attributeMappings;
    private final String timeRestrictionPath;

    private Mapping(TermCode key, String resourceType, String termCodeSearchParameter, String valueSearchParameter,
                    String valueTypeFhir, List<Modifier> fixedCriteria,
                    Map<TermCode, AttributeMapping> attributeMappings, String timeRestrictionPath) {
        this.key = requireNonNull(key);
        this.resourceType = requireNonNull(resourceType);
        this.termCodeSearchParameter = requireNonNull(termCodeSearchParameter);
        this.valueSearchParameter = valueSearchParameter;
        this.valueTypeFhir = valueTypeFhir;
        this.fixedCriteria = List.copyOf(fixedCriteria);
        this.attributeMappings = attributeMappings;
        this.timeRestrictionPath = timeRestrictionPath;
    }

    public static Mapping of(TermCode concept, String resourceType, String termCodeSearchParameter) {
        return new Mapping(concept, resourceType, termCodeSearchParameter, "value", null, List.of(), Map.of(), null);
    }

    public Mapping withValueSearchParameter(String valueSearchParameter) {
        return new Mapping(key, resourceType, termCodeSearchParameter, requireNonNull(valueSearchParameter),
                valueTypeFhir, fixedCriteria, attributeMappings, timeRestrictionPath);
    }

    public Mapping appendAttributeMapping(AttributeMapping attributeMapping) {
        var attributeMappings = new HashMap<>(this.attributeMappings);
        attributeMappings.put(attributeMapping.key(), attributeMapping);
        return new Mapping(key, resourceType, termCodeSearchParameter, valueSearchParameter, valueTypeFhir,
                fixedCriteria, Map.copyOf(attributeMappings), timeRestrictionPath);
    }

    @JsonCreator
    public static Mapping of(@JsonProperty("key") TermCode key,
                             @JsonProperty("fhirResourceType") String resourceType,
                             @JsonProperty("termCodeSearchParameter") String termCodeSearchParameter,
                             @JsonProperty("valueSearchParameter") String valueSearchParameter,
                             @JsonProperty("valueTypeFhir") String valueTypeFhir,
                             @JsonProperty("fixedCriteria") List<Modifier> fixedCriteria,
                             @JsonProperty("attributeSearchParameters") List<AttributeMapping> attributeMappings,
                             @JsonProperty("timeRestrictionPath") String timeRestrictionPath) {
        return new Mapping(key, resourceType, termCodeSearchParameter == null ? "code" : termCodeSearchParameter,
                valueSearchParameter,
                valueTypeFhir,
                fixedCriteria == null ? List.of() : List.copyOf(fixedCriteria),
                (attributeMappings == null ? Map.of() : attributeMappings.stream()
                        .collect(Collectors.toMap(AttributeMapping::key, Function.identity()))),
                timeRestrictionPath);
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

    public Optional<String> valueSearchParameter() {
        return Optional.ofNullable(valueSearchParameter);
    }

    public Mono<AttributeMapping> findAttributeMapping(TermCode code) {
        AttributeMapping mapping = attributeMappings.get(code);
        return mapping == null ? Mono.error(new AttributeMappingNotFoundException(key, code)) : Mono.just(mapping);
    }
}
