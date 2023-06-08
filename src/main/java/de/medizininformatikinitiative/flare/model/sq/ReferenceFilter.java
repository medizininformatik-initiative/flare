package de.medizininformatikinitiative.flare.model.sq;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.medizininformatikinitiative.flare.Either;
import de.medizininformatikinitiative.flare.Util;
import de.medizininformatikinitiative.flare.model.mapping.Mapping;
import de.medizininformatikinitiative.flare.model.mapping.MappingContext;
import de.medizininformatikinitiative.flare.model.sq.expanded.ExpandedCodeFilter;
import de.medizininformatikinitiative.flare.model.sq.expanded.ExpandedFilter;

import java.util.LinkedList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReferenceFilter(List<Criterion> criteria,
                              TermCode attributeCode) {

    public Either<Exception, List<Either<Exception, List<List<ExpandedFilter>>>>> expand(Mapping outerTermCodeMapping, MappingContext mappingContext) {
        return outerTermCodeMapping.findAttributeMapping(attributeCode).flatMap(attrMapping -> criteria.stream()
                .map(singleRefCrit -> singleRefCrit.expandConcept(mappingContext).map(termCodes -> termCodes.stream()
                        .map(critTermCode -> mappingContext.findMapping(critTermCode).flatMap(mapping ->
                                singleRefCrit.expandNonReferenceFilters(mappingContext.today(), mapping, attrMapping.searchParameter())
                                        .map(filters -> {
                                            ExpandedFilter termCodeFilter = new ExpandedCodeFilter(mapping.termCodeSearchParameter(),
                                                    critTermCode.code(), attrMapping.searchParameter());
                                            List<List<ExpandedFilter>> filterList = new LinkedList<>(filters);
                                            filterList.add(List.of(termCodeFilter));
                                            return filterList;
                                        }))).toList())
                ).reduce(Either.right(List.of()), Either.liftBinOp(Util::concat)));
    }

}
