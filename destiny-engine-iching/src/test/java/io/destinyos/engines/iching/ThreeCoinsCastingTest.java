package io.destinyos.engines.iching;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Three Coins — distribution verified by exhaustive enumeration (2^3 = 8
 * equally likely paths), not Monte Carlo, per
 * {@code VERIFICATION_OPUS_R12.md} §A6/§C: an exact integer count is
 * possible here, so a statistical/tolerance-based test would be strictly
 * worse.
 */
class ThreeCoinsCastingTest {

    @Test
    @DisplayName("Exhaustive enumeration of all 8 coin-triples gives exactly 1:3:3:1 (6:7:8:9)")
    void exactDistributionOverAllEightOutcomes() {
        Map<LineValue, Integer> counts = new EnumMap<>(LineValue.class);
        for (boolean c1 : List.of(true, false)) {
            for (boolean c2 : List.of(true, false)) {
                for (boolean c3 : List.of(true, false)) {
                    LineValue v = LineValue.fromTotal(ThreeCoinsCasting.coinTotal(c1, c2, c3));
                    counts.merge(v, 1, Integer::sum);
                }
            }
        }
        assertThat(counts.get(LineValue.OLD_YIN)).isEqualTo(1);
        assertThat(counts.get(LineValue.YOUNG_YANG)).isEqualTo(3);
        assertThat(counts.get(LineValue.YOUNG_YIN)).isEqualTo(3);
        assertThat(counts.get(LineValue.OLD_YANG)).isEqualTo(1);
        assertThat(counts.values().stream().mapToInt(Integer::intValue).sum()).isEqualTo(8);
    }

    @Test
    @DisplayName("A cast always produces exactly 6 lines, each a valid LineValue")
    void castProducesSixLines() {
        List<LineValue> lines = ThreeCoinsCasting.cast(new Random(42));
        assertThat(lines).hasSize(6);
    }

    @Test
    @DisplayName("Same seed reproduces the same reading")
    void deterministicGivenSeed() {
        assertThat(ThreeCoinsCasting.cast(new Random(1234)))
                .isEqualTo(ThreeCoinsCasting.cast(new Random(1234)));
    }
}
