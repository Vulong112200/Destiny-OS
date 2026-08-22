package io.destinyos.persistence.retention;

import io.destinyos.core.retention.RetentionClass;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Objects;

/**
 * Decides a calculation's retention class and expiry at the moment it is
 * written (DATA_MODEL_AND_RETENTION.md §7–§8).
 *
 * <p>Classifying at write time rather than at cleanup time is the whole design.
 * A cleanup job that re-derived the policy for every row would silently
 * reinterpret history every time an operator changed a duration — shorten
 * {@code daily-duration} and yesterday's readings become instantly deletable,
 * with no record that the rule changed underneath them. Deriving once and
 * storing the answer means a policy change applies to new rows only, which is
 * what an operator actually expects, and it makes the cleanup query a plain
 * indexed range scan.
 *
 * <p>Deliberately a plain object with no Spring annotation: a decision this
 * consequential should be testable by calling it, with no container.
 */
public final class RetentionClassifier {

    private final RetentionProperties properties;

    public RetentionClassifier(RetentionProperties properties) {
        this.properties = Objects.requireNonNull(properties, "properties");
    }

    /**
     * @param scenarioId  the scenario the run was for, or {@code null} for a
     *                    bare engine run outside scenario orchestration
     * @param completedAt when the run finished — the expiry is measured from
     *                    this, not from {@code Instant.now()}, so recording the
     *                    same run twice yields the same expiry and the whole
     *                    thing stays reproducible (Master Spec §25)
     */
    public RetentionDecision classify(String scenarioId, Instant completedAt) {
        Objects.requireNonNull(completedAt, "completedAt");

        // Every scenario run starts life EPHEMERAL. Nothing this system
        // computes automatically deserves PERSISTENT: a natal profile is
        // persistent, but that lives in birth_profiles, not here. USER_SAVED is
        // reached only by an explicit user action, and AUDIT only by an audit
        // writer - neither is something a scenario run can classify itself as.
        boolean daily = scenarioId != null
                && properties.dailyScenarioIds().contains(scenarioId.toUpperCase(Locale.ROOT));

        var lifetime = daily ? properties.dailyDuration() : properties.transientDuration();

        // Truncated to milliseconds so the value round-trips through the
        // database unchanged. PostgreSQL and H2 both store
        // "timestamp with time zone" at microsecond precision, so an Instant
        // carrying nanoseconds comes back different from what was written -
        // caught by an end-to-end test that ran a scenario and then read it
        // back, and found the API reporting two different expiry instants for
        // the same row. On a policy measured in days the discarded digits are
        // noise; being exactly reproducible is worth more (Master Spec section 25).
        Instant expiresAt = completedAt.plus(lifetime).truncatedTo(ChronoUnit.MILLIS);
        return new RetentionDecision(RetentionClass.EPHEMERAL, expiresAt);
    }

    /**
     * @param retentionClass why the row is kept
     * @param expiresAt      when it becomes deletable, or {@code null} for never
     */
    public record RetentionDecision(RetentionClass retentionClass, Instant expiresAt) {
        public RetentionDecision {
            Objects.requireNonNull(retentionClass, "retentionClass");
            if (!retentionClass.isAutoDeletable() && expiresAt != null) {
                throw new IllegalArgumentException(
                        retentionClass + " is never auto-deleted, so an expiry date would "
                                + "misrepresent it. Pass null.");
            }
            if (retentionClass.isAutoDeletable() && expiresAt == null) {
                // The dangerous half of the same invariant: an EPHEMERAL row
                // with no expiry is immortal without saying so, which is the
                // unbounded growth CLAUDE.md §7 forbids, hidden behind a label
                // that claims otherwise.
                throw new IllegalArgumentException(
                        retentionClass + " must carry an expiry, or it is kept forever while "
                                + "claiming to be temporary.");
            }
        }
    }
}
