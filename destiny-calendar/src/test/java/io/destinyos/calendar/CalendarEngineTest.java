package io.destinyos.calendar;

import static org.assertj.core.api.Assertions.assertThat;

import io.destinyos.core.context.BirthTimePrecision;
import io.destinyos.core.context.UncertaintyKind;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CalendarEngineTest {

    private static Instant civilInstant(int year, int month, int day, int hour, int minute, double utcOffsetHours) {
        ZoneOffset offset = ZoneOffset.ofTotalSeconds((int) Math.round(utcOffsetHours * 3600));
        return ZonedDateTime.of(year, month, day, hour, minute, 0, 0, offset).toInstant();
    }

    @Test
    @DisplayName("A fully resolvable birth returns lunar date, all four pillars, and only the informational R14a note")
    void fullyResolvedBirth() {
        // 1 January 2000, 12:00 civil time, Hanoi (UTC+7), reunified era -> unambiguous.
        Instant instant = civilInstant(2000, 1, 1, 12, 0, 7.0);

        var resolution = CalendarEngine.resolve(instant, VietnameseRegion.NORTH, 105.85, BirthTimePrecision.EXACT);

        assertThat(resolution.isFullyResolved()).isTrue();
        assertThat(resolution.dayPillar()).isNotNull();
        assertThat(resolution.hourPillar()).isNotNull();
        assertThat(resolution.dayPillar().stem()).isEqualTo(HeavenlyStem.MAU);
        assertThat(resolution.dayPillar().branch()).isEqualTo(EarthlyBranch.HORSE);

        boolean onlyInformational = resolution.uncertainties().stream().allMatch(u -> !u.affectsResult());
        assertThat(onlyInformational)
                .as("a fully resolvable, longitude-known birth should carry no result-affecting uncertainty")
                .isTrue();
    }

    @Test
    @DisplayName("No longitude supplied -> civil time used, LONGITUDE_UNKNOWN uncertainty recorded")
    void noLongitudeRecordsUncertainty() {
        Instant instant = civilInstant(2000, 1, 1, 12, 0, 7.0);
        var resolution = CalendarEngine.resolve(instant, VietnameseRegion.NORTH, null, BirthTimePrecision.EXACT);

        assertThat(resolution.uncertainties())
                .anyMatch(u -> u.kind() == UncertaintyKind.LONGITUDE_UNKNOWN && u.affectsResult());
    }

    @Test
    @DisplayName("A region-ambiguous 1955-1975 birth with UNKNOWN region resolves nothing, not a guess")
    void unresolvableHistoricalWindowFabricatesNothing() {
        Instant instant = civilInstant(1970, 6, 1, 12, 0, 7.0);
        var resolution = CalendarEngine.resolve(instant, VietnameseRegion.UNKNOWN, 105.85, BirthTimePrecision.EXACT);

        assertThat(resolution.isFullyResolved()).isFalse();
        assertThat(resolution.lunarDate()).isNull();
        assertThat(resolution.yearPillar()).isNull();
        assertThat(resolution.dayPillar()).isNull();
        assertThat(resolution.uncertainties())
                .anyMatch(u -> u.kind() == UncertaintyKind.HISTORICAL_TIMEZONE_RULE_UNKNOWN
                        && u.affectsResult() && "R14b".equals(u.researchId()));
    }

    @Test
    @DisplayName("APPROXIMATE birth time precision omits day/hour pillars rather than guessing them")
    void approximatePrecisionOmitsHourSensitivePillars() {
        Instant instant = civilInstant(2000, 1, 1, 12, 0, 7.0);
        var resolution = CalendarEngine.resolve(instant, VietnameseRegion.NORTH, 105.85, BirthTimePrecision.APPROXIMATE);

        assertThat(resolution.isFullyResolved()).isTrue();
        assertThat(resolution.yearPillar()).isNotNull();
        assertThat(resolution.dayPillar()).isNull();
        assertThat(resolution.hourPillar()).isNull();
        assertThat(resolution.uncertainties())
                .anyMatch(u -> u.kind() == UncertaintyKind.BIRTH_TIME_IMPRECISE && u.affectsResult());
    }
}
