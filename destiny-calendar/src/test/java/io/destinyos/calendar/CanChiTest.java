package io.destinyos.calendar;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

    /**
     * {@link CanChi#monthPillarOffset} exists for Bát Tự's Đại Vận (R2), which
     * walks the month-pillar sequence forwards or backwards. It was initially
     * covered only through that engine; these assertions prove the arithmetic
     * where it lives, so a failure distinguishes a broken step from a broken
     * luck-cycle conversion.
     */
    @Nested
    @DisplayName("monthPillarOffset — stepping along the month-pillar sequence")
    class MonthPillarOffset {

        @Test
        @DisplayName("Offset zero is the month pillar itself")
        void zeroOffsetIsIdentity() {
            assertThat(CanChi.monthPillarOffset(1984, 3, 0))
                    .isEqualTo(CanChi.monthPillar(1984, 3));
        }

        @Test
        @DisplayName("One step forward advances both stem and branch by one")
        void oneStepForward() {
            CanChiPillar base = CanChi.monthPillar(1990, 5);
            CanChiPillar next = CanChi.monthPillarOffset(1990, 5, 1);

            assertThat(next.stem().index()).isEqualTo(base.stem().index() % 10 + 1);
            assertThat(next.branch().index()).isEqualTo(base.branch().index() % 12 + 1);
        }

        @Test
        @DisplayName("One step backward is the inverse of one step forward")
        void backwardInvertsForward() {
            assertThat(CanChi.monthPillarOffset(1990, 5, -1))
                    .isEqualTo(CanChi.monthPillarOffset(1990, 4, 0));
            assertThat(CanChi.monthPillarOffset(1990, 5, 1))
                    .isEqualTo(CanChi.monthPillarOffset(1990, 6, 0));
        }

        @Test
        @DisplayName("Stepping past month 12 continues into the next year, not back to month 1")
        void crossesTheYearBoundary() {
            // The reason this method exists rather than passing an
            // out-of-range month to monthPillar: a luck cycle routinely walks
            // off the end of a year, and wrapping to month 1 of the same year
            // would silently repeat a pillar instead of advancing.
            assertThat(CanChi.monthPillarOffset(1990, 12, 1))
                    .isEqualTo(CanChi.monthPillar(1991, 1));
            assertThat(CanChi.monthPillarOffset(1991, 1, -1))
                    .isEqualTo(CanChi.monthPillar(1990, 12));
        }

        @Test
        @DisplayName("Sixty steps in either direction return to the starting pillar")
        void sixtyStepPeriodicity() {
            CanChiPillar base = CanChi.monthPillar(1984, 7);
            assertThat(CanChi.monthPillarOffset(1984, 7, 60)).isEqualTo(base);
            assertThat(CanChi.monthPillarOffset(1984, 7, -60)).isEqualTo(base);
        }

        @Test
        @DisplayName("Twelve steps return the same branch but a different stem")
        void twelveStepsShareABranch() {
            // 12 and 10 are coprime only up to gcd 2, so twelve steps move the
            // stem by exactly two - the property that makes the sequence
            // 60-periodic rather than 12-periodic.
            CanChiPillar base = CanChi.monthPillar(1984, 7);
            CanChiPillar twelveOn = CanChi.monthPillarOffset(1984, 7, 12);

            assertThat(twelveOn.branch()).isEqualTo(base.branch());
            assertThat(twelveOn.stem()).isNotEqualTo(base.stem());
            assertThat(twelveOn.stem().index()).isEqualTo((base.stem().index() + 1) % 10 + 1);
        }

        @Test
        @DisplayName("Eight consecutive steps are all distinct — a luck sequence never repeats early")
        void eightStepsAreDistinct() {
            // LuckCycles.PERIOD_COUNT is 8; if any two coincided, two luck
            // periods would carry the same pillar.
            java.util.Set<CanChiPillar> seen = new java.util.HashSet<>();
            for (int i = 1; i <= 8; i++) {
                seen.add(CanChi.monthPillarOffset(1990, 5, i));
            }
            assertThat(seen).hasSize(8);
        }
    }
}
