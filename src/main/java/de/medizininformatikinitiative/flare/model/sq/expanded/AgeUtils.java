package de.medizininformatikinitiative.flare.model.sq.expanded;

import de.medizininformatikinitiative.flare.model.sq.CalculationException;
import de.medizininformatikinitiative.flare.model.sq.Comparator;
import de.medizininformatikinitiative.flare.model.sq.TermCode;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class AgeUtils {

    /**
     * Calculates the birth date of a patient from the given age in the given unit.
     *
     * @param today the day to base the calculation on
     * @param age   the value of the age
     * @param unit  the unit of the age value
     * @return a {@link Mono} containing either the birth date or an {@link CalculationException} if the unit is unknown
     */
    private static Mono<LocalDate> birthDate(LocalDate today, BigDecimal age, TermCode unit) {
        return switch (unit.code()) {
            case "a" -> Mono.just(today.minusYears(age.intValue()));
            case "mo" -> Mono.just(today.minusMonths(age.intValue()));
            case "wk" -> Mono.just(today.minusWeeks(age.intValue()));
            default -> Mono.error(new CalculationException("Unknown age unit `%s`.".formatted(unit.code())));
        };
    }

    private static Mono<LocalDate> equalCaseLowerDate(LocalDate today, BigDecimal age, TermCode unit) {
        return birthDate(today, age.add(BigDecimal.ONE), unit).map(date -> date.plusDays(1));
    }

    private static Mono<LocalDate> equalCaseUpperDate(LocalDate today, BigDecimal age, TermCode unit) {
        return birthDate(today, age, unit);
    }

    public static Mono<List<ExpandedFilter>> expandedAgeFilterFromComparator(LocalDate today, Comparator comparator,
                                                                             BigDecimal age, TermCode unit) {
        if (comparator.equals(Comparator.EQUAL)) {
            return equalCaseLowerDate(today, age, unit)
                    .flatMap(lowerBound -> equalCaseUpperDate(today, age, unit)
                            .map(upperBound -> List.of(new ExpandedDateRangeFilter("birthdate", lowerBound, upperBound))));
        } else {
            return birthDate(today, age, unit).map(birthDate ->
                    List.of(new ExpandedDateComparatorFilter("birthdate", comparator.reverse(), birthDate)));
        }
    }

    public static Mono<List<ExpandedFilter>> expandedAgeFilterFromRange(LocalDate today, BigDecimal ageLowerBound,
                                                                        BigDecimal ageUpperBound, TermCode unit) {
        return birthDate(today, ageLowerBound, unit).flatMap(upperBoundDate -> birthDate(today, ageUpperBound, unit)
                .map(lowerBoundDate -> List.of(new ExpandedDateRangeFilter("birthdate", lowerBoundDate, upperBoundDate))));
    }
}
