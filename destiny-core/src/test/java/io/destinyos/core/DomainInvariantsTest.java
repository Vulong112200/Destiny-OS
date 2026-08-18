package io.destinyos.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.destinyos.core.context.BirthTimePrecision;
import io.destinyos.core.context.CalculationContext;
import io.destinyos.core.context.Uncertainty;
import io.destinyos.core.context.UncertaintyKind;
import io.destinyos.core.result.EngineResult;
import io.destinyos.core.result.EngineStatus;
import io.destinyos.core.result.ResearchReference;
import io.destinyos.core.signal.Applicability;
import io.destinyos.core.signal.Dimension;
import io.destinyos.core.signal.Polarity;
import io.destinyos.core.signal.Signal;
import io.destinyos.core.signal.Strength;
import io.destinyos.core.version.CalendarMethodologyRef;
import io.destinyos.core.version.MethodologyVersions;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * The invariants the domain model exists to guarantee.
 *
 * <p>Each test here corresponds to a specific rule in the specification, and
 * to a specific way the system could quietly become dishonest if the rule were
 * lost.
 */
class DomainInvariantsTest {

    private static MethodologyVersions versions() {
        return new MethodologyVersions("1.0", "1.0", "1.0", "1.0");
    }

    private static CalculationContext context(List<Uncertainty> uncertainties) {
        return new CalculationContext("calc-1", "TEST", versions(),
                ZoneId.of("Asia/Ho_Chi_Minh"), Locale.forLanguageTag("vi-VN"), null,
                Instant.parse("2026-08-18T00:00:00Z"), "VN_NORTH", null,
                BirthTimePrecision.EXACT, uncertainties);
    }

    @Nested
    @DisplayName("An engine that declines to answer must explain itself (ADR D7)")
    class HonestNonAnswers {

        @Test
        @DisplayName("RESEARCH_REQUIRED without a reference is rejected at construction")
        void researchRequiredNeedsAReference() {
            assertThatThrownBy(() -> new EngineResult<String>(
                    EngineStatus.RESEARCH_REQUIRED, null, List.of(), List.of(),
                    List.of(), List.of(), null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ResearchReference");
        }

        @Test
        @DisplayName("NOT_IMPLEMENTED without a reference is rejected too")
        void notImplementedNeedsAReference() {
            assertThatThrownBy(() -> new EngineResult<String>(
                    EngineStatus.NOT_IMPLEMENTED, null, List.of(), List.of(),
                    List.of(), List.of(), null, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("A blocked engine names what is missing, not just that it failed")
        void blockedEngineIsInformative() {
            var result = EngineResult.researchRequired(new ResearchReference(
                    "R1", "Bát Tự", "Dụng Thần school not selected",
                    "RESEARCH_BLOCKERS.md", List.of("phù ức", "điều hậu")));

            assertThat(result.status()).isEqualTo(EngineStatus.RESEARCH_REQUIRED);
            assertThat(result.researchReference().missing()).isNotBlank();
            assertThat(result.researchReference().knownVariants()).hasSize(2);
        }

        @Test
        @DisplayName("An honest non-answer is not classed as a failure")
        void nonAnswerIsNotFailure() {
            assertThat(EngineStatus.RESEARCH_REQUIRED.isHonestNonAnswer()).isTrue();
            assertThat(EngineStatus.RESEARCH_REQUIRED.isFailure()).isFalse();
            assertThat(EngineStatus.NOT_APPLICABLE.isHonestNonAnswer()).isTrue();
            assertThat(EngineStatus.NOT_APPLICABLE.isFailure()).isFalse();
            assertThat(EngineStatus.FAILED_RECOVERABLE.isFailure()).isTrue();
        }
    }

    @Nested
    @DisplayName("NOT_APPLICABLE must never behave like NEUTRAL (risk RK7)")
    class ApplicabilitySeparation {

        @Test
        @DisplayName("Applicability and Polarity are separate axes")
        void separateAxes() {
            // The type system is the enforcement: there is no NOT_APPLICABLE
            // member of Polarity to accidentally select.
            assertThat(Polarity.values())
                    .extracting(Enum::name)
                    .doesNotContain("NOT_APPLICABLE");
        }

        @Test
        @DisplayName("A non-applicable signal does not participate")
        void nonApplicableDoesNotParticipate() {
            var signal = new Signal("s1", "TAROT", "RWS", Dimension.FINANCE,
                    "FINANCE_SUPPORT", Polarity.SUPPORT, Strength.STRONG,
                    Applicability.NOT_APPLICABLE, false, List.of(), null);

            assertThat(signal.participates()).isFalse();
        }

        @Test
        @DisplayName("participatingSignals filters non-applicable ones out")
        void resultFiltersNonParticipating() {
            var applicable = new Signal("s1", "A", null, Dimension.FINANCE, "T",
                    Polarity.SUPPORT, Strength.MEDIUM, Applicability.HIGH, false, List.of(), null);
            var notApplicable = new Signal("s2", "B", null, Dimension.FINANCE, "T",
                    Polarity.CAUTION, Strength.STRONG, Applicability.NOT_APPLICABLE,
                    false, List.of(), null);

            var result = EngineResult.success("data", List.of(), List.of(applicable, notApplicable));

            assertThat(result.signals()).hasSize(2);
            assertThat(result.participatingSignals())
                    .extracting(Signal::signalId)
                    .containsExactly("s1");
        }
    }

    @Nested
    @DisplayName("Criticality has exactly one encoding (DECISION_LOG C3)")
    class CriticalityEncoding {

        @Test
        @DisplayName("Strength carries magnitude only; CRITICAL is not a member")
        void strengthHasNoCriticalMember() {
            assertThat(Strength.values())
                    .extracting(Enum::name)
                    .containsExactly("WEAK", "MEDIUM", "STRONG")
                    .doesNotContain("CRITICAL");
        }

        @Test
        @DisplayName("A weak signal can still be critical - the axes are orthogonal")
        void weakCanBeCritical() {
            var signal = new Signal("s1", "CALENDAR", null, Dimension.TIMING, "BOUNDARY",
                    Polarity.CAUTION, Strength.WEAK, Applicability.HIGH, true, List.of(), null);

            assertThat(signal.critical()).isTrue();
            assertThat(signal.strength()).isEqualTo(Strength.WEAK);
            assertThat(signal.isActiveCritical()).isTrue();
        }

        @Test
        @DisplayName("A critical signal that does not apply is not an active critical")
        void nonApplicableCriticalIsInactive() {
            var signal = new Signal("s1", "X", null, Dimension.TIMING, "T",
                    Polarity.CAUTION, Strength.STRONG, Applicability.NOT_APPLICABLE,
                    true, List.of(), null);

            assertThat(signal.isActiveCritical()).isFalse();
        }
    }

    @Nested
    @DisplayName("Uncertainty is preserved, not resolved away (ADR D3)")
    class UncertaintyPreservation {

        @Test
        @DisplayName("A result-affecting uncertainty is visible on the context")
        void resultAffectingUncertaintyIsVisible() {
            var ctx = context(List.of(Uncertainty.of(
                    UncertaintyKind.HISTORICAL_TIMEZONE_RULE_UNKNOWN,
                    "No sourced rule for 1962-03-01 in VN_NORTH", "R14")));

            assertThat(ctx.hasResultAffectingUncertainty()).isTrue();
            assertThat(ctx.resultAffectingUncertainties()).hasSize(1);
        }

        @Test
        @DisplayName("An informational uncertainty does not raise the user-facing flag")
        void informationalDoesNotFlag() {
            var ctx = context(List.of(Uncertainty.informational(
                    UncertaintyKind.DAY_BOUNDARY, "Both conventions agree here")));

            assertThat(ctx.hasResultAffectingUncertainty()).isFalse();
        }

        @Test
        @DisplayName("Adding an uncertainty returns a copy; the context stays immutable")
        void withUncertaintyIsImmutable() {
            var original = context(List.of());
            var updated = original.withUncertainty(Uncertainty.of(
                    UncertaintyKind.BIRTH_REGION_UNKNOWN, "region missing", "R17"));

            assertThat(original.uncertainties()).isEmpty();
            assertThat(updated.uncertainties()).hasSize(1);
        }

        @Test
        @DisplayName("The uncertainty list cannot be mutated through the caller's reference")
        void listIsDefensivelyCopied() {
            var mutable = new ArrayList<Uncertainty>();
            mutable.add(Uncertainty.of(UncertaintyKind.DAY_BOUNDARY, "d", "R10"));

            var ctx = context(mutable);
            mutable.clear();

            assertThat(ctx.uncertainties()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Calculation identity supports reproducibility (CLAUDE.md section 6)")
    class ReproducibilityIdentity {

        @Test
        @DisplayName("Identical contexts produce identical identity strings")
        void identityIsStable() {
            assertThat(context(List.of()).toIdentityString())
                    .isEqualTo(context(List.of()).toIdentityString());
        }

        @Test
        @DisplayName("Region is part of identity (ADR D3)")
        void regionAffectsIdentity() {
            // Under D3 two people born at the same UTC instant in different
            // regions may legitimately receive different Can Chi, so region
            // cannot be incidental metadata.
            var north = new CalculationContext("c", "S", versions(),
                    ZoneId.of("Asia/Ho_Chi_Minh"), Locale.forLanguageTag("vi-VN"), null,
                    Instant.EPOCH, "VN_NORTH", null, BirthTimePrecision.EXACT, List.of());
            var south = new CalculationContext("c", "S", versions(),
                    ZoneId.of("Asia/Ho_Chi_Minh"), Locale.forLanguageTag("vi-VN"), null,
                    Instant.EPOCH, "VN_SOUTH", null, BirthTimePrecision.EXACT, List.of());

            assertThat(north.toIdentityString()).isNotEqualTo(south.toIdentityString());
        }

        @Test
        @DisplayName("Changing any version component changes identity")
        void versionAffectsIdentity() {
            var v1 = new MethodologyVersions("1.0", "1.0", "1.0", "1.0");
            var v2 = new MethodologyVersions("1.0", "1.1", "1.0", "1.0");

            assertThat(v1.toCanonicalString()).isNotEqualTo(v2.toCanonicalString());
        }

        @Test
        @DisplayName("Birth time precision is part of identity, and defaults to UNKNOWN")
        void precisionDefaultsToUnknown() {
            // Master Spec section 2: never treat UNKNOWN as EXACT. Defaulting
            // to the cautious value means a caller who forgets to set it gets
            // the safe answer rather than a falsely precise one.
            var ctx = new CalculationContext("c", "S", versions(),
                    ZoneId.of("UTC"), null, null, Instant.EPOCH, null, null, null, null);

            assertThat(ctx.birthTimePrecision()).isEqualTo(BirthTimePrecision.UNKNOWN);
            assertThat(ctx.birthTimePrecision().supportsHourPrecision()).isFalse();
        }

        @Test
        @DisplayName("Locale defaults to Vietnamese (CLAUDE.md section 9)")
        void localeDefaultsToVietnamese() {
            var ctx = new CalculationContext("c", "S", versions(),
                    ZoneId.of("UTC"), null, null, Instant.EPOCH, null, null, null, null);

            assertThat(ctx.locale().toLanguageTag()).isEqualTo("vi-VN");
        }
    }

    @Nested
    @DisplayName("Calendar methodology reference (ADR D3)")
    class CalendarMethodology {

        @Test
        @DisplayName("An unresolved timezone rule reads as absent, not as a default")
        void unresolvedRuleIsEmpty() {
            // The crucial distinction: "we have no sourced rule" must not be
            // representable as "we applied the usual one".
            var ref = new CalendarMethodologyRef("VN_TRADITIONAL", "1.0",
                    LocalDate.of(1900, 1, 1), LocalDate.of(2100, 1, 1),
                    "source-tbd", null, null);

            assertThat(ref.resolvedTimezoneRuleIfKnown()).isEmpty();
        }

        @Test
        @DisplayName("Dates outside the validity range are not covered")
        void rangeIsRespected() {
            var ref = new CalendarMethodologyRef("VN_TRADITIONAL", "1.0",
                    LocalDate.of(1900, 1, 1), LocalDate.of(2100, 1, 1),
                    "source-tbd", null, null);

            assertThat(ref.covers(LocalDate.of(1950, 6, 1))).isTrue();
            assertThat(ref.covers(LocalDate.of(1899, 12, 31))).isFalse();
            assertThat(ref.covers(LocalDate.of(2100, 1, 2))).isFalse();
        }
    }

    @Nested
    @DisplayName("Results are immutable")
    class Immutability {

        @Test
        @DisplayName("Signal evidence lists are defensively copied")
        void signalEvidenceIsCopied() {
            var ids = new ArrayList<String>();
            ids.add("ev-1");

            var signal = new Signal("s", "E", null, Dimension.OTHER, "T",
                    Polarity.NEUTRAL, Strength.WEAK, Applicability.LOW, false, ids, null);
            ids.clear();

            assertThat(signal.evidenceIds()).containsExactly("ev-1");
        }

        @Test
        @DisplayName("withMetadata returns a copy rather than mutating")
        void metadataIsCopyOnWrite() {
            var original = EngineResult.success("d", List.of(), List.of());
            var updated = original.withMetadata(java.util.Map.of("k", "v"));

            assertThat(original.metadata()).isEmpty();
            assertThat(updated.metadata()).containsEntry("k", "v");
        }
    }
}
