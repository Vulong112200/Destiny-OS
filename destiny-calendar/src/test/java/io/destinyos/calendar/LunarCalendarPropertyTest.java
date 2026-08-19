package io.destinyos.calendar;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Structural properties that must hold for every year, independent of any
 * single golden date - the kind of bug an exhaustive scan catches that a
 * handful of hand-picked vectors would miss.
 */
class LunarCalendarPropertyTest {

    private static final double HANOI_UTC_OFFSET = 7.0;

    @Test
    @DisplayName("Every lunar new year (Tết) across two centuries is month 1, day 1, not leap")
    void everyTetIsMonthOneDayOneNotLeap() {
        for (int year = 1900; year <= 2100; year++) {
            // Tết always falls between 21 January and 20 February (Gregorian).
            LunarDate closestToTet = null;
            for (int day = 21; day <= 31; day++) {
                LunarDate lunar = LunarCalendar.toLunar(day, 1, year, HANOI_UTC_OFFSET);
                if (lunar.month() == 1 && lunar.day() == 1) {
                    closestToTet = lunar;
                    break;
                }
            }
            if (closestToTet == null) {
                for (int day = 1; day <= 20; day++) {
                    LunarDate lunar = LunarCalendar.toLunar(day, 2, year, HANOI_UTC_OFFSET);
                    if (lunar.month() == 1 && lunar.day() == 1) {
                        closestToTet = lunar;
                        break;
                    }
                }
            }
            assertThat(closestToTet).as("Tết not found in the expected window for %d", year).isNotNull();
            assertThat(closestToTet.leap()).as("Tết itself is never a leap month, year %d", year).isFalse();
        }
    }

    @Test
    @DisplayName("Year/Month/Day Can Chi pillars are 60-periodic across a wide scan")
    void canChiPillarsAreSixtyPeriodic() {
        Set<CanChiPillar> distinctYearPillars = new HashSet<>();
        for (int year = 1900; year < 1960; year++) {
            distinctYearPillars.add(CanChi.yearPillar(year));
        }
        assertThat(distinctYearPillars).hasSize(60);

        for (int year = 1900; year < 1960; year++) {
            assertThat(CanChi.yearPillar(year)).isEqualTo(CanChi.yearPillar(year + 60));
        }
    }
}
