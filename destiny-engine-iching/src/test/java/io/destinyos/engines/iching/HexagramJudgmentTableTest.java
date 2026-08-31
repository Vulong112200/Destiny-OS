package io.destinyos.engines.iching;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Completeness of the R24/R25 content tables — every hexagram must have a
 * quẻ từ and exactly six hào từ (plus the two classical "dụng" specials for
 * hexagrams 1/2), matching the count HexagramTable itself already guarantees
 * for the 64-hexagram King Wen sequence.
 */
class HexagramJudgmentTableTest {

    @Test
    @DisplayName("All 64 hexagrams have a quẻ từ, with non-blank Hán tự/Hán Việt/nghĩa")
    void allHexagramsHaveQueTu() {
        for (int number = 1; number <= 64; number++) {
            int hexagramNumber = number;
            HexagramJudgment judgment = HexagramJudgmentTable.byNumber(number)
                    .orElseThrow(() -> new AssertionError("Missing quẻ từ for hexagram " + hexagramNumber));
            assertThat(judgment.hanTu()).as("hanTu for %d", number).isNotBlank();
            assertThat(judgment.hanViet()).as("hanViet for %d", number).isNotBlank();
            assertThat(judgment.nghia()).as("nghia for %d", number).isNotBlank();
            assertThat(judgment.sourcePage()).as("sourcePage for %d", number).isGreaterThan(0);
        }
    }

    @Test
    @DisplayName("Every hexagram has exactly six hào từ (positions 1-6), non-blank")
    void everyHexagramHasSixLines() {
        for (int number = 1; number <= 64; number++) {
            for (int position = 1; position <= 6; position++) {
                int hexagramNumber = number;
                int linePosition = position;
                LineJudgment line = LineJudgmentTable.at(number, position)
                        .orElseThrow(() -> new AssertionError(
                                "Missing hào từ for hexagram " + hexagramNumber + " position " + linePosition));
                assertThat(line.hexagramNumber()).isEqualTo(number);
                assertThat(line.position()).isEqualTo(position);
                assertThat(line.hanTu()).as("hanTu for %d/%d", number, position).isNotBlank();
                assertThat(line.hanViet()).as("hanViet for %d/%d", number, position).isNotBlank();
                assertThat(line.nghia()).as("nghia for %d/%d", number, position).isNotBlank();
            }
        }
    }

    @Test
    @DisplayName("Only hexagrams 1 (Kiền) and 2 (Khôn) have a dụng cửu/dụng lục line")
    void onlyKienAndKhonHaveDungLine() {
        assertThat(LineJudgmentTable.dungLine(1)).isPresent();
        assertThat(LineJudgmentTable.dungLine(2)).isPresent();
        for (int number = 3; number <= 64; number++) {
            assertThat(LineJudgmentTable.dungLine(number))
                    .as("hexagram %d should have no dụng line", number)
                    .isEmpty();
        }
    }

    @Test
    @DisplayName("Hexagrams 1-8's Hán tự is flagged cross-checked (R24 pilot); 9-64 is not")
    void crossCheckFlagMatchesR24Pilot() {
        for (int number = 1; number <= 8; number++) {
            assertThat(HexagramJudgmentTable.byNumber(number).orElseThrow().hanTuCrossChecked())
                    .as("hexagram %d", number).isTrue();
        }
        for (int number = 9; number <= 64; number++) {
            assertThat(HexagramJudgmentTable.byNumber(number).orElseThrow().hanTuCrossChecked())
                    .as("hexagram %d", number).isFalse();
        }
    }
}
