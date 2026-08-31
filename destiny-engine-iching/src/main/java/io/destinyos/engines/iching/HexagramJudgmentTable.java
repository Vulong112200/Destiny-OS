package io.destinyos.engines.iching;

import java.util.Map;
import java.util.Optional;

/**
 * Lookup table over {@link HexagramJudgments}' 64 entries, mirroring
 * {@link HexagramTable}'s shape.
 */
public final class HexagramJudgmentTable {

    private static final Map<Integer, HexagramJudgment> BY_NUMBER = HexagramJudgments.entries();

    private HexagramJudgmentTable() {
    }

    public static Optional<HexagramJudgment> byNumber(int kingWenNumber) {
        return Optional.ofNullable(BY_NUMBER.get(kingWenNumber));
    }
}
