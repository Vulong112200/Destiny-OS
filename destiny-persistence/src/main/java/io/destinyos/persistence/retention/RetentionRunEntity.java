package io.destinyos.persistence.retention;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;

/**
 * One execution of the cleanup job, recorded whether it deleted anything or
 * not (DATA_MODEL_AND_RETENTION.md §11: <em>"cron/job: ... dry-run; audit;
 * batch delete; retry"</em>).
 *
 * <p>A scheduled job that destroys rows and leaves no trace is unauditable by
 * construction: when a user asks "where did my reading go", the only honest
 * answer available would be a guess. This table makes the answer checkable —
 * which run, at what cutoff, how many candidates it found, how many it
 * actually deleted, and whether anything failed.
 *
 * <p>Dry runs are recorded too, and that is not redundancy: the difference
 * between "found 400 candidates, deleted 0" and no row at all is the
 * difference between a rehearsal that happened and one that never ran.
 */
@Entity
@Table(name = "retention_runs")
public class RetentionRunEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dry_run", nullable = false)
    private boolean dryRun;

    @Column(nullable = false)
    private Instant cutoff;

    @Column(name = "candidates_found", nullable = false)
    private int candidatesFound;

    @Column(name = "calculations_deleted", nullable = false)
    private int calculationsDeleted;

    @Column(nullable = false)
    private int failures;

    @Column(name = "first_failure", length = 500)
    private String firstFailure;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at", nullable = false)
    private Instant completedAt;

    protected RetentionRunEntity() {
        // JPA
    }

    public RetentionRunEntity(boolean dryRun, Instant cutoff, int candidatesFound,
                              int calculationsDeleted, int failures, String firstFailure,
                              Instant startedAt, Instant completedAt) {
        this.dryRun = dryRun;
        this.cutoff = Objects.requireNonNull(cutoff, "cutoff");
        this.candidatesFound = candidatesFound;
        this.calculationsDeleted = calculationsDeleted;
        this.failures = failures;
        this.firstFailure = truncate(firstFailure);
        this.startedAt = Objects.requireNonNull(startedAt, "startedAt");
        this.completedAt = Objects.requireNonNull(completedAt, "completedAt");
    }

    /**
     * The column is 500 characters and a JDBC exception message can be longer.
     * Truncating here keeps a failing run auditable; letting it overflow would
     * turn "the cleanup failed" into "the cleanup failed and so did recording
     * that it failed".
     */
    private static String truncate(String message) {
        if (message == null) {
            return null;
        }
        return message.length() <= 500 ? message : message.substring(0, 497) + "...";
    }

    public Long id() {
        return id;
    }

    public boolean dryRun() {
        return dryRun;
    }

    public Instant cutoff() {
        return cutoff;
    }

    public int candidatesFound() {
        return candidatesFound;
    }

    public int calculationsDeleted() {
        return calculationsDeleted;
    }

    public int failures() {
        return failures;
    }

    public String firstFailure() {
        return firstFailure;
    }

    public Instant startedAt() {
        return startedAt;
    }

    public Instant completedAt() {
        return completedAt;
    }
}
