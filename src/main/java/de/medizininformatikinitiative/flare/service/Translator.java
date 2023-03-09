package de.medizininformatikinitiative.flare.service;

import de.medizininformatikinitiative.flare.model.Query;
import de.numcodex.sq2cql.model.structured_query.Criterion;
import org.springframework.stereotype.Component;

@Component
public class Translator {

    public Query translate(Criterion criterion) {
        return Query.ofType("Observation");
    }
}
