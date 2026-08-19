package io.destinyos.calendar;

/**
 * Time of the k-th new moon after the new moon of 1 January 1900, 13:52 UTC
 * (lunation number {@code k}, epoch and periodic terms as given by Meeus).
 *
 * <p>Source: Jean Meeus, <i>Astronomical Algorithms</i> (1998) — the
 * low-precision new-moon series (mean new moon plus an eleven-term periodic
 * correction from the sun's and moon's mean anomalies and the moon's
 * argument of latitude, with a small ΔT adjustment). Coefficients
 * cross-checked byte-for-byte against https://github.com/vanng822/amlich
 * (lib/amlich-aa98.js, function {@code NewMoon}) and Vietnamese Wikipedia's
 * "Mô đun:Âm lịch" Lua module (function of the same name) — both agree on
 * every constant below, and both implementations are themselves used only
 * as cross-check oracles (ADR D3), not copied: this is an independent
 * expression of the same published formula.
 */
public final class NewMoon {

    private static final double DEG_TO_RAD = Math.PI / 180.0;

    private NewMoon() {
    }

    /**
     * @param k lunation number relative to the mean new moon of
     *          1 January 1900 (negative for dates before that epoch)
     * @return Julian day number (with fractional time-of-day) of that new
     *         moon, in UTC
     */
    public static double julianDay(int k) {
        double t = k / 1236.85; // Julian centuries from 1900 January 0.5
        double t2 = t * t;
        double t3 = t2 * t;

        double meanNewMoon = 2415020.75933 + 29.53058868 * k + 0.0001178 * t2 - 0.000000155 * t3;
        meanNewMoon += 0.00033 * Math.sin(DEG_TO_RAD * (166.56 + 132.87 * t - 0.009173 * t2));

        double sunMeanAnomaly = 359.2242 + 29.10535608 * k - 0.0000333 * t2 - 0.00000347 * t3;
        double moonMeanAnomaly = 306.0253 + 385.81691806 * k + 0.0107306 * t2 + 0.00001236 * t3;
        double moonArgOfLatitude = 21.2964 + 390.67050646 * k - 0.0016528 * t2 - 0.00000239 * t3;

        double m = DEG_TO_RAD * sunMeanAnomaly;
        double mpr = DEG_TO_RAD * moonMeanAnomaly;
        double f = DEG_TO_RAD * moonArgOfLatitude;

        double c1 = (0.1734 - 0.000393 * t) * Math.sin(m) + 0.0021 * Math.sin(2 * m);
        c1 -= 0.4068 * Math.sin(mpr) - 0.0161 * Math.sin(2 * mpr);
        c1 -= 0.0004 * Math.sin(3 * mpr);
        c1 += 0.0104 * Math.sin(2 * f) - 0.0051 * Math.sin(m + mpr);
        c1 -= 0.0074 * Math.sin(m - mpr) - 0.0004 * Math.sin(2 * f + m);
        c1 -= 0.0004 * Math.sin(2 * f - m) + 0.0006 * Math.sin(2 * f + mpr);
        c1 += 0.0010 * Math.sin(2 * f - mpr) + 0.0005 * Math.sin(2 * mpr + m);

        double deltaT;
        if (t < -11) {
            deltaT = 0.001 + 0.000839 * t + 0.0002261 * t2 - 0.00000845 * t3 - 0.000000081 * t * t3;
        } else {
            deltaT = -0.000278 + 0.000265 * t + 0.000262 * t2;
        }

        return meanNewMoon + c1 - deltaT;
    }
}
