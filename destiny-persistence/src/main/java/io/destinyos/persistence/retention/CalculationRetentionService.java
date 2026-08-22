package io.destinyos.persistence.retention;

import io.destinyos.core.retention.RetentionClass;
import io.destinyos.persistence.calculation.CalculationEntity;
import io.destinyos.persistence.calculation.CalculationRepository;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

/**
 * The cleanup job DATA_MODEL_AND_RETENTION.md §11 specifies: select by
 * retention class, dry-run, audit, batch delete, bounded retry, and never touch
 * USER_SAVED.
 *
 * <p>Deliberately <strong>not</strong> {@code @Transactional}. Each calculation
 * is deleted in its own transaction by {@link CalculationPurger} so one bad row
 * cannot take the batch down with it; wrapping this orchestration in an outer
 * transaction would defeat that and, worse, would hold a single long
 * transaction open across a 500-row delete.
 *
 * <p>Nothing here decides <em>whether</em> a row is deletable beyond reading
 * {@link RetentionClass#isAutoDeletable()}. The class was assigned at write
 * time by {@code RetentionClassifier}; re-deriving policy at cleanup time is
 * what would let an operator's config change retroactively condemn old rows.
 */
@Service
public class CalculationRetentionService {

    private static final Logger log = LoggerFactory.getLogger(CalculationRetentionService.class);

    /**
     * Attempts per calculation. Two, not "until it works": CLAUDE.md §5 forbids
     * unbounded retry, and a delete that fails twice is failing for a reason a
     * third attempt will not fix — a lock, a constraint, a missing migration.
     * Better to record it as a failure an operator can see than to spin.
     */
    private static final int MAX_ATTEMPTS = 2;

    private final CalculationRepository calculations;
    private final CalculationPurger purger;
    private final RetentionRunRepository runs;
    private final RetentionProperties properties;

    public CalculationRetentionService(CalculationRepository calculations,
                                      CalculationPurger purger,
                                      RetentionRunRepository runs,
                                      RetentionProperties properties) {
        this.calculations = calculations;
        this.purger = purger;
        this.runs = runs;
        this.properties = properties;
    }

    /**
     * Reports what a real run would delete, and deletes nothing
     * (DATA_MODEL_AND_RETENTION.md §11 "dry-run").
     *
     * <p>Records an audit row all the same. The distinction between "a rehearsal
     * ran and found 400 candidates" and "no rehearsal happened" is exactly what
     * an operator needs before switching the job on.
     */
    public RetentionRunSummary dryRun(Instant now) {
        return execute(now, true);
    }

    /** Deletes up to {@code batchSize} expired calculations and records the run. */
    public RetentionRunSummary purgeExpired(Instant now) {
        return execute(now, false);
    }

    /**
     * The user asked to keep this result: promote it to
     * {@link RetentionClass#USER_SAVED} and clear its expiry.
     *
     * <p>Lives here rather than on a query service because it is the only way
     * a row ever leaves {@code EPHEMERAL}, and keeping every retention
     * transition in one class is what makes "cleanup never deletes USER_SAVED"
     * checkable by reading one file.
     *
     * <p>Idempotent: saving an already-saved calculation is a no-op that still
     * reports success, because a user pressing the button twice has not done
     * anything wrong.
     *
     * @return the updated row, or empty if no such calculation exists
     */
    @org.springframework.transaction.annotation.Transactional
    public Optional<CalculationEntity> markUserSaved(String calculationId) {
        Objects.requireNonNull(calculationId, "calculationId");
        return calculations.findById(calculationId).map(calculation -> {
            calculation.promoteToUserSaved();
            return calculations.save(calculation);
        });
    }

    private RetentionRunSummary execute(Instant now, boolean dryRun) {
        Objects.requireNonNull(now, "now");
        Instant startedAt = Instant.now();

        long totalCandidates = calculations.countByRetentionClassAndExpiresAtLessThanEqual(
                RetentionClass.EPHEMERAL, now);
        List<CalculationEntity> batch =
                calculations.findByRetentionClassAndExpiresAtLessThanEqualOrderByExpiresAtAsc(
                        RetentionClass.EPHEMERAL, now, PageRequest.of(0, properties.batchSize()));

        int deleted = 0;
        int failures = 0;
        String firstFailure = null;

        for (CalculationEntity candidate : batch) {
            // Belt and braces against a future query change: the class is
            // re-checked on the object itself, so a widened finder could not
            // delete a USER_SAVED row even if it returned one.
            if (!candidate.retentionClass().isAutoDeletable()) {
                log.warn("Retention query returned a non-deletable calculation {} ({}); skipping.",
                        candidate.calculationId(), candidate.retentionClass());
                continue;
            }
            if (dryRun) {
                continue;
            }

            String failure = purgeWithRetry(candidate.calculationId());
            if (failure == null) {
                deleted++;
            } else {
                failures++;
                if (firstFailure == null) {
                    firstFailure = failure;
                }
            }
        }

        Instant completedAt = Instant.now();
        var run = runs.save(new RetentionRunEntity(dryRun, now, (int) Math.min(totalCandidates,
                Integer.MAX_VALUE), deleted, failures, firstFailure, startedAt, completedAt));

        var summary = new RetentionRunSummary(run.id(), dryRun, now, totalCandidates,
                batch.size(), deleted, failures, firstFailure);
        log.info("Retention run {}: {}", run.id(), summary.describe());
        return summary;
    }

    /** @return null on success, or the failure message after {@link #MAX_ATTEMPTS} tries */
    private String purgeWithRetry(String calculationId) {
        RuntimeException last = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                purger.purge(calculationId);
                return null;
            } catch (RuntimeException e) {
                last = e;
                log.warn("Attempt {}/{} to delete calculation {} failed: {}",
                        attempt, MAX_ATTEMPTS, calculationId, e.getMessage());
            }
        }
        return last == null ? "unknown failure" : last.getClass().getSimpleName()
                + ": " + last.getMessage();
    }

    /**
     * What one run did.
     *
     * @param runId            audit row id
     * @param dryRun           whether anything was actually deleted
     * @param cutoff           the instant compared against {@code expires_at}
     * @param candidatesFound  total expired calculations, which may exceed
     *                         {@code batchSize} — a caller seeing
     *                         {@code candidatesFound > batchConsidered} knows to
     *                         run again rather than assuming the queue is empty
     * @param batchConsidered  how many this run looked at
     * @param deleted          how many were removed
     * @param failures         how many could not be removed after retries
     * @param firstFailure     the first failure message, or {@code null}
     */
    public record RetentionRunSummary(
            Long runId,
            boolean dryRun,
            Instant cutoff,
            long candidatesFound,
            int batchConsidered,
            int deleted,
            int failures,
            String firstFailure
    ) {
        /** Whether expired calculations remain after this run. */
        public boolean hasMoreWork() {
            return candidatesFound > deleted;
        }

        public String describe() {
            return (dryRun ? "dry-run" : "purge")
                    + " cutoff=" + cutoff
                    + " candidates=" + candidatesFound
                    + " considered=" + batchConsidered
                    + " deleted=" + deleted
                    + " failures=" + failures
                    + (firstFailure == null ? "" : " firstFailure=" + firstFailure);
        }
    }
}
