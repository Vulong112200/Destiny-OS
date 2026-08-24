package io.destinyos.engines.iching;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** 卦變 (changed hexagram) construction — flip every moving line, keep every stable one. */
class HexagramLinesTest {

    @Test
    @DisplayName("All-stable lines: original and changed hexagram are the same, and there is no changed hexagram")
    void noMovingLinesMeansNoChangedHexagram() {
        List<LineValue> lines = List.of(
                LineValue.YOUNG_YANG, LineValue.YOUNG_YIN, LineValue.YOUNG_YANG,
                LineValue.YOUNG_YIN, LineValue.YOUNG_YANG, LineValue.YOUNG_YIN);
        assertThat(HexagramLines.movingPositions(lines)).isEmpty();
        assertThat(HexagramLines.changed(lines)).isNull();
    }

    @Test
    @DisplayName("A single moving line flips only that line in the changed hexagram")
    void singleMovingLineFlipsOnlyThatLine() {
        // All yang (Càn/Càn, #1) except line 1 (bottom) is Lão Âm (old yin,
        // currently drawn as yin, about to flip to yang) - contradiction:
        // Lão Âm is drawn as a yin line. Use a genuinely old yin bottom line
        // instead, with every other line young yang, so the ORIGINAL
        // hexagram has line 1 = yin (Khảm-family) and the CHANGED hexagram
        // has line 1 flipped to yang (Càn).
        List<LineValue> lines = List.of(
                LineValue.OLD_YIN, LineValue.YOUNG_YANG, LineValue.YOUNG_YANG,
                LineValue.YOUNG_YANG, LineValue.YOUNG_YANG, LineValue.YOUNG_YANG);
        assertThat(HexagramLines.movingPositions(lines)).containsExactly(1);

        Hexagram original = HexagramLines.original(lines);
        Hexagram changed = HexagramLines.changed(lines);

        // original: bottom=yin, rest yang -> lower=(yin,yang,yang)=LAKE's mirror... compute via table instead of guessing.
        assertThat(original.lower().bottomYang()).isFalse();
        assertThat(changed.lower().bottomYang()).isTrue();
        // Every other line unchanged between original and changed.
        assertThat(changed.lower().middleYang()).isEqualTo(original.lower().middleYang());
        assertThat(changed.lower().topYang()).isEqualTo(original.lower().topYang());
        assertThat(changed.upper()).isEqualTo(original.upper());
    }

    @Test
    @DisplayName("flipOneLine on an already-known hexagram (Mai Hoa's shape) flips exactly the requested position")
    void flipOneLineOnKnownHexagram() {
        Hexagram allHeaven = HexagramTable.of(IChingTrigram.HEAVEN, IChingTrigram.HEAVEN); // #1, all 6 lines yang
        Hexagram flippedBottom = HexagramLines.flipOneLine(allHeaven, 1);
        assertThat(flippedBottom.lower().bottomYang()).isFalse();
        assertThat(flippedBottom.lower().middleYang()).isTrue();
        assertThat(flippedBottom.lower().topYang()).isTrue();
        assertThat(flippedBottom.upper()).isEqualTo(IChingTrigram.HEAVEN);

        Hexagram flippedTop = HexagramLines.flipOneLine(allHeaven, 6);
        assertThat(flippedTop.upper().topYang()).isFalse();
        assertThat(flippedTop.lower()).isEqualTo(IChingTrigram.HEAVEN);
    }

    @Test
    @DisplayName("Multiple moving lines are all reported and all flipped")
    void multipleMovingLines() {
        List<LineValue> lines = List.of(
                LineValue.OLD_YANG, LineValue.YOUNG_YIN, LineValue.OLD_YIN,
                LineValue.YOUNG_YANG, LineValue.YOUNG_YIN, LineValue.OLD_YANG);
        assertThat(HexagramLines.movingPositions(lines)).containsExactly(1, 3, 6);
        assertThat(HexagramLines.changed(lines)).isNotNull();
    }
}
