package io.destinyos.calendar;

/**
 * Julian day number conversions (noon-referenced, proleptic Gregorian with
 * the historical Julian/Gregorian cutover at 15 October 1582).
 *
 * <p>Formula: Jean Meeus, <i>Astronomical Algorithms</i> (1998), chapter 7
 * (equivalent to the widely-republished Fliegel &amp; Van Flandern form
 * documented at http://www.tondering.dk/claus/calendar.html). Cross-checked
 * against two independent, long-standing implementations of the Vietnamese
 * lunar calendar algorithm that both build on this exact formula: the
 * JavaScript port at https://github.com/vanng822/amlich
 * (lib/amlich-aa98.js, functions {@code jdFromDate}/{@code jdToDate}) and
 * Vietnamese Wikipedia's "Mô đun:Âm lịch" Lua module ({@code UniversalToJD}/
 * {@code UniversalFromJD}, an equivalent but differently-derived expression
 * of the same Meeus algorithm). Every date exercised in this module's test
 * suite agrees with both.
 */
public final class JulianDay {

    private JulianDay() {
    }

    private static long floor(double d) {
        return (long) Math.floor(d);
    }

    /** Julian day number for a proleptic-Gregorian-or-Julian calendar date. */
    public static long fromDate(int day, int month, int year) {
        long a = floor((14 - month) / 12.0);
        long y = year + 4800 - a;
        long m = month + 12 * a - 3;
        long jd = day + floor((153 * m + 2) / 5.0) + 365 * y + floor(y / 4.0)
                - floor(y / 100.0) + floor(y / 400.0) - 32045;
        if (jd < 2299161) {
            // Before 15 October 1582: still-proleptic Julian calendar, no
            // century leap-year correction.
            jd = day + floor((153 * m + 2) / 5.0) + 365 * y + floor(y / 4.0) - 32083;
        }
        return jd;
    }

    /**
     * The 0h-UT-referenced Julian date (fractional) for one local wall-clock
     * instant — the input every function in {@link SolarPosition},
     * {@link SolarTerm} and {@link NewMoon} is defined against.
     *
     * <p>Exists because this conversion is the single most error-prone step in
     * the module and was previously open-coded at three call sites, each
     * repeating the {@code -0.5} noon-to-midnight shift and the
     * {@code -offset/24} local-to-UT shift. {@link SolarTerm#termIndexAt}'s
     * local-midnight form is exactly this method with a zero time-of-day, and
     * a test asserts that equivalence so the two can never drift apart.
     *
     * @param local           local wall-clock date and time
     * @param utcOffsetHours  the civil UTC offset in force there (R14a)
     */
    public static double fromLocalDateTime(java.time.LocalDateTime local, double utcOffsetHours) {
        java.util.Objects.requireNonNull(local, "local");
        long dayNumber = fromDate(local.getDayOfMonth(), local.getMonthValue(), local.getYear());
        double dayFraction = local.toLocalTime().toNanoOfDay() / 86_400_000_000_000.0;
        return dayNumber - 0.5 + dayFraction - utcOffsetHours / 24.0;
    }

    /** Inverse of {@link #fromDate}: day/month/year for a Julian day number. */
    public static int[] toDate(long jd) {
        long a;
        long b;
        long c;
        if (jd > 2299160) {
            a = jd + 32044;
            b = Math.floorDiv(4 * a + 3, 146097);
            c = a - Math.floorDiv(b * 146097, 4);
        } else {
            b = 0;
            c = jd + 32082;
        }
        long d = Math.floorDiv(4 * c + 3, 1461);
        long e = c - Math.floorDiv(1461 * d, 4);
        long m = Math.floorDiv(5 * e + 2, 153);
        int day = (int) (e - Math.floorDiv(153 * m + 2, 5) + 1);
        int month = (int) (m + 3 - 12 * Math.floorDiv(m, 10));
        int year = (int) (b * 100 + d - 4800 + Math.floorDiv(m, 10));
        return new int[] {day, month, year};
    }
}
