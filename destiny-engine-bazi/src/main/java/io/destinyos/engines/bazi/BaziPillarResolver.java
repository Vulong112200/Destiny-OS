package io.destinyos.engines.bazi;

import io.destinyos.calendar.CanChi;
import io.destinyos.calendar.CanChiPillar;
import io.destinyos.calendar.EarthlyBranch;
import io.destinyos.calendar.HourBranchResolver;
import io.destinyos.calendar.JulianDay;
import io.destinyos.calendar.SolarTerm;
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
 */
final class BaziPillarResolver {

    /** Solar longitude, in degrees, of Lập Xuân — the start of solar month 1 (Dần). */
    private static final double LAP_XUAN_DEGREES = 315.0;

    /** Each solar-term month spans two of the 24 terms, so 30 degrees. */
    private static final double DEGREES_PER_SOLAR_MONTH = 30.0;

    private BaziPillarResolver() {
    }

    /**
     * 1-12 with Dần = 1, from the sun's ecliptic longitude at the birth
     * instant. Month 1 (Dần) starts at Lập Xuân (315°), month 2 (Mão) at
     * Kinh Trập (345°), and so on every 30°.
     */
    static int solarMonthIndex(double julianDateUt) {
        double longitude = SolarTerm.solarLongitudeDegreesAtJulianDate(julianDateUt);
        double fromLapXuan = ((longitude - LAP_XUAN_DEGREES) % 360.0 + 360.0) % 360.0;
        return (int) Math.floor(fromLapXuan / DEGREES_PER_SOLAR_MONTH) + 1;
    }

    /**
     * The year number whose Can Chi is the Bát Tự year pillar, under
     * {@link BaziYearBoundary#LAP_XUAN}.
     *
     * <p>Derivation, rather than a magic condition. The Bát Tự year runs from
     * one Lập Xuân to the next, so it coincides with the Gregorian year
     * except for instants that fall before Lập Xuân — which is to say, in
     * solar months 11 (Tý) or 12 (Sửu) <em>and</em> in a January or February
     * of the calendar. Those two solar months are the only ones that can
     * straddle 1 January (Tý runs ~7 Dec to ~5 Jan, Sửu ~5 Jan to ~4 Feb),
     * and solar months 1-10 never occur in January or February at all, so the
     * pair of conditions is exact rather than approximate.
     *
     * <p>Worked check against published tables: 1 January 2000 is in solar
     * month 11 and in January, so the year pillar is 1999's Kỷ Mão, not
     * 2000's Canh Thìn — which is what published Four Pillars tables give.
     * 20 December 2024 is also in solar month 11 but in December, so it keeps
     * 2024's Giáp Thìn.
     */
    static int baziYear(LocalDate localDate, int solarMonthIndex) {
        boolean beforeLapXuan = solarMonthIndex >= 11 && localDate.getMonthValue() <= 2;
        return beforeLapXuan ? localDate.getYear() - 1 : localDate.getYear();
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
