package io.destinyos.engines.astrology;

/**
 * Greenwich and local sidereal time — the bridge between a birth instant and
 * which point of the ecliptic sits on the local meridian (RAMC), which every
 * chart-angle formula in {@link ChartAngles} is built from.
 *
 * <p>Source: Jean Meeus, <i>Astronomical Algorithms</i> (1998), equation
 * 12.4. Verified against the book's own worked example (retrieved via a
 * secondary citation of it, cross-checked against an independent GMST
 * reference page citing the IAU 1982 form of the same expression):
 * 1994 June 16 at 18ʰ UT gives GMST = 174.7711135° (= 11ʰ39ᵐ05.0672ˢ). See
 * {@code SiderealTimeTest} for the reproduction of that exact instant.
 */
final class SiderealTime {

    private SiderealTime() {
    }

    /**
     * Julian centuries from J2000.0 (JD 2451545.0), the time argument every
     * formula in this package is expressed in terms of.
     */
    static double julianCenturies(double julianDateUt) {
        return (julianDateUt - 2451545.0) / 36525.0;
    }

    /**
     * Greenwich Mean Sidereal Time at {@code julianDateUt}, in degrees,
     * normalized to [0, 360).
     */
    static double greenwichMeanSiderealTimeDegrees(double julianDateUt) {
        double t = julianCenturies(julianDateUt);
        double t2 = t * t;
        double t3 = t2 * t;
        double degrees = 280.46061837
                + 360.98564736629 * (julianDateUt - 2451545.0)
                + 0.000387933 * t2
                - t3 / 38710000.0;
        return normalizeDegrees(degrees);
    }

    /**
     * Local Sidereal Time — equivalently RAMC, the right ascension of the
     * meridian — in degrees, normalized to [0, 360).
     *
     * @param longitudeDegreesEast birth longitude, positive east (matches
     *                             {@code BaziInput.longitudeDegreesIfKnown}'s
     *                             convention throughout this project)
     */
    static double localSiderealTimeDegrees(double julianDateUt, double longitudeDegreesEast) {
        return normalizeDegrees(
                greenwichMeanSiderealTimeDegrees(julianDateUt) + longitudeDegreesEast);
    }

    private static double normalizeDegrees(double degrees) {
        double normalized = degrees % 360.0;
        return normalized < 0 ? normalized + 360.0 : normalized;
    }
}
