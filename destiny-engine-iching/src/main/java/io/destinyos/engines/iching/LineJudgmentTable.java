package io.destinyos.engines.iching;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lookup table over the four {@code LineJudgments1..4} sources' 386 entries
 * (384 ordinary lines + the two classical "dung cuu"/"dung luc" specials for
 * hexagrams 1 and 2), keyed by (hexagram number, position).
 */
public final class LineJudgmentTable {

    private static final Map<Long, LineJudgment> BY_KEY = buildIndex();

    private LineJudgmentTable() {
    }

    private static Map<Long, LineJudgment> buildIndex() {
        Map<Long, LineJudgment> index = new HashMap<>();
        for (List<LineJudgment> batch : List.of(
                LineJudgments1.entries(), LineJudgments2.entries(),
                LineJudgments3.entries(), LineJudgments4.entries())) {
            for (LineJudgment lj : batch) {
                index.put(key(lj.hexagramNumber(), lj.position()), lj);
            }
        }
        return index;
    }

    private static long key(int hexagramNumber, int position) {
        return hexagramNumber * 10L + position;
    }

    /** {@code position} is 1-6 counting from the bottom line upward. */
    public static Optional<LineJudgment> at(int kingWenNumber, int position) {
        return Optional.ofNullable(BY_KEY.get(key(kingWenNumber, position)));
    }

    /**
     * The classical "dung cuu" (hexagram 1) / "dung luc" (hexagram 2) line,
     * or empty for every other hexagram - see {@link LineJudgment}'s Javadoc.
     */
    public static Optional<LineJudgment> dungLine(int kingWenNumber) {
        return Optional.ofNullable(BY_KEY.get(key(kingWenNumber, 0)));
    }
}
