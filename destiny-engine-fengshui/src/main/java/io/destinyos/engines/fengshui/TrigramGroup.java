package io.destinyos.engines.fengshui;

/**
 * Đông tứ trạch / Tây tứ trạch — the two halves Bát Trạch divides people and
 * sites into.
 *
 * <p>Not a separate rule, but a consequence of the line-change derivation: a
 * trigram's four auspicious directions always turn out to be the four trigrams
 * in its own group. {@code BatTrachTableTest} asserts that as a property rather
 * than assuming it, because if it ever failed, either the group assignments or
 * the derivation would be wrong.
 */
public enum TrigramGroup {
    /** Đông tứ: Khảm (1), Chấn (3), Tốn (4), Ly (9). */
    EAST,
    /** Tây tứ: Khôn (2), Kiền (6), Đoài (7), Cấn (8). */
    WEST
}
