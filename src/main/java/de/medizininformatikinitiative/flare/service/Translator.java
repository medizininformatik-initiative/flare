package de.medizininformatikinitiative.flare.service;

import de.medizininformatikinitiative.flare.model.Query;
import de.medizininformatikinitiative.flare.model.mapping.MappingContext;
import de.medizininformatikinitiative.flare.model.sq.Criterion;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class Translator {

    private final MappingContext mappingContext;

    public Translator(MappingContext mappingContext) {
        this.mappingContext = Objects.requireNonNull(mappingContext);
    }

    public List<Query> toQuery(Criterion criterion) {
        return criterion.toQuery(mappingContext);
    }
}
