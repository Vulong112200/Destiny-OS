package io.destinyos.engines.astrology;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ZodiacSignTest {

    @Test
    @DisplayName("0 degrees is exactly Aries 0")
    void zeroIsAries() {
        var position = ZodiacSign.at(0.0);
        assertThat(position.sign()).isEqualTo(ZodiacSign.ARIES);
        assertThat(position.degreesIntoSign()).isZero();
    }

    @Test
    @DisplayName("Each 30-degree boundary starts the next sign in order")
    void boundariesAdvanceInOrder() {
        ZodiacSign[] expected = ZodiacSign.values();
        for (int i = 0; i < 12; i++) {
            var position = ZodiacSign.at(i * 30.0);
            assertThat(position.sign()).as("boundary at %d degrees", i * 30).isEqualTo(expected[i]);
            assertThat(position.degreesIntoSign()).isZero();
        }
    }

    @Test
    @DisplayName("29.999... degrees is still Aries, not Taurus")
    void justBelowBoundaryStaysInSign() {
        var position = ZodiacSign.at(29.9999);
        assertThat(position.sign()).isEqualTo(ZodiacSign.ARIES);
        assertThat(position.degreesIntoSign()).isCloseTo(29.9999, within(1e-9));
    }

    @Test
    @DisplayName("360 degrees wraps to Aries 0, same as 0 degrees")
    void wrapsAtFullCircle() {
        assertThat(ZodiacSign.at(360.0)).isEqualTo(ZodiacSign.at(0.0));
    }

    @Test
    @DisplayName("Negative longitudes normalize before lookup")
    void negativeLongitudeNormalizes() {
        assertThat(ZodiacSign.at(-30.0)).isEqualTo(ZodiacSign.at(330.0));
        assertThat(ZodiacSign.at(-1.0).sign()).isEqualTo(ZodiacSign.PISCES);
    }

    @Test
    @DisplayName("Mid-sign example: 195 degrees is 15 degrees into Libra")
    void midSignExample() {
        var position = ZodiacSign.at(195.0);
        assertThat(position.sign()).isEqualTo(ZodiacSign.LIBRA);
        assertThat(position.degreesIntoSign()).isCloseTo(15.0, within(1e-9));
    }
}
