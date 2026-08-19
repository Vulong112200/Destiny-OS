package io.destinyos.calendar;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class SolarTimeCorrectionTest {

    @Test
    void longitudeExactlyOnTheStandardMeridianNeedsNoCorrection() {
        Duration correction = SolarTimeCorrection.meanSolarTimeCorrection(105.0, 7.0);
        assertThat(correction).isEqualTo(Duration.ZERO);
    }

    @Test
    void oneDegreeEastOfTheMeridianAddsFourMinutes() {
        Duration correction = SolarTimeCorrection.meanSolarTimeCorrection(106.0, 7.0);
        assertThat(correction).isEqualTo(Duration.ofMinutes(4));
    }

    @Test
    void oneDegreeWestOfTheMeridianSubtractsFourMinutes() {
        Duration correction = SolarTimeCorrection.meanSolarTimeCorrection(104.0, 7.0);
        assertThat(correction).isEqualTo(Duration.ofMinutes(-4));
    }

    @Test
    void differentCivilOffsetShiftsTheReferenceMeridian() {
        // UTC+8's standard meridian is 120 degrees East, not 105.
        Duration correction = SolarTimeCorrection.meanSolarTimeCorrection(120.0, 8.0);
        assertThat(correction).isEqualTo(Duration.ZERO);
    }
}
