package io.destinyos.calendar;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class JulianDayTest {

    @Test
    void matchesTheWellKnownJ2000Epoch() {
        // 2000-01-01 12:00 TT is JD 2451545.0 - one of the most widely cited
        // anchors in astronomical software, independent of any calendar
        // library. fromDate is noon-referenced, so day 1/1/2000 lands here.
        assertThat(JulianDay.fromDate(1, 1, 2000)).isEqualTo(2451545L);
    }

    @Test
    void matchesTheUnixEpoch() {
        // 1970-01-01 is JD 2440588 (noon-referenced day number).
        assertThat(JulianDay.fromDate(1, 1, 1970)).isEqualTo(2440588L);
    }

    @Test
    void roundTripsAcrossTheGregorianCutover() {
        int[][] dates = {
                {15, 10, 1582}, {1, 1, 1600}, {29, 2, 2000}, {31, 12, 1999},
                {1, 1, 1900}, {13, 6, 1975}, {21, 1, 1985}, {17, 2, 2007},
        };
        for (int[] d : dates) {
            long jd = JulianDay.fromDate(d[0], d[1], d[2]);
            int[] back = JulianDay.toDate(jd);
            assertThat(back).as("round trip for %d/%d/%d", d[0], d[1], d[2]).containsExactly(d[0], d[1], d[2]);
        }
    }
}
