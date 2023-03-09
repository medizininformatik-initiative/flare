package de.medizininformatikinitiative.flare.model.mapping;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.numcodex.sq2cql.model.AttributeMapping;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.structured_query.Modifier;

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
    private final String valueType;
    private final List<Modifier> fixedCriteria;
    private final Map<TermCode, AttributeMapping> attributeMappings;
    private final String timeRestrictionPath;
    private final TermCode primaryCode;

    public Mapping(TermCode key, String resourceType, String termCodeSearchParameter, String valueType, List<Modifier> fixedCriteria,
                   List<AttributeMapping> attributeMappings, String timeRestrictionPath, TermCode primaryCode) {
        this.key = requireNonNull(key);
        this.resourceType = requireNonNull(resourceType);
        this.termCodeSearchParameter = requireNonNull(termCodeSearchParameter);
        this.valueType = valueType;
        this.fixedCriteria = List.copyOf(fixedCriteria);
        this.attributeMappings = (attributeMappings == null ? Map.of() : attributeMappings.stream()
                .collect(Collectors.toMap(AttributeMapping::key, Function.identity())));
        this.timeRestrictionPath = timeRestrictionPath;
        this.primaryCode = primaryCode;
    }

    public static Mapping of(TermCode key, String resourceType) {
        return new Mapping(key, resourceType, "value", null, List.of(), List.of(), null, null);
    }

    public static Mapping of(TermCode concept, String resourceType, String termCodeSearchParameter) {
        return new Mapping(concept, resourceType, termCodeSearchParameter, null, List.of(), List.of(), null, null);
    }

    public static Mapping of(TermCode concept, String resourceType, String termCodeSearchParameter, String valueType) {
        return new Mapping(concept, resourceType, termCodeSearchParameter, valueType, List.of(), List.of(), null, null);
    }

    public static Mapping of(TermCode key, String resourceType, String termCodeSearchParameter, String valueType, List<Modifier> fixedCriteria,List<AttributeMapping> attributeMappings) {
        return new Mapping(key, resourceType, termCodeSearchParameter == null ? "value" : termCodeSearchParameter, valueType,
            fixedCriteria == null ? List.of() : List.copyOf(fixedCriteria),
            attributeMappings, null, null);
    }

    public static Mapping of(TermCode key, String resourceType, String termCodeSearchParameter, String valueType, List<Modifier> fixedCriteria, List<AttributeMapping> attributeMappings, String timeRestrictionPath) {
        return new Mapping(key, resourceType, termCodeSearchParameter == null ? "value" : termCodeSearchParameter, valueType,
            fixedCriteria == null ? List.of() : List.copyOf(fixedCriteria),
            attributeMappings, timeRestrictionPath, null);
    }

    @JsonCreator
    public static Mapping of(@JsonProperty("key") TermCode key,
                             @JsonProperty("fhirResourceType") String resourceType,
                             @JsonProperty("termCodeSearchParameter") String termCodeSearchParameter,
                             @JsonProperty("valueType") String valueType,
                             @JsonProperty("fixedCriteria") List<Modifier> fixedCriteria,
                             @JsonProperty("attributeSearchParameters") List<AttributeMapping> attributeMappings,
                             @JsonProperty("timeRestrictionPath") String timeRestrictionPath,
                             @JsonProperty("primaryCode") TermCode primaryCode) {
        return new Mapping(key, resourceType, termCodeSearchParameter == null ? "value" : termCodeSearchParameter, valueType,
                fixedCriteria == null ? List.of() : List.copyOf(fixedCriteria),
                attributeMappings, timeRestrictionPath, primaryCode);
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

    public String valueType() {
        return valueType;
    }

    public List<Modifier> fixedCriteria() {
        return fixedCriteria;
    }

    public Map<TermCode, AttributeMapping> attributeMappings() {
        return attributeMappings;
    }

    public Optional<String> timeRestrictionPath() {
        return Optional.ofNullable(timeRestrictionPath);
    }

    /**
     * Returns the primary code of this mapping. The primary code is used in the retrieve CQL
     * expression to identify the resources of interest. The path for the primary code path is
     * implicitly given in CQL but can be looked up at https://github.com/cqframework/clinical_quality_language/blob/master/Src/java/quick/src/main/resources/org/hl7/fhir/fhir-modelinfo-4.0.1.xml
     *
     * @return the primary code of this mapping or the key if no primary code is defined
     */
    public TermCode primaryCode() {
      // TODO: decouple key and primary code. The key should only be used for the mapping lookup.
      return primaryCode == null ? key : primaryCode;
    }
}
