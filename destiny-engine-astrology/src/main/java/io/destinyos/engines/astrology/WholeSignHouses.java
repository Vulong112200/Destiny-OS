package io.destinyos.engines.astrology;

import java.util.EnumMap;
import java.util.Map;

/**
 * The Whole Sign house system: House 1 is the entire sign containing the
 * Ascendant, and each subsequent house is the next sign in zodiac order — no
 * further division of a sign, and no dependence on latitude or MC.
 *
 * <p><strong>Why this system for the first version</strong> (owner decision,
 * R6, 2026-08-23): it needs only the Ascendant, so it is defined and correct
 * at every latitude, including inside the polar circle where Placidus and
 * Koch become mathematically undefined because a point can fail to rise or
 * set at all. It is also the system Hellenistic and Vedic astrology use as
 * their default, so choosing it is not merely an engineering shortcut with
 * no traditional standing. Master Spec §15 requires {@code houseSystem} to be
 * explicit and versioned per chart; adding a second (e.g. Placidus) later is
 * additive and does not disturb this one.
 */
final class WholeSignHouses {

    private WholeSignHouses() {
    }

    /**
     * House 1 through 12, each mapped to its sign, starting from the sign
     * containing the Ascendant.
     */
    static Map<AstrologicalHouse, ZodiacSign> cusps(ZodiacSign ascendantSign) {
        Map<AstrologicalHouse, ZodiacSign> houses = new EnumMap<>(AstrologicalHouse.class);
        ZodiacSign[] signs = ZodiacSign.values();
        int start = ascendantSign.ordinal();
        for (int houseIndex = 0; houseIndex < 12; houseIndex++) {
            ZodiacSign sign = signs[(start + houseIndex) % 12];
            houses.put(AstrologicalHouse.fromNumber(houseIndex + 1), sign);
        }
        return Map.copyOf(houses);
    }

    /** Which house a given ecliptic longitude falls in, under Whole Sign. */
    static AstrologicalHouse houseOf(ZodiacSign ascendantSign, ZodiacSign pointSign) {
        int offset = Math.floorMod(pointSign.ordinal() - ascendantSign.ordinal(), 12);
        return AstrologicalHouse.fromNumber(offset + 1);
    }
}
