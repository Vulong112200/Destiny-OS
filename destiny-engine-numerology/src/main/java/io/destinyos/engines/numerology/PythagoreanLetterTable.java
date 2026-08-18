package io.destinyos.engines.numerology;

import java.util.Map;

/**
 * The standard Pythagorean A-Z to 1-9 letter-value table (research item R8).
 *
 * <p>This mapping itself is fixed and not in dispute — confirmed identical
 * across every source consulted during R8's research. What R8 actually
 * required deciding was the Vietnamese-name <em>normalization</em> step
 * ({@link VietnameseNameNormalizer}) that produces the plain A-Z input this
 * table expects, not the table itself.
 */
public final class PythagoreanLetterTable {

    private static final Map<Character, Integer> VALUES = buildTable();

    private PythagoreanLetterTable() {
    }

    /**
     * The letter's value, 1-9.
     *
     * @throws IllegalArgumentException if {@code letter} is not an A-Z
     *         letter (callers must normalize first — see
     *         {@link VietnameseNameNormalizer})
     */
    public static int valueOf(char letter) {
        Integer value = VALUES.get(Character.toUpperCase(letter));
        if (value == null) {
            throw new IllegalArgumentException(
                    "Not a plain A-Z letter after normalization: '" + letter + "'. "
                            + "Callers must normalize the name first.");
        }
        return value;
    }

    private static Map<Character, Integer> buildTable() {
        String[] groups = {
                "AJS", "BKT", "CLU", "DMV", "ENW", "FOX", "GPY", "HQZ", "IR"
        };
        var map = new java.util.HashMap<Character, Integer>();
        for (int i = 0; i < groups.length; i++) {
            int value = i + 1;
            for (char c : groups[i].toCharArray()) {
                map.put(c, value);
            }
        }
        return Map.copyOf(map);
    }
}
