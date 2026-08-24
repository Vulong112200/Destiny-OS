package io.destinyos.engines.iching;

/**
 * The four possible values a single drawn line can take, per Three Coins
 * (总數 6/7/8/9) or Yarrow-stalk casting. Every casting method in this module
 * reduces to exactly one of these four, regardless of its own internal
 * mechanics — this is the point where Three Coins, Yarrow, and (indirectly,
 * via the moving-line count) the Mai Hoa methods all agree.
 *
 * <p>Old lines ({@link #isMoving()} true) flip to their opposite polarity in
 * the changed hexagram (卦變); young lines carry through unchanged. Source:
 * consistent across every text found in
 * {@code docs/research_drafts/R12_iching_maihoa.md} §6 — no competing rule.
 */
public enum LineValue {

    /** Lão Âm 老陰 — tổng 6. Đứt, ĐỘNG (biến thành Dương). */
    OLD_YIN(6, false, true),
    /** Thiếu Dương 少陽 — tổng 7. Liền, tĩnh. */
    YOUNG_YANG(7, true, false),
    /** Thiếu Âm 少陰 — tổng 8. Đứt, tĩnh. */
    YOUNG_YIN(8, false, false),
    /** Lão Dương 老陽 — tổng 9. Liền, ĐỘNG (biến thành Âm). */
    OLD_YANG(9, true, true);

    private final int total;
    private final boolean yang;
    private final boolean moving;

    LineValue(int total, boolean yang, boolean moving) {
        this.total = total;
        this.yang = yang;
        this.moving = moving;
    }

    public int total() {
        return total;
    }

    public boolean isYang() {
        return yang;
    }

    /** True for Lão Âm/Lão Dương — this line flips in the changed hexagram. */
    public boolean isMoving() {
        return moving;
    }

    /** The line's polarity after flipping a moving line (used to build the changed hexagram). */
    public boolean yangAfterChange() {
        return moving != yang;
    }

    public static LineValue fromTotal(int total) {
        return switch (total) {
            case 6 -> OLD_YIN;
            case 7 -> YOUNG_YANG;
            case 8 -> YOUNG_YIN;
            case 9 -> OLD_YANG;
            default -> throw new IllegalArgumentException("Line total must be 6-9, got " + total);
        };
    }
}
