package io.destinyos.engines.astrology;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import io.destinyos.calendar.JulianDay;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Greenwich Mean Sidereal Time, against Jean Meeus's own worked example.
 *
 * <p>Not reproduced by hand-picking an instant and trusting the formula in
 * isolation — this is the textbook's own example 12.a (as cited by an
 * independent GMST reference page attributing the same Meeus formula to IAU
 * 1982), so a mismatch here would mean either this project's JulianDay
 * conversion or the GMST formula itself is wrong, not merely that a made-up
 * test expectation was miscalculated.
 */
class SiderealTimeTest {

    @Test
    @DisplayName("1994-06-16 18h UT: GMST = 174.7711135 degrees (Meeus example 12.a)")
    void meeusWorkedExample() {
        double julianDateUt = JulianDay.fromLocalDateTime(
                LocalDateTime.of(1994, 6, 16, 18, 0), 0.0);

        double gmst = SiderealTime.greenwichMeanSiderealTimeDegrees(julianDateUt);

        // Meeus states the result to 7 significant figures; matching to
        // 1e-4 degrees (~0.36 arcsecond) confirms the formula and the
        // Julian date conversion agree with the book, not just with each
        // other.
        assertThat(gmst).isCloseTo(174.7711135, within(1e-4));
    }

    @Test
    @DisplayName("GMST is always normalized to [0, 360)")
    void alwaysNormalized() {
        for (int year = 1950; year <= 2050; year += 7) {
            double jd = JulianDay.fromLocalDateTime(
                    LocalDateTime.of(year, 3, 15, 11, 0), 0.0);
            double gmst = SiderealTime.greenwichMeanSiderealTimeDegrees(jd);
            assertThat(gmst).isGreaterThanOrEqualTo(0.0).isLessThan(360.0);
        }
    }

    @Test
    @DisplayName("Local sidereal time adds east longitude, and still normalizes")
    void localAddsEastLongitude() {
        double jd = JulianDay.fromLocalDateTime(
                LocalDateTime.of(1994, 6, 16, 18, 0), 0.0);
        double gmst = SiderealTime.greenwichMeanSiderealTimeDegrees(jd);

        // East of Greenwich: LST is ahead of GMST.
        assertThat(SiderealTime.localSiderealTimeDegrees(jd, 45.0))
                .isCloseTo(gmst + 45.0, within(1e-9));

        // West of Greenwich, far enough to wrap past zero.
        double west = SiderealTime.localSiderealTimeDegrees(jd, -(gmst + 10.0));
        assertThat(west).isCloseTo(350.0, within(1e-6));
    }

    @Test
    @DisplayName("Julian centuries is zero exactly at J2000.0")
    void julianCenturiesAtEpoch() {
        assertThat(SiderealTime.julianCenturies(2451545.0)).isZero();
    }
}
