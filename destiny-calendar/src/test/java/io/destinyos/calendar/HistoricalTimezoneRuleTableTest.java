package io.destinyos.calendar;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * R14a (adopted table) / R14b (unresolved geographic boundary). A gap must
 * resolve to {@link java.util.Optional#empty()}, never to a guessed offset
 * (ADR D3).
 */
class HistoricalTimezoneRuleTableTest {

    @ParameterizedTest(name = "{0} in {1} -> UTC+{2}")
    @CsvSource({
            "1950-01-01, SOUTH, 8.0",
            "1950-01-01, NORTH, 8.0",
            "1956-01-01, SOUTH, 7.0",
            "1962-01-01, SOUTH, 8.0",
            "1970-01-01, NORTH, 7.0",
            "1970-01-01, SOUTH, 8.0",
            "1970-06-01, NORTH, 7.0",
            "1980-01-01, NORTH, 7.0",
            "1980-01-01, SOUTH, 7.0",
    })
    @DisplayName("Every sourced (date, region) resolves to the cited offset")
    void resolvesSourcedWindows(LocalDate date, VietnameseRegion region, double expectedOffset) {
        var rule = HistoricalTimezoneRuleTable.resolve(date, region);
        assertThat(rule).isPresent();
        assertThat(rule.get().utcOffsetHours()).isEqualTo(expectedOffset);
    }

    @Test
    @DisplayName("A region-dependent window (1955-1975) with UNKNOWN region is an honest gap, not a guess")
    void regionDependentWindowWithUnknownRegionIsUnresolved() {
        // North is UTC+7 (post-1968-01-01, Quyết định 121-CP) and South is UTC+8
        // (post-1960-01-01, Sắc lệnh 362-TTP) throughout 1970 - genuinely
        // different offsets, so UNKNOWN must not silently pick a side.
        assertThat(HistoricalTimezoneRuleTable.resolve(LocalDate.of(1970, 1, 1), VietnameseRegion.UNKNOWN))
                .isEmpty();
        assertThat(HistoricalTimezoneRuleTable.resolve(LocalDate.of(1970, 6, 1), VietnameseRegion.UNKNOWN))
                .isEmpty();
    }

    @Test
    @DisplayName("Pre-partition and post-reunification dates resolve even with UNKNOWN region")
    void unifiedPeriodsResolveRegardlessOfRegion() {
        assertThat(HistoricalTimezoneRuleTable.resolve(LocalDate.of(1950, 1, 1), VietnameseRegion.UNKNOWN))
                .isPresent();
        assertThat(HistoricalTimezoneRuleTable.resolve(LocalDate.of(1980, 1, 1), VietnameseRegion.UNKNOWN))
                .isPresent();
    }
}
