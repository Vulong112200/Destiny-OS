package io.destinyos.calendar;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** R10: Giờ Tý starts at 23:00, not midnight. */
class HourBranchResolverTest {

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
            "23:00, RAT", "23:59, RAT", "00:00, RAT", "00:59, RAT",
            "01:00, OX", "02:59, OX",
            "03:00, TIGER", "04:59, TIGER",
            "21:00, PIG", "22:59, PIG",
    })
    void mapsTimeOfDayToTheCorrectBranch(String time, EarthlyBranch expected) {
        EarthlyBranch actual = HourBranchResolver.branchAt(LocalTime.parse(time), ZiHourBoundaryPolicy.ZI_HOUR_23_00);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("22:59 and 23:00 on the same calendar date are consecutive day pillars, not the same one")
    void dayPillarRollsOverAt2300NotMidnight() {
        assertThat(HourBranchResolver.rollsOverToNextDay(LocalTime.of(22, 59), ZiHourBoundaryPolicy.ZI_HOUR_23_00))
                .isFalse();
        assertThat(HourBranchResolver.rollsOverToNextDay(LocalTime.of(23, 0), ZiHourBoundaryPolicy.ZI_HOUR_23_00))
                .isTrue();
        assertThat(HourBranchResolver.rollsOverToNextDay(LocalTime.of(23, 59), ZiHourBoundaryPolicy.ZI_HOUR_23_00))
                .isTrue();
        assertThat(HourBranchResolver.rollsOverToNextDay(LocalTime.of(0, 0), ZiHourBoundaryPolicy.ZI_HOUR_23_00))
                .isFalse();
    }
}
