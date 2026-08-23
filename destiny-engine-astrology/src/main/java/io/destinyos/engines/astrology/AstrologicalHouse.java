package io.destinyos.engines.astrology;

/**
 * One of the twelve houses of a Western astrology chart.
 *
 * <p><strong>A distinct type from {@code ZiWeiPalace}</strong> (Tử Vi's 12
 * cung) and from {@link ZodiacSign} — Master Spec §1's terminology rule
 * exists precisely because "12 of something" invites conflating these three
 * unrelated systems. A house here is a wedge of the sky relative to the
 * horizon and meridian, not a sign of the zodiac and not a Tử Vi cung, even
 * though under Whole Sign houses (this engine's system for now) each house
 * happens to span exactly one sign.
 */
public enum AstrologicalHouse {
    HOUSE_1, HOUSE_2, HOUSE_3, HOUSE_4, HOUSE_5, HOUSE_6,
    HOUSE_7, HOUSE_8, HOUSE_9, HOUSE_10, HOUSE_11, HOUSE_12;

    private static final AstrologicalHouse[] VALUES = values();

    /** 1-based house number, for display. */
    public int number() {
        return ordinal() + 1;
    }

    static AstrologicalHouse fromNumber(int oneBased) {
        return VALUES[oneBased - 1];
    }
}
