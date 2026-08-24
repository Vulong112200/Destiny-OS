package io.destinyos.engines.iching;

/**
 * The 8 trigrams (Bát Quái), each three lines read bottom-to-top.
 *
 * <p>Named in English (HEAVEN, LAKE, ...) rather than transliterated
 * Vietnamese, the same reasoning {@link io.destinyos.calendar.EarthlyBranch}
 * gives for using zodiac animals instead of Tý/Sửu/...: stripping tone marks
 * collides Càn (乾) and Cấn (艮) into the same ASCII string. Vietnamese
 * display names live in {@code VietnameseLabels} (destiny-i18n), not here.
 *
 * <p>{@link #tienThienNumber()} is not an arbitrary lookup — it is derivable
 * arithmetically as {@code 8 - (binary value of the three lines, bottom line
 * as the most significant bit, yang = 1)}, confirmed for all 8 trigrams in
 * {@code docs/research_drafts/VERIFICATION_OPUS_R12.md} §A3. It is still
 * stored explicitly here (rather than computed from the line booleans at call
 * time) so the number is the citable fact and the formula is the thing a test
 * checks the table against, not the other way around.
 *
 * <p><strong>Deliberately a second copy, not a shared type with
 * {@code io.destinyos.engines.fengshui.Trigram}.</strong> That class's own
 * Javadoc anticipated this exact moment ("If Kinh Dịch is ever implemented
 * it will need trigrams too... that is the point at which this type should
 * move to a shared module — not now, on speculation"). This module still
 * keeps its own copy rather than doing that extraction, for a narrower
 * reason than speculation this time: Bát Trạch's version carries Kua-number,
 * direction and East/West-group fields this module has no use for, so the
 * two are not quite the same concept, only overlapping in their three line
 * values — and refactoring a stable, already golden-tested engine as a side
 * effect of shipping an unrelated one is exactly the kind of scope creep
 * CLAUDE.md's workflow discipline warns against. Left here as a named,
 * deliberate duplication for a future cleanup pass to pick up, not a missed
 * cross-reference.
 */
public enum IChingTrigram {

    /** Càn 乾 ☰ — three yang lines. Tiên Thiên số 1. */
    HEAVEN(true, true, true, 1),
    /** Đoài 兌 ☱. Tiên Thiên số 2. */
    LAKE(true, true, false, 2),
    /** Ly 離 ☲. Tiên Thiên số 3. */
    FIRE(true, false, true, 3),
    /** Chấn 震 ☳. Tiên Thiên số 4. */
    THUNDER(true, false, false, 4),
    /** Tốn 巽 ☴. Tiên Thiên số 5. */
    WIND(false, true, true, 5),
    /** Khảm 坎 ☵. Tiên Thiên số 6. */
    WATER(false, true, false, 6),
    /** Cấn 艮 ☶. Tiên Thiên số 7. */
    MOUNTAIN(false, false, true, 7),
    /** Khôn 坤 ☷ — three yin lines. Tiên Thiên số 8. */
    EARTH(false, false, false, 8);

    private final boolean bottomYang;
    private final boolean middleYang;
    private final boolean topYang;
    private final int tienThienNumber;

    IChingTrigram(boolean bottomYang, boolean middleYang, boolean topYang, int tienThienNumber) {
        this.bottomYang = bottomYang;
        this.middleYang = middleYang;
        this.topYang = topYang;
        this.tienThienNumber = tienThienNumber;
    }

    public boolean bottomYang() {
        return bottomYang;
    }

    public boolean middleYang() {
        return middleYang;
    }

    public boolean topYang() {
        return topYang;
    }

    /** Số Tiên Thiên (Càn 1 ... Khôn 8) — dùng cho mọi phép chia-8 gieo quẻ theo Số/Thời (Mai Hoa卷一). */
    public int tienThienNumber() {
        return tienThienNumber;
    }

    /**
     * Ngũ Hổ 卦以八除: bất kỳ số nào chia cho 8, số dư (1-8, dư 0 đọc là 8)
     * tra ra trigram này. Nguồn cấp 1: 「凡起卦不問數多少，即以此數作卦數，過八
     * 數即以八數遞除...如得八數整，即坤卦，更不必除也」 (梅花易數 卷一).
     */
    public static IChingTrigram fromNumber(int number) {
        int remainder = number % 8;
        int tienThien = remainder == 0 ? 8 : remainder;
        for (IChingTrigram t : values()) {
            if (t.tienThienNumber == tienThien) {
                return t;
            }
        }
        throw new IllegalStateException("unreachable: no trigram for Tiên Thiên number " + tienThien);
    }

    /** Reverse of the three line accessors — used to rebuild a trigram after flipping a moving line. */
    public static IChingTrigram fromLines(boolean bottomYang, boolean middleYang, boolean topYang) {
        for (IChingTrigram t : values()) {
            if (t.bottomYang == bottomYang && t.middleYang == middleYang && t.topYang == topYang) {
                return t;
            }
        }
        throw new IllegalStateException("unreachable: every 3-line combination maps to one of the 8 trigrams");
    }
}
