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
