package io.destinyos.engines.iching;

import java.util.List;
import java.util.Objects;

/**
 * The result of one hexagram casting — chart construction only. No judgment
 * or line text is included (Rule B; see {@link IChingEngine}'s
 * {@code BlockedSection}).
 *
 * @param method                which classical procedure produced this reading
 * @param originalHexagram      quẻ gốc (bản quái)
 * @param changedHexagram       quẻ biến (chi quái) — null if no line moves
 *                              (possible only for {@link CastingMethod#THREE_COINS}
 *                              or {@link CastingMethod#YARROW}; the two Mai
 *                              Hoa methods always produce exactly one moving
 *                              line)
 * @param movingLinePositions   1 (bottom) to 6 (top), in ascending order;
 *                              empty for a coin/yarrow cast with no old lines
 * @param lines                 the six drawn {@link LineValue}s, bottom to
 *                              top — only present for {@link CastingMethod#THREE_COINS}
 *                              and {@link CastingMethod#YARROW}, since Mai
 *                              Hoa's methods derive the hexagram directly
 *                              from numbers rather than drawing each line
 * @param seed                  the seed used for a random casting method —
 *                              null for the two deterministic Mai Hoa methods
 */
public record IChingReading(
        CastingMethod method,
        Hexagram originalHexagram,
        Hexagram changedHexagram,
        List<Integer> movingLinePositions,
        List<LineValue> lines,
        Long seed
) {
    public IChingReading {
        Objects.requireNonNull(method, "method");
        Objects.requireNonNull(originalHexagram, "originalHexagram");
        movingLinePositions = movingLinePositions == null ? List.of() : List.copyOf(movingLinePositions);
        lines = lines == null ? List.of() : List.copyOf(lines);
    }
}
