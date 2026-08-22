package io.destinyos.calendar;

/**
 * The 10 Heavenly Stems (Thiên Can), in their canonical cyclic order.
 *
 * <p>{@link #index()} (1-10) is the value the closed-form Can Chi formulas
 * in {@link CanChi} are defined against (source: Vietnamese Wikipedia
 * "Mô đun:Âm lịch" — function {@code canchi}, where stem index {@code a}
 * satisfies {@code a = x mod 10}, with remainder 0 read as index 10).
 * Vietnamese display names live in {@code VietnameseLabels}
 * (destiny-i18n), not here — this enum is identity only, the same
 * separation every other user-facing enum in this project uses.
 */
public enum HeavenlyStem {
    GIAP(1, FiveElement.WOOD),
    AT(2, FiveElement.WOOD),
    BINH(3, FiveElement.FIRE),
    DINH(4, FiveElement.FIRE),
    MAU(5, FiveElement.EARTH),
    KY(6, FiveElement.EARTH),
    CANH(7, FiveElement.METAL),
    TAN(8, FiveElement.METAL),
    NHAM(9, FiveElement.WATER),
    QUY(10, FiveElement.WATER);

    private final int index;
    private final FiveElement element;

    HeavenlyStem(int index, FiveElement element) {
        this.index = index;
        this.element = element;
    }

    public int index() {
        return index;
    }

    /**
     * Ngũ Hành of this stem. Pairs run in cycle order — Giáp/Ất Mộc,
     * Bính/Đinh Hỏa, Mậu/Kỷ Thổ, Canh/Tân Kim, Nhâm/Quý Thủy — with the
     * odd member Dương and the even member Âm. Universal across every
     * source consulted; no school variation exists to select between.
     */
    public FiveElement element() {
        return element;
    }

    /** Âm Dương of this stem, from its cycle position (odd = Dương). */
    public YinYang polarity() {
        return YinYang.ofCyclePosition(index);
    }

    public static HeavenlyStem fromIndex(int index) {
        for (HeavenlyStem stem : values()) {
            if (stem.index == index) {
                return stem;
            }
        }
        throw new IllegalArgumentException("No HeavenlyStem with index " + index);
    }
}
