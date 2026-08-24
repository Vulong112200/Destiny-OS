package io.destinyos.engines.iching;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The 64-hexagram table, checked exactly the way
 * {@code VERIFICATION_OPUS_R12.md} §A1 checked it — a re-check here catches
 * any future edit that breaks the table the same way the original Wikipedia
 * transcription error (§A2) would have been caught, had this test existed
 * first.
 */
class HexagramTableTest {

    @Test
    @DisplayName("All 64 hexagrams are present, numbered 1-64 with no gaps")
    void allNumbersPresent() {
        List<Hexagram> all = HexagramTable.all();
        assertThat(all).hasSize(64);
        for (int n = 1; n <= 64; n++) {
            assertThat(HexagramTable.byNumber(n).number()).isEqualTo(n);
        }
    }

    @Test
    @DisplayName("Bijection: every (upper, lower) trigram pair appears exactly once")
    void isABijection() {
        Set<String> seen = new HashSet<>();
        for (Hexagram h : HexagramTable.all()) {
            String key = h.upper() + "/" + h.lower();
            assertThat(seen.add(key)).as("duplicate pair " + key).isTrue();
        }
        assertThat(seen).hasSize(64);
        for (IChingTrigram upper : IChingTrigram.values()) {
            for (IChingTrigram lower : IChingTrigram.values()) {
                assertThat(HexagramTable.of(upper, lower)).as(upper + "/" + lower).isNotNull();
            }
        }
    }

    @Test
    @DisplayName("The 8 pure hexagrams (same trigram doubled) sit at their known King Wen numbers")
    void pureHexagramsAtKnownNumbers() {
        assertThat(HexagramTable.of(IChingTrigram.HEAVEN, IChingTrigram.HEAVEN).number()).isEqualTo(1);
        assertThat(HexagramTable.of(IChingTrigram.EARTH, IChingTrigram.EARTH).number()).isEqualTo(2);
        assertThat(HexagramTable.of(IChingTrigram.WATER, IChingTrigram.WATER).number()).isEqualTo(29);
        assertThat(HexagramTable.of(IChingTrigram.FIRE, IChingTrigram.FIRE).number()).isEqualTo(30);
        assertThat(HexagramTable.of(IChingTrigram.THUNDER, IChingTrigram.THUNDER).number()).isEqualTo(51);
        assertThat(HexagramTable.of(IChingTrigram.MOUNTAIN, IChingTrigram.MOUNTAIN).number()).isEqualTo(52);
        assertThat(HexagramTable.of(IChingTrigram.WIND, IChingTrigram.WIND).number()).isEqualTo(57);
        assertThat(HexagramTable.of(IChingTrigram.LAKE, IChingTrigram.LAKE).number()).isEqualTo(58);
    }

    @Test
    @DisplayName("Every King Wen pair (2k-1, 2k) is a 180-degree rotation or a full complement of its partner")
    void kingWenPairsConform() {
        for (int k = 1; k <= 32; k++) {
            Hexagram a = HexagramTable.byNumber(2 * k - 1);
            Hexagram b = HexagramTable.byNumber(2 * k);
            boolean[] linesA = sixLines(a);
            boolean[] linesB = sixLines(b);

            boolean isRotation = true;
            boolean isComplement = true;
            for (int i = 0; i < 6; i++) {
                if (linesB[i] != linesA[5 - i]) {
                    isRotation = false;
                }
                if (linesB[i] != !linesA[i]) {
                    isComplement = false;
                }
            }
            assertThat(isRotation || isComplement)
                    .as("pair (%d,%d) must be a rotation or complement of each other", 2 * k - 1, 2 * k)
                    .isTrue();
        }
    }

    @Test
    @DisplayName("#63 (Ký Tế, Thủy/Hỏa) and #64 (Vị Tế, Hỏa/Thủy) — pinned explicitly")
    void hexagram63And64ArePinned() {
        // The structural pair-rule above cannot settle this pair on its own
        // (Ký Tế/Vị Tế are simultaneously a rotation AND a complement of each
        // other), which is exactly why this project's own research caught a
        // real transcription error here (VERIFICATION_OPUS_R12.md §A2) - a
        // first fetch of Chinese Wikipedia had these two rows swapped. Only
        // the traditional naming convention (水火既濟 = Water over Fire)
        // settles it, so it is pinned directly rather than left to the
        // weaker structural check alone.
        assertThat(HexagramTable.byNumber(63).upper()).isEqualTo(IChingTrigram.WATER);
        assertThat(HexagramTable.byNumber(63).lower()).isEqualTo(IChingTrigram.FIRE);
        assertThat(HexagramTable.byNumber(64).upper()).isEqualTo(IChingTrigram.FIRE);
        assertThat(HexagramTable.byNumber(64).lower()).isEqualTo(IChingTrigram.WATER);
    }

    private static boolean[] sixLines(Hexagram h) {
        return new boolean[] {
            h.lower().bottomYang(), h.lower().middleYang(), h.lower().topYang(),
            h.upper().bottomYang(), h.upper().middleYang(), h.upper().topYang(),
        };
    }
}
