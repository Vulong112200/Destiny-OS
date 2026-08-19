package io.destinyos.calendar;

/**
 * The 12 Earthly Branches (Địa Chi), in their canonical cyclic order.
 *
 * <p>Named by zodiac animal rather than transliterated Vietnamese syllable
 * deliberately: stripping tone marks collides Tý (rat) and Tỵ (snake) into
 * the same ASCII string, which is exactly the kind of silent, invisible
 * bug this project's own discipline exists to avoid. Vietnamese display
 * names ("Tý", "Sửu", ...) live in {@code VietnameseLabels}
 * (destiny-i18n), not here.
 *
 * <p>{@link #index()} (1-12) is the value the closed-form Can Chi formulas
 * in {@link CanChi} are defined against (source: Vietnamese Wikipedia
 * "Mô đun:Âm lịch" — function {@code canchi}, where branch index {@code b}
 * satisfies {@code b = x mod 12}, with remainder 0 read as index 12).
 */
public enum EarthlyBranch {
    RAT(1), OX(2), TIGER(3), RABBIT(4), DRAGON(5), SNAKE(6),
    HORSE(7), GOAT(8), MONKEY(9), ROOSTER(10), DOG(11), PIG(12);

    private final int index;

    EarthlyBranch(int index) {
        this.index = index;
    }

    public int index() {
        return index;
    }

    public static EarthlyBranch fromIndex(int index) {
        for (EarthlyBranch branch : values()) {
            if (branch.index == index) {
                return branch;
            }
        }
        throw new IllegalArgumentException("No EarthlyBranch with index " + index);
    }
}
