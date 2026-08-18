package io.destinyos.persistence.calculation;

import io.destinyos.core.result.EngineStatus;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EngineStatus status;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

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

    public EngineStatus status() {
        return status;
    }

    public Instant startedAt() {
        return startedAt;
    }

    public Instant completedAt() {
        return completedAt;
    }
}
