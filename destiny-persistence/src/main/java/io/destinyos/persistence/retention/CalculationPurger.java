package io.destinyos.persistence.retention;

import io.destinyos.persistence.calculation.CalculationEngineResultRepository;
import io.destinyos.persistence.calculation.CalculationRepository;
import io.destinyos.persistence.calculation.ConflictRepository;
import io.destinyos.persistence.calculation.EvidenceRepository;
import io.destinyos.persistence.calculation.FusionResultRepository;
import io.destinyos.persistence.calculation.SignalRepository;
import io.destinyos.persistence.narrative.NarrativeRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Deletes one calculation and everything hanging off it, in its own
 * transaction.
 *
 * <p><strong>Why {@code REQUIRES_NEW}, and why this is a separate bean.</strong>
 * A cleanup pass over a batch must survive one bad row: if a single
 * calculation fails to delete, the other 499 should still go. Sharing one
 * transaction makes that impossible — the first failure marks the whole
 * transaction rollback-only and every subsequent delete is lost too, silently.
 * A new transaction per calculation isolates the failure. It lives in its own
 * bean rather than as another method on {@code CalculationRetentionService}
 * because Spring's proxying ignores {@code @Transactional} on self-invocation,
 * so a same-class helper would quietly run in the caller's transaction and the
 * isolation would be decorative.
 */
@Component
public class CalculationPurger {

    private final CalculationRepository calculations;
    private final CalculationEngineResultRepository engineResults;
    private final EvidenceRepository evidence;
    private final SignalRepository signals;
    private final FusionResultRepository fusionResults;
    private final ConflictRepository conflicts;
    private final NarrativeRepository narratives;

    public CalculationPurger(CalculationRepository calculations,
                             CalculationEngineResultRepository engineResults,
                             EvidenceRepository evidence,
                             SignalRepository signals,
                             FusionResultRepository fusionResults,
                             ConflictRepository conflicts,
                             NarrativeRepository narratives) {
        this.calculations = calculations;
        this.engineResults = engineResults;
        this.evidence = evidence;
        this.signals = signals;
        this.fusionResults = fusionResults;
        this.conflicts = conflicts;
        this.narratives = narratives;
    }

    /**
     * Deletes the calculation and its children.
     *
     * <p>The order below is dictated by the foreign keys in migrations V4-V7 and
     * is not cosmetic. {@code signal_evidence_refs} references both
     * {@code signals} and {@code evidence}, so signals must go before evidence —
     * the reverse order raises a constraint violation, which is exactly the bug
     * {@code CalculationRetentionServiceTest} pins by deleting a calculation that
     * really does have signals citing evidence.
     *
     * <p>The {@code fusion_result_*} and {@code signal_evidence_refs} collection
     * tables are {@code @ElementCollection}s, so deleting the owning entity
     * cascades to them; they are not deleted explicitly here.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void purge(String calculationId) {
        narratives.findById(calculationId).ifPresent(narratives::delete);

        signals.deleteAll(signals.findByCalculationId(calculationId));
        evidence.deleteAll(evidence.findByCalculationId(calculationId));

        conflicts.deleteAll(conflicts.findByCalculationId(calculationId));
        fusionResults.findByCalculationId(calculationId).ifPresent(fusionResults::delete);

        engineResults.deleteAll(engineResults.findByCalculationId(calculationId));

        calculations.deleteById(calculationId);
    }
}
