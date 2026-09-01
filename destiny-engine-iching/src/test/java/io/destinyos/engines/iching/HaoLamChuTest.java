package io.destinyos.engines.iching;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Invariants for {@link HaoLamChu}, including this module's first golden test
 * in the project's own sense of the term — a worked example with the answer
 * printed in an independent source, checked end to end.
 *
 * <p>Source for every expectation here: Nguyễn Hiến Lê, <em>Kinh Dịch — Đạo
 * Của Người Quân Tử</em>, NXB Văn Học, tr.101–103.
 */
class HaoLamChuTest {

    @Nested
    @DisplayName("Golden: the two worked examples the source prints")
    class Golden {

        @Test
        @DisplayName("Quẻ 16 Lôi địa Dự — five âm, one dương at line 4 → hào 4 governs")
        void queDu() {
            // tr.102, verbatim: "Thí dụ quẻ Lôi địa Dự có năm hào âm, một hào
            // dương (hào thứ tư) thì lấy hào dương đó làm chủ cả quẻ, hào chủ
            // động trong quẻ, ý nghĩa toàn quẻ tùy thuộc nó cả."
            assertThat(HaoLamChu.of(HexagramTable.byNumber(16))).contains(4);
            assertThat(HaoLamChu.isYang(HexagramTable.byNumber(16), 4)).isTrue();
        }

        @Test
        @DisplayName("Quẻ 43 Trạch thiên Quải — five dương, one âm at line 6 → hào 6 governs")
        void queQuai() {
            // tr.102, verbatim: "quẻ Trạch thiên Quải có năm hào dương, một hào
            // âm thì lấy hào âm (hào 6) làm chủ".
            assertThat(HaoLamChu.of(HexagramTable.byNumber(43))).contains(6);
            assertThat(HaoLamChu.isYang(HexagramTable.byNumber(43), 6)).isFalse();
        }

        @Test
        @DisplayName("The two examples disagree about good and bad, which is the source's own point")
        void governingSaysNothingAboutPolarity() {
            // tr.102: "Tóm lại một hào tốt (hào 4 trong quẻ Lôi địa Dự) làm chủ
            // cả quẻ mà một hào xấu (hào 6 trong quẻ Trạch thiên Quải) cũng có
            // thể làm chủ cả quẻ." One favourable, one unfavourable, same rule.
            // Pinned as a test because it is exactly the inference a future
            // reader is most likely to make and the source most explicitly bars.
            assertThat(HaoLamChu.of(HexagramTable.byNumber(16))).isPresent();
            assertThat(HaoLamChu.of(HexagramTable.byNumber(43))).isPresent();
            assertThat(HaoLamChu.NEUTRALITY_NOTE_VI)
                    .contains("không phải vì tốt hay xấu");
        }
    }

    @Nested
    @DisplayName("The rule speaks only when exactly one line stands alone")
    class MinorityOfOne {

        @Test
        @DisplayName("Pure Kiền and pure Khôn have no governing line — there is no minority at all")
        void pureHexagramsHaveNone() {
            // tr.102 states the rule "không kể hai quẻ càn, khôn ba hào đều
            // dương hoặc đều âm".
            assertThat(HaoLamChu.of(HexagramTable.byNumber(1))).isEmpty();
            assertThat(HaoLamChu.of(HexagramTable.byNumber(2))).isEmpty();
        }

        @Test
        @DisplayName("A 3-3, 4-2 or 2-4 split has no governing line rather than an arbitrary one")
        void balancedHexagramsHaveNone() {
            // Empty is the honest answer here. Picking "the first minority line"
            // in a 4-2 split would be inventing a rule the source never states,
            // and it would be invisible — the function would keep returning a
            // plausible number for every hexagram.
            for (int number = 1; number <= 64; number++) {
                Hexagram hexagram = HexagramTable.byNumber(number);
                int yang = 0;
                for (int position = 1; position <= 6; position++) {
                    if (HaoLamChu.isYang(hexagram, position)) {
                        yang++;
                    }
                }
                boolean hasMinorityOfOne = yang == 1 || yang == 5;
                assertThat(HaoLamChu.of(hexagram).isPresent())
                        .as("hexagram %d has %d dương lines", number, yang)
                        .isEqualTo(hasMinorityOfOne);
            }
        }

        @Test
        @DisplayName("Exactly twelve of the 64 hexagrams have a governing line under this rule")
        void countIsDerivable() {
            // Independently derivable rather than looked up: there are six ways
            // to place a single dương among five âm and six the other way, so
            // twelve hexagrams and no others can qualify. A change in this
            // number means the counting logic drifted, not that the data did.
            long withGoverningLine = 0;
            for (int number = 1; number <= 64; number++) {
                if (HaoLamChu.of(HexagramTable.byNumber(number)).isPresent()) {
                    withGoverningLine++;
                }
            }
            assertThat(withGoverningLine).isEqualTo(12);
        }
    }

    @Nested
    @DisplayName("The exception the source names is carried, not compiled in")
    class SourceNamedException {

        @Test
        @DisplayName("Quẻ 44 Cấu still returns hào 1 — the rule is not silently special-cased")
        void cauStillFollowsTheRule() {
            // tr.103: "qui tắc 'chúng dĩ quả vi chủ' có nhiều lệ ngoại, như quẻ
            // Cấu, hào 1 là hào âm duy nhất mà không phải là hào quan trọng
            // nhất". Returning something else for quẻ 44 would make of() stop
            // matching the rule it documents, and the divergence would be
            // invisible at the call site. The caveat travels alongside instead.
            Optional<Integer> governing = HaoLamChu.of(HexagramTable.byNumber(44));
            assertThat(governing).contains(1);
            assertThat(HaoLamChu.isYang(HexagramTable.byNumber(44), 1)).isFalse();
            assertThat(HaoLamChu.isSourceNamedException(44)).isTrue();
        }

        @Test
        @DisplayName("Quẻ 44 is the only hexagram flagged as an exception")
        void onlyCauIsFlagged() {
            for (int number = 1; number <= 64; number++) {
                assertThat(HaoLamChu.isSourceNamedException(number))
                        .as("hexagram %d", number)
                        .isEqualTo(number == 44);
            }
        }

        @Test
        @DisplayName("Both caveats quote the source rather than paraphrasing it")
        void caveatsQuoteTheSource() {
            assertThat(HaoLamChu.EXCEPTION_NOTE_VI).contains("tr.103", "quẻ Cấu");
            assertThat(HaoLamChu.NEUTRALITY_NOTE_VI).contains("tr.102");
        }
    }

    @Nested
    @DisplayName("Argument handling")
    class Arguments {

        @Test
        @DisplayName("A position outside 1-6 is rejected rather than wrapped")
        void positionIsRangeChecked() {
            Hexagram hexagram = HexagramTable.byNumber(16);
            assertThat(HaoLamChu.isYang(hexagram, 1)).isFalse();
            assertThat(HaoLamChu.isYang(hexagram, 4)).isTrue();
            org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                    () -> HaoLamChu.isYang(hexagram, 0));
            org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                    () -> HaoLamChu.isYang(hexagram, 7));
        }
    }
}
