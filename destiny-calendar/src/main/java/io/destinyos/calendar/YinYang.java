package io.destinyos.calendar;

/**
 * Âm Dương polarity of a Heavenly Stem or Earthly Branch.
 *
 * <p>The rule is positional and universal: odd positions in the cycle are
 * Dương (Yang), even positions Âm (Yin). Giáp/Bính/Mậu/Canh/Nhâm are Dương
 * stems; Tý/Dần/Thìn/Ngọ/Thân/Tuất are Dương branches.
 *
 * <p><strong>Known subtlety, deliberately not papered over:</strong> some
 * Bát Tự texts treat Tý as functionally Âm water and Ngọ as functionally
 * Dương fire ("thể dương dụng âm"), diverging from the positional rule.
 * That distinction only matters when polarity is read off a <em>branch</em>.
 * This project never does: Thập Thần in {@code destiny-engine-bazi} is
 * derived from stems only — the four pillar stems plus the stems hidden in
 * the branches — which is also how every source consulted derives it. The
 * subtlety is therefore recorded here and made unreachable rather than
 * silently resolved one way.
 */
public enum YinYang {
    /** Dương. */
    YANG,
    /** Âm. */
    YIN;

    /** @param cyclePosition 1-based position in a 10- or 12-member cycle */
    static YinYang ofCyclePosition(int cyclePosition) {
        return cyclePosition % 2 == 1 ? YANG : YIN;
    }
}
