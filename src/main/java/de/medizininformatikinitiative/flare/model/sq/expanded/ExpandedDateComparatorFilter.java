package de.medizininformatikinitiative.flare.model.sq.expanded;

import de.medizininformatikinitiative.flare.model.fhir.QueryParams;
import de.medizininformatikinitiative.flare.model.sq.Comparator;

import java.time.LocalDate;

import static de.medizininformatikinitiative.flare.model.fhir.QueryParams.dateValue;
import static java.util.Objects.requireNonNull;

public record ExpandedDateComparatorFilter(String searchParameter, Comparator comparator, LocalDate value)
        implements ExpandedFilter {

    public ExpandedDateComparatorFilter {
        requireNonNull(searchParameter);
        requireNonNull(comparator);
        requireNonNull(value);
    }

    public static ExpandedDateComparatorFilter of(String referenceSearchParam, String searchParameter, Comparator comparator, LocalDate value) {
        return new ExpandedDateComparatorFilter(ExpandedFilter.combinedSearchParam(referenceSearchParam, searchParameter), comparator, value);
    }

    @Override
    public QueryParams toParams() {
        return QueryParams.EMPTY.appendParam(searchParameter, dateValue(comparator, value));
    }
}
