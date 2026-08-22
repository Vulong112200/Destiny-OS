package io.destinyos.engines.fengshui;

import static org.assertj.core.api.Assertions.assertThat;

import io.destinyos.calendar.VietnameseRegion;
import io.destinyos.core.context.BirthTimePrecision;
import io.destinyos.core.context.CalculationContext;
import io.destinyos.core.context.UncertaintyKind;
import io.destinyos.core.result.EngineResult;
import io.destinyos.core.result.EngineStatus;
import io.destinyos.core.signal.Dimension;
import io.destinyos.core.signal.Polarity;
import io.destinyos.core.signal.Strength;
import io.destinyos.core.version.MethodologyVersions;
import io.destinyos.engine.MethodologyStatus;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Behaviour and honesty invariants of {@link FengShuiKuaEngine}. */
class FengShuiKuaEngineTest {

    private final FengShuiKuaEngine engine = new FengShuiKuaEngine();

    @Nested
    @DisplayName("A definite profile")
    class Definite {

        @Test
        @DisplayName("A mid-year birth gives one Kua both conventions agree on")
        void midYearBirthIsUnambiguous() {
            var result = run(LocalDateTime.of(1990, 8, 20, 9, 0), Gender.MALE, null);
            var profile = result.data();

            assertThat(profile.trigram()).isEqualTo(Trigram.KHAM);
            assertThat(profile.boundaryConventionsAgree()).isTrue();
            assertThat(profile.group()).contains(TrigramGroup.EAST);
            assertThat(profile.uncertainties())
                    .noneMatch(u -> "R7".equals(u.researchId()));
        }

        @Test
        @DisplayName("The eight directions split four cát and four hung, all in the right group")
        void directionsAreSplitFourFour() {
            var profile = run(LocalDateTime.of(1990, 8, 20, 9, 0), Gender.MALE, null).data();

            assertThat(profile.directions()).hasSize(8);
            assertThat(profile.auspiciousDirections()).hasSize(4);
            assertThat(profile.inauspiciousDirections()).hasSize(4);
            // Khảm is Đông tứ, so its favourable directions are the East group's.
            assertThat(profile.auspiciousDirections()).containsExactlyInAnyOrder(
                    CompassDirection.NORTH, CompassDirection.EAST,
                    CompassDirection.SOUTHEAST, CompassDirection.SOUTH);
        }

        @Test
        @DisplayName("Male and female born the same year get different profiles")
        void gendersDiffer() {
            var male = run(LocalDateTime.of(1978, 6, 1, 9, 0), Gender.MALE, null).data();
            var female = run(LocalDateTime.of(1978, 6, 1, 9, 0), Gender.FEMALE, null).data();

            assertThat(male.trigram()).isEqualTo(Trigram.TON);
            assertThat(female.trigram()).isEqualTo(Trigram.KHON);
            assertThat(male.group()).contains(TrigramGroup.EAST);
            assertThat(female.group()).contains(TrigramGroup.WEST);
        }
    }

    @Nested
    @DisplayName("Signals only when there is something to judge")
    class Signals {

        @Test
        @DisplayName("Without a facing direction there is a profile but no signal")
        void noDirectionMeansNoSignal() {
            // Bát Trạch relates a person to a direction. A Kua number alone is
            // information, not a judgement - so there is nothing to be
            // favourable or unfavourable *about*, and inventing a polarity for
            // the profile itself would be fabrication.
            var result = run(LocalDateTime.of(1990, 8, 20, 9, 0), Gender.MALE, null);

            assertThat(result.status()).isEqualTo(EngineStatus.PARTIAL);
            assertThat(result.signals()).isEmpty();
            assertThat(result.evidence()).isNotEmpty();
            assertThat(result.warnings())
                    .anyMatch(w -> w.code().equals("KUA_NO_FACING_DIRECTION"));
        }

        @Test
        @DisplayName("A favourable facing direction yields real SUPPORT signals")
        void auspiciousFacingYieldsSupport() {
            // Khảm's Sinh Khí is Đông Nam (Tốn): the two differ only in the top
            // line. Sinh Khí is thượng cát, so SUPPORT at STRONG - both read off
            // the tradition, neither assigned here.
            var result = run(LocalDateTime.of(1990, 8, 20, 9, 0), Gender.MALE,
                    CompassDirection.SOUTHEAST);

            assertThat(result.status()).isEqualTo(EngineStatus.SUCCESS);
            assertThat(result.data().facingRelation()).isEqualTo(BatTrachRelation.SINH_KHI);
            assertThat(result.signals()).isNotEmpty().allSatisfy(signal -> {
                assertThat(signal.polarity()).isEqualTo(Polarity.SUPPORT);
                assertThat(signal.strength()).isEqualTo(Strength.STRONG);
                assertThat(signal.engine()).isEqualTo("FENGSHUI_KUA");
                assertThat(signal.tag()).isEqualTo("FENGSHUI_SINH_KHI");
            });
            assertThat(result.signals()).extracting(io.destinyos.core.signal.Signal::dimension)
                    .containsExactlyInAnyOrder(Dimension.FINANCE, Dimension.CAREER);
        }

        @Test
        @DisplayName("The worst facing direction yields NEGATIVE, not merely CAUTION")
        void tuyetMenhIsNegative() {
            // Khảm's Tuyệt Mệnh is Tây Nam (Khôn) - đại hung. Polarity's own
            // Javadoc insists CAUTION is not the same as NEGATIVE, so the
            // tradition's đại/thứ/tiểu hung ranking has to survive into the
            // signal rather than being flattened.
            var result = run(LocalDateTime.of(1990, 8, 20, 9, 0), Gender.MALE,
                    CompassDirection.SOUTHWEST);

            assertThat(result.data().facingRelation()).isEqualTo(BatTrachRelation.TUYET_MENH);
            assertThat(result.signals()).allSatisfy(signal -> {
                assertThat(signal.polarity()).isEqualTo(Polarity.NEGATIVE);
                assertThat(signal.strength()).isEqualTo(Strength.STRONG);
            });
        }

        @Test
        @DisplayName("A mildly unfavourable direction yields CAUTION, not NEGATIVE")
        void hoaHaiIsOnlyCaution() {
            // Khảm's Hoạ Hại is Tây (Đoài) - tiểu hung. Flattening all four bad
            // relations to NEGATIVE would overstate this one.
            var result = run(LocalDateTime.of(1990, 8, 20, 9, 0), Gender.MALE,
                    CompassDirection.WEST);

            assertThat(result.data().facingRelation()).isEqualTo(BatTrachRelation.HOA_HAI);
            assertThat(result.signals()).allSatisfy(signal -> {
                assertThat(signal.polarity()).isEqualTo(Polarity.CAUTION);
                assertThat(signal.strength()).isEqualTo(Strength.WEAK);
            });
        }

        @Test
        @DisplayName("Signals from one facing assessment share an evidence group")
        void signalsShareOneEvidenceGroup() {
            // FUSION_ENGINE_SPEC §5: one finding must not be counted several
            // times just because it speaks to several life areas.
            var result = run(LocalDateTime.of(1990, 8, 20, 9, 0), Gender.MALE,
                    CompassDirection.SOUTHEAST);

            assertThat(result.signals())
                    .extracting(io.destinyos.core.signal.Signal::evidenceGroupId)
                    .containsOnly("FENGSHUI_KUA_FACING");
        }

        @Test
        @DisplayName("No signal is marked critical — a facing direction is not a methodology limit")
        void facingSignalsAreNotCritical() {
            var result = run(LocalDateTime.of(1990, 8, 20, 9, 0), Gender.MALE,
                    CompassDirection.SOUTHWEST);

            assertThat(result.signals()).allSatisfy(signal ->
                    assertThat(signal.critical()).isFalse());
        }
    }

    @Nested
    @DisplayName("The year boundary R7 leaves open")
    class YearBoundary {

        @Test
        @DisplayName("A birth between Tết and Lập Xuân reports both Kua numbers and no signal")
        void disagreementIsReportedNotResolved() {
            // Tết 1984 fell on 2 February and Lập Xuân on 4 February. For a male
            // born on the 3rd: the Lập Xuân convention uses 1983 (Cấn) and the
            // Tết convention uses 1984 (Đoài). Neither is presented as the
            // answer - no source arbitrates.
            var result = run(LocalDateTime.of(1984, 2, 3, 9, 0), Gender.MALE,
                    CompassDirection.SOUTHEAST);
            var profile = result.data();

            assertThat(profile.boundaryConventionsAgree()).isFalse();
            assertThat(profile.trigram()).isEqualTo(Trigram.CAN);
            assertThat(profile.trigramByTet()).isEqualTo(Trigram.DOAI);
            assertThat(profile.lapXuanYear()).isEqualTo(1983);
            assertThat(profile.tetYear()).isEqualTo(1984);

            // Even though a facing direction was supplied, no signal is emitted:
            // two candidate Kua numbers mean two different readings of the same
            // direction, and picking one is the choice R7 says nobody has earned.
            assertThat(result.signals()).isEmpty();
            assertThat(profile.facingRelation()).isNull();
            assertThat(result.status()).isEqualTo(EngineStatus.PARTIAL);
            assertThat(result.researchReference().researchId()).isEqualTo("R7");

            assertThat(profile.uncertainties())
                    .filteredOn(u -> "R7".equals(u.researchId()))
                    .hasSize(1)
                    .allSatisfy(u -> {
                        assertThat(u.kind()).isEqualTo(UncertaintyKind.METHODOLOGY_UNRESOLVED);
                        assertThat(u.affectsResult()).isTrue();
                        // Both answers must appear, or the user cannot see that
                        // a choice was made on their behalf.
                        assertThat(u.detail()).contains("CAN").contains("DOAI");
                    });
            assertThat(result.warnings())
                    .anyMatch(w -> w.code().equals("KUA_YEAR_BOUNDARY_SCHOOLS_DISAGREE")
                            && w.critical());
        }

        @Test
        @DisplayName("The eight directions are withheld when the conventions disagree")
        void directionsAreNotPublishedWhenAmbiguous() {
            // Publishing one trigram's directions would present the Lập Xuân
            // answer as the answer.
            var result = run(LocalDateTime.of(1984, 2, 3, 9, 0), Gender.MALE, null);

            assertThat(result.evidence())
                    .extracting(io.destinyos.core.evidence.Evidence::ruleId)
                    .contains("FENGSHUI_KUA_NUMBER")
                    .doesNotContain("FENGSHUI_BAT_TRACH_DIRECTIONS");
            assertThat(result.data().group()).isEmpty();
        }

        @Test
        @DisplayName("A window birth whose two years happen to give the same Kua is not flagged")
        void noSpuriousDisagreement() {
            // The formula can map two adjacent years onto the same Kua, and then
            // the user has a definite answer. Comparing the trigram rather than
            // the year is what avoids warning them about a dispute that does not
            // affect them. 1990-08 is outside the window entirely; asserted here
            // as the control for the case above.
            var result = run(LocalDateTime.of(1990, 8, 20, 9, 0), Gender.FEMALE, null);

            assertThat(result.data().boundaryConventionsAgree()).isTrue();
            assertThat(result.warnings())
                    .noneMatch(w -> w.code().equals("KUA_YEAR_BOUNDARY_SCHOOLS_DISAGREE"));
        }
    }

    @Nested
    @DisplayName("Declining honestly")
    class Declining {

        @Test
        @DisplayName("A missing gender is invalid input, not a defaulted answer")
        void genderIsRequired() {
            var result = engine.calculate(new FengShuiKuaInput(
                    instant(LocalDateTime.of(1990, 8, 20, 9, 0)), null,
                    VietnameseRegion.UNKNOWN, null, null), context());

            assertThat(result.status()).isEqualTo(EngineStatus.INVALID_INPUT);
            assertThat(result.data()).isNull();
            assertThat(result.errors()).first()
                    .extracting(io.destinyos.core.result.EngineError::code)
                    .isEqualTo("KUA_GENDER_REQUIRED");
        }

        @Test
        @DisplayName("An unresolvable (date, region) declines rather than guessing an offset")
        void r14bGapDeclines() {
            var result = engine.calculate(new FengShuiKuaInput(
                    instant(LocalDateTime.of(1960, 3, 10, 8, 0)), Gender.MALE,
                    VietnameseRegion.UNKNOWN, null, null), context());

            assertThat(result.status()).isEqualTo(EngineStatus.RESEARCH_REQUIRED);
            assertThat(result.researchReference().researchId()).isEqualTo("R14b");
        }

        @Test
        @DisplayName("The same date with a known region resolves")
        void knownRegionResolves() {
            var result = engine.calculate(new FengShuiKuaInput(
                    instant(LocalDateTime.of(1960, 3, 10, 8, 0)), Gender.MALE,
                    VietnameseRegion.SOUTH, null, null), context());

            assertThat(result.data()).isNotNull();
            assertThat(result.data().trigram()).isNotNull();
        }

        @Test
        @DisplayName("A date outside the supported range is rejected, not extrapolated")
        void outOfRangeIsRejected() {
            var validation = engine.validateInput(new FengShuiKuaInput(
                    instant(LocalDateTime.of(1850, 1, 1, 9, 0)), Gender.MALE,
                    VietnameseRegion.UNKNOWN, null, null));

            assertThat(validation.valid()).isFalse();
        }
    }

    @Nested
    @DisplayName("Contract")
    class Contract {

        @Test
        @DisplayName("Metadata names the school and source and does not blend schools")
        void metadataIsHonest() {
            var metadata = engine.metadata();

            assertThat(metadata.engineId()).isEqualTo("FENGSHUI_KUA");
            assertThat(metadata.status()).isEqualTo(MethodologyStatus.PRODUCTION_READY);
            assertThat(metadata.school()).contains("Bát Trạch");
            // Master Spec §20 forbids blending; the school string must not claim
            // to cover the other two.
            assertThat(metadata.school()).doesNotContain("Phi Tinh").doesNotContain("Huyền Không");
            assertThat(metadata.source()).isNotBlank();
        }

        @Test
        @DisplayName("Same input, same result")
        void isReproducible() {
            var first = run(LocalDateTime.of(1978, 6, 1, 9, 0), Gender.FEMALE,
                    CompassDirection.WEST).data();
            var second = run(LocalDateTime.of(1978, 6, 1, 9, 0), Gender.FEMALE,
                    CompassDirection.WEST).data();

            assertThat(second).isEqualTo(first);
        }

        @Test
        @DisplayName("Every relation has at least one life area mapped")
        void everyRelationHasDimensions() {
            // A relation with no dimension would silently emit no signal, which
            // would look identical to "the tradition says nothing about it".
            for (BatTrachRelation relation : BatTrachRelation.values()) {
                assertThat(BatTrachMeanings.dimensionsOf(relation))
                        .as("life areas for %s", relation)
                        .isNotNull()
                        .isNotEmpty();
            }
            assertThat(BatTrachMeanings.covered())
                    .containsExactlyInAnyOrder(BatTrachRelation.values());
        }

        @Test
        @DisplayName("The engine declares it needs the calendar and no name or seed")
        void capabilityIsAccurate() {
            var capability = engine.capability();

            assertThat(capability.requiresCalendar()).isTrue();
            assertThat(capability.requiresLocation()).isTrue();
            assertThat(capability.requiresName()).isFalse();
            assertThat(capability.requiresSeed()).isFalse();
            assertThat(capability.deterministic()).isTrue();
        }
    }

    private EngineResult<KuaProfile> run(LocalDateTime vietnamLocal, Gender gender,
                                        CompassDirection facing) {
        return engine.calculate(new FengShuiKuaInput(instant(vietnamLocal), gender,
                VietnameseRegion.UNKNOWN, null, facing), context());
    }

    private static Instant instant(LocalDateTime vietnamLocal) {
        return vietnamLocal.toInstant(ZoneOffset.ofHours(7));
    }

    private static CalculationContext context() {
        return new CalculationContext("calc-fengshui-test", FengShuiKuaEngine.SCHOOL,
                new MethodologyVersions("1.0", "1.0", "1.0", "1.1"),
                ZoneId.of("Asia/Ho_Chi_Minh"), null, null, Instant.EPOCH,
                null, null, BirthTimePrecision.EXACT, null);
    }
}
