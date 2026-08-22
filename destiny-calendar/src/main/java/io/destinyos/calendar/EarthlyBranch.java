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
    RAT(1, FiveElement.WATER),
    OX(2, FiveElement.EARTH),
    TIGER(3, FiveElement.WOOD),
    RABBIT(4, FiveElement.WOOD),
    DRAGON(5, FiveElement.EARTH),
    SNAKE(6, FiveElement.FIRE),
    HORSE(7, FiveElement.FIRE),
    GOAT(8, FiveElement.EARTH),
    MONKEY(9, FiveElement.METAL),
    ROOSTER(10, FiveElement.METAL),
    DOG(11, FiveElement.EARTH),
    PIG(12, FiveElement.WATER);

    private final int index;
    private final FiveElement element;

    EarthlyBranch(int index, FiveElement element) {
        this.index = index;
        this.element = element;
    }

    public int index() {
        return index;
    }

    /**
     * Ngũ Hành of this branch: Tý Thủy, Sửu Thổ, Dần/Mão Mộc, Thìn Thổ,
     * Tỵ/Ngọ Hỏa, Mùi Thổ, Thân/Dậu Kim, Tuất Thổ, Hợi Thủy. Universal
     * across every source consulted.
     */
    public FiveElement element() {
        return element;
    }

    /**
     * Âm Dương from cycle position (odd = Dương). See {@link YinYang} for
     * why the "Tý is functionally Âm" subtlety is recorded there and never
     * reached by any calculation in this project.
     */
    public YinYang polarity() {
        return YinYang.ofCyclePosition(index);
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
