package io.destinyos.engines.fengshui;

import io.destinyos.calendar.FiveElement;
import java.util.Objects;

/**
 * The eight trigrams (Bát Quái) as Bát Trạch uses them: a Kua number, a
 * compass direction, an element, an East/West group, and — the part that does
 * real work here — the three line values.
 *
 * <p><strong>The lines are not decoration.</strong> Bát Trạch's eight
 * relations are derived from <em>which lines differ</em> between two trigrams
 * (see {@link BatTrachTable}), so the line values are the primary data and the
 * 8×8 table is a consequence. Storing them means the table cannot contain a
 * transcription typo, because there is no transcribed table.
 *
 * <p>Line order below is bottom, middle, top — the order the Bát Biến Du Niên
 * rule is stated in, and the order the trigram symbols are drawn in.
 * {@code true} is Dương (yang, unbroken), {@code false} is Âm (yin, broken).
 *
 * <p>Deliberately lives in this engine rather than in {@code destiny-calendar}:
 * Ngũ Hành sits there because it is part of the Can Chi system itself, whereas
 * Bát Quái has no tie to the calendar. If Kinh Dịch (research item R12) is ever
 * implemented it will need trigrams too, and since one engine may not depend on
 * another, that is the point at which this type should move to a shared module —
 * not now, on speculation.
 */
public enum Trigram {

    /** Khảm ☵ — Bắc, Thủy, Đông tứ, Kua 1. */
    KHAM(1, false, true, false, CompassDirection.NORTH, FiveElement.WATER, TrigramGroup.EAST),
    /** Khôn ☷ — Tây Nam, Thổ, Tây tứ, Kua 2. */
    KHON(2, false, false, false, CompassDirection.SOUTHWEST, FiveElement.EARTH, TrigramGroup.WEST),
    /** Chấn ☳ — Đông, Mộc, Đông tứ, Kua 3. */
    CHAN(3, true, false, false, CompassDirection.EAST, FiveElement.WOOD, TrigramGroup.EAST),
    /** Tốn ☴ — Đông Nam, Mộc, Đông tứ, Kua 4. */
    TON(4, false, true, true, CompassDirection.SOUTHEAST, FiveElement.WOOD, TrigramGroup.EAST),
    /** Kiền (Càn) ☰ — Tây Bắc, Kim, Tây tứ, Kua 6. */
    KIEN(6, true, true, true, CompassDirection.NORTHWEST, FiveElement.METAL, TrigramGroup.WEST),
    /** Đoài ☱ — Tây, Kim, Tây tứ, Kua 7. */
    DOAI(7, true, true, false, CompassDirection.WEST, FiveElement.METAL, TrigramGroup.WEST),
    /** Cấn ☶ — Đông Bắc, Thổ, Tây tứ, Kua 8. */
    CAN(8, false, false, true, CompassDirection.NORTHEAST, FiveElement.EARTH, TrigramGroup.WEST),
    /** Ly ☲ — Nam, Hỏa, Đông tứ, Kua 9. */
    LY(9, true, false, true, CompassDirection.SOUTH, FiveElement.FIRE, TrigramGroup.EAST);

    private final int kuaNumber;
    private final boolean bottomYang;
    private final boolean middleYang;
    private final boolean topYang;
    private final CompassDirection direction;
    private final FiveElement element;
    private final TrigramGroup group;

    Trigram(int kuaNumber, boolean bottomYang, boolean middleYang, boolean topYang,
            CompassDirection direction, FiveElement element, TrigramGroup group) {
        this.kuaNumber = kuaNumber;
        this.bottomYang = bottomYang;
        this.middleYang = middleYang;
        this.topYang = topYang;
        this.direction = direction;
        this.element = element;
        this.group = group;
    }

    /**
     * 1-9 excluding 5. There is no Kua 5 trigram: 5 is the centre of the Lạc
     * Thư square and has no direction, which is exactly why the Kua formula
     * needs a substitution rule for it ({@link KuaNumber}).
     */
    public int kuaNumber() {
        return kuaNumber;
    }

    public CompassDirection direction() {
        return direction;
    }

    public FiveElement element() {
        return element;
    }

    /** Đông tứ trạch or Tây tứ trạch. */
    public TrigramGroup group() {
        return group;
    }

    boolean bottomYang() {
        return bottomYang;
    }

    boolean middleYang() {
        return middleYang;
    }

    boolean topYang() {
        return topYang;
    }

    public static Trigram ofKuaNumber(int kuaNumber) {
        for (Trigram trigram : values()) {
            if (trigram.kuaNumber == kuaNumber) {
                return trigram;
            }
        }
        throw new IllegalArgumentException(
                "No trigram for Kua number " + kuaNumber
                        + " (valid: 1, 2, 3, 4, 6, 7, 8, 9 — 5 is the centre and must be "
                        + "substituted before reaching here)");
    }

    public static Trigram ofDirection(CompassDirection direction) {
        Objects.requireNonNull(direction, "direction");
        for (Trigram trigram : values()) {
            if (trigram.direction == direction) {
                return trigram;
            }
        }
        throw new IllegalStateException("Direction not covered by any trigram: " + direction);
    }
}
