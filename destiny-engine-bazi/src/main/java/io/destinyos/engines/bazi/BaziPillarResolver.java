package io.destinyos.engines.bazi;

import io.destinyos.calendar.CanChi;
import io.destinyos.calendar.CanChiPillar;
import io.destinyos.calendar.EarthlyBranch;
import io.destinyos.calendar.HourBranchResolver;
import io.destinyos.calendar.JulianDay;
import io.destinyos.calendar.SolarYear;
import io.destinyos.calendar.ZiHourBoundaryPolicy;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Turns one local instant into the four Bát Tự pillars under the Tử Bình
 * convention.
 *
 * <p><strong>What is new here, and what is reused.</strong> The sexagenary
 * arithmetic itself is not reimplemented: {@link CanChi} already carries the
 * Ngũ Hổ Độn (month stem from year stem) and Ngũ Thử Độn (hour stem from day
 * stem) rules, golden-tested in {@code destiny-calendar}. What Bát Tự adds is
 * only <em>which year and which month number to feed them</em>:
 *
 * <ul>
 *   <li>the Vietnamese lunisolar calendar's year advances at Tết and its
 *       months are lunar months;</li>
 *   <li>Bát Tự's year advances at Lập Xuân and its months are solar-term
 *       months (research item R18 records that this differs, and that the
 *       other convention exists).</li>
 * </ul>
 *
 * <p>The two happen to share a numbering that makes the reuse exact: lunar
 * month 1 and solar month 1 both carry the branch Dần, so
 * {@link CanChi#monthPillar} applied to a solar-term month index gives the
 * correct Bát Tự month pillar. Verified against published tables — see
 * {@code BaziEngineGoldenTest}.
 *
 * <p>The Lập Xuân year/month arithmetic itself moved to
 * {@link SolarYear} once Bát Trạch needed the same answer: two engines may not
 * depend on each other, so shared derivations belong in shared infrastructure.
 */
final class BaziPillarResolver {

    private BaziPillarResolver() {
    }

    /**
     * 1-12 with Dần = 1 — the solar-term month the month pillar is built from.
     *
     * <p>Delegates to {@link SolarYear}, which is where this arithmetic lives
     * now that a second methodology (Bát Trạch's Kua number, R7) needs the same
     * answer. Kept as a named method here rather than inlined at the call site
     * so the Bát Tự-specific meaning stays visible: what {@code destiny-calendar}
     * computes is "which 30-degree solar sector", and what Bát Tự decides is
     * that this sector is its month.
     */
    static int solarMonthIndex(double julianDateUt) {
        return SolarYear.solarMonthIndex(julianDateUt);
    }

    /**
     * The year number whose Can Chi is the Bát Tự year pillar, under
     * {@link BaziYearBoundary#LAP_XUAN}.
     *
     * <p>The Lập Xuân boundary arithmetic — and the derivation of why the
     * condition is exact rather than approximate — is documented on
     * {@link SolarYear#lapXuanBasedYear}. Choosing that boundary over Tết is
     * this engine's own declared decision (R18); computing it is not.
     */
    static int baziYear(LocalDate localDate, int solarMonthIndex) {
        return SolarYear.lapXuanBasedYear(localDate, solarMonthIndex);
    }

    static CanChiPillar yearPillar(int baziYear) {
        return CanChi.yearPillar(baziYear);
    }

    /**
     * Month pillar: branch from the solar-term month, stem from the Bát Tự
     * year stem via Ngũ Hổ Độn. Note the year fed in here must be the
     * Lập Xuân-based one — a January birth taking its month stem from the
     * Gregorian year's stem is off by a full sexagenary step, and published
     * tables disagree with it (1 January 2000's month pillar is Bính Tý,
     * which is what 1999's Kỷ year stem yields, not what 2000's Canh yields).
     */
    static CanChiPillar monthPillar(int baziYear, int solarMonthIndex) {
        return CanChi.monthPillar(baziYear, solarMonthIndex);
    }

    /**
     * Day pillar, applying R10's 23:00 Giờ Tý rollover: a birth from 23:00
     * onwards belongs to the next date's day pillar.
     */
    static CanChiPillar dayPillar(LocalDateTime localSolar, double utcOffsetHours) {
        boolean rollsOver = HourBranchResolver.rollsOverToNextDay(
                localSolar.toLocalTime(), ZiHourBoundaryPolicy.ZI_HOUR_23_00);
        LocalDate pillarDate = rollsOver
                ? localSolar.toLocalDate().plusDays(1)
                : localSolar.toLocalDate();
        long dayNumber = JulianDay.fromDate(pillarDate.getDayOfMonth(),
                pillarDate.getMonthValue(), pillarDate.getYear());
        return CanChi.dayPillar(dayNumber, utcOffsetHours);
    }

    static CanChiPillar hourPillar(CanChiPillar dayPillar, LocalDateTime localSolar) {
        EarthlyBranch hourBranch = HourBranchResolver.branchAt(
                localSolar.toLocalTime(), ZiHourBoundaryPolicy.ZI_HOUR_23_00);
        return CanChi.hourPillar(dayPillar.stem(), hourBranch);
    }
}
