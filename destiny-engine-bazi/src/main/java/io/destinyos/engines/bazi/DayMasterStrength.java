package io.destinyos.engines.bazi;

import io.destinyos.calendar.FiveElement;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * The verdict of Thiệu Vĩ Hoa's point-scoring method (R3, resolved
 * 2026-08-24 — see {@code docs/DECISION_LOG.md} for the four Rule D choices
 * this implementation makes, and
 * {@code docs/research_drafts/VERIFICATION_OPUS_R3.md} for what was
 * verified).
 *
 * <p>This is chart data from one named school, not a universal fact about
 * the chart — a different school could compute a different answer for the
 * same eight characters, and this record's presence never implies otherwise.
 *
 * @param vuong           true if the Day Master is Vượng (strong), i.e.
 *                        {@code 5 * ownSideDegrees >= 2 * totalDegrees} —
 *                        the integer-exact form of "own side >= 40% of total"
 * @param elementDegrees  final degree total per element, after every rule
 *                        (chỗ dựa, combinations, tương khắc, seasonal
 *                        command); always non-negative, per the book's own
 *                        worked examples (Ví dụ 3 floors 9-12 at 0)
 * @param ownSideDegrees  M — the sum of the two "phe mình" elements (the one
 *                        that generates the Day Master, and the Day Master's
 *                        own element)
 * @param totalDegrees    T — the sum of all five elements' final degrees
 * @param seasonalElement which element is "in season" (chi tháng nắm lệnh)
 *                        and so received the +1/5 modifier — the element the
 *                        month branch rules, or a combination's resultant
 *                        element if the month branch itself transformed
 */
public record DayMasterStrength(
        boolean vuong,
        Map<FiveElement, Integer> elementDegrees,
        int ownSideDegrees,
        int totalDegrees,
        FiveElement seasonalElement
) {
    public DayMasterStrength {
        Objects.requireNonNull(elementDegrees, "elementDegrees");
        Objects.requireNonNull(seasonalElement, "seasonalElement");
        elementDegrees = Map.copyOf(new EnumMap<>(elementDegrees));
    }
}
