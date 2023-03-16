package de.medizininformatikinitiative.flare.model.sq.expanded;

import de.medizininformatikinitiative.flare.model.fhir.QueryParams;
import de.medizininformatikinitiative.flare.model.sq.Comparator;

import java.time.LocalDate;

import static java.util.Objects.requireNonNull;

public record ExpandedDateComparatorFilter(String searchParameter, Comparator comparator, LocalDate date)
        implements ExpandedFilter {

    public ExpandedDateComparatorFilter {
        requireNonNull(searchParameter);
        requireNonNull(comparator);
        requireNonNull(date);
    }

    @Override
    public QueryParams toParams() {
        return QueryParams.EMPTY.appendParam(searchParameter, comparator, date);
    }
}
