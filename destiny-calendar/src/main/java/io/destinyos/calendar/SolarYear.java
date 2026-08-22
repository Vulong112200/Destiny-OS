package io.destinyos.calendar;

import java.time.LocalDate;
import java.util.Objects;

/**
 * The solar year and solar-term month, both measured from Lập Xuân.
 *
 * <p>Distinct from {@link LunarCalendar}'s year, which turns at Tết. Several
 * Eastern methodologies index their year on Lập Xuân instead — Bát Tự does
 * (research item R18) and Bát Trạch's Kua number does under classical practice
 * (R7) — so this arithmetic lives here, in shared calendar infrastructure,
 * rather than inside whichever engine needed it first.
 *
 * <p><strong>Why it moved.</strong> It was originally package-private inside
 * {@code destiny-engine-bazi}. The moment a second methodology needed the same
 * answer, leaving it there would have forced one engine to depend on another,
 * which {@code ArchitectureRulesTest.enginesStayIndependent} forbids outright —
 * and rightly, since two engines sharing a derivation are not two independent
 * sources. Choosing Lập Xuân over Tết remains each engine's own declared
 * decision; computing it is not.
 */
public final class SolarYear {

    /** Solar longitude, in degrees, of Lập Xuân — the start of solar month 1 (Dần). */
    private static final double LAP_XUAN_DEGREES = 315.0;

    /** Each solar-term month spans two of the 24 terms, so 30 degrees. */
    private static final double DEGREES_PER_SOLAR_MONTH = 30.0;

    /**
     * Days to search either side of an instant for its month boundary. One
     * solar month is about 30.4 days, so 32 brackets it with margin while
     * staying far short of the ±180° at which the signed gap wraps.
     */
    private static final double SEARCH_WINDOW_DAYS = 32.0;

    /**
     * Bisection steps. 32 days halved 60 times is far below any precision the
     * underlying solar series claims (R19), so this converges to the limit of
     * {@code double} rather than to a chosen tolerance — and a fixed step
     * count keeps the result reproducible (Master Spec §25).
     */
    private static final int BISECTION_STEPS = 60;

    private SolarYear() {
    }

    /**
     * 1-12 with Dần = 1, from the sun's ecliptic longitude at an exact instant.
     * Month 1 (Dần) starts at Lập Xuân (315°), month 2 (Mão) at Kinh Trập
     * (345°), and so on every 30°.
     *
     * @param julianDateUt 0h-UT-referenced Julian date, from
     *                     {@link JulianDay#fromLocalDateTime}
     */
    public static int solarMonthIndex(double julianDateUt) {
        double longitude = SolarTerm.solarLongitudeDegreesAtJulianDate(julianDateUt);
        double fromLapXuan = ((longitude - LAP_XUAN_DEGREES) % 360.0 + 360.0) % 360.0;
        return (int) Math.floor(fromLapXuan / DEGREES_PER_SOLAR_MONTH) + 1;
    }

    /**
     * Julian date of the Tiết that <em>opens</em> the solar month containing
     * this instant — i.e. the moment the current month pillar began.
     *
     * <p><strong>Why this is the twelve "Tiết" and not all twenty-four terms.</strong>
     * The boundaries returned here are exactly {@code 315° + 30k}: Lập Xuân,
     * Kinh Trập, Thanh Minh, Lập Hạ, Mang Chủng, Tiểu Thử, Lập Thu, Bạch Lộ,
     * Hàn Lộ, Lập Đông, Đại Tuyết, Tiểu Hàn. Those are the twelve sectional
     * terms (節); the twelve intervening principal terms (中氣, flagged on
     * {@link SolarTerm}) fall mid-month and never move a month pillar. This is
     * not a convention chosen here — it follows from {@link #solarMonthIndex}
     * stepping every 30°, and Bát Tự sources state the same restriction
     * explicitly ("推算大運要以節來推算，不能用氣來推算").
     *
     * <p><strong>Precision.</strong> Root-found from {@link SolarPosition},
     * so this inherits that series' limit exactly — research item R19 measures
     * it at roughly 7 to 16 minutes against published tables. Callers whose
     * answer flips on which side of the boundary an instant falls must guard
     * that window rather than trust this to the minute.
     *
     * @param julianDateUt 0h-UT-referenced Julian date, from
     *                     {@link JulianDay#fromLocalDateTime}
     */
    public static double solarMonthStartJulianDate(double julianDateUt) {
        return boundaryInstant(currentBoundaryDegrees(julianDateUt),
                julianDateUt - SEARCH_WINDOW_DAYS, julianDateUt);
    }

    /**
     * Julian date of the Tiết that opens the <em>next</em> solar month — the
     * moment the current month pillar ends.
     *
     * <p>See {@link #solarMonthStartJulianDate} for which twelve instants
     * these are and for the precision caveat.
     */
    public static double nextSolarMonthStartJulianDate(double julianDateUt) {
        double next = (currentBoundaryDegrees(julianDateUt) + DEGREES_PER_SOLAR_MONTH) % 360.0;
        return boundaryInstant(next, julianDateUt, julianDateUt + SEARCH_WINDOW_DAYS);
    }

    /**
     * Solar longitude, in degrees, of the boundary that opened the solar month
     * containing this instant.
     */
    private static double currentBoundaryDegrees(double julianDateUt) {
        double longitude = SolarTerm.solarLongitudeDegreesAtJulianDate(julianDateUt);
        double fromLapXuan = ((longitude - LAP_XUAN_DEGREES) % 360.0 + 360.0) % 360.0;
        double stepsPast = Math.floor(fromLapXuan / DEGREES_PER_SOLAR_MONTH);
        return (LAP_XUAN_DEGREES + stepsPast * DEGREES_PER_SOLAR_MONTH) % 360.0;
    }

    /**
     * Bisects for the instant the sun's longitude reaches {@code targetDegrees}.
     *
     * <p>Bisection rather than a closed form because the longitude series is
     * not analytically invertible; it is also how the deviation in R19 was
     * measured, so the two agree by construction. {@code signedGapDegrees} is
     * monotonically increasing across a window this short (the sun never
     * retrogrades, and 32 days spans only about 31.5°, far from the ±180° at
     * which the normalisation wraps), so the bracket is guaranteed valid.
     */
    private static double boundaryInstant(double targetDegrees, double low, double high) {
        for (int i = 0; i < BISECTION_STEPS; i++) {
            double mid = (low + high) / 2.0;
            if (signedGapDegrees(targetDegrees, mid) < 0) {
                low = mid;
            } else {
                high = mid;
            }
        }
        return (low + high) / 2.0;
    }

    /** How far past {@code targetDegrees} the sun is, normalised to (-180, 180]. */
    private static double signedGapDegrees(double targetDegrees, double julianDateUt) {
        double longitude = SolarTerm.solarLongitudeDegreesAtJulianDate(julianDateUt);
        double gap = ((longitude - targetDegrees) % 360.0 + 360.0) % 360.0;
        return gap > 180.0 ? gap - 360.0 : gap;
    }

    /**
     * The year number whose boundary is Lập Xuân rather than 1 January.
     *
     * <p>Derivation, rather than a magic condition. The Lập Xuân year coincides
     * with the Gregorian year except for instants falling before Lập Xuân —
     * which is to say, in solar months 11 (Tý) or 12 (Sửu) <em>and</em> in a
     * January or February of the calendar. Those two solar months are the only
     * ones that can straddle 1 January (Tý runs ~7 Dec to ~5 Jan, Sửu ~5 Jan to
     * ~4 Feb), and solar months 1-10 never occur in January or February at all,
     * so the pair of conditions is exact rather than approximate.
     *
     * <p>Worked check against published tables: 1 January 2000 is in solar month
     * 11 and in January, so its Bát Tự year pillar is 1999's Kỷ Mão, not 2000's
     * Canh Thìn — which is what published Four Pillars tables give. 20 December
     * 2024 is also in solar month 11 but in December, so it keeps 2024.
     *
     * @param localDate       the local date at the birth location
     * @param solarMonthIndex from {@link #solarMonthIndex}, for the same instant
     */
    public static int lapXuanBasedYear(LocalDate localDate, int solarMonthIndex) {
        Objects.requireNonNull(localDate, "localDate");
        boolean beforeLapXuan = solarMonthIndex >= 11 && localDate.getMonthValue() <= 2;
        return beforeLapXuan ? localDate.getYear() - 1 : localDate.getYear();
    }
}
