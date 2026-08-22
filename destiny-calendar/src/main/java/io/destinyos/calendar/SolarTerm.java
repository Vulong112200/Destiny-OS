package io.destinyos.calendar;

/**
 * The 24 solar terms (Tiết Khí), each a 15-degree sector of
 * {@link SolarPosition#longitudeRadians(double)} — the standard,
 * non-controversial partition of the ecliptic used by every lunisolar
 * calendar tradition. This is the same cited solar-longitude function used
 * for the leap-month rule ({@link LunarCalendar}), which only needs the
 * coarser 12 "principal terms" (Trung Khí, 30-degree sectors, index 0-11 of
 * this enum's even-numbered entries) — R9's research note is explicit that
 * this 24-term calendar is the same algorithm at finer resolution, not a
 * separate one.
 *
 * <p>{@link #LAP_XUAN} (Lập Xuân, index 3 — 315 degrees) is named
 * explicitly because {@code CALENDAR_AND_ASTRONOMY_SPEC.md} section 6
 * requires it as its own boundary test category, distinct from Tết.
 */
public enum SolarTerm {
    XUAN_PHAN,      // 0  - 0 deg   - Xuân phân (March equinox) - principal term
    THANH_MINH,     // 1  - 15 deg  - Thanh minh
    COC_VU,         // 2  - 30 deg  - Cốc vũ - principal term
    LAP_HA,         // 3  - 45 deg  - Lập hạ
    TIEU_MAN,       // 4  - 60 deg  - Tiểu mãn - principal term
    MANG_CHUNG,     // 5  - 75 deg  - Mang chủng
    HA_CHI,         // 6  - 90 deg  - Hạ chí (June solstice) - principal term
    TIEU_THU,       // 7  - 105 deg - Tiểu thử
    DAI_THU,        // 8  - 120 deg - Đại thử - principal term
    LAP_THU,        // 9  - 135 deg - Lập thu
    XU_THU,         // 10 - 150 deg - Xử thử - principal term
    BACH_LO,        // 11 - 165 deg - Bạch lộ
    THU_PHAN,       // 12 - 180 deg - Thu phân (September equinox) - principal term
    HAN_LO,         // 13 - 195 deg - Hàn lộ
    SUONG_GIANG,    // 14 - 210 deg - Sương giáng - principal term
    LAP_DONG,       // 15 - 225 deg - Lập đông
    TIEU_TUYET,     // 16 - 240 deg - Tiểu tuyết - principal term
    DAI_TUYET,      // 17 - 255 deg - Đại tuyết
    DONG_CHI,       // 18 - 270 deg - Đông chí (December solstice) - principal term
    TIEU_HAN,       // 19 - 285 deg - Tiểu hàn
    DAI_HAN,        // 20 - 300 deg - Đại hàn - principal term
    LAP_XUAN,       // 21 - 315 deg - Lập xuân (start of spring) - required boundary test
    VU_THUY,        // 22 - 330 deg - Vũ thủy - principal term
    KINH_TRAP       // 23 - 345 deg - Kinh trập
    ;

    private static final SolarTerm[] VALUES = values();

    /** Degrees of ecliptic longitude spanned by one of the 24 terms. */
    private static final double DEGREES_PER_TERM = 15.0;

    /**
     * The sun's mean daily motion in ecliptic longitude, degrees per day
     * (360 / 365.2422 tropical days). Used only to convert an angular
     * distance from a term boundary into an approximate time distance for
     * {@link #minutesToNearestTermBoundary} — never to compute a term
     * instant, which always comes from {@link SolarPosition}.
     */
    private static final double MEAN_DEGREES_PER_DAY = 0.98564736;

    /**
     * 0-23 index of the term containing the sun's longitude at one exact
     * instant, rather than at local midnight.
     *
     * <p>Bát Tự needs this: its month pillar changes at the term instant, not
     * at the start of the day containing it, so a birth at 22:00 and one at
     * 23:30 on the same date can legitimately fall in different months
     * (verified against published tables — Lập Xuân 1984 fell at 23:18
     * Beijing time).
     *
     * @param julianDateUt 0h-UT-referenced Julian date, as produced by
     *                     {@link JulianDay#fromLocalDateTime}
     */
    public static int termIndexAtJulianDate(double julianDateUt) {
        double longitude = SolarPosition.longitudeRadians(julianDateUt);
        return (int) Math.floor(longitude / Math.PI * 12.0);
    }

    /** {@link #termIndexAtJulianDate} as an enum value. */
    public static SolarTerm atJulianDate(double julianDateUt) {
        return VALUES[termIndexAtJulianDate(julianDateUt)];
    }

    /**
     * The sun's ecliptic longitude in degrees at one instant — the quantity
     * every solar-term boundary is defined by, exposed in degrees because
     * that is how published Tiết Khí tables state them.
     */
    public static double solarLongitudeDegreesAtJulianDate(double julianDateUt) {
        return Math.toDegrees(SolarPosition.longitudeRadians(julianDateUt));
    }

    /**
     * Approximate distance, in minutes, from this instant to the nearest of
     * the 24 term boundaries — always non-negative.
     *
     * <p>Deliberately computed from the angular gap divided by the sun's mean
     * motion rather than by root-finding the exact boundary instant. The
     * caller's only use for this number is deciding whether a birth is close
     * enough to a boundary that the answer must be flagged as
     * boundary-sensitive, and for that purpose a value good to a few percent
     * is enough; a root-found instant would imply a precision the underlying
     * low-precision solar series does not have (see research item R19).
     */
    public static double minutesToNearestTermBoundary(double julianDateUt) {
        double longitude = solarLongitudeDegreesAtJulianDate(julianDateUt);
        double withinTerm = longitude % DEGREES_PER_TERM;
        double gapDegrees = Math.min(withinTerm, DEGREES_PER_TERM - withinTerm);
        return gapDegrees / MEAN_DEGREES_PER_DAY * 1440.0;
    }

    /** 0-23 index of the term containing the sun's longitude at local midnight of the given day. */
    public static int termIndexAt(long dayNumber, double timezoneOffsetHours) {
        double jdLocalMidnight = dayNumber - 0.5 - timezoneOffsetHours / 24.0;
        double longitude = SolarPosition.longitudeRadians(jdLocalMidnight);
        return (int) Math.floor(longitude / Math.PI * 12.0);
    }

    /**
     * 0-11 index of the coarser "principal term" (Trung Khí) containing the
     * sun's longitude — used only by {@link LunarCalendar}'s leap-month
     * rule, matching the reference implementations' {@code getSunLongitude}.
     */
    public static int principalTermIndexAt(long dayNumber, double timezoneOffsetHours) {
        double jdLocalMidnight = dayNumber - 0.5 - timezoneOffsetHours / 24.0;
        double longitude = SolarPosition.longitudeRadians(jdLocalMidnight);
        return (int) Math.floor(longitude / Math.PI * 6.0);
    }

    public static SolarTerm at(long dayNumber, double timezoneOffsetHours) {
        return VALUES[termIndexAt(dayNumber, timezoneOffsetHours)];
    }
}
