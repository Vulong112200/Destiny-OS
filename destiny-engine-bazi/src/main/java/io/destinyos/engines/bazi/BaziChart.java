package io.destinyos.engines.bazi;

import io.destinyos.calendar.EarthlyBranch;
import io.destinyos.calendar.HeavenlyStem;
import io.destinyos.calendar.SolarTerm;
import io.destinyos.core.context.Uncertainty;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A constructed Tứ Trụ chart — the hard data of Bát Tự, with the parts that
 * need unresolved research named as blocked rather than absent.
 *
 * <p>Year and month pillars are always present when this record exists at
 * all. Day and hour pillars are {@code null} together whenever birth time
 * precision does not support them, and with them the Day Master; see
 * {@link BaziPillar} for why Thập Thần then disappears too.
 *
 * @param yearPillar        Trụ Năm, under {@code yearBoundary}
 * @param monthPillar       Trụ Tháng, from the solar term at birth
 * @param dayPillar         Trụ Ngày, or {@code null} without an exact hour
 * @param hourPillar        Trụ Giờ, or {@code null} without an exact hour
 * @param yearBoundary      which convention decided the year pillar (R18)
 * @param baziYear          the Lập Xuân-based year number the year pillar was
 *                          derived from — often one less than the Gregorian
 *                          year for January and early-February births, which
 *                          is exactly the fact users find surprising
 * @param solarTermAtBirth  the Tiết Khí containing the birth instant
 * @param solarMonthIndex   1-12 with Dần = 1, the solar-term month the month
 *                          pillar was derived from (not a lunar month number)
 * @param localSolarDateTime the local time actually used, after the R10 solar
 *                          correction when longitude was known
 * @param elementTally      integer element counts, in three ungrouped tallies
 * @param blockedSections   the reading sections this engine refuses to
 *                          approximate (R1, R2, R3)
 * @param uncertainties     conditions that must reach the user (ADR D3)
 */
public record BaziChart(
        BaziPillar yearPillar,
        BaziPillar monthPillar,
        BaziPillar dayPillar,
        BaziPillar hourPillar,
        BaziYearBoundary yearBoundary,
        int baziYear,
        SolarTerm solarTermAtBirth,
        int solarMonthIndex,
        LocalDateTime localSolarDateTime,
        ElementTally elementTally,
        List<BlockedSection> blockedSections,
        List<Uncertainty> uncertainties
) {
    public BaziChart {
        Objects.requireNonNull(yearPillar, "yearPillar");
        Objects.requireNonNull(monthPillar, "monthPillar");
        Objects.requireNonNull(yearBoundary, "yearBoundary");
        Objects.requireNonNull(solarTermAtBirth, "solarTermAtBirth");
        Objects.requireNonNull(elementTally, "elementTally");
        if ((dayPillar == null) != (hourPillar == null)) {
            // The two stand or fall together: the hour stem is derived from
            // the day stem (Ngu Thu Don), so an hour pillar without a day
            // pillar is not a partial result, it is an impossible one.
            throw new IllegalArgumentException(
                    "Day and hour pillars must both be present or both absent.");
        }
        blockedSections = blockedSections == null ? List.of() : List.copyOf(blockedSections);
        uncertainties = uncertainties == null ? List.of() : List.copyOf(uncertainties);
    }

    /** The Nhật Chủ — the day pillar's stem. Empty without an exact birth hour. */
    public Optional<HeavenlyStem> dayMaster() {
        return dayPillar == null ? Optional.empty() : Optional.of(dayPillar.stem());
    }

    /** All present pillars, in Year-Month-Day-Hour order. */
    public List<BaziPillar> pillars() {
        return dayPillar == null
                ? List.of(yearPillar, monthPillar)
                : List.of(yearPillar, monthPillar, dayPillar, hourPillar);
    }

    /** Whether the day and hour pillars — and so the Day Master — are present. */
    public boolean hasHourPrecision() {
        return dayPillar != null;
    }

    /** The month pillar's branch, i.e. which solar-term month this is. */
    public EarthlyBranch solarMonthBranch() {
        return monthPillar.branch();
    }
}
