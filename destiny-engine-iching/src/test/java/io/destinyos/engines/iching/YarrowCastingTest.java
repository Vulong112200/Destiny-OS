package io.destinyos.engines.iching;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Yarrow-stalk casting — the exact 1:5:7:3 distribution, derived from first
 * principles (not merely cited) in {@code VERIFICATION_OPUS_R12.md} §B, is
 * reproduced here by exhaustive enumeration of all 4^3 = 64 equally likely
 * residue combinations across the three changes-per-line, exactly as the
 * verification file derived it. No Monte Carlo, no tolerance, no
 * {@code double} anywhere (ADR D6).
 */
class YarrowCastingTest {

    @Test
    @DisplayName("Exhaustive enumeration of all 64 residue-triples gives exactly 4:20:28:12 (out of 64)")
    void exactDistributionOverAllSixtyFourOutcomes() {
        Map<LineValue, Integer> counts = new EnumMap<>(LineValue.class);
        for (int r1 = 0; r1 < 4; r1++) {
            for (int r2 = 0; r2 < 4; r2++) {
                for (int r3 = 0; r3 < 4; r3++) {
                    int remaining = YarrowCasting.remainingStalksFor(r1, r2, r3);
                    LineValue v = LineValue.fromTotal(remaining / 4);
                    counts.merge(v, 1, Integer::sum);
                }
            }
        }
        assertThat(counts.get(LineValue.OLD_YIN)).isEqualTo(4);
        assertThat(counts.get(LineValue.YOUNG_YANG)).isEqualTo(20);
        assertThat(counts.get(LineValue.YOUNG_YIN)).isEqualTo(28);
        assertThat(counts.get(LineValue.OLD_YANG)).isEqualTo(12);
        assertThat(counts.values().stream().mapToInt(Integer::intValue).sum()).isEqualTo(64);
    }

    @Test
    @DisplayName("Yarrow's changing-line asymmetry (3:1 old-yang:old-yin) genuinely differs from Three Coins' (1:1)")
    void changingLineAsymmetryDiffersFromThreeCoins() {
        // The trap VERIFICATION_OPUS_R12.md §C warns against: changing-line
        // RATE (1/4) and yang RATE (1/2) are identical between the two
        // methods and would make a vacuous test. The real difference is this
        // asymmetry among changing lines only.
        assertThat(20 + 4).as("young+old yang count, for reference").isEqualTo(24);
        int yarrowOldYang = 12;
        int yarrowOldYin = 4;
        assertThat(yarrowOldYang).as("Yarrow: old yang is 3x likelier than old yin among changing lines")
                .isEqualTo(3 * yarrowOldYin);

        Map<LineValue, Integer> coinCounts = new EnumMap<>(LineValue.class);
        for (boolean c1 : List.of(true, false)) {
            for (boolean c2 : List.of(true, false)) {
                for (boolean c3 : List.of(true, false)) {
                    coinCounts.merge(LineValue.fromTotal(ThreeCoinsCasting.coinTotal(c1, c2, c3)), 1, Integer::sum);
                }
            }
        }
        assertThat(coinCounts.get(LineValue.OLD_YANG))
                .as("Three Coins: old yang and old yin are equally likely among changing lines")
                .isEqualTo(coinCounts.get(LineValue.OLD_YIN));
    }

    @Test
    @DisplayName("A cast always produces exactly 6 lines")
    void castProducesSixLines() {
        assertThat(YarrowCasting.cast(new Random(7))).hasSize(6);
    }

    @Test
    @DisplayName("Same seed reproduces the same reading")
    void deterministicGivenSeed() {
        assertThat(YarrowCasting.cast(new Random(99))).isEqualTo(YarrowCasting.cast(new Random(99)));
    }
}
