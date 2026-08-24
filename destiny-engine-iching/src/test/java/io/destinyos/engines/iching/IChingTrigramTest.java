package io.destinyos.engines.iching;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The 8 trigrams — arithmetic properties verified in VERIFICATION_OPUS_R12.md §A3. */
class IChingTrigramTest {

    @Test
    @DisplayName("Tiên Thiên number equals 8 minus the binary value (bottom line as MSB, yang=1)")
    void tienThienNumberIsArithmeticallyDerivable() {
        for (IChingTrigram t : IChingTrigram.values()) {
            int binary = (t.bottomYang() ? 4 : 0) + (t.middleYang() ? 2 : 0) + (t.topYang() ? 1 : 0);
            assertThat(t.tienThienNumber()).as(t.name()).isEqualTo(8 - binary);
        }
    }

    @Test
    @DisplayName("Càn is three yang lines (Tiên Thiên 1), Khôn is three yin lines (Tiên Thiên 8)")
    void pureTrigramsAtTheExtremes() {
        assertThat(IChingTrigram.HEAVEN.bottomYang()).isTrue();
        assertThat(IChingTrigram.HEAVEN.middleYang()).isTrue();
        assertThat(IChingTrigram.HEAVEN.topYang()).isTrue();
        assertThat(IChingTrigram.HEAVEN.tienThienNumber()).isEqualTo(1);

        assertThat(IChingTrigram.EARTH.bottomYang()).isFalse();
        assertThat(IChingTrigram.EARTH.middleYang()).isFalse();
        assertThat(IChingTrigram.EARTH.topYang()).isFalse();
        assertThat(IChingTrigram.EARTH.tienThienNumber()).isEqualTo(8);
    }

    @Test
    @DisplayName("fromNumber applies 卦以八除: remainder 0 reads as 8 (Khôn), never as 0")
    void fromNumberHandlesRemainderZeroAsEight() {
        assertThat(IChingTrigram.fromNumber(8)).isEqualTo(IChingTrigram.EARTH);
        assertThat(IChingTrigram.fromNumber(16)).isEqualTo(IChingTrigram.EARTH);
        assertThat(IChingTrigram.fromNumber(1)).isEqualTo(IChingTrigram.HEAVEN);
        assertThat(IChingTrigram.fromNumber(9)).isEqualTo(IChingTrigram.HEAVEN);
        assertThat(IChingTrigram.fromNumber(3)).isEqualTo(IChingTrigram.FIRE);
        assertThat(IChingTrigram.fromNumber(6)).isEqualTo(IChingTrigram.WATER);
    }

    @Test
    @DisplayName("fromLines is the exact inverse of the three line accessors, for all 8 trigrams")
    void fromLinesRoundTrips() {
        for (IChingTrigram t : IChingTrigram.values()) {
            assertThat(IChingTrigram.fromLines(t.bottomYang(), t.middleYang(), t.topYang())).isEqualTo(t);
        }
    }
}
