package io.destinyos.engines.bazi;

import io.destinyos.calendar.EarthlyBranch;
import io.destinyos.calendar.HeavenlyStem;
import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

/**
 * One ten-year Đại Vận period.
 *
 * <p>Carries <em>when</em> it runs and <em>which</em> pillar governs it, and
 * nothing about whether it is fortunate. That judgement needs the Dụng Thần
 * (R1) and the Day Master's strength (R3), both still unresolved — so this
 * record deliberately has no place to put one.
 *
 * @param ordinal   1-based position in the sequence
 * @param stem      Thiên Can of this period
 * @param branch    Địa Chi of this period
 * @param startAge  age when the period begins, as years/months/days rather
 *                  than a single number — the tradition's own conversion
 *                  produces all three, and collapsing them to a whole number
 *                  is a presentation choice, not the calculation
 * @param startDate calendar date the period begins
 */
public record LuckPillar(
        int ordinal,
        HeavenlyStem stem,
        EarthlyBranch branch,
        Period startAge,
        LocalDate startDate
) {
    public LuckPillar {
        Objects.requireNonNull(stem, "stem");
        Objects.requireNonNull(branch, "branch");
        Objects.requireNonNull(startAge, "startAge");
        Objects.requireNonNull(startDate, "startDate");
        if (ordinal < 1) {
            throw new IllegalArgumentException("ordinal is 1-based, got " + ordinal);
        }
    }

    /** Age in whole years when this period begins — for display only. */
    public int startAgeYears() {
        return startAge.getYears();
    }
}
