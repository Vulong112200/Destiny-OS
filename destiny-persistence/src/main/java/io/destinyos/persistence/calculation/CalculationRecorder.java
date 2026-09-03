package io.destinyos.persistence.calculation;

import io.destinyos.core.context.CalculationContext;
import io.destinyos.core.evidence.Evidence;
import io.destinyos.core.result.EngineStatus;
import io.destinyos.core.signal.Signal;
import io.destinyos.execution.EngineExecution;
import io.destinyos.execution.ExecutionOutcome;
import io.destinyos.fusion.FusionResult;
import io.destinyos.persistence.retention.RetentionClassifier;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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

    private final RetentionClassifier retentionClassifier;

    /**
     * Used instead of the repositories' {@code save()} for the rows this
     * class inserts.
     *
     * <p>Every entity written here has an <strong>assigned</strong>
     * {@code String} id and none of them implement {@code Persistable}, so
     * Spring Data's {@code save()} sees {@code isNew() == false} and calls
     * {@code EntityManager#merge} - which issues a SELECT before every
     * INSERT to find out whether the row already exists. This class is the
     * only writer of these rows and it creates them moments earlier, so
     * that question is already answered: they are new by construction.
     *
     * <p>The cost was not theoretical. Against a remote database one Tarot
     * Celtic Cross run writes ~60 evidence and signal rows, and the doubled
     * round trips put a six-engine scenario at ~21s - past the web client's
     * budget, surfacing to the user as a timeout with no failing engine to
     * blame. {@code persist} states the knowledge this class already has.
     */
    @PersistenceContext
    private EntityManager entityManager;

    public CalculationRecorder(RetentionClassifier retentionClassifier) {
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
     * Records a full run for a scenario, with no user question attached — the
     * pre-V9 signature, kept for callers that genuinely have nothing to record
     * (a bare engine run, a replay, a test fixture).
     *
     * <p>Delegates with {@link CalculationRequestContext#NONE} rather than
     * {@code null} so the entity's own setter can keep rejecting null and the
     * "asked nothing" case stays an explicit value instead of an absence.
     */
    @Transactional
    public CalculationEntity record(CalculationContext context, String scenarioId,
                                    ExecutionOutcome execution, FusionResult fusion) {
        return record(context, scenarioId, CalculationRequestContext.NONE, execution, fusion);
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
     *
     * <p>{@code requestContext} is what the <em>user</em> asked (V9). It is
     * written to the calculation row and read back by the API and the
     * narrative layer; it is never passed to an engine and never consulted by
     * anything that computes. See {@link CalculationRequestContext} for why it
     * arrives here rather than on {@link CalculationContext}.
     */
    @Transactional
    public CalculationEntity record(CalculationContext context, String scenarioId,
                                    CalculationRequestContext requestContext,
                                    ExecutionOutcome execution, FusionResult fusion) {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(requestContext, "requestContext");
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
        calculation.applyRequestContext(requestContext);
        context.seedIfPresent().ifPresent(calculation::setSeed);
        entityManager.persist(calculation);

        for (EngineExecution exec : execution.executions()) {
            var engineResultEntity = new CalculationEngineResultEntity(
                    context.calculationId(), exec.engineId(), exec.status(),
                    exec.result().errors().isEmpty() ? null : exec.result().errors().get(0).code(),
                    exec.duration().toMillis(), exec.timedOut());
            entityManager.persist(engineResultEntity);

            // Evidence must be persisted before the signals that cite it
            // (signal_evidence_refs has a foreign key to evidence).
            for (Evidence evidence : exec.result().evidence()) {
                entityManager.persist(new EvidenceEntity(evidence.evidenceId(),
                        context.calculationId(), evidence));
            }
            for (Signal signal : exec.result().signals()) {
                entityManager.persist(new SignalEntity(context.calculationId(), signal));
            }
        }

        String resultHash = computeResultHash(context, fusion);

        if (fusion != null) {
            entityManager.persist(new FusionResultEntity(context.calculationId(), fusion));
            fusion.conflicts().forEach(conflict ->
                    entityManager.persist(new ConflictEntity(context.calculationId(), conflict)));
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

        // No second save: `calculation` has been managed since the persist
        // above, so the two mutations after it (markCompleted, applyRetention)
        // are flushed by dirty checking at commit. The previous
        // `calculations.save(calculation)` here was a second merge - another
        // SELECT plus an UPDATE - for a row already in the persistence context.
        return calculation;
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
