package de.medizininformatikinitiative.flare.service;

import de.medizininformatikinitiative.flare.model.Query;
import de.medizininformatikinitiative.flare.model.mapping.MappingContext;
import de.medizininformatikinitiative.flare.model.sq.Criterion;
import de.medizininformatikinitiative.flare.model.sq.expanded.ExpandedCriterion;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Objects;

@Component
public class Translator {

    private final MappingContext mappingContext;

    public Translator(MappingContext mappingContext) {
        this.mappingContext = Objects.requireNonNull(mappingContext);
    }

    public Flux<Query> toQuery(Criterion criterion) {
        return criterion.expand(mappingContext).map(ExpandedCriterion::toQuery);
    }
}
