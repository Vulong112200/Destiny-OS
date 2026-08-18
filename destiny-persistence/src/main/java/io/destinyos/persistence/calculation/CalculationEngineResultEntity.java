package io.destinyos.persistence.calculation;

import io.destinyos.core.result.EngineStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;

/**
 * One engine's execution record within a calculation (V4 migration,
 * CLAUDE.md Rule F). Every engine that ran gets its own row regardless of
 * how the others fared — a timeout on one row never touches another.
 */
@Entity
@Table(name = "calculation_engine_results")
public class CalculationEngineResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "calculation_id", nullable = false, length = 100)
    private String calculationId;

    @Column(nullable = false, length = 60)
    private String engine;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EngineStatus status;

    @Column(name = "result_uri", length = 500)
    private String resultUri;

    @Column(name = "result_hash", length = 128)
    private String resultHash;

    @Column(name = "error_code", length = 60)
    private String errorCode;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "timed_out", nullable = false)
    private boolean timedOut;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected CalculationEngineResultEntity() {
        // JPA
    }

    public CalculationEngineResultEntity(String calculationId, String engine, EngineStatus status,
                                         String errorCode, long durationMs, boolean timedOut) {
        this.calculationId = Objects.requireNonNull(calculationId, "calculationId");
        this.engine = Objects.requireNonNull(engine, "engine");
        this.status = Objects.requireNonNull(status, "status");
        this.errorCode = errorCode;
        this.durationMs = durationMs;
        this.timedOut = timedOut;
    }

    public Long id() {
        return id;
    }

    public String calculationId() {
        return calculationId;
    }

    public String engine() {
        return engine;
    }

    public EngineStatus status() {
        return status;
    }

    public String resultUri() {
        return resultUri;
    }

    public void setResultUri(String resultUri) {
        this.resultUri = resultUri;
    }

    public String resultHash() {
        return resultHash;
    }

    public void setResultHash(String resultHash) {
        this.resultHash = resultHash;
    }

    public String errorCode() {
        return errorCode;
    }

    public Long durationMs() {
        return durationMs;
    }

    public boolean timedOut() {
        return timedOut;
    }

    public Instant createdAt() {
        return createdAt;
    }

    @jakarta.persistence.PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
