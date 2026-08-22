package io.destinyos.engines.bazi;

import static org.assertj.core.api.Assertions.assertThat;

import io.destinyos.calendar.EarthlyBranch;
import io.destinyos.calendar.HeavenlyStem;
import io.destinyos.calendar.VietnameseRegion;
import io.destinyos.core.context.BirthTimePrecision;
import io.destinyos.core.context.CalculationContext;
import io.destinyos.core.result.EngineStatus;
import io.destinyos.core.version.MethodologyVersions;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Golden vectors for Bát Tự chart construction.
 *
 * <p><strong>Where these numbers come from.</strong> Every expected pillar
 * below was read off a published Four Pillars table, not produced by this
 * project's code (CLAUDE.md §32). Primary source: smxs.com's per-date
 * {@code 干支} listings (m.smxs.com/wxque/YYYY-M-D.html), retrieved
 * 2026-08-22. The 1984-02-05 row was independently confirmed against
 * k366.com's listing for the same date, and the 2000-01-01 day pillar (戊午,
 * Mậu Ngọ) was already independently documented and is cited in
 * {@code CanChi}'s own Javadoc.
 *
 * <p><strong>Why noon.</strong> The published tables state one set of pillars
 * per calendar date, which pins the year and month pillar but says nothing
 * about the hour. Sampling at 12:00 local time keeps the solar-term month and
 * the calendar date unambiguous in both UTC+7 and the sources' UTC+8 — the two
 * only diverge in the 23:00-24:00 window — so the published year/month/day
 * pillars apply directly. Hour pillars are exercised separately in
 * {@link BaziEngineTest}, against the Ngũ Thử Độn rule rather than against a
 * chart nobody published.
 *
 * <p><strong>What makes this suite worth having.</strong> The 1984-02-04 and
 * 1984-02-05 rows straddle Lập Xuân (published 23:18:44 Beijing on 4 February)
 * while Tết 1984 had already passed on 2 February. They are therefore the pair
 * that distinguishes the convention this engine implements from the lunar-year
 * convention: get the boundary wrong and both the year <em>and</em> the month
 * stem move.
 */
class BaziEngineGoldenTest {

    private final BaziEngine engine = new BaziEngine();

    @Test
    @DisplayName("1984-02-04, before Lập Xuân: Quý Hợi / Ất Sửu / Mậu Thìn")
    void beforeLapXuan1984() {
        BaziChart chart = chartAtVietnamNoon(1984, 2, 4);

        assertPillar(chart.yearPillar(), HeavenlyStem.QUY, EarthlyBranch.PIG);
        assertPillar(chart.monthPillar(), HeavenlyStem.AT, EarthlyBranch.OX);
        assertPillar(chart.dayPillar(), HeavenlyStem.MAU, EarthlyBranch.DRAGON);

        // The year pillar is 1983's even though the Gregorian year is 1984 -
        // the fact users find most surprising, and the one an off-by-one in the
        // boundary rule would erase.
        assertThat(chart.baziYear()).isEqualTo(1983);
        assertThat(chart.solarMonthIndex()).as("Sửu is solar month 12").isEqualTo(12);
    }

    @Test
    @DisplayName("1984-02-05, after Lập Xuân: Giáp Tý / Bính Dần / Kỷ Tỵ")
    void afterLapXuan1984() {
        BaziChart chart = chartAtVietnamNoon(1984, 2, 5);

        assertPillar(chart.yearPillar(), HeavenlyStem.GIAP, EarthlyBranch.RAT);
        assertPillar(chart.monthPillar(), HeavenlyStem.BINH, EarthlyBranch.TIGER);
        assertPillar(chart.dayPillar(), HeavenlyStem.KY, EarthlyBranch.SNAKE);

        assertThat(chart.baziYear()).isEqualTo(1984);
        assertThat(chart.solarMonthIndex()).as("Dần is solar month 1").isEqualTo(1);
    }

    @Test
    @DisplayName("2000-01-01: Kỷ Mão / Bính Tý / Mậu Ngọ — a January birth keeps 1999's year")
    void newYearsDay2000() {
        BaziChart chart = chartAtVietnamNoon(2000, 1, 1);

        assertPillar(chart.yearPillar(), HeavenlyStem.KY, EarthlyBranch.RABBIT);
        assertPillar(chart.monthPillar(), HeavenlyStem.BINH, EarthlyBranch.RAT);
        assertPillar(chart.dayPillar(), HeavenlyStem.MAU, EarthlyBranch.HORSE);

        assertThat(chart.baziYear()).isEqualTo(1999);
        // Bính Tý is what 1999's Kỷ year stem yields under Ngũ Hổ Độn. Taking
        // the month stem from 2000's Canh instead would give Giáp Tý - so this
        // row also pins that the month stem is derived from the Bát Tự year,
        // not the Gregorian one.
        assertThat(chart.solarMonthIndex()).as("Tý is solar month 11").isEqualTo(11);
    }

    @Test
    @DisplayName("1990-03-15: Canh Ngọ / Kỷ Mão / Kỷ Mão")
    void midMarch1990() {
        BaziChart chart = chartAtVietnamNoon(1990, 3, 15);

        assertPillar(chart.yearPillar(), HeavenlyStem.CANH, EarthlyBranch.HORSE);
        assertPillar(chart.monthPillar(), HeavenlyStem.KY, EarthlyBranch.RABBIT);
        assertPillar(chart.dayPillar(), HeavenlyStem.KY, EarthlyBranch.RABBIT);

        assertThat(chart.baziYear()).isEqualTo(1990);
        assertThat(chart.solarMonthIndex()).as("Mão is solar month 2").isEqualTo(2);
    }

    @Test
    @DisplayName("2024-02-04 noon, still before Lập Xuân: Quý Mão / Ất Sửu / Mậu Tuất")
    void lapXuanDay2024BeforeTheInstant() {
        // Lập Xuân 2024 fell at 15:27 UTC+7 (published). A noon birth on the
        // same calendar date is still in the previous Bát Tự year - the case
        // that proves the boundary is an instant, not a date.
        BaziChart chart = chartAtVietnamNoon(2024, 2, 4);

        assertPillar(chart.yearPillar(), HeavenlyStem.QUY, EarthlyBranch.RABBIT);
        assertPillar(chart.monthPillar(), HeavenlyStem.AT, EarthlyBranch.OX);
        assertPillar(chart.dayPillar(), HeavenlyStem.MAU, EarthlyBranch.DOG);

        assertThat(chart.baziYear()).isEqualTo(2023);
    }

    @Test
    @DisplayName("2024-02-04 late evening, after Lập Xuân: the year and month pillars advance")
    void lapXuanDay2024AfterTheInstant() {
        BaziChart chart = chartAt(LocalDateTime.of(2024, 2, 4, 21, 0));

        assertPillar(chart.yearPillar(), HeavenlyStem.GIAP, EarthlyBranch.DRAGON);
        assertPillar(chart.monthPillar(), HeavenlyStem.BINH, EarthlyBranch.TIGER);
        assertThat(chart.baziYear()).isEqualTo(2024);

        // Same calendar date as the previous test, different year pillar. This
        // is the whole point of resolving the term at the instant.
        assertThat(chart.dayPillar().stem()).isEqualTo(HeavenlyStem.MAU);
        assertThat(chart.dayPillar().branch()).isEqualTo(EarthlyBranch.DOG);
    }

    private BaziChart chartAtVietnamNoon(int year, int month, int day) {
        return chartAt(LocalDateTime.of(year, month, day, 12, 0));
    }

    private BaziChart chartAt(LocalDateTime vietnamLocal) {
        // Vietnam has been UTC+7 nationally since 13 June 1975
        // (HistoricalTimezoneRuleTable), so a UTC+7 local time is the civil
        // time the engine will reconstruct for every date in this suite.
        Instant instant = vietnamLocal.toInstant(ZoneOffset.ofHours(7));
        var result = engine.calculate(
                new BaziInput(instant, VietnameseRegion.UNKNOWN, null, BirthTimePrecision.EXACT),
                context());

        assertThat(result.status())
                .as("engine status for %s", vietnamLocal)
                .isEqualTo(EngineStatus.PARTIAL);
        return result.data();
    }

    private static void assertPillar(BaziPillar pillar, HeavenlyStem stem, EarthlyBranch branch) {
        assertThat(pillar.stem()).as("%s pillar stem", pillar.position()).isEqualTo(stem);
        assertThat(pillar.branch()).as("%s pillar branch", pillar.position()).isEqualTo(branch);
    }

    private static CalculationContext context() {
        return new CalculationContext("calc-bazi-golden", BaziEngine.SCHOOL,
                new MethodologyVersions("1.0", "1.0", "1.0", "1.1"),
                ZoneId.of("Asia/Ho_Chi_Minh"), null, null, Instant.EPOCH,
                null, null, BirthTimePrecision.EXACT, null);
    }
}
