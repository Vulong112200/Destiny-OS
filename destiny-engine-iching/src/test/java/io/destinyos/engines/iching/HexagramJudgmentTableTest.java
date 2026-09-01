package io.destinyos.engines.iching;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Content invariants for the R24/R25 quẻ từ / hào từ tables.
 *
 * <p><strong>These are derivation checks, not presence checks, on purpose.</strong>
 * The first version of this test asserted only {@code isNotBlank()} on each
 * field. It passed while 287 of 386 line entries carried the wrong Chinese
 * text — the parser had grabbed the neighbouring 象曰 commentary, which is
 * non-blank and looks entirely plausible. A test that pins the wrong content
 * into the spec is worse than no test, and this project has been bitten by
 * exactly that before (see the withdrawn R22 golden test in
 * {@code docs/DECISION_LOG.md}). So every assertion below checks a property
 * that can be <em>computed independently</em> of the data it is checking.
 */
class HexagramJudgmentTableTest {

    /** Trigram line values, bottom-to-top, yang = true — mirrors {@link IChingTrigram}. */
    private static boolean[] lines(IChingTrigram t) {
        return new boolean[] {t.bottomYang(), t.middleYang(), t.topYang()};
    }

    /** Is line {@code position} (1 = bottom) of this hexagram yang? */
    private static boolean isYang(int kingWenNumber, int position) {
        Hexagram h = HexagramTable.byNumber(kingWenNumber);
        boolean[] lower = lines(h.lower());
        boolean[] upper = lines(h.upper());
        return position <= 3 ? lower[position - 1] : upper[position - 4];
    }

    @Nested
    @DisplayName("Completeness")
    class Completeness {

        @Test
        @DisplayName("All 64 hexagrams have a quẻ từ")
        void allHexagramsHaveQueTu() {
            for (int number = 1; number <= 64; number++) {
                int n = number;
                HexagramJudgment j = HexagramJudgmentTable.byNumber(number)
                        .orElseThrow(() -> new AssertionError("Missing quẻ từ for hexagram " + n));
                assertThat(j.number()).isEqualTo(number);
                assertThat(j.sourcePage()).as("sourcePage for %d", number).isGreaterThan(0);
            }
        }

        @Test
        @DisplayName("Every hexagram has exactly six hào từ, positions 1-6")
        void everyHexagramHasSixLines() {
            for (int number = 1; number <= 64; number++) {
                for (int position = 1; position <= 6; position++) {
                    int n = number;
                    int p = position;
                    LineJudgment line = LineJudgmentTable.at(number, position)
                            .orElseThrow(() -> new AssertionError(
                                    "Missing hào từ for hexagram " + n + " position " + p));
                    assertThat(line.hexagramNumber()).isEqualTo(number);
                    assertThat(line.position()).isEqualTo(position);
                }
            }
        }

        @Test
        @DisplayName("Only Kiền and Khôn carry the classical dụng cửu / dụng lục line")
        void onlyKienAndKhonHaveDungLine() {
            assertThat(LineJudgmentTable.dungLine(1).orElseThrow().label()).isEqualTo("Dụng Cửu");
            assertThat(LineJudgmentTable.dungLine(2).orElseThrow().label()).isEqualTo("Dụng Lục");
            for (int number = 3; number <= 64; number++) {
                assertThat(LineJudgmentTable.dungLine(number))
                        .as("hexagram %d should have no dụng line", number).isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("Derivable properties — the checks that caught real errors")
    class Derived {

        @Test
        @DisplayName("Each label's Cửu/Lục matches the line's actual yin/yang in that hexagram")
        void labelMatchesLineParity() {
            for (int number = 1; number <= 64; number++) {
                for (int position = 1; position <= 6; position++) {
                    LineJudgment line = LineJudgmentTable.at(number, position).orElseThrow();
                    boolean labelSaysYang = line.label().contains("Cửu");
                    assertThat(labelSaysYang)
                            .as("hexagram %d line %d is labelled '%s'", number, position, line.label())
                            .isEqualTo(isYang(number, position));
                }
            }
        }

        @Test
        @DisplayName("Chinese text is CJK Unified Ideographs — no Kangxi Radical look-alikes")
        void chineseUsesProperCodepoints() {
            for (int number = 1; number <= 64; number++) {
                assertNoRadicalCodepoints(HexagramJudgmentTable.byNumber(number).orElseThrow().hanTu(),
                        "quẻ từ " + number);
                for (int position = 1; position <= 6; position++) {
                    assertNoRadicalCodepoints(LineJudgmentTable.at(number, position).orElseThrow().hanTu(),
                            "hào từ " + number + "/" + position);
                }
            }
        }

        private void assertNoRadicalCodepoints(String text, String where) {
            text.codePoints().forEach(cp -> assertThat(cp)
                    .as("%s contains a Kangxi Radical / CJK Radical Supplement codepoint "
                            + "(U+%04X) instead of a CJK Unified Ideograph", where, cp)
                    .matches(c -> c < 0x2E80 || c > 0x2FDF));
        }

        @Test
        @DisplayName("The quẻ từ's Chinese opens with the hexagram's own name")
        void queTuNamesItsOwnHexagram() {
            for (int number = 1; number <= 64; number++) {
                HexagramJudgment j = HexagramJudgmentTable.byNumber(number).orElseThrow();
                assertThat(j.hanTu())
                        .as("quẻ từ %d (%s) should open with its own name", number, j.chineseName())
                        .startsWith(j.chineseName());
            }
        }

        @Test
        @DisplayName("Hán-Việt syllable count tracks the Chinese character count")
        void hanVietTracksChineseLength() {
            // A Hán-Việt reading is one syllable per Chinese character, so a
            // gross mismatch means the Vietnamese was paired to the wrong line.
            for (int number = 1; number <= 64; number++) {
                for (int position = 1; position <= 6; position++) {
                    LineJudgment line = LineJudgmentTable.at(number, position).orElseThrow();
                    long chars = line.hanTu().codePoints()
                            .filter(cp -> cp >= 0x4E00 && cp <= 0x9FFF).count();
                    long syllables = line.hanViet().split("[^\\p{L}]+").length;
                    assertThat(Math.abs(syllables - chars))
                            .as("hexagram %d line %d: %d Hán-Việt syllables vs %d Chinese characters "
                                    + "(hanViet=%s)", number, position, syllables, chars, line.hanViet())
                            .isLessThanOrEqualTo(Math.max(3, chars / 2));
                }
            }
        }
    }

    @Nested
    @DisplayName("Provenance is recorded, not assumed")
    class Provenance {

        @Test
        @DisplayName("Every documented repair of a source-book defect carries its reason")
        void repairsAreExplained() {
            Set<String> repaired = new HashSet<>();
            for (int number = 1; number <= 64; number++) {
                for (int position = 0; position <= 6; position++) {
                    String key = number + "/" + position;
                    (position == 0 ? LineJudgmentTable.dungLine(number)
                                   : LineJudgmentTable.at(number, position))
                            .flatMap(LineJudgment::noteIfPresent)
                            .ifPresent(note -> {
                                assertThat(note).hasSizeGreaterThan(40);
                                repaired.add(key);
                            });
                }
            }
            // Two distinct categories, pinned separately and exhaustively so a
            // sixth defect of either kind cannot be added without this test
            // going red and forcing someone to say which kind it is.
            //
            // (1) Defects in the SOURCE BOOK itself — the print or the scan is
            //     wrong and the repair had to be argued case by case against the
            //     hexagram's own structure. See LineJudgment's Javadoc and R24 §C.
            Set<String> sourceDefects = Set.of(
                    "2/6",   // Khôn Thượng Lục — book printed "Dịch nghĩa" twice
                    "45/1",  // Tụy — book's label contradicts the hexagram's structure
                    "48/1",  // Tỉnh — book repeats the transliteration twice
                    "51/4",  // Chấn — book's label contradicts its own gloss
                    "53/2",  // Tiệm — book omits the line label entirely
                    "45/6"); // Tụy — book prints "Háo" for "Hào"

            // (2) Defects this project's own EXTRACTION introduced, found and
            //     repaired 2026-09-01. The book is fine here; the extractor ran
            //     the book's GIẢI NGHĨA commentary into the `nghia` field with no
            //     separator, so entries held a translation followed by an essay.
            //     The cut is mechanical — at the commentary's own marker — which
            //     is why these needed no case-by-case argument, and why they are
            //     listed apart from category (1) rather than blurred into it.
            Set<String> extractionDefects = Set.of(
                    "1/1", "6/3", "8/5", "9/5", "11/5", "16/3", "20/6", "22/4",
                    "24/6", "28/5", "31/5", "34/4", "36/3", "37/4", "41/1", "42/5",
                    "50/1", "51/2", "52/4", "53/5", "54/4", "57/2", "63/6", "64/4",
                    "16/6"); // this one held Tượng truyện text instead of the gloss

            Set<String> expected = new HashSet<>(sourceDefects);
            expected.addAll(extractionDefects);
            assertThat(repaired).containsExactlyInAnyOrderElementsOf(expected);
        }

        @Test
        @DisplayName("No entry's gloss carries the book's commentary — the defect the old suite could not see")
        void glossCarriesNoCommentary() {
            // The previous version of this suite asserted only that `nghia` was
            // non-blank. A 2,000-character entry whose first 32 characters are
            // the translation and whose remaining 2,000 are Trình Di's essay
            // satisfies that perfectly, which is how 27 wrong entries shipped.
            // These markers are the commentary's own section headings, including
            // the OCR-mangled "GỊẢI" the scan produced in one place.
            List<String> commentaryMarkers = List.of(
                    "GIẢI NGHĨA", "GỊẢI NGHĨA",
                    "Truyện của Trình Di", "Truyện của Trinh Di",
                    "Bản nghĩa của Trình Di", "Bản nghĩa của Chu Hy",
                    "Lời bàn của Tiên Nho", "Lời Tượng nói rằng");

            for (int number = 1; number <= 64; number++) {
                String queTu = HexagramJudgmentTable.byNumber(number).orElseThrow().nghia();
                assertThat(queTu).as("quẻ từ %d", number).doesNotContain(commentaryMarkers);
                for (int position = 0; position <= 6; position++) {
                    (position == 0 ? LineJudgmentTable.dungLine(number)
                                   : LineJudgmentTable.at(number, position))
                            .ifPresent(line -> assertThat(line.nghia())
                                    .as("hào từ %d/%d", line.hexagramNumber(), line.position())
                                    .doesNotContain(commentaryMarkers));
                }
            }
        }

        @Test
        @DisplayName("Every gloss opens with the position label derivable from the line itself")
        void glossOpensWithItsOwnLabel() {
            // This is what caught 16/6, whose gloss opened "Lời Tượng nói rằng"
            // — grammatical, plausible, and the wrong text entirely.
            //
            // Four entries legitimately have no label, and they are pinned here
            // rather than excused by a loosened assertion. Ngô Tất Tố simply
            // does not reprint the position label at those four places, and his
            // wording is not edited to satisfy a test: the translation is a
            // named translator's text (R24 §C1), so a missing label is a fact
            // about the book, not a defect to patch. Pinning the set means a
            // fifth one cannot appear unnoticed.
            Set<String> bookPrintsNoLabel = Set.of(
                    "2/6",   // Khôn Thượng Lục
                    "38/5",  // Khuê Lục Ngũ
                    "47/5",  // Khốn Cửu Ngũ
                    "53/2"); // Tiệm Lục Nhị

            for (int number = 1; number <= 64; number++) {
                for (int position = 1; position <= 6; position++) {
                    LineJudgment line = LineJudgmentTable.at(number, position).orElseThrow();
                    if (bookPrintsNoLabel.contains(number + "/" + position)) {
                        assertThat(line.nghia())
                                .as("hào từ %d/%d: sách không in nhãn, nên phải KHÔNG có tiền tố "
                                        + "— nếu có thì ai đó đã thêm chữ vào lời dịch",
                                        number, position)
                                .doesNotStartWith("Hào ");
                        continue;
                    }
                    assertThat(line.nghia())
                            .as("hào từ %d/%d phải mở đầu bằng nhãn vị trí của chính nó", number, position)
                            .startsWith("Hào ");
                }
            }
        }

        @Test
        @DisplayName("A gloss stays proportionate to the Chinese it translates")
        void glossLengthTracksTheChinese() {
            // The cheapest possible guard against commentary bleeding back in.
            // A gloss is a rendering of a short classical line, so it cannot run
            // to twenty times the character count of what it renders. Generous
            // on purpose: this is a tripwire for a 2,000-character essay, not an
            // opinion about how long a good translation should be.
            for (int number = 1; number <= 64; number++) {
                for (int position = 1; position <= 6; position++) {
                    LineJudgment line = LineJudgmentTable.at(number, position).orElseThrow();
                    long chineseChars = line.hanTu().codePoints()
                            .filter(cp -> cp >= 0x4E00 && cp <= 0x9FFF).count();
                    assertThat((long) line.nghia().length())
                            .as("hào từ %d/%d: %d chữ Hán nhưng %d ký tự dịch nghĩa",
                                    number, position, chineseChars, line.nghia().length())
                            .isLessThan(Math.max(200L, chineseChars * 20L));
                }
            }
        }

        @Test
        @DisplayName("The line table holds exactly 386 entries — a duplicate key would otherwise overwrite in silence")
        void lineTableHoldsEveryEntryExactlyOnce() {
            // LineJudgmentTable keys on hexagram*10+position across four source
            // files with no cross-file check. Two entries claiming the same
            // (hexagram, position) would have the later one silently replace the
            // earlier, and every completeness assertion here would still pass
            // because the key would still resolve. Only a count catches it.
            assertThat(LineJudgmentTable.size()).isEqualTo(386);
        }

        @Test
        @DisplayName("Where the book's own Chinese was OCR-damaged, that is recorded per entry")
        void bookDivergenceIsRecorded() {
            long divergentLines = 0;
            for (int number = 1; number <= 64; number++) {
                for (int position = 1; position <= 6; position++) {
                    if (LineJudgmentTable.at(number, position).orElseThrow().bookHanDiffered()) {
                        divergentLines++;
                    }
                }
            }
            // Measured during verification: the book's text layer diverged from
            // the canonical Chinese on most lines, which is why hanTu is sourced
            // from wikisource rather than from the book. Asserted as a range so
            // the finding stays visible without pinning a brittle exact count.
            assertThat(divergentLines).isBetween(200L, 320L);
        }
    }
}
