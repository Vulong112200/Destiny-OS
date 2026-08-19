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
    GIAP(1), AT(2), BINH(3), DINH(4), MAU(5), KY(6), CANH(7), TAN(8), NHAM(9), QUY(10);

    private final int index;

    HeavenlyStem(int index) {
        this.index = index;
    }

    public int index() {
        return index;
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
