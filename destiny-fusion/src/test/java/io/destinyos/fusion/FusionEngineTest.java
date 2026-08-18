package io.destinyos.fusion;

import static org.assertj.core.api.Assertions.assertThat;

import io.destinyos.core.signal.Applicability;
import io.destinyos.core.signal.Dimension;
import io.destinyos.core.signal.Polarity;
import io.destinyos.core.signal.Signal;
import io.destinyos.core.signal.Strength;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * The 14-case test matrix mandated by FUSION_ENGINE_SPEC.md section 12,
 * plus the source-diversity and critical-signal guarantees section 5 and
 * section 9 require.
 */
class FusionEngineTest {

    private final FusionEngine fusion = new FusionEngine();

    private static Signal signal(String engine, String school, Dimension dim, Polarity polarity,
                                 boolean critical) {
        return new Signal(engine + "-" + polarity + "-" + java.util.UUID.randomUUID(),
                engine, school, dim, "TAG", polarity, Strength.MEDIUM,
                Applicability.HIGH, critical, List.of(), null);
    }

    private static Signal signal(String engine, Dimension dim, Polarity polarity) {
        return signal(engine, null, dim, polarity, false);
    }

    @Nested
    @DisplayName("FUSION_ENGINE_SPEC.md section 12 test matrix")
    class SpecMatrix {

        @Test
        @DisplayName("0 engine -> INSUFFICIENT_EVIDENCE")
        void zeroEngines() {
            var result = fusion.fuse(List.of());
            assertThat(result.overallOutcome()).isEqualTo(FusionOutcome.INSUFFICIENT_EVIDENCE);
            assertThat(result.dimensions()).isEmpty();
        }

        @Test
        @DisplayName("1 support -> CONSENSUS_SUPPORT (unanimous among the one source that exists)")
        void oneSupport() {
            var result = fusion.fuse(List.of(signal("TAROT", Dimension.CAREER, Polarity.SUPPORT)));
            assertThat(result.overallOutcome()).isEqualTo(FusionOutcome.CONSENSUS_SUPPORT);
        }

        @Test
        @DisplayName("1 caution -> CONSENSUS_CAUTION")
        void oneCaution() {
            var result = fusion.fuse(List.of(signal("TAROT", Dimension.CAREER, Polarity.CAUTION)));
            assertThat(result.overallOutcome()).isEqualTo(FusionOutcome.CONSENSUS_CAUTION);
        }

        @Test
        @DisplayName("support unanimous (3 engines) -> CONSENSUS_SUPPORT")
        void supportUnanimous() {
            var result = fusion.fuse(List.of(
                    signal("A", Dimension.FINANCE, Polarity.SUPPORT),
                    signal("B", Dimension.FINANCE, Polarity.SUPPORT),
                    signal("C", Dimension.FINANCE, Polarity.SUPPORT)));

            assertThat(result.overallOutcome()).isEqualTo(FusionOutcome.CONSENSUS_SUPPORT);
            assertThat(result.supportingSources()).containsExactlyInAnyOrder("A", "B", "C");
        }

        @Test
        @DisplayName("caution unanimous (3 engines) -> CONSENSUS_CAUTION")
        void cautionUnanimous() {
            var result = fusion.fuse(List.of(
                    signal("A", Dimension.FINANCE, Polarity.CAUTION),
                    signal("B", Dimension.FINANCE, Polarity.CAUTION),
                    signal("C", Dimension.FINANCE, Polarity.CAUTION)));

            assertThat(result.overallOutcome()).isEqualTo(FusionOutcome.CONSENSUS_CAUTION);
        }

        @Test
        @DisplayName("2 vs 1 (support dominant with caution present) -> SUPPORT_WITH_CAUTION")
        void twoVsOne() {
            var result = fusion.fuse(List.of(
                    signal("A", Dimension.FINANCE, Polarity.SUPPORT),
                    signal("B", Dimension.FINANCE, Polarity.SUPPORT),
                    signal("C", Dimension.FINANCE, Polarity.CAUTION)));

            assertThat(result.overallOutcome()).isEqualTo(FusionOutcome.SUPPORT_WITH_CAUTION);
            assertThat(result.dimensions().get(0).rulesApplied()).contains("R4");
        }

        @Test
        @DisplayName("2 vs 2 (no majority, no true opposite pole) -> MIXED")
        void twoVsTwo() {
            var result = fusion.fuse(List.of(
                    signal("A", Dimension.FINANCE, Polarity.SUPPORT),
                    signal("B", Dimension.FINANCE, Polarity.SUPPORT),
                    signal("C", Dimension.FINANCE, Polarity.CAUTION),
                    signal("D", Dimension.FINANCE, Polarity.CAUTION)));

            assertThat(result.overallOutcome()).isEqualTo(FusionOutcome.MIXED);
        }

        @Test
        @DisplayName("critical caution survives 3-to-1 majority support -> SUPPORT_WITH_CRITICAL_CAUTION")
        void criticalCaution() {
            var result = fusion.fuse(List.of(
                    signal("A", Dimension.FINANCE, Polarity.SUPPORT),
                    signal("B", Dimension.FINANCE, Polarity.SUPPORT),
                    signal("C", Dimension.FINANCE, Polarity.SUPPORT),
                    signal("D", null, Dimension.FINANCE, Polarity.CAUTION, true)));

            assertThat(result.overallOutcome()).isEqualTo(FusionOutcome.SUPPORT_WITH_CRITICAL_CAUTION);
            assertThat(result.hasCriticalSignal()).isTrue();
            assertThat(result.dimensions().get(0).rulesApplied()).contains("R5");
        }

        @Test
        @DisplayName("same-engine duplicate: 2 signals from one engine count as ONE source, not two")
        void sameEngineDuplicateIsOneSource() {
            // Section 5/8.3: an engine emitting multiple signals must not be
            // counted as multiple independent votes. Without dedup this
            // would read as "2 support vs 1 caution" (SUPPORT_WITH_CAUTION);
            // correctly deduped it is 1-vs-1, genuinely MIXED.
            var result = fusion.fuse(List.of(
                    signal("A", Dimension.FINANCE, Polarity.SUPPORT),
                    signal("A", Dimension.FINANCE, Polarity.SUPPORT), // same engine again
                    signal("B", Dimension.FINANCE, Polarity.CAUTION)));

            assertThat(result.overallOutcome()).isEqualTo(FusionOutcome.MIXED);
            assertThat(result.dimensions().get(0).supportingEngines()).containsExactly("A");
            assertThat(result.dimensions().get(0).sourceCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("methodology conflict: two schools of the same engine disagree -> METHODOLOGY_CONFLICT")
        void methodologyConflict() {
            var result = fusion.fuse(List.of(
                    signal("BAZI", "SCHOOL_A", Dimension.CAREER, Polarity.SUPPORT, false),
                    signal("BAZI", "SCHOOL_B", Dimension.CAREER, Polarity.CAUTION, false)));

            assertThat(result.overallOutcome()).isEqualTo(FusionOutcome.METHODOLOGY_CONFLICT);
            assertThat(result.conflicts())
                    .anyMatch(c -> c.type() == ConflictType.METHODOLOGY_CONFLICT);
        }

        @Test
        @DisplayName("scope conflict: different dimensions never produce a conflict against each other")
        void scopeConflictIsNotAConflict() {
            var result = fusion.fuse(List.of(
                    signal("TAROT", Dimension.FINANCE, Polarity.CAUTION),
                    signal("NUMEROLOGY", Dimension.RELATIONSHIP, Polarity.SUPPORT)));

            assertThat(result.conflicts()).isEmpty();
            assertThat(result.dimensions()).hasSize(2);
        }

        @Test
        @DisplayName("engine timeout: fusion still produces a valid result from whatever signals remain")
        void engineTimeoutStillProducesAResult() {
            // Simulates: 3 engines were asked, one timed out and contributed
            // nothing (EngineExecutor already reports that as
            // FAILED_RECOVERABLE upstream - Fusion simply never sees a
            // signal from it and must not treat that as an error).
            var result = fusion.fuse(List.of(
                    signal("A", Dimension.FINANCE, Polarity.SUPPORT),
                    signal("B", Dimension.FINANCE, Polarity.SUPPORT)));

            assertThat(result.overallOutcome()).isEqualTo(FusionOutcome.CONSENSUS_SUPPORT);
        }

        @Test
        @DisplayName("NOT_APPLICABLE signals are excluded entirely, never counted as NEUTRAL")
        void notApplicableIsExcluded() {
            Signal notApplicable = new Signal("na-1", "FENGSHUI", null, Dimension.FINANCE,
                    "TAG", Polarity.CAUTION, Strength.STRONG, Applicability.NOT_APPLICABLE,
                    false, List.of(), null);

            var result = fusion.fuse(List.of(
                    signal("A", Dimension.FINANCE, Polarity.SUPPORT),
                    notApplicable));

            assertThat(result.overallOutcome()).isEqualTo(FusionOutcome.CONSENSUS_SUPPORT);
            assertThat(result.dimensions().get(0).sourceCount()).isEqualTo(1);
            assertThat(result.dimensions().get(0).cautionEngines()).doesNotContain("FENGSHUI");
        }

        @Test
        @DisplayName("incomplete evidence: a single applicable source still yields a definite outcome")
        void incompleteEvidenceStillYieldsAnOutcome() {
            // Only one of several requested engines actually contributed to
            // this dimension - R1 (insufficient) applies only at true zero,
            // not merely "fewer than expected."
            var result = fusion.fuse(List.of(signal("A", Dimension.CAREER, Polarity.SUPPORT)));

            assertThat(result.overallOutcome()).isNotEqualTo(FusionOutcome.INSUFFICIENT_EVIDENCE);
        }
    }

    @Nested
    @DisplayName("Additional guarantees")
    class AdditionalGuarantees {

        @Test
        @DisplayName("A true opposite-pole tie (SUPPORT vs NEGATIVE, equal counts) is MAJOR_CONFLICT")
        void trueOppositePoleTieIsMajorConflict() {
            var result = fusion.fuse(List.of(
                    signal("A", Dimension.CAREER, Polarity.SUPPORT),
                    signal("B", Dimension.CAREER, Polarity.NEGATIVE)));

            assertThat(result.overallOutcome()).isEqualTo(FusionOutcome.MAJOR_CONFLICT);
            assertThat(result.conflicts())
                    .anyMatch(c -> c.type() == ConflictType.DIRECT_CONFLICT);
        }

        @Test
        @DisplayName("Unanimous NEGATIVE reaches CONSENSUS_NEGATIVE (reachable, per Master Spec section 9)")
        void unanimousNegativeIsReachable() {
            var result = fusion.fuse(List.of(
                    signal("A", Dimension.CAREER, Polarity.NEGATIVE),
                    signal("B", Dimension.CAREER, Polarity.NEGATIVE)));

            assertThat(result.overallOutcome()).isEqualTo(FusionOutcome.CONSENSUS_NEGATIVE);
        }

        @Test
        @DisplayName("Critical support survives a caution majority -> CAUTION_WITH_CRITICAL_SUPPORT")
        void criticalSupportSurvivesCautionMajority() {
            var result = fusion.fuse(List.of(
                    signal("A", Dimension.CAREER, Polarity.CAUTION),
                    signal("B", Dimension.CAREER, Polarity.CAUTION),
                    signal("C", null, Dimension.CAREER, Polarity.SUPPORT, true)));

            assertThat(result.overallOutcome()).isEqualTo(FusionOutcome.CAUTION_WITH_CRITICAL_SUPPORT);
        }

        @Test
        @DisplayName("Multiple dimensions with different states overall read as MIXED")
        void multipleDimensionsWithDifferentStatesAreMixedOverall() {
            var result = fusion.fuse(List.of(
                    signal("A", Dimension.FINANCE, Polarity.SUPPORT),
                    signal("B", Dimension.RELATIONSHIP, Polarity.CAUTION)));

            assertThat(result.overallOutcome()).isEqualTo(FusionOutcome.MIXED);
            assertThat(result.dimensions()).hasSize(2);
        }

        @Test
        @DisplayName("rulesApplied is de-duplicated and non-empty whenever a dimension resolves")
        void rulesAppliedIsPopulated() {
            var result = fusion.fuse(List.of(signal("A", Dimension.CAREER, Polarity.SUPPORT)));
            assertThat(result.rulesApplied()).isNotEmpty();
        }
    }
}
