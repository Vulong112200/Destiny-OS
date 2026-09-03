package io.destinyos.engines.bazi;

import static org.assertj.core.api.Assertions.assertThat;

import io.destinyos.calendar.EarthlyBranch;
import io.destinyos.calendar.FiveElement;
import io.destinyos.calendar.HeavenlyStem;
import io.destinyos.calendar.VietnameseRegion;
import io.destinyos.core.context.BirthTimePrecision;
import io.destinyos.core.context.CalculationContext;
import io.destinyos.core.context.UncertaintyKind;
import io.destinyos.core.result.EngineResult;
import io.destinyos.core.result.EngineStatus;
import io.destinyos.core.result.EngineWarning;
import io.destinyos.core.signal.Dimension;
import io.destinyos.core.version.MethodologyVersions;
import io.destinyos.engine.MethodologyStatus;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Behaviour and honesty invariants of {@link BaziEngine}. */
class BaziEngineTest {

    private final BaziEngine engine = new BaziEngine();

    @Nested
    @DisplayName("Honesty about what is not computed")
    class Honesty {

        @Test
        @DisplayName("The engine emits no signals at all, and says why")
        void emitsNoSignals() {
            // The load-bearing assertion of this whole module. A Bát Tự signal
            // needs a polarity, a polarity needs Day Master strength (R3) and a
            // Dụng Thần school (R1), and both are open. Filling this list is a
            // decision someone must make deliberately, against a failing test -
            // not something that happens by drifting.
            EngineResult<BaziChart> result = run(LocalDateTime.of(1990, 5, 20, 9, 30));

            assertThat(result.signals()).isEmpty();
            assertThat(result.status()).isEqualTo(EngineStatus.PARTIAL);
            assertThat(result.researchReferenceIfPresent()).isPresent();
            assertThat(result.researchReference().knownVariants())
                    .anyMatch(v -> v.contains("R1"))
                    .anyMatch(v -> v.contains("R3"));
            // R2 dropped off this list when Đại Vận was implemented
            // (2026-08-22): the sequence is now real chart data. It still
            // yields no signal, because whether a period is favourable needs
            // R1 and R3 — so the reference names only those two.
            assertThat(result.researchReference().knownVariants())
                    .noneMatch(v -> v.contains("R2"));
        }

        @Test
        @DisplayName("Điều Hầu/Thông Quan and Day Master strength cross-school comparability are reported as blocked, not absent")
        void blockedSectionsAreNamedWithTheirResearchIds() {
            BaziChart chart = run(LocalDateTime.of(1990, 5, 20, 9, 30)).data();

            // Đại Vận left this list on 2026-08-22 when R2 closed; R20-R22
            // joined it on 2026-08-23, when an audit against Master Spec §13
            // found three named components with no research id at all — and
            // therefore no way for the engine to admit they were missing.
            // DUNG_THAN itself left 2026-09-03 when R1 was decided and
            // BaziDungThanResolver shipped for 8/10 cách phổ thông; what
            // remains named here is the narrower residual (Điều Hầu/Thông
            // Quan), not the whole topic. The list is asserted exactly, not
            // as a subset, so a section can neither appear nor disappear
            // without a test changing on purpose.
            assertThat(chart.blockedSections())
                    .extracting(BlockedSection::sectionId)
                    .containsExactlyInAnyOrder("DUNG_THAN_DIEU_HAU_TONG_QUAN", "NHAT_CHU_CUONG_DO",
                            "HOP_XUNG_HINH_HAI_PHA", "LUU_NIEN", "THAN_SAT");
            assertThat(chart.blockedSections())
                    .extracting(BlockedSection::researchId)
                    .containsExactlyInAnyOrder("R1", "R3", "R20", "R21", "R22");
            // A blocked section with no named variants reads as an oversight
            // rather than as a real disagreement (Rule D).
            assertThat(chart.blockedSections())
                    .allSatisfy(section -> assertThat(section.knownVariants()).isNotEmpty());
        }

        @Test
        @DisplayName("Every component Master Spec §13 names is either computed or named as blocked")
        void everySpecifiedComponentIsAccountedFor() {
            // The test that would have caught the 2026-08-23 audit finding.
            // Until then this list was checked only against itself, so three
            // components named in the specification - combinations/clashes,
            // Liu Nian/Yue/Ri, and Shen Sha - were missing from it without
            // anything failing. A component that is neither computed nor
            // blocked is invisible, which is worse than an admitted gap: the
            // user cannot even know to ask.
            BaziChart chart = run(LocalDateTime.of(1990, 5, 20, 9, 30)).data();
            var blockedIds = chart.blockedSections().stream()
                    .map(BlockedSection::sectionId).toList();

            // Computed today - present in the chart itself.
            assertThat(chart.pillars()).as("Four Pillars").isNotEmpty();
            assertThat(chart.pillars().get(0).stem()).as("Heavenly Stems").isNotNull();
            assertThat(chart.pillars().get(0).branch()).as("Earthly Branches").isNotNull();
            assertThat(chart.pillars().get(0).stemPolarity()).as("Yin/Yang").isNotNull();
            assertThat(chart.pillars().get(0).stemElement()).as("Five Elements").isNotNull();
            assertThat(chart.pillars().get(0).hiddenStems()).as("Hidden Stems").isNotNull();
            assertThat(chart.dayMaster()).as("Day Master").isPresent();
            assertThat(chart.monthPillar().stemTenGod()).as("Ten Gods").isNotNull();
            // Da Yun needs a gender, which this fixture omits; LuckCycleTest
            // covers it. What matters here is that it is no longer blocked.
            assertThat(blockedIds).as("Da Yun is computed since R2 closed")
                    .doesNotContain("DAI_VAN");
            // Dụng Thần itself (as opposed to its Điều Hầu/Thông Quan
            // residual) is computed since R1 closed 2026-09-03, for 8/10
            // cách phổ thông - BaziDungThanResolverGoldenTest covers whether
            // any given chart's cách resolves or refuses.
            assertThat(blockedIds).as("Dụng Thần itself is computed since R1 closed 2026-09-03")
                    .doesNotContain("DUNG_THAN");

            // Not computed - and each must therefore be named as blocked.
            assertThat(blockedIds).contains(
                    "HOP_XUNG_HINH_HAI_PHA",           // combinations/clashes/harm/punishment/break
                    "NHAT_CHU_CUONG_DO",                // strength methodology
                    "DUNG_THAN_DIEU_HAU_TONG_QUAN",     // Điều Hầu/Thông Quan, the residual of Useful Element
                    "LUU_NIEN",                          // Liu Nian / Liu Yue / Liu Ri
                    "THAN_SAT");                         // Shen Sha (conditional in the spec)
        }

        @Test
        @DisplayName("Every blocked section also surfaces as a critical warning")
        void blockedSectionsSurviveNarrativePruning() {
            // AI_NARRATIVE_SPEC keeps critical warnings through pruning. If
            // these were plain warnings, the one thing the user most needs to
            // know - that the Dụng Thần is missing on purpose - is the first
            // thing a token budget would drop.
            List<EngineWarning> warnings = run(LocalDateTime.of(1990, 5, 20, 9, 30)).warnings();

            assertThat(warnings)
                    .filteredOn(w -> w.code().startsWith("BAZI_SECTION_BLOCKED_"))
                    .hasSize(5)
                    .allSatisfy(w -> assertThat(w.critical()).isTrue());
        }

        @Test
        @DisplayName("The element tally stays three separate integer counts with no total")
        void elementTallyIsNotAStrengthScore() {
            BaziChart chart = run(LocalDateTime.of(1984, 2, 5, 12, 0)).data();

            // Four stems and four branches, always - that is what makes these
            // counts checkable. Hidden stems vary by which branches appear.
            assertThat(sum(chart.elementTally().stems())).isEqualTo(4);
            assertThat(sum(chart.elementTally().branches())).isEqualTo(4);
            assertThat(sum(chart.elementTally().hiddenStems())).isBetween(4, 12);

            // Zero-filled rather than sparse, so "no Kim at all" is visible as
            // a 0 instead of as a missing key a renderer might skip.
            assertThat(chart.elementTally().stems()).containsOnlyKeys(FiveElement.values());
        }

        private static int sum(java.util.Map<FiveElement, Integer> tally) {
            return tally.values().stream().mapToInt(Integer::intValue).sum();
        }
    }

    @Nested
    @DisplayName("Birth time precision (Master Spec section 2)")
    class Precision {

        @Test
        @DisplayName("Without an exact hour there is no day pillar, no Day Master, no Thập Thần")
        void unknownPrecisionDropsTheDayAndHourPillars() {
            EngineResult<BaziChart> result = engine.calculate(
                    new BaziInput(instant(LocalDateTime.of(1990, 5, 20, 9, 30)),
                            VietnameseRegion.UNKNOWN, null, BirthTimePrecision.UNKNOWN, null),
                    context());
            BaziChart chart = result.data();

            assertThat(chart.hasHourPrecision()).isFalse();
            assertThat(chart.dayPillar()).isNull();
            assertThat(chart.hourPillar()).isNull();
            assertThat(chart.dayMaster()).isEmpty();
            assertThat(chart.pillars()).hasSize(2);

            // No Day Master means no Thập Thần anywhere - not a Thập Thần
            // computed against the year stem instead.
            assertThat(chart.pillars())
                    .allSatisfy(pillar -> {
                        assertThat(pillar.stemTenGod()).isNull();
                        assertThat(pillar.hiddenStemTenGods()).isEmpty();
                    });
            assertThat(result.warnings())
                    .anyMatch(w -> w.code().equals("BAZI_NO_DAY_MASTER") && w.critical());
        }

        @Test
        @DisplayName("APPROXIMATE is treated as not-exact, never as exact")
        void approximateIsNotExact() {
            BaziChart chart = engine.calculate(
                    new BaziInput(instant(LocalDateTime.of(1990, 5, 20, 9, 30)),
                            VietnameseRegion.UNKNOWN, null, BirthTimePrecision.APPROXIMATE, null),
                    context()).data();

            assertThat(chart.hasHourPrecision()).isFalse();
        }

        @Test
        @DisplayName("With an exact hour, the Day Master carries no Thập Thần of its own")
        void dayMasterHasNoRoleRelativeToItself() {
            BaziChart chart = run(LocalDateTime.of(1990, 5, 20, 9, 30)).data();

            assertThat(chart.dayMaster()).isPresent();
            assertThat(chart.dayPillar().stemTenGod())
                    .as("the day stem is the reference point, not a role")
                    .isNull();
            assertThat(chart.yearPillar().stemTenGod()).isNotNull();
            assertThat(chart.monthPillar().stemTenGod()).isNotNull();
            assertThat(chart.hourPillar().stemTenGod()).isNotNull();
        }

        @Test
        @DisplayName("Hour branch follows R10's 23:00 Giờ Tý boundary")
        void hourBranchFollowsTheZiBoundary() {
            assertThat(run(LocalDateTime.of(2000, 6, 15, 23, 30)).data().hourPillar().branch())
                    .as("23:30 is Giờ Tý, not Giờ Hợi")
                    .isEqualTo(EarthlyBranch.RAT);
            assertThat(run(LocalDateTime.of(2000, 6, 15, 12, 0)).data().hourPillar().branch())
                    .isEqualTo(EarthlyBranch.HORSE);
        }
    }

    @Nested
    @DisplayName("Uncertainty that must reach the user (ADR D3)")
    class Uncertainties {

        @Test
        @DisplayName("An unresolvable (date, region) declines instead of guessing an offset")
        void r14bGapReturnsResearchRequired() {
            // 1960-1975 is the window where North and South ran different
            // offsets, and R14b has no source for the geographic boundary. With
            // an UNKNOWN region there is nothing to resolve.
            EngineResult<BaziChart> result = engine.calculate(
                    new BaziInput(instant(LocalDateTime.of(1960, 3, 10, 8, 0)),
                            VietnameseRegion.UNKNOWN, null, BirthTimePrecision.EXACT, null),
                    context());

            assertThat(result.status()).isEqualTo(EngineStatus.RESEARCH_REQUIRED);
            assertThat(result.data()).isNull();
            assertThat(result.researchReference().researchId()).isEqualTo("R14b");
        }

        @Test
        @DisplayName("The same date with a known region resolves normally")
        void sameDateWithAKnownRegionResolves() {
            EngineResult<BaziChart> result = engine.calculate(
                    new BaziInput(instant(LocalDateTime.of(1960, 3, 10, 8, 0)),
                            VietnameseRegion.SOUTH, null, BirthTimePrecision.EXACT, null),
                    context());

            assertThat(result.status()).isEqualTo(EngineStatus.PARTIAL);
            assertThat(result.data().hasHourPrecision()).isTrue();
        }

        @Test
        @DisplayName("A birth between Tết and Lập Xuân reports both year pillars (R18)")
        void yearBoundaryDisagreementIsReportedNotResolved() {
            // Tết 1984 fell on 2 February; Lập Xuân on 4 February. A birth on
            // 3 February is Giáp Tý by the Tết convention and Quý Hợi by the
            // Lập Xuân convention this engine uses.
            BaziChart chart = run(LocalDateTime.of(1984, 2, 3, 12, 0)).data();

            assertThat(chart.yearPillar().stem()).isEqualTo(HeavenlyStem.QUY);
            assertThat(chart.uncertainties())
                    .filteredOn(u -> "R18".equals(u.researchId()))
                    .hasSize(1)
                    .allSatisfy(u -> {
                        assertThat(u.kind()).isEqualTo(UncertaintyKind.METHODOLOGY_UNRESOLVED);
                        assertThat(u.affectsResult()).isTrue();
                        // Both answers must appear, or the user cannot see that
                        // a choice was made on their behalf.
                        assertThat(u.detail()).contains("QUY").contains("GIAP");
                    });
        }

        @Test
        @DisplayName("A mid-year birth raises no year-boundary uncertainty")
        void noSpuriousYearBoundaryWarning() {
            BaziChart chart = run(LocalDateTime.of(1990, 8, 20, 12, 0)).data();

            assertThat(chart.uncertainties())
                    .noneMatch(u -> "R18".equals(u.researchId()));
        }

        @Test
        @DisplayName("A birth within the guard window of a Tiết Khí instant is flagged (R19)")
        void solarTermBoundaryIsFlagged() {
            // Lập Xuân 2024 computed at 15:11 UTC+7; 15:00 is 11 minutes short,
            // well inside the 40-minute window the model's own accuracy needs.
            EngineResult<BaziChart> result = run(LocalDateTime.of(2024, 2, 4, 15, 0));

            assertThat(result.data().uncertainties())
                    .filteredOn(u -> u.kind() == UncertaintyKind.SOLAR_TERM_BOUNDARY)
                    .hasSize(1)
                    .allSatisfy(u -> assertThat(u.affectsResult()).isTrue());
            assertThat(result.warnings())
                    .anyMatch(w -> w.code().equals("BAZI_SOLAR_TERM_BOUNDARY") && w.critical());
        }

        @Test
        @DisplayName("A birth far from any boundary is not flagged")
        void noSpuriousSolarTermWarning() {
            assertThat(run(LocalDateTime.of(2024, 2, 12, 12, 0)).data().uncertainties())
                    .noneMatch(u -> u.kind() == UncertaintyKind.SOLAR_TERM_BOUNDARY);
        }

        @Test
        @DisplayName("A missing longitude is recorded rather than silently ignored (R10)")
        void missingLongitudeIsRecorded() {
            assertThat(run(LocalDateTime.of(1990, 8, 20, 12, 0)).data().uncertainties())
                    .anyMatch(u -> u.kind() == UncertaintyKind.LONGITUDE_UNKNOWN);
        }

        @Test
        @DisplayName("A supplied longitude shifts local time to mean solar time and drops the caveat")
        void longitudeAppliesTheSolarCorrection() {
            // Ho Chi Minh City sits at ~106.7 E, 1.7 degrees east of the UTC+7
            // standard meridian, so mean solar time runs about 7 minutes ahead.
            var result = engine.calculate(
                    new BaziInput(instant(LocalDateTime.of(1990, 8, 20, 12, 0)),
                            VietnameseRegion.UNKNOWN, 106.7, BirthTimePrecision.EXACT, null),
                    context());
            BaziChart chart = result.data();

            assertThat(chart.localSolarDateTime())
                    .isAfter(LocalDateTime.of(1990, 8, 20, 12, 0))
                    .isBefore(LocalDateTime.of(1990, 8, 20, 12, 15));
            assertThat(chart.uncertainties())
                    .noneMatch(u -> u.kind() == UncertaintyKind.LONGITUDE_UNKNOWN);
        }
    }

    @Nested
    @DisplayName("Contract, evidence and reproducibility")
    class Contract {

        @Test
        @DisplayName("Metadata names the school and source, and admits content is missing")
        void metadataIsHonest() {
            var metadata = engine.metadata();

            assertThat(metadata.engineId()).isEqualTo("BAZI");
            assertThat(metadata.methodologyId()).isEqualTo("BAZI_TUBINH_CHART");
            assertThat(metadata.status()).isEqualTo(MethodologyStatus.CONTENT_REQUIRED);
            assertThat(metadata.school()).contains("Lập Xuân").contains("Tiết Khí");
            assertThat(metadata.source()).isNotBlank();
        }

        @Test
        @DisplayName("Evidence covers every pillar, the boundary, the tally and each blocker")
        void evidenceIsComplete() {
            var evidence = run(LocalDateTime.of(1984, 2, 5, 12, 0)).evidence();

            assertThat(evidence).extracting(io.destinyos.core.evidence.Evidence::ruleId)
                    .contains("BAZI_PILLAR_YEAR", "BAZI_PILLAR_MONTH", "BAZI_PILLAR_DAY",
                            "BAZI_PILLAR_HOUR", "BAZI_BOUNDARY", "BAZI_ELEMENT_TALLY",
                            "BAZI_BLOCKED_DUNG_THAN_DIEU_HAU_TONG_QUAN", "BAZI_BLOCKED_NHAT_CHU_CUONG_DO")
                    // Đại Vận moved from a blocked section to real evidence
                    // (R2, 2026-08-22) — but only when a gender was supplied,
                    // and this fixture supplies none, so neither rule id is
                    // present here. LuckCycleTest covers the supplied case.
                    .doesNotContain("BAZI_BLOCKED_DAI_VAN", "BAZI_LUCK_CYCLES");
            // One group id, so pruning and deduplication treat the chart as a
            // single finding rather than as nine unrelated ones.
            assertThat(evidence).extracting(io.destinyos.core.evidence.Evidence::evidenceGroupId)
                    .containsOnly("BAZI_CHART");
            assertThat(evidence).allSatisfy(e ->
                    assertThat(e.ruleVersion()).isEqualTo(BaziEngine.RULE_VERSION));
        }

        @Test
        @DisplayName("The boundary evidence states which convention produced the year pillar")
        void boundaryEvidenceNamesTheConvention() {
            var boundary = run(LocalDateTime.of(1984, 2, 5, 12, 0)).evidence().stream()
                    .filter(e -> e.ruleId().equals("BAZI_BOUNDARY"))
                    .findFirst()
                    .orElseThrow();

            assertThat(boundary.fact())
                    .containsEntry("yearBoundary", "LAP_XUAN")
                    .containsEntry("baziYear", 1984)
                    .containsEntry("solarMonthIndex", 1)
                    .containsEntry("solarMonthBranch", "TIGER");
            assertThat(boundary.dimension()).isEqualTo(Dimension.TIMING);
        }

        @Test
        @DisplayName("Same input, same versions, same result (Master Spec section 25)")
        void isReproducible() {
            var first = run(LocalDateTime.of(1984, 2, 5, 12, 0)).data();
            var second = run(LocalDateTime.of(1984, 2, 5, 12, 0)).data();

            // Evidence ids are per-calculation UUIDs by design, so the chart -
            // not the EngineResult - is what must compare equal.
            assertThat(second).isEqualTo(first);
        }

        @Test
        @DisplayName("A date outside the supported range is rejected, not extrapolated")
        void outOfRangeDateIsRejected() {
            var validation = engine.validateInput(
                    new BaziInput(instant(LocalDateTime.of(1850, 1, 1, 12, 0)),
                            VietnameseRegion.UNKNOWN, null, BirthTimePrecision.EXACT, null));

            assertThat(validation.valid()).isFalse();
            assertThat(validation.errors()).first()
                    .extracting(io.destinyos.core.result.EngineError::code)
                    .isEqualTo("OUTSIDE_SUPPORTED_RANGE");
        }

        @Test
        @DisplayName("An impossible longitude is rejected")
        void invalidLongitudeIsRejected() {
            var validation = engine.validateInput(
                    new BaziInput(instant(LocalDateTime.of(1990, 1, 1, 12, 0)),
                            VietnameseRegion.UNKNOWN, 500.0, BirthTimePrecision.EXACT, null));

            assertThat(validation.valid()).isFalse();
        }

        @Test
        @DisplayName("A valid input passes validation")
        void validInputPasses() {
            assertThat(engine.validateInput(
                    new BaziInput(instant(LocalDateTime.of(1990, 1, 1, 12, 0)),
                            VietnameseRegion.UNKNOWN, 105.85, BirthTimePrecision.EXACT, null))
                    .valid()).isTrue();
        }

        @Test
        @DisplayName("The engine declares it needs the calendar, and does not need a name or seed")
        void capabilityIsAccurate() {
            var capability = engine.capability();

            assertThat(capability.requiresCalendar()).isTrue();
            assertThat(capability.requiresName()).isFalse();
            assertThat(capability.requiresSeed()).isFalse();
            assertThat(capability.deterministic()).isTrue();
            // False even though an hour improves the result: the engine degrades
            // to year and month pillars rather than declining outright.
            assertThat(capability.requiresBirthTime()).isFalse();
        }
    }

    private EngineResult<BaziChart> run(LocalDateTime vietnamLocal) {
        return engine.calculate(
                new BaziInput(instant(vietnamLocal), VietnameseRegion.UNKNOWN, null,
                        BirthTimePrecision.EXACT, null),
                context());
    }

    private static Instant instant(LocalDateTime vietnamLocal) {
        return vietnamLocal.toInstant(ZoneOffset.ofHours(7));
    }

    private static CalculationContext context() {
        return new CalculationContext("calc-bazi-test", BaziEngine.SCHOOL,
                new MethodologyVersions("1.0", "1.0", "1.0", "1.1"),
                ZoneId.of("Asia/Ho_Chi_Minh"), null, null, Instant.EPOCH,
                null, null, BirthTimePrecision.EXACT, null);
    }
}
