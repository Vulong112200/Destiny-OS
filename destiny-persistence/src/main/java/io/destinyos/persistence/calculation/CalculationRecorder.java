package io.destinyos.persistence.calculation;

import io.destinyos.core.context.CalculationContext;
import io.destinyos.core.evidence.Evidence;
import io.destinyos.core.result.EngineStatus;
import io.destinyos.core.signal.Signal;
import io.destinyos.execution.EngineExecution;
import io.destinyos.execution.ExecutionOutcome;
import io.destinyos.fusion.FusionResult;
import io.destinyos.persistence.retention.RetentionClassifier;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persists one full calculation run: the calculation record, every engine's
 * execution outcome, all evidence and signals produced, and the fused
 * conclusion (CLAUDE.md section 6 reproducibility; DATA_MODEL_AND_RETENTION.md
 * sections 3-6; DECISION_LOG C7).
 *
 * <p>This is what closes the loop the domain model has been promising since
 * Phase 1: {@code CalculationContext}, {@code EngineResult}, {@code Signal}
 * and {@code FusionResult} were all designed to be reproducible, but nothing
 * durably recorded them until now. A calculation run before this class
 * existed produced a correct answer that vanished the moment the JVM did.
 *
 * <p>Also assigns the row's retention class and expiry
 * ({@link RetentionClassifier}, CLAUDE.md §7). Before that existed the
 * opposite problem applied: every run, including a throwaway daily reading,
 * was kept forever.
 */
@Service
public class CalculationRecorder {

    private final CalculationRepository calculations;
    private final CalculationEngineResultRepository engineResults;
    private final EvidenceRepository evidenceRepo;
    private final SignalRepository signalRepo;
    private final FusionResultRepository fusionResultRepo;
    private final ConflictRepository conflictRepo;
    private final RetentionClassifier retentionClassifier;

    public CalculationRecorder(CalculationRepository calculations,
                               CalculationEngineResultRepository engineResults,
                               EvidenceRepository evidenceRepo,
                               SignalRepository signalRepo,
                               FusionResultRepository fusionResultRepo,
                               ConflictRepository conflictRepo,
                               RetentionClassifier retentionClassifier) {
        this.calculations = calculations;
        this.engineResults = engineResults;
        this.evidenceRepo = evidenceRepo;
        this.signalRepo = signalRepo;
        this.fusionResultRepo = fusionResultRepo;
        this.conflictRepo = conflictRepo;
        this.retentionClassifier = retentionClassifier;
    }

    /**
     * Records a full run with no associated scenario (e.g. a bare engine
     * run outside {@code destiny-scenario}'s orchestration). See
     * {@link #record(CalculationContext, String, ExecutionOutcome, FusionResult)}.
     */
    @Transactional
    public CalculationEntity record(CalculationContext context, ExecutionOutcome execution,
                                    FusionResult fusion) {
        return record(context, null, execution, fusion);
    }

    /**
     * Records a full run. {@code fusion} may be {@code null} — a scenario
     * with no defined applicability policy (see {@code destiny-scenario}'s
     * {@code ScenarioDefinition#policyDefined()}) legitimately never reaches
     * Fusion, and that absence is itself worth recording honestly rather
     * than being forced through with a fabricated outcome.
     *
     * <p>{@code scenarioId} is separate from {@link CalculationContext#school()}:
     * {@code school} is Rule D's per-methodology selection, meaningless at
     * the level of a whole scenario run that may span several engines with
     * several different schools, so a scenario orchestrator has nothing
     * correct to put there. {@code scenarioId} is this table's own way of
     * recording which scenario the run was for.
     */
    @Transactional
    public CalculationEntity record(CalculationContext context, String scenarioId,
                                    ExecutionOutcome execution, FusionResult fusion) {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(execution, "execution");

        var calculation = new CalculationEntity(
                context.calculationId(),
                context.toIdentityString(),
                context.versions().methodologyVersion(),
                context.versions().algorithmVersion(),
                context.versions().ruleVersion(),
                context.timezone().getId(),
                context.calculatedAt());
        calculation.setCalendarVersion(context.versions().calendarVersion());
        calculation.setScenarioId(scenarioId);
        context.seedIfPresent().ifPresent(calculation::setSeed);
        calculations.save(calculation);

        for (EngineExecution exec : execution.executions()) {
            var engineResultEntity = new CalculationEngineResultEntity(
                    context.calculationId(), exec.engineId(), exec.status(),
                    exec.result().errors().isEmpty() ? null : exec.result().errors().get(0).code(),
                    exec.duration().toMillis(), exec.timedOut());
            engineResults.save(engineResultEntity);

            // Evidence must be persisted before the signals that cite it
            // (signal_evidence_refs has a foreign key to evidence).
            for (Evidence evidence : exec.result().evidence()) {
                evidenceRepo.save(new EvidenceEntity(evidence.evidenceId(),
                        context.calculationId(), evidence));
            }
            for (Signal signal : exec.result().signals()) {
                signalRepo.save(new SignalEntity(context.calculationId(), signal));
            }
        }

        String resultHash = computeResultHash(context, fusion);

        if (fusion != null) {
            fusionResultRepo.save(new FusionResultEntity(context.calculationId(), fusion));
            fusion.conflicts().forEach(conflict ->
                    conflictRepo.save(new ConflictEntity(context.calculationId(), conflict)));
        }

        Instant completedAt = Instant.now();
        calculation.markCompleted(execution.overallStatus(), resultHash, completedAt);

        // Retention is decided here, once, and stored (CLAUDE.md §7). Deciding
        // it at cleanup time instead would mean an operator shortening
        // destiny.retention.daily-duration retroactively condemns readings that
        // were written under the old rule - a config edit quietly becoming a
        // deletion. Measured from completedAt rather than a fresh
        // Instant.now() so re-recording the same run is reproducible.
        var decision = retentionClassifier.classify(scenarioId, completedAt);
        calculation.applyRetention(decision.retentionClass(), decision.expiresAt());

        return calculations.save(calculation);
    }

    /**
     * SHA-256 over the calculation's identity string (input + every version
     * component + timezone + seed, per {@code CalculationContext#toIdentityString()})
     * and the fused outcome, if one exists. Two calculations with identical
     * input, versions and seed that reach the same fused outcome hash
     * identically; changing any of those changes the hash — the property
     * CLAUDE.md section 6 requires.
     */
    private static String computeResultHash(CalculationContext context, FusionResult fusion) {
        String basis = context.toIdentityString() + "|"
                + (fusion == null ? "NO_FUSION" : fusion.overallOutcome().name());
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(basis.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is mandated by every JDK distribution; this cannot
            // actually happen, but the checked exception must go somewhere.
            throw new IllegalStateException("SHA-256 is not available on this JVM", e);
        }
    }

    /** Whether this calculation's engine batch had a genuine failure, not merely a non-answer. */
    public static boolean isFullySuccessful(ExecutionOutcome outcome) {
        return outcome.overallStatus() == EngineStatus.SUCCESS;
    }
}
