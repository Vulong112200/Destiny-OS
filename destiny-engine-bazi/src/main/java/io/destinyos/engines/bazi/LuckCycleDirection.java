package io.destinyos.engines.bazi;

import io.destinyos.calendar.HeavenlyStem;
import io.destinyos.calendar.YinYang;
import io.destinyos.core.context.Gender;
import java.util.Objects;

/**
 * Which way the Đại Vận (luck cycle) walks along the month-pillar sequence.
 *
 * <p><strong>The rule</strong> (research item R2, closed 2026-08-22): a male
 * born in a yang-stem year, or a female born in a yin-stem year, counts
 * forward; the other two combinations count backward. Stated identically by
 * every source checked — Vietnamese, Chinese and English alike — with no
 * variant found, and confirmed end-to-end by two published worked examples
 * running in opposite directions (see {@code LuckCycleResolverTest}).
 *
 * <p>Note the rule keys on the <em>stem</em> of the year pillar, not on the
 * branch and not on the Gregorian year's parity. Under the Lập Xuân boundary
 * this project uses (R18), a birth in January can carry the previous year's
 * stem, which flips the direction — so this must be handed the Bát Tự year
 * pillar's stem, never a stem derived from the calendar year.
 */
public enum LuckCycleDirection {

    /** Thuận — forward through the sexagenary cycle from the month pillar. */
    THUAN,

    /** Nghịch — backward through the sexagenary cycle from the month pillar. */
    NGHICH;

    /**
     * @param yearStem the <em>Bát Tự</em> year pillar's stem (Lập Xuân-based)
     * @param gender   required; there is no defensible default (see
     *                 {@link Gender})
     */
    public static LuckCycleDirection forBirth(HeavenlyStem yearStem, Gender gender) {
        Objects.requireNonNull(yearStem, "yearStem");
        Objects.requireNonNull(gender, "gender");
        boolean yangYear = yearStem.polarity() == YinYang.YANG;
        boolean male = gender == Gender.MALE;
        return yangYear == male ? THUAN : NGHICH;
    }

    /** {@code +1} forward, {@code -1} backward — the step along the cycle. */
    public int step() {
        return this == THUAN ? 1 : -1;
    }
}
