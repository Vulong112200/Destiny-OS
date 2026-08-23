package io.destinyos.engines.astrology;

/**
 * The tilt (ε) of Earth's equator relative to the ecliptic — the single angle
 * every conversion between ecliptic coordinates (zodiac longitude) and
 * equatorial coordinates (right ascension/declination, and so sidereal time)
 * depends on.
 *
 * <p>Source: Jean Meeus, <i>Astronomical Algorithms</i> (1998), equation
 * 22.2 (the low-accuracy series, valid to about 0.01 arcsecond over a few
 * centuries around J2000 — far more than this module needs, since a chart's
 * angles are insensitive to ε at the arcsecond level). Independently
 * corroborated: astrogreg.com's IAU-1982-sourced GMST reference page and
 * radixpro.com's own worked value both state
 * 23°26'21.448" = 23.4392911° for J2000.0, matching the constant term here.
 */
final class ObliquityOfEcliptic {

    private ObliquityOfEcliptic() {
    }

    /**
     * Mean obliquity of the ecliptic, in degrees.
     *
     * @param julianCenturiesFromJ2000 T = (JD − 2451545.0) / 36525
     */
    static double meanObliquityDegrees(double julianCenturiesFromJ2000) {
        double t = julianCenturiesFromJ2000;
        double t2 = t * t;
        double t3 = t2 * t;
        // 23°26'21.448" - 46.8150"T - 0.00059"T² + 0.001813"T³, arcseconds
        // converted to degrees (divide by 3600).
        return 23.4392911 - 0.0130042 * t - 0.00000016 * t2 + 0.000000504 * t3;
    }
}
