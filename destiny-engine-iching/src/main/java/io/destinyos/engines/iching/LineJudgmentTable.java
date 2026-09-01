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

    /**
     * Builds the index, and <strong>refuses a duplicate key</strong> rather
     * than letting the later entry win.
     *
     * <p>The four {@code LineJudgments1..4} sources are split purely by file
     * size and nothing coordinates them. Two entries claiming the same
     * (hexagram, position) used to overwrite in silence: the key still
     * resolved, so every completeness and lookup assertion still passed, and
     * one real line text would simply be gone. Failing at class
     * initialisation makes that outcome unreachable instead of merely
     * detectable.
     */
    private static Map<Long, LineJudgment> buildIndex() {
        Map<Long, LineJudgment> index = new HashMap<>();
        for (List<LineJudgment> batch : List.of(
                LineJudgments1.entries(), LineJudgments2.entries(),
                LineJudgments3.entries(), LineJudgments4.entries())) {
            for (LineJudgment lj : batch) {
                LineJudgment clash = index.put(key(lj.hexagramNumber(), lj.position()), lj);
                if (clash != null) {
                    throw new IllegalStateException("Duplicate line judgment for hexagram "
                            + lj.hexagramNumber() + " position " + lj.position()
                            + " — one of the two would have been lost silently");
                }
            }
        }
        return index;
    }

    /**
     * How many entries the table holds; 386 when complete (384 ordinary lines
     * plus dụng cửu and dụng lục). Exposed so a test can pin the total, which
     * is the only assertion a dropped entry cannot survive.
     */
    public static int size() {
        return BY_KEY.size();
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
