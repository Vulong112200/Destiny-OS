package io.destinyos.engines.iching;

import static org.assertj.core.api.Assertions.assertThat;

import io.destinyos.core.signal.Polarity;
import io.destinyos.core.signal.Strength;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Invariants for {@link CatHungLexicon}.
 *
 * <p><strong>The interesting tests here are the cross-checks, not the unit
 * cases.</strong> A lexicon is the kind of table that is easy to write
 * confidently and wrong, so the valences are checked against a source this
 * project already cites and ships: Ngô Tất Tố's Vietnamese translation, which
 * sits in the same records as the Chinese being scanned. If 吉 really means
 * "tốt lành", then his rendering of a line containing 吉 should say "tốt" —
 * and it does, in 95% of them. That turns the table from an assertion into
 * something a second source agrees with, and it is the check that would catch
 * a future edit quietly flipping a polarity.
 */
class CatHungLexiconTest {

    @Nested
    @DisplayName("Compound terms win over the characters they contain")
    class Ordering {

        @Test
        @DisplayName("无咎 reads as favourable, never as the 咎 inside it")
        void voCuuIsNotCuu() {
            // The single most consequential case: 92 of the shipped entries
            // carry 无咎/無咎 against 7 with a bare 咎. Getting this backwards
            // would mislabel 92 entries as faulty when their text says the
            // opposite — "không có lỗi".
            List<CatHungLexicon.Match> matches = CatHungLexicon.scan("无咎");
            assertThat(matches).hasSize(1);
            assertThat(matches.getFirst().term()).isEqualTo("无咎");
            assertThat(matches.getFirst().polarity()).isEqualTo(Polarity.SUPPORT);
        }

        @Test
        @DisplayName("無咎 (the other orthography) reads the same way")
        void voCuuTraditionalForm() {
            assertThat(CatHungLexicon.scan("無咎")).singleElement()
                    .satisfies(m -> assertThat(m.polarity()).isEqualTo(Polarity.SUPPORT));
        }

        @Test
        @DisplayName("A bare 咎 with no 无 in front of it reads as a fault")
        void bareCuuIsCaution() {
            assertThat(CatHungLexicon.scan("何咎")).singleElement()
                    .satisfies(m -> {
                        assertThat(m.term()).isEqualTo("咎");
                        assertThat(m.polarity()).isEqualTo(Polarity.CAUTION);
                    });
        }

        @Test
        @DisplayName("悔亡 inverts 悔 — the regret disappears")
        void hoiVongIsNotHoi() {
            assertThat(CatHungLexicon.scan("悔亡")).singleElement()
                    .satisfies(m -> assertThat(m.polarity()).isEqualTo(Polarity.SUPPORT));
            assertThat(CatHungLexicon.scan("小有悔")).singleElement()
                    .satisfies(m -> assertThat(m.polarity()).isEqualTo(Polarity.CAUTION));
        }

        @Test
        @DisplayName("元吉 outranks a plain 吉 in strength, not just in name")
        void nguyenCatIsStronger() {
            assertThat(CatHungLexicon.scan("元吉")).singleElement()
                    .satisfies(m -> assertThat(m.strength()).isEqualTo(Strength.STRONG));
            assertThat(CatHungLexicon.scan("小人吉")).singleElement()
                    .satisfies(m -> assertThat(m.strength()).isEqualTo(Strength.MEDIUM));
        }

        @Test
        @DisplayName("A character is claimed once — no term double-counts another's characters")
        void charactersAreClaimedOnce() {
            // 貞吉 contains 吉. If both matched, one line would emit two
            // favourable signals for what the text says once, inflating it.
            assertThat(CatHungLexicon.scan("貞吉")).singleElement()
                    .satisfies(m -> assertThat(m.term()).isEqualTo("貞吉"));
        }
    }

    @Nested
    @DisplayName("What the lexicon deliberately declines to score")
    class DeclaredExclusions {

        @Test
        @DisplayName("貞 alone scores nothing — it is a condition, not a verdict")
        void trinhAloneIsNotAVerdict() {
            // tr.173: "Trinh là chính và bền". A virtue held conditionally.
            // Scoring it would turn "cát if you hold to chính bền" into an
            // unconditional favourable reading.
            assertThat(CatHungLexicon.scan("利貞")).isEmpty();
        }

        @Test
        @DisplayName("Tứ đức (元, 亨, 利) score nothing on their own")
        void tuDucAreNotVerdicts() {
            // The source lists five competing readings of tứ đức and declares
            // it follows one of them; reading (đ) takes 亨 as 享, a sacrificial
            // offering, which is not a judgement at all.
            assertThat(CatHungLexicon.scan("元亨利貞")).isEmpty();
        }

        @Test
        @DisplayName("孚 (trust) and 厲 (danger) score nothing — one is not a verdict, one is unglossed")
        void unglossedTermsScoreNothing() {
            assertThat(CatHungLexicon.scan("有孚")).isEmpty();
            assertThat(CatHungLexicon.scan("夕惕若厲")).isEmpty();
        }

        @Test
        @DisplayName("A text with no judgment vocabulary yields nothing rather than a guess")
        void silenceIsSilence() {
            // Hexagram 1's first line: 潛龍勿用 — an instruction, not a verdict.
            assertThat(CatHungLexicon.scan("潛龍勿用。")).isEmpty();
            assertThat(CatHungLexicon.scan(null)).isEmpty();
            assertThat(CatHungLexicon.scan("   ")).isEmpty();
        }
    }

    @Nested
    @DisplayName("Cross-checked against Ngô Tất Tố's own translation")
    class CrossCheckedAgainstTheTranslation {

        /**
         * The check that makes this table more than an assertion: for each
         * scored character, the translator this project already cites should
         * render it with a matching Vietnamese word. Thresholds are below the
         * measured rates so normal editing does not break the build, but high
         * enough that a flipped polarity would.
         */
        @Test
        @DisplayName("吉 is rendered 'tốt', 咎 'lỗi', 吝 'tiếc' — the translator agrees with the valences")
        void translationAgreesWithTheTable() {
            assertAgreement("吉", "tốt", 0.85);
            assertAgreement("咎", "lỗi", 0.85);
            assertAgreement("吝", "tiếc", 0.80);
        }

        private void assertAgreement(String chinese, String vietnamese, double minRate) {
            int carrying = 0;
            int agreeing = 0;
            for (int number = 1; number <= 64; number++) {
                HexagramJudgment que = HexagramJudgmentTable.byNumber(number).orElseThrow();
                if (que.hanTu().contains(chinese)) {
                    carrying++;
                    if (que.nghia().toLowerCase().contains(vietnamese)) {
                        agreeing++;
                    }
                }
                for (int position = 1; position <= 6; position++) {
                    LineJudgment line = LineJudgmentTable.at(number, position).orElseThrow();
                    if (line.hanTu().contains(chinese)) {
                        carrying++;
                        if (line.nghia().toLowerCase().contains(vietnamese)) {
                            agreeing++;
                        }
                    }
                }
            }
            assertThat(carrying)
                    .as("%s should appear in the shipped Chinese at all", chinese)
                    .isGreaterThan(10);
            assertThat((double) agreeing / carrying)
                    .as("%d of %d entries containing %s render it as '%s'",
                            agreeing, carrying, chinese, vietnamese)
                    .isGreaterThanOrEqualTo(minRate);
        }
    }

    @Nested
    @DisplayName("Coverage over the whole shipped corpus")
    class Corpus {

        @Test
        @DisplayName("Roughly two thirds of entries carry a verdict; the rest are honestly silent")
        void coverageIsMeasuredNotAssumed() {
            int total = 0;
            int silent = 0;
            int mixed = 0;
            for (int number = 1; number <= 64; number++) {
                total++;
                List<CatHungLexicon.Match> que = CatHungLexicon.scan(
                        HexagramJudgmentTable.byNumber(number).orElseThrow().hanTu());
                if (que.isEmpty()) {
                    silent++;
                } else if (CatHungLexicon.isMixed(que)) {
                    mixed++;
                }
                for (int position = 1; position <= 6; position++) {
                    total++;
                    List<CatHungLexicon.Match> hao = CatHungLexicon.scan(
                            LineJudgmentTable.at(number, position).orElseThrow().hanTu());
                    if (hao.isEmpty()) {
                        silent++;
                    } else if (CatHungLexicon.isMixed(hao)) {
                        mixed++;
                    }
                }
            }
            assertThat(total).isEqualTo(448); // 64 quẻ từ + 384 hào từ

            // Ranges rather than exact counts: these are properties of the
            // corpus, and pinning them exactly would make any legitimate text
            // repair look like a lexicon regression. Wide enough to survive
            // editing, narrow enough that losing the compound-first ordering —
            // which would move ~92 entries across the mixed boundary — fails.
            assertThat(silent).as("entries with no judgment vocabulary").isBetween(120, 190);
            assertThat(mixed).as("entries carrying both a favourable and an unfavourable term")
                    .isBetween(15, 45);
        }

        @Test
        @DisplayName("Positive terms dominate, as the classical text does — a sign-flip would show here")
        void polarityMixIsPlausible() {
            Map<Polarity, Integer> tally = new TreeMap<>();
            for (int number = 1; number <= 64; number++) {
                count(tally, HexagramJudgmentTable.byNumber(number).orElseThrow().hanTu());
                for (int position = 1; position <= 6; position++) {
                    count(tally, LineJudgmentTable.at(number, position).orElseThrow().hanTu());
                }
            }
            // 无咎 alone accounts for ~92 favourable readings, so favourable
            // must outnumber unfavourable by a wide margin. If a future edit
            // dropped the compound handling, those 92 would land in CAUTION
            // and this ratio would invert.
            int favourable = tally.getOrDefault(Polarity.SUPPORT, 0);
            int unfavourable = tally.getOrDefault(Polarity.CAUTION, 0)
                    + tally.getOrDefault(Polarity.NEGATIVE, 0);
            assertThat(favourable).isGreaterThan(2 * unfavourable);
        }

        private void count(Map<Polarity, Integer> tally, String hanTu) {
            for (CatHungLexicon.Match match : CatHungLexicon.scan(hanTu)) {
                tally.merge(match.polarity(), 1, Integer::sum);
            }
        }
    }

    @Nested
    @DisplayName("Provenance of each entry is declared (Rule D)")
    class Provenance {

        @Test
        @DisplayName("The five glossed terms say so; every derived compound says it is derived")
        void glossedAndDerivedAreDistinguished() {
            // A reader must be able to tell "this is what the source says" from
            // "this is what we inferred". Collapsing the two is how a project
            // ends up citing itself.
            assertThat(CatHungLexicon.scan("吉").getFirst().fromGloss()).isTrue();
            assertThat(CatHungLexicon.scan("凶").getFirst().fromGloss()).isTrue();
            assertThat(CatHungLexicon.scan("无咎").getFirst().fromGloss()).isTrue();
            assertThat(CatHungLexicon.scan("元吉").getFirst().fromGloss()).isFalse();
            assertThat(CatHungLexicon.scan("悔亡").getFirst().fromGloss()).isFalse();
        }

        @Test
        @DisplayName("Every match reports where in the text it was found")
        void positionIsRecorded() {
            List<CatHungLexicon.Match> matches = CatHungLexicon.scan("小有悔，終吉。");
            assertThat(matches).hasSize(2);
            assertThat(matches.get(0).position()).isLessThan(matches.get(1).position());
            assertThat(matches).allSatisfy(m -> assertThat(m.glossVi()).isNotBlank());
        }
    }
}
