package io.destinyos.persistence.calculation;

import io.destinyos.core.evidence.DataConfidence;
import io.destinyos.core.evidence.Evidence;
import io.destinyos.core.signal.Dimension;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Persisted form of {@link Evidence} (V5 migration, DECISION_LOG C4, C8).
 *
 * <p>{@code evidenceId} is the domain's own identifier, used directly as
 * primary key (same convention as {@link CalculationEntity}).
 */
@Entity
@Table(name = "evidence")
public class EvidenceEntity {

    @Id
    @Column(name = "evidence_id", length = 100)
    private String evidenceId;

    @Column(name = "calculation_id", nullable = false, length = 100)
    private String calculationId;

    @Column(nullable = false, length = 60)
    private String engine;

    @Column(length = 200)
    private String school;

    @Column(name = "rule_id", nullable = false, length = 100)
    private String ruleId;

    @Column(name = "rule_version", nullable = false, length = 30)
    private String ruleVersion;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Dimension dimension;

    @Convert(converter = JsonMapConverter.class)
    @Column(name = "fact_json", columnDefinition = "TEXT")
    private Map<String, Object> fact;

    @Column(length = 200)
    private String source;

    @Column(name = "evidence_group_id", length = 100)
    private String evidenceGroupId;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_confidence", length = 30)
    private DataConfidence dataConfidence;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected EvidenceEntity() {
        // JPA
    }

    public EvidenceEntity(String evidenceId, String calculationId, Evidence evidence) {
        this.evidenceId = Objects.requireNonNull(evidenceId, "evidenceId");
        this.calculationId = Objects.requireNonNull(calculationId, "calculationId");
        Objects.requireNonNull(evidence, "evidence");
        this.engine = evidence.engine();
        this.school = evidence.school();
        this.ruleId = evidence.ruleId();
        this.ruleVersion = evidence.ruleVersion();
        this.dimension = evidence.dimension();
        this.fact = evidence.fact();
        this.source = evidence.source();
        this.evidenceGroupId = evidence.evidenceGroupId();
        this.dataConfidence = evidence.dataConfidence();
    }

    public String evidenceId() {
        return evidenceId;
    }

    public String calculationId() {
        return calculationId;
    }

    public String engine() {
        return engine;
    }

    public String school() {
        return school;
    }

    public String ruleId() {
        return ruleId;
    }

    public String ruleVersion() {
        return ruleVersion;
    }

    public Dimension dimension() {
        return dimension;
    }

    public Map<String, Object> fact() {
        return fact;
    }

    public String source() {
        return source;
    }

    public String evidenceGroupId() {
        return evidenceGroupId;
    }

    public DataConfidence dataConfidence() {
        return dataConfidence;
    }

    public Instant createdAt() {
        return createdAt;
    }

    @jakarta.persistence.PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
