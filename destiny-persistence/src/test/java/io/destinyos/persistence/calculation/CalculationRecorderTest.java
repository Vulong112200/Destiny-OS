package io.destinyos.persistence.calculation;

import static org.assertj.core.api.Assertions.assertThat;

import io.destinyos.core.context.BirthTimePrecision;
import io.destinyos.core.context.CalculationContext;
import io.destinyos.core.evidence.Evidence;
import io.destinyos.core.result.EngineError;
import io.destinyos.core.result.EngineResult;
import io.destinyos.core.result.EngineStatus;
import io.destinyos.core.signal.Applicability;
import io.destinyos.core.signal.Dimension;
import io.destinyos.core.signal.Polarity;
import io.destinyos.core.signal.Signal;
import io.destinyos.core.signal.Strength;
import io.destinyos.core.version.MethodologyVersions;
import io.destinyos.execution.EngineExecution;
import io.destinyos.execution.ExecutionOutcome;
import io.destinyos.fusion.Conflict;
import io.destinyos.fusion.ConflictType;
import io.destinyos.fusion.DimensionAnalysis;
import io.destinyos.fusion.DimensionState;
import io.destinyos.fusion.FusionOutcome;
import io.destinyos.fusion.FusionResult;
import io.destinyos.persistence.TestApplication;
import jakarta.persistence.EntityManager;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

/**
 * Round-trip tests for the V4-V6 schema: a calculation, its per-engine
 * results, evidence, signals and fused conclusion must all be recoverable
 * exactly as computed (CLAUDE.md section 6).
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestApplication.class, CalculationRecorder.class})
class CalculationRecorderTest {

    @Autowired
    private CalculationRecorder recorder;
    @Autowired
    private CalculationRepository calculations;
    @Autowired
    private CalculationEngineResultRepository engineResults;
    @Autowired
    private EvidenceRepository evidenceRepo;
    @Autowired
    private SignalRepository signalRepo;
    @Autowired
    private FusionResultRepository fusionResultRepo;
    @Autowired
    private ConflictRepository conflictRepo;
    @Autowired
    private EntityManager entityManager;

    private static CalculationContext context(String calculationId) {
        return new CalculationContext(calculationId, "TEST_SCHOOL",
                new MethodologyVersions("1.0", "1.0", "1.0", "1.0"),
                ZoneId.of("Asia/Ho_Chi_Minh"), Locale.forLanguageTag("vi-VN"), 42L,
                Instant.parse("2026-08-18T00:00:00Z"), "VN_SOUTH", null,
                BirthTimePrecision.EXACT, List.of());
    }

    private static Evidence evidence(String id) {
        return new Evidence(id, "TAROT", "RWS", "TAROT_SEEDED_DRAW", "1.0",
                Dimension.FINANCE, Map.of("cardName", "The Fool", "orientation", "UPRIGHT"),
                "seeded-draw", "group-1", null);
    }

    private static Signal signal(String id, String engine, Polarity polarity, List<String> evidenceIds) {
        return new Signal(id, engine, "RWS", Dimension.FINANCE, "TAG", polarity, Strength.MEDIUM,
                Applicability.HIGH, false, evidenceIds, "group-1");
    }

    @Nested
    @DisplayName("Full round trip")
    class FullRoundTrip {

        @Test
        @DisplayName("A calculation with evidence, signals and a fusion result round-trips exactly")
        void fullRoundTrip() {
            var ctx = context("calc-rt-1");
            var ev = evidence("ev-rt-1");
            var sig = signal("sig-rt-1", "TAROT", Polarity.SUPPORT, List.of("ev-rt-1"));

            var engineResult = EngineResult.success("payload", List.of(ev), List.of(sig));
            var execution = new ExecutionOutcome(List.of(
                    new EngineExecution("TAROT", engineResult, Duration.ofMillis(120), false)));

            var dimensionAnalysis = new DimensionAnalysis(Dimension.FINANCE, DimensionState.POSITIVE,
                    Set.of("TAROT"), Set.of(), Set.of(), Set.of(), List.of(), List.of("R2"));
            var fusion = new FusionResult(FusionOutcome.CONSENSUS_SUPPORT, List.of(dimensionAnalysis),
                    List.of(), List.of(), List.of("R2"), Set.of("TAROT"), Set.of());

            recorder.record(ctx, execution, fusion);
            entityManager.flush();
            entityManager.clear();

            var savedCalc = calculations.findById("calc-rt-1").orElseThrow();
            assertThat(savedCalc.status()).isEqualTo(EngineStatus.SUCCESS);
            assertThat(savedCalc.resultHash()).isNotBlank();
            assertThat(savedCalc.seed()).isEqualTo(42L);
            assertThat(savedCalc.calendarVersion()).isEqualTo("1.0");
            assertThat(savedCalc.completedAt()).isNotNull();

            var savedEngineResults = engineResults.findByCalculationId("calc-rt-1");
            assertThat(savedEngineResults).hasSize(1);
            assertThat(savedEngineResults.get(0).status()).isEqualTo(EngineStatus.SUCCESS);
            assertThat(savedEngineResults.get(0).timedOut()).isFalse();

            var savedEvidence = evidenceRepo.findById("ev-rt-1").orElseThrow();
            assertThat(savedEvidence.dimension()).isEqualTo(Dimension.FINANCE);
            assertThat(savedEvidence.fact()).containsEntry("cardName", "The Fool");

            var savedSignal = signalRepo.findById("sig-rt-1").orElseThrow();
            assertThat(savedSignal.polarity()).isEqualTo(Polarity.SUPPORT);
            assertThat(savedSignal.evidenceIds()).containsExactly("ev-rt-1");

            var savedFusion = fusionResultRepo.findByCalculationId("calc-rt-1").orElseThrow();
            assertThat(savedFusion.overallOutcome()).isEqualTo(FusionOutcome.CONSENSUS_SUPPORT);
            assertThat(savedFusion.rulesApplied()).containsExactly("R2");
            assertThat(savedFusion.supportingSources()).containsExactly("TAROT");
            assertThat(savedFusion.dimensions()).hasSize(1);
            assertThat(savedFusion.dimensions().get(0).dimension()).isEqualTo("FINANCE");
            assertThat(savedFusion.dimensions().get(0).state()).isEqualTo("POSITIVE");
        }

        @Test
        @DisplayName("Conflicts are persisted and remain unresolved, not merged away")
        void conflictsArePersisted() {
            var ctx = context("calc-rt-2");
            var execution = new ExecutionOutcome(List.of());
            var conflict = new Conflict(ConflictType.METHODOLOGY_CONFLICT, Dimension.CAREER,
                    List.of("BAZI"), "Hai trường phái BAZI bất đồng");
            var fusion = new FusionResult(FusionOutcome.METHODOLOGY_CONFLICT, List.of(),
                    List.of(conflict), List.of(), List.of(), Set.of(), Set.of());

            recorder.record(ctx, execution, fusion);
            entityManager.flush();
            entityManager.clear();

            var savedConflicts = conflictRepo.findByCalculationId("calc-rt-2");
            assertThat(savedConflicts).hasSize(1);
            assertThat(savedConflicts.get(0).type()).isEqualTo(ConflictType.METHODOLOGY_CONFLICT);
            assertThat(savedConflicts.get(0).involvedEngines()).containsExactly("BAZI");
        }

        @Test
        @DisplayName("A null fusion result (undefined scenario policy) is recorded honestly, not fabricated")
        void nullFusionResultIsHandledHonestly() {
            var ctx = context("calc-rt-3");
            var execution = new ExecutionOutcome(List.of());

            recorder.record(ctx, execution, null);
            entityManager.flush();
            entityManager.clear();

            assertThat(fusionResultRepo.findByCalculationId("calc-rt-3")).isEmpty();
            assertThat(calculations.findById("calc-rt-3")).isPresent();
        }

        @Test
        @DisplayName("An engine's timeout is recorded on its own row without affecting the calculation")
        void engineTimeoutIsRecordedIndependently() {
            var ctx = context("calc-rt-4");
            var failedResult = EngineResult.<String>failedRecoverable(
                    EngineError.timeout("SLOW_ENGINE", 5000));
            var execution = new ExecutionOutcome(List.of(
                    new EngineExecution("SLOW_ENGINE", failedResult, Duration.ofSeconds(5), true)));

            recorder.record(ctx, execution, null);
            entityManager.flush();
            entityManager.clear();

            var results = engineResults.findByCalculationId("calc-rt-4");
            assertThat(results).hasSize(1);
            assertThat(results.get(0).timedOut()).isTrue();
            assertThat(results.get(0).status()).isEqualTo(EngineStatus.FAILED_RECOVERABLE);
            assertThat(results.get(0).errorCode()).isEqualTo("ENGINE_TIMEOUT");

            var savedCalc = calculations.findById("calc-rt-4").orElseThrow();
            assertThat(savedCalc.status()).isEqualTo(EngineStatus.PARTIAL);
        }
    }

    @Nested
    @DisplayName("Reproducibility (CLAUDE.md section 6)")
    class Reproducibility {

        @Test
        @DisplayName("Identical input, versions and seed reaching the same outcome hash identically")
        void identicalInputProducesIdenticalHash() {
            var ctx1 = context("calc-hash-1");
            var ctx2 = context("calc-hash-2");
            var execution = new ExecutionOutcome(List.of());
            var fusion = new FusionResult(FusionOutcome.CONSENSUS_SUPPORT, List.of(), List.of(),
                    List.of(), List.of(), Set.of(), Set.of());

            var saved1 = recorder.record(ctx1, execution, fusion);
            var saved2 = recorder.record(ctx2, execution, fusion);

            // Same identity string (everything except calculationId itself)
            // and the same fused outcome must hash identically.
            assertThat(saved1.resultHash()).isEqualTo(saved2.resultHash());
        }

        @Test
        @DisplayName("A different fused outcome changes the hash even with identical input")
        void differentOutcomeChangesHash() {
            var ctx1 = context("calc-hash-3");
            var ctx2 = context("calc-hash-4");
            var execution = new ExecutionOutcome(List.of());
            var supportFusion = new FusionResult(FusionOutcome.CONSENSUS_SUPPORT, List.of(), List.of(),
                    List.of(), List.of(), Set.of(), Set.of());
            var cautionFusion = new FusionResult(FusionOutcome.CONSENSUS_CAUTION, List.of(), List.of(),
                    List.of(), List.of(), Set.of(), Set.of());

            var saved1 = recorder.record(ctx1, execution, supportFusion);
            var saved2 = recorder.record(ctx2, execution, cautionFusion);

            assertThat(saved1.resultHash()).isNotEqualTo(saved2.resultHash());
        }

        @Test
        @DisplayName("A different seed changes the hash")
        void differentSeedChangesHash() {
            var ctxSeedA = new CalculationContext("calc-hash-5", "TEST_SCHOOL",
                    new MethodologyVersions("1.0", "1.0", "1.0", "1.0"), ZoneId.of("Asia/Ho_Chi_Minh"),
                    Locale.forLanguageTag("vi-VN"), 1L, Instant.parse("2026-08-18T00:00:00Z"),
                    "VN_SOUTH", null, BirthTimePrecision.EXACT, List.of());
            var ctxSeedB = new CalculationContext("calc-hash-6", "TEST_SCHOOL",
                    new MethodologyVersions("1.0", "1.0", "1.0", "1.0"), ZoneId.of("Asia/Ho_Chi_Minh"),
                    Locale.forLanguageTag("vi-VN"), 2L, Instant.parse("2026-08-18T00:00:00Z"),
                    "VN_SOUTH", null, BirthTimePrecision.EXACT, List.of());
            var execution = new ExecutionOutcome(List.of());

            var saved1 = recorder.record(ctxSeedA, execution, null);
            var saved2 = recorder.record(ctxSeedB, execution, null);

            assertThat(saved1.resultHash()).isNotEqualTo(saved2.resultHash());
        }
    }
}
