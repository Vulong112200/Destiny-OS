package io.destinyos.engines.astrology;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import io.destinyos.core.context.CalculationContext;
import io.destinyos.core.context.BirthTimePrecision;
import io.destinyos.core.result.EngineResult;
import io.destinyos.core.result.EngineStatus;
import io.destinyos.core.version.MethodologyVersions;
import io.destinyos.engine.EngineCapability;
import io.destinyos.engine.MethodologyStatus;
import io.destinyos.engine.ValidationResult;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Behaviour and honesty invariants of {@link WesternAstrologyEngine}. */
class WesternAstrologyEngineTest {

    private final WesternAstrologyEngine engine = new WesternAstrologyEngine();

    @Nested
    @DisplayName("Honesty about what is not computed")
    class Honesty {

        @Test
        @DisplayName("The engine emits no signals at all")
        void emitsNoSignals() {
            // Same load-bearing shape as BaziEngineTest.emitsNoSignals: a
            // signal needs interpretive content this phase has not authored,
            // and most of the chart (nine of ten bodies) is not computed yet
            // regardless. Filling this in must be a deliberate act against a
            // failing test.
            var result = run(hanoi());
            assertThat(result.signals()).isEmpty();
            assertThat(result.status()).isEqualTo(EngineStatus.PARTIAL);
        }

        @Test
        @DisplayName("Planets beyond the Sun, and aspects, are named as blocked")
        void blockedSectionsAreNamed() {
            var chart = run(hanoi()).data();
            assertThat(chart.blockedSections())
                    .extracting(BlockedSection::sectionId)
                    .containsExactlyInAnyOrder("PLANETS_BEYOND_SUN", "ASPECTS");
            assertThat(chart.blockedSections())
                    .extracting(BlockedSection::researchId)
                    .containsExactlyInAnyOrder("R5", "R6");
            assertThat(chart.blockedSections())
                    .allSatisfy(section -> assertThat(section.knownVariants()).isNotEmpty());
        }

        @Test
        @DisplayName("Every blocked section also surfaces as a critical warning")
        void blockedSectionsSurviveNarrativePruning() {
            var warnings = run(hanoi()).warnings();
            assertThat(warnings)
                    .filteredOn(w -> w.code().startsWith("ASTROLOGY_SECTION_BLOCKED_"))
                    .hasSize(2)
                    .allSatisfy(w -> assertThat(w.critical()).isTrue());
        }

        @Test
        @DisplayName("Metadata names the school and cites a source, and admits content is missing")
        void metadataIsHonest() {
            var metadata = engine.metadata();
            assertThat(metadata.engineId()).isEqualTo("WESTERN_ASTROLOGY");
            assertThat(metadata.status()).isEqualTo(MethodologyStatus.CONTENT_REQUIRED);
            assertThat(metadata.school()).contains("Tropical").contains("Whole Sign");
            assertThat(metadata.source()).isNotBlank();
        }

        @Test
        @DisplayName("The chart declares its zodiac and house system explicitly")
        void chartDeclaresItsOwnConventions() {
            // Master Spec section 15: every chart must carry zodiacSystem and
            // houseSystem on itself, not only in engine-level metadata -
            // otherwise a chart persisted today cannot be told apart from one
            // computed after a future version adds sidereal or Placidus.
            var chart = run(hanoi()).data();
            assertThat(chart.zodiacSystem()).isEqualTo("TROPICAL");
            assertThat(chart.houseSystem()).isEqualTo("WHOLE_SIGN");
        }
    }

    @Nested
    @DisplayName("Chart construction, wired end to end")
    class ChartConstruction {

        @Test
        @DisplayName("At RAMC=90, latitude=0: MC is Cancer 0 deg and Ascendant is Libra 0 deg")
        void cardinalCaseWiresCorrectly() {
            // Reuses SiderealTimeTest's own verified instant (Meeus's worked
            // example, GMST=174.7711135 deg) and picks a longitude that
            // forces RAMC to exactly 90 deg for that instant, so this test
            // exercises the full engine pipeline (JulianDay conversion, GMST,
            // longitude addition, ChartAngles) against the two
            // independently-derived cardinal cases in ChartAnglesTest,
            // rather than trusting an external chart.
            Instant instant = LocalDateTime.of(1994, 6, 16, 18, 0).toInstant(ZoneOffset.UTC);
            double longitudeForRamc90 = 90.0 - 174.7711135;

            var input = new WesternAstrologyInput(instant, 0.0, longitudeForRamc90);
            var chart = engine.calculate(input, context()).data();

            assertThat(chart.midheaven().sign()).isEqualTo(ZodiacSign.CANCER);
            assertThat(chart.midheaven().degreesIntoSign()).isCloseTo(0.0, within(1e-3));
            assertThat(chart.ascendant().sign()).isEqualTo(ZodiacSign.LIBRA);
            assertThat(chart.ascendant().degreesIntoSign()).isCloseTo(0.0, within(1e-3));
        }

        @Test
        @DisplayName("Ascendant is always House 1 of its own chart")
        void ascendantIsAlwaysHouseOne() {
            var chart = run(hanoi()).data();
            assertThat(chart.houseOf(chart.ascendant().sign())).isEqualTo(AstrologicalHouse.HOUSE_1);
        }

        @Test
        @DisplayName("All twelve houses are present and cover all twelve signs exactly once")
        void allHousesPresent() {
            var chart = run(hanoi()).data();
            assertThat(chart.houses()).hasSize(12);
            assertThat(chart.houses().values()).containsExactlyInAnyOrder(ZodiacSign.values());
        }

        @Test
        @DisplayName("Reproducible: the same input twice gives the same chart")
        void reproducible() {
            var first = run(hanoi()).data();
            var second = run(hanoi()).data();
            assertThat(first).isEqualTo(second);
        }

        @Test
        @DisplayName("Obliquity and RAMC are carried on the chart for audit")
        void frameValuesArePresent() {
            var chart = run(hanoi()).data();
            // Obliquity near J2000 is well known to be close to 23.44 deg;
            // this is a sanity bound, not a precision assertion (that lives
            // in ObliquityOfEcliptic's own derivation and SiderealTimeTest).
            assertThat(chart.obliquityDegrees()).isBetween(23.0, 23.6);
            assertThat(chart.ramcDegrees()).isBetween(0.0, 360.0);
        }
    }

    @Nested
    @DisplayName("Validation")
    class Validation {

        @Test
        @DisplayName("Latitude outside [-90, 90] is rejected")
        void invalidLatitudeRejected() {
            var input = new WesternAstrologyInput(hanoi().utcInstant(), 91.0, 105.8);
            ValidationResult result = engine.validateInput(input);
            assertThat(result.valid()).isFalse();
        }

        @Test
        @DisplayName("Longitude outside [-180, 180] is rejected")
        void invalidLongitudeRejected() {
            var input = new WesternAstrologyInput(hanoi().utcInstant(), 21.0, 200.0);
            ValidationResult result = engine.validateInput(input);
            assertThat(result.valid()).isFalse();
        }

        @Test
        @DisplayName("A birth at the polar circle is accepted, not rejected")
        void polarLatitudeIsAccepted() {
            // The entire point of choosing Whole Sign (R6): unlike
            // Placidus/Koch, it is defined at every latitude.
            var input = new WesternAstrologyInput(hanoi().utcInstant(), 78.0, 15.6);
            assertThat(engine.validateInput(input).valid()).isTrue();
            assertThat(run(input).status()).isEqualTo(EngineStatus.PARTIAL);
        }

        @Test
        @DisplayName("Capability declares birth time and location as required")
        void capabilityDeclaresRequirements() {
            EngineCapability capability = engine.capability();
            assertThat(capability.requiresBirthTime()).isTrue();
            assertThat(capability.requiresLocation()).isTrue();
            assertThat(capability.requiresCalendar()).isFalse();
        }
    }

    private WesternAstrologyInput hanoi() {
        Instant instant = LocalDateTime.of(1990, 8, 20, 14, 30)
                .atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant();
        return new WesternAstrologyInput(instant, 21.0285, 105.8542);
    }

    private EngineResult<WesternAstrologyChart> run(WesternAstrologyInput input) {
        return engine.calculate(input, context());
    }

    private static CalculationContext context() {
        return new CalculationContext("calc-astrology-test", WesternAstrologyEngine.SCHOOL,
                new MethodologyVersions("1.0", "1.0", "1.0", "1.0"),
                ZoneId.of("Asia/Ho_Chi_Minh"), null, null, Instant.EPOCH,
                null, null, BirthTimePrecision.EXACT, null);
    }
}
