package io.destinyos.engines.bazi;

import static org.assertj.core.api.Assertions.assertThat;

import io.destinyos.calendar.EarthlyBranch;
import io.destinyos.calendar.HeavenlyStem;
import io.destinyos.calendar.VietnameseRegion;
import io.destinyos.core.context.BirthTimePrecision;
import io.destinyos.core.context.CalculationContext;
import io.destinyos.core.context.Gender;
import io.destinyos.core.context.UncertaintyKind;
import io.destinyos.core.result.EngineResult;
import io.destinyos.core.version.MethodologyVersions;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Đại Vận — direction, start age, and the ten-year sequence (research item R2).
 *
 * <p><strong>The suite is split on purpose, because the two halves fail for
 * different reasons.</strong> The conversion from a day count to a start age is
 * pure arithmetic and is tested against the exact whole-day counts published
 * worked examples state — those assertions are exact and can never drift. The
 * astronomy (how far the birth actually is from the adjacent Tiết) inherits the
 * solar series' known precision limit (R19), so those assertions carry a
 * tolerance. Merging them would force a tolerance onto arithmetic that has
 * none, and hide which half broke.
 *
 * <p><strong>Where the vectors come from</strong> — all published by third
 * parties, none produced by this project (CLAUDE.md §32):
 *
 * <ul>
 *   <li><strong>Backward.</strong> btime.com's worked example: 公历 1990年1月1日
 *       11时10分, male, pillars 己巳 丙子 丙寅 癸巳, "逆向往回数到上一个节气大雪，
 *       有25天。25÷3=8余数1。起运年为8岁4个月".</li>
 *   <li><strong>Forward.</strong> k366.com: "某男一九九四年正月十七日寅时生 …
 *       一九九四年为甲戌年阳年男命顺排 … 从出生到惊蛰节为八天，合为两年零八个月".</li>
 *   <li><strong>Conversion only.</strong> chanweitang.com (6 days 7 canh giờ →
 *       2 years 2 months 10 days) and k366.com (2 days → 8 months; 7 days →
 *       2 years 4 months).</li>
 * </ul>
 */
class LuckCycleTest {

    private final BaziEngine engine = new BaziEngine();

    @Nested
    @DisplayName("Direction (unanimous across sources)")
    class Direction {

        @Test
        @DisplayName("Yang year + male, and yin year + female, both count forward")
        void forwardCombinations() {
            assertThat(LuckCycleDirection.forBirth(HeavenlyStem.GIAP, Gender.MALE))
                    .isEqualTo(LuckCycleDirection.THUAN);
            assertThat(LuckCycleDirection.forBirth(HeavenlyStem.AT, Gender.FEMALE))
                    .isEqualTo(LuckCycleDirection.THUAN);
        }

        @Test
        @DisplayName("Yang year + female, and yin year + male, both count backward")
        void backwardCombinations() {
            assertThat(LuckCycleDirection.forBirth(HeavenlyStem.GIAP, Gender.FEMALE))
                    .isEqualTo(LuckCycleDirection.NGHICH);
            assertThat(LuckCycleDirection.forBirth(HeavenlyStem.KY, Gender.MALE))
                    .isEqualTo(LuckCycleDirection.NGHICH);
        }

        @Test
        @DisplayName("Every stem-gender pair resolves, and each gender splits 5/5")
        void allTwentyCombinations() {
            long maleForward = 0;
            long femaleForward = 0;
            for (HeavenlyStem stem : HeavenlyStem.values()) {
                if (LuckCycleDirection.forBirth(stem, Gender.MALE) == LuckCycleDirection.THUAN) {
                    maleForward++;
                }
                if (LuckCycleDirection.forBirth(stem, Gender.FEMALE) == LuckCycleDirection.THUAN) {
                    femaleForward++;
                }
                // The two genders must always disagree for a given stem. That
                // is the structural content of the rule; a formula that ever
                // agreed would have collapsed the gender term.
                assertThat(LuckCycleDirection.forBirth(stem, Gender.MALE))
                        .as("stem %s", stem)
                        .isNotEqualTo(LuckCycleDirection.forBirth(stem, Gender.FEMALE));
            }
            assertThat(maleForward).isEqualTo(5);
            assertThat(femaleForward).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("Day count to start age — exact, no tolerance")
    class Conversion {

        @Test
        @DisplayName("25 days → 8 years 4 months (btime worked example)")
        void twentyFiveDays() {
            assertThat(LuckCycleResolver.toStartAge(Duration.ofDays(25)))
                    .isEqualTo(Period.of(8, 4, 0));
        }

        @Test
        @DisplayName("8 days → 2 years 8 months (k366 worked example)")
        void eightDays() {
            assertThat(LuckCycleResolver.toStartAge(Duration.ofDays(8)))
                    .isEqualTo(Period.of(2, 8, 0));
        }

        @Test
        @DisplayName("2 days → 8 months, with no whole year (k366 worked example)")
        void twoDays() {
            assertThat(LuckCycleResolver.toStartAge(Duration.ofDays(2)))
                    .isEqualTo(Period.of(0, 8, 0));
        }

        @Test
        @DisplayName("7 days → 2 years 4 months (k366 worked example)")
        void sevenDays() {
            assertThat(LuckCycleResolver.toStartAge(Duration.ofDays(7)))
                    .isEqualTo(Period.of(2, 4, 0));
        }

        @Test
        @DisplayName("6 days 7 canh giờ → 2 years 2 months 10 days (chanweitang worked example)")
        void sixDaysSevenDoubleHours() {
            // A canh giờ is two hours; the example is the only one found that
            // exercises the sub-day part of the conversion, which is exactly
            // the part a whole-day-only implementation would silently drop.
            Duration distance = Duration.ofDays(6).plusHours(14);
            assertThat(LuckCycleResolver.toStartAge(distance))
                    .isEqualTo(Period.of(2, 2, 10));
        }

        @Test
        @DisplayName("The finer equivalences the sources quote all fall out of one ratio")
        void proportionalChain() {
            // 3 days = 1 year, 1 day = 4 months, 1 canh giờ = 10 days,
            // 1 hour = 5 days, 12 minutes = 1 day. Sources quote these as a
            // list; if they are really one ratio, each must follow.
            assertThat(LuckCycleResolver.toStartAge(Duration.ofDays(3)))
                    .isEqualTo(Period.of(1, 0, 0));
            assertThat(LuckCycleResolver.toStartAge(Duration.ofDays(1)))
                    .isEqualTo(Period.of(0, 4, 0));
            assertThat(LuckCycleResolver.toStartAge(Duration.ofHours(2)))
                    .isEqualTo(Period.of(0, 0, 10));
            assertThat(LuckCycleResolver.toStartAge(Duration.ofHours(1)))
                    .isEqualTo(Period.of(0, 0, 5));
            assertThat(LuckCycleResolver.toStartAge(Duration.ofMinutes(12)))
                    .isEqualTo(Period.of(0, 0, 1));
        }

        @Test
        @DisplayName("A birth on the boundary starts its cycle at zero, not at one")
        void zeroDistance() {
            // Some summaries claim a minimum of one year. No worked example
            // supports it, so the conversion is left linear through zero and
            // the claim is not implemented.
            assertThat(LuckCycleResolver.toStartAge(Duration.ZERO))
                    .isEqualTo(Period.of(0, 0, 0));
        }
    }

    @Nested
    @DisplayName("End-to-end against published examples")
    class Published {

        @Test
        @DisplayName("1990-01-01 11:10, male: backward to Đại Tuyết, 25 days, 8 years 4 months")
        void backwardVector() {
            // The source states Beijing time; this project reconstructs the
            // offset itself, so the instant is given in UTC+8 directly.
            BaziChart chart = chart(LocalDateTime.of(1990, 1, 1, 11, 10), 8, Gender.MALE);

            // The pillars the same source publishes, as a cross-check that the
            // vector is about the chart this engine actually built. This is
            // also the Lập Xuân case: a 1 January birth keeps 1989's pillar.
            assertThat(chart.yearPillar().stem()).isEqualTo(HeavenlyStem.KY);
            assertThat(chart.yearPillar().branch()).isEqualTo(EarthlyBranch.SNAKE);
            assertThat(chart.monthPillar().stem()).isEqualTo(HeavenlyStem.BINH);
            assertThat(chart.monthPillar().branch()).isEqualTo(EarthlyBranch.RAT);
            assertThat(chart.dayPillar().stem()).isEqualTo(HeavenlyStem.BINH);
            assertThat(chart.dayPillar().branch()).isEqualTo(EarthlyBranch.TIGER);

            LuckCycles cycles = chart.luckCycles();
            assertThat(cycles).isNotNull();
            assertThat(cycles.direction()).isEqualTo(LuckCycleDirection.NGHICH);
            assertThat(cycles.boundaryTerm()).isEqualTo(io.destinyos.calendar.SolarTerm.DAI_TUYET);

            // 25 days, to the resolution the source states it in. The measured
            // value is 24.996 days: the shortfall is about six minutes, well
            // inside the solar series' known deviation (R19).
            assertThat(cycles.distanceToBoundary().toDays()).isEqualTo(24);
            assertThat(cycles.distanceToBoundary())
                    .isBetween(Duration.ofDays(25).minusHours(1), Duration.ofDays(25));

            // Published: 8 years 4 months. Six minutes short of 25 whole days
            // lands a few days under, which is what an exact conversion should
            // report rather than rounding up to match.
            assertThat(cycles.startAge().getYears()).isEqualTo(8);
            assertThat(cycles.startAge().getMonths()).isEqualTo(3);
            assertThat(cycles.startAge().getDays()).isEqualTo(29);
        }

        @Test
        @DisplayName("1994-02-26 giờ Dần, male Giáp Tuất: forward to Kinh Trập, 8 days")
        void forwardVector() {
            // k366 gives the birth as lunar 17/1/1994, which converts to
            // 1994-02-26; giờ Dần is 03:00-05:00, sampled at its midpoint.
            BaziChart chart = chart(LocalDateTime.of(1994, 2, 26, 4, 0), 8, Gender.MALE);

            assertThat(chart.yearPillar().stem())
                    .as("Giáp — a yang stem, which is what makes this forward")
                    .isEqualTo(HeavenlyStem.GIAP);
            assertThat(chart.yearPillar().branch()).isEqualTo(EarthlyBranch.DOG);

            LuckCycles cycles = chart.luckCycles();
            assertThat(cycles.direction()).isEqualTo(LuckCycleDirection.THUAN);
            assertThat(cycles.boundaryTerm())
                    .isEqualTo(io.destinyos.calendar.SolarTerm.KINH_TRAP);

            // Published: 8 days → 2 years 8 months. Measured 7.98 days.
            assertThat(cycles.distanceToBoundary())
                    .isBetween(Duration.ofDays(8).minusHours(2), Duration.ofDays(8));
            assertThat(cycles.startAge().getYears()).isEqualTo(2);
            assertThat(cycles.startAge().getMonths()).isEqualTo(7);
        }

        @Test
        @DisplayName("The two vectors run in opposite directions — the rule is exercised both ways")
        void bothDirectionsCovered() {
            LuckCycles backward =
                    chart(LocalDateTime.of(1990, 1, 1, 11, 10), 8, Gender.MALE).luckCycles();
            LuckCycles forward =
                    chart(LocalDateTime.of(1994, 2, 26, 4, 0), 8, Gender.MALE).luckCycles();
            assertThat(backward.direction()).isNotEqualTo(forward.direction());
        }
    }

    @Nested
    @DisplayName("The sequence itself")
    class Sequence {

        @Test
        @DisplayName("Forward periods step forward from the month pillar, one sexagenary step each")
        void forwardStepsFromMonthPillar() {
            BaziChart chart = chart(LocalDateTime.of(1994, 2, 26, 4, 0), 8, Gender.MALE);
            LuckCycles cycles = chart.luckCycles();

            assertThat(cycles.pillars()).hasSize(LuckCycles.PERIOD_COUNT);

            // The first period is the month pillar advanced by one, not the
            // month pillar itself - a common off-by-one, and one that would
            // still look plausible.
            LuckPillar first = cycles.pillars().get(0);
            assertThat(first.branch())
                    .isEqualTo(next(chart.monthPillar().branch()));
            assertThat(first.stem())
                    .isEqualTo(next(chart.monthPillar().stem()));
        }

        @Test
        @DisplayName("Backward periods step backward, and never repeat the month pillar")
        void backwardStepsFromMonthPillar() {
            BaziChart chart = chart(LocalDateTime.of(1990, 1, 1, 11, 10), 8, Gender.MALE);
            LuckCycles cycles = chart.luckCycles();

            LuckPillar first = cycles.pillars().get(0);
            assertThat(first.branch()).isEqualTo(previous(chart.monthPillar().branch()));
            assertThat(first.stem()).isEqualTo(previous(chart.monthPillar().stem()));
        }

        @Test
        @DisplayName("Periods are ten years apart and strictly increasing in age")
        void tenYearSpacing() {
            LuckCycles cycles =
                    chart(LocalDateTime.of(1994, 2, 26, 4, 0), 8, Gender.MALE).luckCycles();

            for (int i = 0; i < cycles.pillars().size(); i++) {
                LuckPillar pillar = cycles.pillars().get(i);
                assertThat(pillar.ordinal()).isEqualTo(i + 1);
                assertThat(pillar.startAgeYears())
                        .as("period %d start age", i + 1)
                        .isEqualTo(cycles.startAge().getYears()
                                + i * LuckCycles.YEARS_PER_PERIOD);
                if (i > 0) {
                    assertThat(pillar.startDate())
                            .isAfter(cycles.pillars().get(i - 1).startDate());
                }
            }
        }

        @Test
        @DisplayName("No period carries any judgement — R1 and R3 are still open")
        void noVerdictAnywhere() {
            LuckCycles cycles =
                    chart(LocalDateTime.of(1994, 2, 26, 4, 0), 8, Gender.MALE).luckCycles();

            // A LuckPillar has no field that could hold a polarity, strength or
            // score, and this asserts that stays true: the record's components
            // are checked by name so adding one becomes a deliberate act
            // against a failing test, exactly as emitsNoSignals does for
            // signals.
            assertThat(LuckPillar.class.getRecordComponents())
                    .extracting(java.lang.reflect.RecordComponent::getName)
                    .containsExactly("ordinal", "stem", "branch", "startAge", "startDate");
            assertThat(cycles).isNotNull();
        }
    }

    @Nested
    @DisplayName("Degradation")
    class Degradation {

        @Test
        @DisplayName("No gender: no luck cycles, a stated reason, and the chart intact")
        void withoutGender() {
            EngineResult<BaziChart> result = calculate(
                    LocalDateTime.of(1990, 1, 1, 11, 10), 8, null, BirthTimePrecision.EXACT);
            BaziChart chart = result.data();

            assertThat(chart.luckCycles()).isNull();
            assertThat(chart.luckCyclesIfPresent()).isEmpty();

            // The chart is untouched - that is the whole reason gender is
            // optional here and required in Phong Thủy.
            assertThat(chart.yearPillar().stem()).isEqualTo(HeavenlyStem.KY);
            assertThat(chart.dayPillar()).isNotNull();

            assertThat(chart.uncertainties())
                    .anySatisfy(u -> {
                        assertThat(u.kind()).isEqualTo(UncertaintyKind.REQUIRED_INPUT_MISSING);
                        assertThat(u.researchId()).isEqualTo("R2");
                    });
            assertThat(result.warnings())
                    .anyMatch(w -> w.code().equals("BAZI_NO_LUCK_CYCLES") && w.critical());
        }

        @Test
        @DisplayName("No exact hour: cycles are still produced, with the blur stated")
        void withoutExactHour() {
            EngineResult<BaziChart> result = calculate(
                    LocalDateTime.of(1990, 1, 1, 11, 10), 8, Gender.MALE,
                    BirthTimePrecision.UNKNOWN);
            BaziChart chart = result.data();

            // Unlike a missing gender, a missing hour does not invalidate the
            // sequence - it only blurs where it starts. Withholding it would
            // lose more than it protects.
            assertThat(chart.luckCycles()).isNotNull();
            assertThat(chart.luckCycles().pillars()).hasSize(LuckCycles.PERIOD_COUNT);
            assertThat(chart.uncertainties())
                    .anySatisfy(u -> {
                        assertThat(u.kind()).isEqualTo(UncertaintyKind.BIRTH_TIME_IMPRECISE);
                        assertThat(u.researchId()).isEqualTo("R2");
                    });
        }

        @Test
        @DisplayName("Đại Vận is no longer a blocked section, and R1/R3 still are")
        void blockedSectionsShrank() {
            BaziChart chart = chart(LocalDateTime.of(1990, 1, 1, 11, 10), 8, Gender.MALE);
            assertThat(chart.blockedSections())
                    .extracting(BlockedSection::researchId)
                    .containsExactlyInAnyOrder("R1", "R3")
                    .doesNotContain("R2");
        }

        @Test
        @DisplayName("Still no signals: a luck cycle is chart data, not a verdict")
        void stillNoSignals() {
            EngineResult<BaziChart> result = calculate(
                    LocalDateTime.of(1994, 2, 26, 4, 0), 8, Gender.MALE,
                    BirthTimePrecision.EXACT);
            assertThat(result.signals()).isEmpty();
        }

        @Test
        @DisplayName("Reproducible: the same input twice gives the same sequence")
        void reproducible() {
            LuckCycles first =
                    chart(LocalDateTime.of(1994, 2, 26, 4, 0), 8, Gender.MALE).luckCycles();
            LuckCycles second =
                    chart(LocalDateTime.of(1994, 2, 26, 4, 0), 8, Gender.MALE).luckCycles();
            assertThat(first).isEqualTo(second);
        }
    }

    // --- helpers ---

    private BaziChart chart(LocalDateTime local, int offsetHours, Gender gender) {
        return calculate(local, offsetHours, gender, BirthTimePrecision.EXACT).data();
    }

    private EngineResult<BaziChart> calculate(LocalDateTime local, int offsetHours,
                                              Gender gender, BirthTimePrecision precision) {
        Instant instant = local.toInstant(ZoneOffset.ofHours(offsetHours));
        return engine.calculate(
                new BaziInput(instant, VietnameseRegion.UNKNOWN, null, precision, gender),
                context());
    }

    private static HeavenlyStem next(HeavenlyStem stem) {
        return HeavenlyStem.fromIndex(stem.index() % 10 + 1);
    }

    private static HeavenlyStem previous(HeavenlyStem stem) {
        return HeavenlyStem.fromIndex((stem.index() + 8) % 10 + 1);
    }

    private static EarthlyBranch next(EarthlyBranch branch) {
        return EarthlyBranch.fromIndex(branch.index() % 12 + 1);
    }

    private static EarthlyBranch previous(EarthlyBranch branch) {
        return EarthlyBranch.fromIndex((branch.index() + 10) % 12 + 1);
    }

    private static CalculationContext context() {
        return new CalculationContext("calc-bazi-luck", BaziEngine.SCHOOL,
                new MethodologyVersions("1.0", "1.0", "1.0", "1.1"),
                ZoneId.of("Asia/Ho_Chi_Minh"), null, null, Instant.EPOCH,
                null, null, BirthTimePrecision.EXACT, null);
    }
}
