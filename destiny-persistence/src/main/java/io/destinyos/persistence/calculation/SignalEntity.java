package io.destinyos.persistence.calculation;

import io.destinyos.core.signal.Applicability;
import io.destinyos.core.signal.Dimension;
import io.destinyos.core.signal.Polarity;
import io.destinyos.core.signal.Signal;
import io.destinyos.core.signal.Strength;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Persisted form of {@link Signal} (V5 migration). {@code NOT_APPLICABLE}
 * and {@code NEUTRAL} are stored as the distinct real values they are
 * (audit risk RK7) — never collapsed at this layer either.
 */
@Entity
@Table(name = "signals")
public class SignalEntity {

    @Id
    @Column(name = "signal_id", length = 100)
    private String signalId;

    @Column(name = "calculation_id", nullable = false, length = 100)
    private String calculationId;

    @Column(nullable = false, length = 60)
    private String engine;

    @Column(length = 200)
    private String school;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Dimension dimension;

    @Column(nullable = false, length = 100)
    private String tag;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Polarity polarity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Strength strength;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Applicability applicability;

    @Column(nullable = false)
    private boolean critical;

    @Column(name = "evidence_group_id", length = 100)
    private String evidenceGroupId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "signal_evidence_refs", joinColumns = @JoinColumn(name = "signal_id"))
    @Column(name = "evidence_id", length = 100)
    private Set<String> evidenceIds = new LinkedHashSet<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected SignalEntity() {
        // JPA
    }

    public SignalEntity(String calculationId, Signal signal) {
        this.signalId = Objects.requireNonNull(signal, "signal").signalId();
        this.calculationId = Objects.requireNonNull(calculationId, "calculationId");
        this.engine = signal.engine();
        this.school = signal.school();
        this.dimension = signal.dimension();
        this.tag = signal.tag();
        this.polarity = signal.polarity();
        this.strength = signal.strength();
        this.applicability = signal.applicability();
        this.critical = signal.critical();
        this.evidenceGroupId = signal.evidenceGroupId();
        this.evidenceIds = new LinkedHashSet<>(signal.evidenceIds());
    }

    public String signalId() {
        return signalId;
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

    public Dimension dimension() {
        return dimension;
    }

    public String tag() {
        return tag;
    }

    public Polarity polarity() {
        return polarity;
    }

    public Strength strength() {
        return strength;
    }

    public Applicability applicability() {
        return applicability;
    }

    public boolean critical() {
        return critical;
    }

    public String evidenceGroupId() {
        return evidenceGroupId;
    }

    public Set<String> evidenceIds() {
        return Set.copyOf(evidenceIds);
    }

    public Instant createdAt() {
        return createdAt;
    }

    @jakarta.persistence.PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
