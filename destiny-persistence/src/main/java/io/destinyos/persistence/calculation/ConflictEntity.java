package io.destinyos.persistence.calculation;

import io.destinyos.core.signal.Dimension;
import io.destinyos.fusion.Conflict;
import io.destinyos.fusion.ConflictType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Persisted form of {@link Conflict} (V6 migration, FUSION_ENGINE_SPEC.md
 * section 8). Never resolved away — a {@code METHODOLOGY_CONFLICT} row
 * remains exactly as detected, for the same reason the domain type does not
 * offer a way to "resolve" one (Master Spec section 10 Rule F7).
 */
@Entity
@Table(name = "conflicts")
public class ConflictEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "calculation_id", nullable = false, length = 100)
    private String calculationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ConflictType type;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Dimension dimension;

    @Convert(converter = JsonStringListConverter.class)
    @Column(name = "involved_engines_json", columnDefinition = "TEXT")
    private List<String> involvedEngines;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected ConflictEntity() {
        // JPA
    }

    public ConflictEntity(String calculationId, Conflict conflict) {
        this.calculationId = Objects.requireNonNull(calculationId, "calculationId");
        Objects.requireNonNull(conflict, "conflict");
        this.type = conflict.type();
        this.dimension = conflict.dimension();
        this.involvedEngines = conflict.involvedEngines();
        this.description = conflict.description();
    }

    public Long id() {
        return id;
    }

    public String calculationId() {
        return calculationId;
    }

    public ConflictType type() {
        return type;
    }

    public Dimension dimension() {
        return dimension;
    }

    public List<String> involvedEngines() {
        return involvedEngines == null ? List.of() : involvedEngines;
    }

    public String description() {
        return description;
    }

    public Instant createdAt() {
        return createdAt;
    }

    @jakarta.persistence.PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
