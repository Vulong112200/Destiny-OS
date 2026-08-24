package io.destinyos.engines.iching;

import java.util.Objects;

/**
 * One of the 64 hexagrams (64 Quẻ), identified by its King Wen sequence
 * number (1-64) — the near-universal numbering used in every translation,
 * software implementation, and reference table found in this project's
 * research (R12).
 *
 * @param number King Wen number, 1-64
 * @param upper  the upper trigram (thượng quái, lines 4-6)
 * @param lower  the lower trigram (hạ quái, lines 1-3)
 */
public record Hexagram(int number, IChingTrigram upper, IChingTrigram lower) {

    public Hexagram {
        Objects.requireNonNull(upper, "upper");
        Objects.requireNonNull(lower, "lower");
        if (number < 1 || number > 64) {
            throw new IllegalArgumentException("King Wen number must be 1-64, got " + number);
        }
    }
}
