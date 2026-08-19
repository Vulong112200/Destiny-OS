package io.destinyos.calendar;

/**
 * The sun's apparent ecliptic longitude — the shared foundation for solar
 * terms (Tiết Khí, {@link SolarTerm}) and for the leap-month rule
 * ({@link LunarCalendar}); R9 and R16's research notes both point out this
 * is the same function, not two separate algorithms.
 *
 * <p>Source: Jean Meeus, <i>Astronomical Algorithms</i> (1998) — the
 * low-precision solar coordinate series (mean anomaly, mean longitude, a
 * three-term periodic correction; accurate to about 0.01 degree, which is
 * the precision every reference implementation checked here also uses).
 * Coefficients cross-checked byte-for-byte against two independent
 * implementations: https://github.com/vanng822/amlich
 * (lib/amlich-aa98.js, {@code SunLongitude}) and Vietnamese Wikipedia's
 * "Mô đun:Âm lịch" Lua module (function of the same name) — both agree on
 * every constant below.
 */
public final class SolarPosition {

    private static final double TWO_PI = 2 * Math.PI;
    private static final double DEG_TO_RAD = Math.PI / 180.0;

    private SolarPosition() {
    }

    /**
     * Apparent ecliptic longitude of the sun, in radians, normalized to
     * {@code [0, 2*PI)}.
     *
     * @param jd Julian day number (may include a fractional time-of-day)
     */
    public static double longitudeRadians(double jd) {
        double t = (jd - 2451545.0) / 36525.0; // Julian centuries from J2000.0
        double t2 = t * t;
        double meanAnomaly = 357.52910 + 35999.05030 * t - 0.0001559 * t2 - 0.00000048 * t * t2;
        double meanLongitude = 280.46645 + 36000.76983 * t + 0.0003032 * t2;
        double dl = (1.914600 - 0.004817 * t - 0.000014 * t2) * Math.sin(DEG_TO_RAD * meanAnomaly);
        dl += (0.019993 - 0.000101 * t) * Math.sin(DEG_TO_RAD * 2 * meanAnomaly);
        dl += 0.000290 * Math.sin(DEG_TO_RAD * 3 * meanAnomaly);
        double trueLongitudeDegrees = meanLongitude + dl;
        double radians = trueLongitudeDegrees * DEG_TO_RAD;
        radians = radians - TWO_PI * Math.floor(radians / TWO_PI);
        return radians;
    }
}
