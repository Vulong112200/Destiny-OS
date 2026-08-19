package io.destinyos.calendar;

import io.destinyos.core.context.Uncertainty;
import io.destinyos.core.version.CalendarMethodologyRef;
import java.util.List;
import java.util.Objects;

/**
 * The full result of resolving one instant to the Vietnamese lunisolar
 * calendar and Can Chi pillars.
 *
 * <p>{@code lunarDate} and {@code yearPillar}/{@code monthPillar} are
 * {@code null} only when {@code methodology} could not resolve a historical
 * timezone rule at all (R14b gap) — in that case {@code uncertainties} names
 * why and nothing downstream may fabricate a fallback. {@code dayPillar}/
 * {@code hourPillar} are separately {@code null} whenever birth time
 * precision does not support hour-level results (Master Spec section 2:
 * never treat UNKNOWN as EXACT), independent of whether the timezone rule
 * itself resolved.
 */
public record CalendarResolution(
        LunarDate lunarDate,
        CanChiPillar yearPillar,
        CanChiPillar monthPillar,
        CanChiPillar dayPillar,
        CanChiPillar hourPillar,
        SolarTerm solarTermAtBirth,
        CalendarMethodologyRef methodology,
        List<Uncertainty> uncertainties
) {
    public CalendarResolution {
        Objects.requireNonNull(methodology, "methodology");
        uncertainties = uncertainties == null ? List.of() : List.copyOf(uncertainties);
    }

    public boolean isFullyResolved() {
        return lunarDate != null;
    }
}
