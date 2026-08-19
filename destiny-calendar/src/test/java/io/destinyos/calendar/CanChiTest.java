package io.destinyos.calendar;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CanChiTest {

    @ParameterizedTest(name = "year {0} is {1} {2}")
    @DisplayName("Well-known year pillars: 1900 Canh Tý, 1984 Giáp Tý, 2024 Giáp Thìn")
    @CsvSource({
            "1900, CANH, RAT",
            "1984, GIAP, RAT",
            "2024, GIAP, DRAGON",
    })
    void wellKnownYearPillars(int lunarYear, HeavenlyStem expectedStem, EarthlyBranch expectedBranch) {
        CanChiPillar pillar = CanChi.yearPillar(lunarYear);
        assertThat(pillar.stem()).isEqualTo(expectedStem);
        assertThat(pillar.branch()).isEqualTo(expectedBranch);
    }

    @Test
    @DisplayName("1 January 2000 is day Mậu Ngọ at UTC+7 (independently documented fact)")
    void dayPillarFor1Jan2000() {
        long jd = JulianDay.fromDate(1, 1, 2000);
        CanChiPillar pillar = CanChi.dayPillar(jd, 7.0);
        assertThat(pillar.stem()).isEqualTo(HeavenlyStem.MAU);
        assertThat(pillar.branch()).isEqualTo(EarthlyBranch.HORSE);
    }

    @ParameterizedTest(name = "day stem {0}, hour branch {1} -> hour stem {2}")
    @DisplayName("Ngũ Thử Độn mnemonic, all 5 day-stem-pair cases, verified against the Tý hour")
    @CsvSource({
            "GIAP, RAT, GIAP",
            "KY, RAT, GIAP",
            "AT, RAT, BINH",
            "CANH, RAT, BINH",
            "BINH, RAT, MAU",
            "TAN, RAT, MAU",
            "DINH, RAT, CANH",
            "NHAM, RAT, CANH",
            "MAU, RAT, NHAM",
            "QUY, RAT, NHAM",
    })
    void hourStemMnemonic(HeavenlyStem dayStem, EarthlyBranch hourBranch, HeavenlyStem expectedHourStem) {
        CanChiPillar pillar = CanChi.hourPillar(dayStem, hourBranch);
        assertThat(pillar.stem()).isEqualTo(expectedHourStem);
        assertThat(pillar.branch()).isEqualTo(hourBranch);
    }

    @Test
    @DisplayName("Year, month and day Can Chi are each 60-periodic")
    void sixtyYearPeriodicity() {
        assertThat(CanChi.yearPillar(1984)).isEqualTo(CanChi.yearPillar(1984 + 60));
        assertThat(CanChi.monthPillar(1984, 3)).isEqualTo(CanChi.monthPillar(1984 + 5, 3));
        long jd = JulianDay.fromDate(1, 1, 2000);
        assertThat(CanChi.dayPillar(jd, 7.0)).isEqualTo(CanChi.dayPillar(jd + 60, 7.0));
    }
}
