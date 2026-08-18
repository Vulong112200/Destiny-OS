package io.destinyos.persistence.registry;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;

/**
 * Version of one rule/data table within a {@link MethodologyVersionEntity}
 * (DATA_MODEL_AND_RETENTION.md section 2, V2 migration).
 *
 * <p>Structural only as of Phase 2: no methodology version yet has an actual
 * rule table to version, since every calculable methodology so far is either
 * pending a decision (R8 - Numerology normalization) or content-gated
 * (R11 - Tarot meanings). Created now so the schema is ready the moment a
 * real rule table needs a version, per IMPLEMENTATION_PLAN.md section 4.1.
 */
@Entity
@Table(name = "rule_versions")
public class RuleVersionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "methodology_version_id", nullable = false)
    private MethodologyVersionEntity methodologyVersion;

    @Column(name = "rule_table_name", nullable = false, length = 120)
    private String ruleTableName;

    @Column(nullable = false, length = 30)
    private String version;

    @Column(columnDefinition = "TEXT")
    private String source;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected RuleVersionEntity() {
        // JPA
    }

    public RuleVersionEntity(MethodologyVersionEntity methodologyVersion, String ruleTableName,
                             String version, String source) {
        this.methodologyVersion = Objects.requireNonNull(methodologyVersion, "methodologyVersion");
        this.ruleTableName = Objects.requireNonNull(ruleTableName, "ruleTableName");
        this.version = Objects.requireNonNull(version, "version");
        this.source = source;
    }

    public Long id() {
        return id;
    }

    public MethodologyVersionEntity methodologyVersion() {
        return methodologyVersion;
    }

    public String ruleTableName() {
        return ruleTableName;
    }

    public String version() {
        return version;
    }

    public String source() {
        return source;
    }

    public Instant createdAt() {
        return createdAt;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
