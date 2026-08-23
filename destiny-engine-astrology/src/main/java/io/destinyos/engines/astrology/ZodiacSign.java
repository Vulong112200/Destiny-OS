package io.destinyos.engines.astrology;

/**
 * A tropical zodiac sign — a 30° sector of ecliptic longitude measured from
 * the vernal equinox (λ = 0° = Aries 0°).
 *
 * <p><strong>Not the same type as {@code ZiWeiPalace}</strong> (Tử Vi's 12
 * cung) or {@code AstrologicalHouse} (this engine's 12 houses). Master Spec
 * §1 and CLAUDE.md's terminology section require these stay distinct types
 * precisely because a Vietnamese reader's first instinct is to map "12
 * something" onto "12 cung", which is a different system with different
 * boundaries and a different origin point. There is deliberately no shared
 * {@code House}/{@code Palace} supertype.
 *
 * <p>Tropical rather than sidereal (owner decision, R6, 2026-08-23) — the
 * zodiac is defined by the equinox point, not by any fixed star, so it needs
 * no ayanamsa correction.
 */
public enum ZodiacSign {
    ARIES, TAURUS, GEMINI, CANCER, LEO, VIRGO,
    LIBRA, SCORPIO, SAGITTARIUS, CAPRICORN, AQUARIUS, PISCES;

    private static final ZodiacSign[] VALUES = values();
    private static final double DEGREES_PER_SIGN = 30.0;

    /** Which sign contains this tropical ecliptic longitude, and how far into it. */
    static SignPosition at(double eclipticLongitudeDegrees) {
        double normalized = ((eclipticLongitudeDegrees % 360.0) + 360.0) % 360.0;
        int index = (int) Math.floor(normalized / DEGREES_PER_SIGN);
        double degreesIntoSign = normalized - index * DEGREES_PER_SIGN;
        return new SignPosition(VALUES[index], degreesIntoSign);
    }

    /**
     * A sign plus how far into it a longitude falls — the pair every chart
     * point (Sun, Ascendant, MC) is reported as, rather than a bare degree
     * number a reader would have to convert by hand.
     *
     * @param degreesIntoSign 0 (inclusive) to 30 (exclusive)
     */
    record SignPosition(ZodiacSign sign, double degreesIntoSign) {
    }
}
