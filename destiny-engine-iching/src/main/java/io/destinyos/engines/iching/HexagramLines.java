package io.destinyos.engines.iching;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a {@link Hexagram} from six lines (bottom to top) and computes 卦變
 * (the changed hexagram) — flip every moving line, keep every stable line —
 * a mechanical transformation this project's research found no competing
 * rule for (see {@code docs/research_drafts/R12_iching_maihoa.md} §6).
 *
 * <p>Serves two shapes of input: a full 6-{@link LineValue} sequence (Three
 * Coins, Yarrow — where each line is independently old/young) and a single
 * flip applied to an already-known hexagram at one position (Mai Hoa's two
 * methods, which determine the whole hexagram from trigram numbers and then
 * locate exactly one moving line separately).
 */
final class HexagramLines {

    private HexagramLines() {
    }

    static Hexagram original(List<LineValue> lines) {
        return toHexagram(lines, LineValue::isYang);
    }

    /** Null if no line moves — a hexagram with no moving lines has no changed hexagram. */
    static Hexagram changed(List<LineValue> lines) {
        if (movingPositions(lines).isEmpty()) {
            return null;
        }
        return toHexagram(lines, LineValue::yangAfterChange);
    }

    static List<Integer> movingPositions(List<LineValue> lines) {
        requireSixLines(lines);
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            if (lines.get(i).isMoving()) {
                positions.add(i + 1);
            }
        }
        return positions;
    }

    /** Mai Hoa's shape: one already-determined hexagram, exactly one flipped line. */
    static Hexagram flipOneLine(Hexagram hexagram, int position1to6) {
        if (position1to6 < 1 || position1to6 > 6) {
            throw new IllegalArgumentException("Line position must be 1-6, got " + position1to6);
        }
        boolean[] yang = {
            hexagram.lower().bottomYang(), hexagram.lower().middleYang(), hexagram.lower().topYang(),
            hexagram.upper().bottomYang(), hexagram.upper().middleYang(), hexagram.upper().topYang(),
        };
        yang[position1to6 - 1] = !yang[position1to6 - 1];
        IChingTrigram lower = IChingTrigram.fromLines(yang[0], yang[1], yang[2]);
        IChingTrigram upper = IChingTrigram.fromLines(yang[3], yang[4], yang[5]);
        return HexagramTable.of(upper, lower);
    }

    private static Hexagram toHexagram(List<LineValue> lines, java.util.function.Predicate<LineValue> yangOf) {
        requireSixLines(lines);
        IChingTrigram lower = IChingTrigram.fromLines(
                yangOf.test(lines.get(0)), yangOf.test(lines.get(1)), yangOf.test(lines.get(2)));
        IChingTrigram upper = IChingTrigram.fromLines(
                yangOf.test(lines.get(3)), yangOf.test(lines.get(4)), yangOf.test(lines.get(5)));
        return HexagramTable.of(upper, lower);
    }

    private static void requireSixLines(List<LineValue> lines) {
        if (lines == null || lines.size() != 6) {
            throw new IllegalArgumentException("A hexagram needs exactly 6 lines, got "
                    + (lines == null ? "null" : lines.size()));
        }
    }
}
