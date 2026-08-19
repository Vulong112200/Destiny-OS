package io.destinyos.calendar;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Golden vectors quoted directly from Hồ Ngọc Đức's own worked example
 * ("How to compute the Vietnamese lunar calendar",
 * https://www.xemamlich.uhm.vn/calrules_en.html — the two full tables
 * covering 1983-12 through 1986-01, second-precision, GMT/Hanoi/Beijing
 * columns) — never generated from this module's own code
 * (CLAUDE.md section 32).
 */
class LunarCalendarGoldenTest {

    private static final double HANOI_UTC_OFFSET = 7.0;
    private static final double BEIJING_UTC_OFFSET = 8.0;

    /**
     * Every Hanoi-local new moon date from both published tables (deduplicated
     * where the tables overlap). Each one must be day 1 of a lunar month.
     */
    static List<Arguments> hanoiNewMoonDates() {
        int[][] dates = {
                {4, 12, 1983}, {3, 1, 1984}, {2, 2, 1984},
                {3, 3, 1984}, {1, 4, 1984}, {1, 5, 1984},
                {30, 5, 1984}, {29, 6, 1984}, {28, 7, 1984},
                {27, 8, 1984}, {25, 9, 1984}, {24, 10, 1984},
                {23, 11, 1984}, {22, 12, 1984}, {21, 1, 1985},
                {20, 2, 1985}, {21, 3, 1985}, {20, 4, 1985},
                {20, 5, 1985}, {18, 6, 1985}, {18, 7, 1985},
                {16, 8, 1985}, {15, 9, 1985}, {14, 10, 1985},
                {12, 11, 1985}, {12, 12, 1985}, {10, 1, 1986}
        };
        return java.util.Arrays.stream(dates)
                .map(d -> Arguments.of(d[0], d[1], d[2]))
                .toList();
    }

    @ParameterizedTest(name = "{0}/{1}/{2} is day 1 of a Hanoi-local lunar month")
    @MethodSource("hanoiNewMoonDates")
    @DisplayName("Every cited new moon date starts a lunar month at 105°E/UTC+7")
    void everyCitedNewMoonStartsALunarMonth(int day, int month, int year) {
        LunarDate lunar = LunarCalendar.toLunar(day, month, year, HANOI_UTC_OFFSET);
        assertThat(lunar.day()).as("%d/%d/%d", day, month, year).isEqualTo(1);
    }

    @Test
    @DisplayName("Month 11 of lunar year 1983 begins 4 December 1983 (Winter Solstice month)")
    void lunarMonth11Of1983() {
        LunarDate lunar = LunarCalendar.toLunar(4, 12, 1983, HANOI_UTC_OFFSET);
        assertThat(lunar.month()).isEqualTo(11);
        assertThat(lunar.leap()).isFalse();
    }

    @Test
    @DisplayName("Month 11 of lunar year 1984 begins 23 November 1984")
    void lunarMonth11Of1984() {
        LunarDate lunar = LunarCalendar.toLunar(23, 11, 1984, HANOI_UTC_OFFSET);
        assertThat(lunar.month()).isEqualTo(11);
    }

    @Test
    @DisplayName("Month 11 of lunar year 1985 begins 12 December 1985")
    void lunarMonth11Of1985() {
        LunarDate lunar = LunarCalendar.toLunar(12, 12, 1985, HANOI_UTC_OFFSET);
        assertThat(lunar.month()).isEqualTo(11);
    }

    @Test
    @DisplayName("Month 1 of 1984 (Tết) begins 2 February 1984, per the article's own text")
    void tet1984() {
        LunarDate lunar = LunarCalendar.toLunar(2, 2, 1984, HANOI_UTC_OFFSET);
        assertThat(lunar.month()).isEqualTo(1);
        assertThat(lunar.day()).isEqualTo(1);
    }

    @Test
    @DisplayName("The month from 21/03/1985 to 19/04/1985 is the leap month (no Principal Term)")
    void the1985LeapMonth() {
        LunarDate leapStart = LunarCalendar.toLunar(21, 3, 1985, HANOI_UTC_OFFSET);
        assertThat(leapStart.day()).isEqualTo(1);
        assertThat(leapStart.leap()).isTrue();

        LunarDate afterLeap = LunarCalendar.toLunar(20, 4, 1985, HANOI_UTC_OFFSET);
        assertThat(afterLeap.day()).isEqualTo(1);
        assertThat(afterLeap.leap()).isFalse();
    }

    /**
     * Named Vietnamese/Chinese divergence years, quoted directly from the
     * source. The Vietnamese and Chinese calendars use the identical rule
     * and algorithm - they differ only in meridian (105°E/UTC+7 vs
     * 120°E/UTC+8), which this module's {@code timezoneOffsetHours}
     * parameter already models, so both sides are computed with the same
     * {@link LunarCalendar}.
     */
    static List<Arguments> divergenceYears() {
        return List.of(
                Arguments.of("1985 (a full month apart)", 21, 1, 1985, 20, 2, 1985),
                Arguments.of("2007", 17, 2, 2007, 18, 2, 2007),
                Arguments.of("2030", 2, 2, 2030, 3, 2, 2030),
                Arguments.of("2053", 18, 2, 2053, 19, 2, 2053)
        );
    }

    @ParameterizedTest(name = "{0}: Vietnamese Tết {1}/{2}/{3}, Chinese {4}/{5}/{6}")
    @MethodSource("divergenceYears")
    @DisplayName("Vietnamese and Chinese Tết diverge exactly on the cited dates")
    void namedDivergenceYears(String label, int vnDay, int vnMonth, int vnYear,
                              int cnDay, int cnMonth, int cnYear) {
        LunarDate vnTet = LunarCalendar.toLunar(vnDay, vnMonth, vnYear, HANOI_UTC_OFFSET);
        assertThat(vnTet.month()).as(label + " (Vietnamese side)").isEqualTo(1);
        assertThat(vnTet.day()).as(label + " (Vietnamese side)").isEqualTo(1);

        LunarDate cnNewYear = LunarCalendar.toLunar(cnDay, cnMonth, cnYear, BEIJING_UTC_OFFSET);
        assertThat(cnNewYear.month()).as(label + " (Chinese side)").isEqualTo(1);
        assertThat(cnNewYear.day()).as(label + " (Chinese side)").isEqualTo(1);

        // And confirm they genuinely disagree at 105°E on the Chinese date
        // (except when the two dates coincide, which none of these do).
        LunarDate vnOnChineseDate = LunarCalendar.toLunar(cnDay, cnMonth, cnYear, HANOI_UTC_OFFSET);
        assertThat(vnOnChineseDate.day() == 1 && vnOnChineseDate.month() == 1)
                .as(label + ": Vietnamese calendar must NOT also start month 1 on the Chinese New Year date")
                .isFalse();
    }
}
