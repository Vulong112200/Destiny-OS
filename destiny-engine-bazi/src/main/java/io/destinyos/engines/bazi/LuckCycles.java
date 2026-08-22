package io.destinyos.engines.bazi;

import io.destinyos.calendar.SolarTerm;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Objects;

/**
 * The Đại Vận sequence: a direction, a start age, and the ten-year periods
 * that follow (research item R2).
 *
 * <p><strong>What this is and is not.</strong> It is chart construction, in
 * the same sense the Tứ Trụ is: a deterministic sequence derived from the
 * birth instant by a sourced rule. It is <em>not</em> a reading. Whether any
 * period is favourable needs R1 and R3, which remain open, so nothing here
 * carries a polarity and {@code BaziEngine} still emits no signals.
 *
 * <p><strong>Why the intermediate quantities are kept.</strong>
 * {@code distanceToBoundary} and {@code boundaryTerm} are the whole derivation
 * of the start age; publishing only the answer would leave a user unable to
 * check it against any Bát Tự text, all of which state the day count. It is
 * also the quantity a reader can most easily verify by hand.
 *
 * @param direction          forward or backward along the month-pillar sequence
 * @param boundaryTerm       the Tiết the count ran to
 * @param boundaryInstant    when that Tiết occurs, in the same local solar time
 *                           as the rest of the chart
 * @param distanceToBoundary birth to that Tiết. A {@link Duration} rather than
 *                           a fractional day count because ADR D6 keeps
 *                           floating point out of the domain — and because the
 *                           conversion below is exact in integer seconds
 * @param startAge           age at which the first period begins
 * @param startDate          calendar date the first period begins
 * @param pillars            the periods, in order, first one first
 */
public record LuckCycles(
        LuckCycleDirection direction,
        SolarTerm boundaryTerm,
        LocalDateTime boundaryInstant,
        Duration distanceToBoundary,
        Period startAge,
        java.time.LocalDate startDate,
        List<LuckPillar> pillars
) {
    /**
     * How many ten-year periods are produced.
     *
     * <p>Eight covers roughly eighty years from the start age, which is as far
     * as published tables normally run. Not a claim about lifespan — just
     * where the sequence is conventionally truncated.
     */
    public static final int PERIOD_COUNT = 8;

    /** Years each period spans. Unanimous across every source checked. */
    public static final int YEARS_PER_PERIOD = 10;

    public LuckCycles {
        Objects.requireNonNull(direction, "direction");
        Objects.requireNonNull(boundaryTerm, "boundaryTerm");
        Objects.requireNonNull(boundaryInstant, "boundaryInstant");
        Objects.requireNonNull(distanceToBoundary, "distanceToBoundary");
        Objects.requireNonNull(startAge, "startAge");
        Objects.requireNonNull(startDate, "startDate");
        pillars = pillars == null ? List.of() : List.copyOf(pillars);
    }
}
