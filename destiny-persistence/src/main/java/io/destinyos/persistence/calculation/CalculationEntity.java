package io.destinyos.persistence.calculation;

import io.destinyos.core.result.EngineStatus;
import io.destinyos.core.retention.RetentionClass;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;

/**
 * One scenario run (DATA_MODEL_AND_RETENTION.md section 3, V4 migration).
 *
 * <p>{@code calculationId} is the domain's own identifier
 * (CalculationContext.calculationId()), used as the primary key directly —
 * a caller looks this up by the same id it used to request the calculation,
 * with no surrogate-key indirection.
 *
 * <p>Carries {@code resultHash} per DECISION_LOG C7: the original data model
 * omitted it from this table despite requiring one on
 * {@link CalculationEngineResultEntity} and on every snapshot
 * (DATA_MODEL_AND_RETENTION.md section 10).
 */
@Entity
@Table(name = "calculations")
public class CalculationEntity {

    @Id
    @Column(name = "calculation_id", length = 100)
    private String calculationId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "birth_profile_id")
    private Long birthProfileId;

    @Column(name = "scenario_id", length = 60)
    private String scenarioId;

    @Column(name = "input_hash", nullable = false, length = 128)
    private String inputHash;

    @Column(name = "methodology_version", nullable = false, length = 30)
    private String methodologyVersion;

    @Column(name = "algorithm_version", nullable = false, length = 30)
    private String algorithmVersion;

    @Column(name = "rule_version", nullable = false, length = 30)
    private String ruleVersion;

    @Column(name = "calendar_version", length = 30)
    private String calendarVersion;

    @Column(nullable = false, length = 60)
    private String timezone;

    private Long seed;

    @Column(name = "result_hash", length = 128)
    private String resultHash;

    /**
     * The user's own question (V9 migration), or {@code null} when they asked
     * nothing in particular. Recorded because a persisted answer whose
     * question was discarded cannot be read back honestly — see V9's comment.
     */
    @Column(length = 500)
    private String question;

    /**
     * The UI intent the user picked, e.g. {@code "doi-viec"} (V9 migration).
     *
     * <p>Recorded, never acted on. Nothing in the deterministic path reads
     * this: it selects no school (Rule D), changes no engine input, no
     * applicability, and no outcome. It exists so a reading can be shown next
     * to the intent it was requested under, and so an audit can see what the
     * user was looking at. Do not branch on it.
     */
    @Column(name = "focus_id", length = 100)
    private String focusId;

    /** The Vietnamese label shown for {@link #focusId}. Same rule: recorded, never acted on. */
    @Column(name = "focus_label", length = 200)
    private String focusLabel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EngineStatus status;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    /**
     * Why this row is kept (CLAUDE.md §7, V8 migration). Never null: a row
     * whose retention class is unknown is a row nobody can safely delete
     * <em>or</em> safely keep, so the field defaults to the honest reading of
     * an unclassified scenario run rather than to null.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "retention_class", nullable = false, length = 20)
    private RetentionClass retentionClass = RetentionClass.EPHEMERAL;

    /**
     * When the cleanup job becomes allowed to delete this row, or {@code null}
     * for "never". Null is not "unset" — it is the positive statement that this
     * row does not expire, which is why {@link #promoteToUserSaved()} clears it
     * rather than pushing it far into the future.
     */
    @Column(name = "expires_at")
    private Instant expiresAt;

    protected CalculationEntity() {
        // JPA
    }

    public CalculationEntity(String calculationId, String inputHash, String methodologyVersion,
                             String algorithmVersion, String ruleVersion, String timezone,
                             Instant startedAt) {
        this.calculationId = Objects.requireNonNull(calculationId, "calculationId");
        this.inputHash = Objects.requireNonNull(inputHash, "inputHash");
        this.methodologyVersion = Objects.requireNonNull(methodologyVersion, "methodologyVersion");
        this.algorithmVersion = Objects.requireNonNull(algorithmVersion, "algorithmVersion");
        this.ruleVersion = Objects.requireNonNull(ruleVersion, "ruleVersion");
        this.timezone = Objects.requireNonNull(timezone, "timezone");
        this.startedAt = Objects.requireNonNull(startedAt, "startedAt");
        this.status = EngineStatus.PARTIAL; // provisional until markCompleted()
    }

    public void markCompleted(EngineStatus status, String resultHash, Instant completedAt) {
        this.status = Objects.requireNonNull(status, "status");
        this.resultHash = resultHash;
        this.completedAt = Objects.requireNonNull(completedAt, "completedAt");
    }

    public String calculationId() {
        return calculationId;
    }

    public Long userId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long birthProfileId() {
        return birthProfileId;
    }

    public void setBirthProfileId(Long birthProfileId) {
        this.birthProfileId = birthProfileId;
    }

    public String scenarioId() {
        return scenarioId;
    }

    public void setScenarioId(String scenarioId) {
        this.scenarioId = scenarioId;
    }

    public String inputHash() {
        return inputHash;
    }

    public String methodologyVersion() {
        return methodologyVersion;
    }

    public String algorithmVersion() {
        return algorithmVersion;
    }

    public String ruleVersion() {
        return ruleVersion;
    }

    public String calendarVersion() {
        return calendarVersion;
    }

    public void setCalendarVersion(String calendarVersion) {
        this.calendarVersion = calendarVersion;
    }

    public String timezone() {
        return timezone;
    }

    public Long seed() {
        return seed;
    }

    public void setSeed(Long seed) {
        this.seed = seed;
    }

    public String resultHash() {
        return resultHash;
    }

    public String question() {
        return question;
    }

    public String focusId() {
        return focusId;
    }

    public String focusLabel() {
        return focusLabel;
    }

    /**
     * Records what the user asked (V9).
     *
     * <p>One setter for all three fields rather than three independent ones,
     * for the same reason {@link #applyRetention} is one call: the three are a
     * single statement about one request, and letting a caller set a focus
     * label without the question it belonged to produces a row that says
     * something nobody ever asked.
     *
     * <p>Does no normalization of its own. The API layer trims, blank-maps and
     * length-caps this input once ({@code ScenarioContextRequest}) so that what
     * is persisted, what is echoed back to the caller, and what the narrative
     * layer later reads are the same string. A second normalization here would
     * be a second chance for those three to disagree.
     *
     * <p>Public for the same reason {@link #setScenarioId} is, and unlike
     * {@link #applyRetention}: there is no invariant here that a caller could
     * violate. Retention is package-private because a class and an expiry set
     * independently produce a contradictory row; these three fields are simply
     * a record of what was asked.
     */
    public void applyRequestContext(CalculationRequestContext requestContext) {
        Objects.requireNonNull(requestContext, "requestContext");
        this.question = requestContext.question();
        this.focusId = requestContext.focusId();
        this.focusLabel = requestContext.focusLabel();
    }

    public EngineStatus status() {
        return status;
    }

    public Instant startedAt() {
        return startedAt;
    }

    public Instant completedAt() {
        return completedAt;
    }

    public RetentionClass retentionClass() {
        return retentionClass;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    /**
     * Applies a retention decision. Package-private on purpose: the decision
     * belongs to {@code RetentionClassifier}, and letting arbitrary callers set
     * a class and an expiry independently is how a PERSISTENT row ends up with
     * an expiry date.
     */
    void applyRetention(RetentionClass retentionClass, Instant expiresAt) {
        this.retentionClass = Objects.requireNonNull(retentionClass, "retentionClass");
        if (!retentionClass.isAutoDeletable() && expiresAt != null) {
            throw new IllegalArgumentException(
                    "Retention class " + retentionClass + " is never auto-deleted, so an "
                            + "expiry date would be misleading. Pass null.");
        }
        this.expiresAt = expiresAt;
    }

    /**
     * The user asked to keep this result. Clears the expiry, because
     * DATA_MODEL_AND_RETENTION.md §11 requires cleanup to never delete
     * USER_SAVED and a leftover expiry date would tell the UI otherwise.
     *
     * <p>Idempotent, and deliberately one-way here: un-saving is a separate
     * user action with its own consequences (it would re-arm deletion), and
     * nothing in the product asks for it yet.
     */
    public void promoteToUserSaved() {
        this.retentionClass = RetentionClass.USER_SAVED;
        this.expiresAt = null;
    }

    /** Whether the cleanup job may delete this row as of {@code now}. */
    public boolean isExpiredAt(Instant now) {
        Objects.requireNonNull(now, "now");
        return retentionClass.isAutoDeletable() && expiresAt != null && !expiresAt.isAfter(now);
    }
}
