package io.destinyos.persistence.registry;

import io.destinyos.engine.MethodologyStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * One school/version within a {@link MethodologyEntity} (ADR D7, V2 migration).
 *
 * <p>This is the row that makes ADR D7 real. A methodology blocked on
 * research is not omitted from the registry - it is a row here with
 * {@code status = RESEARCH_REQUIRED}, a non-empty {@code researchIds}, and
 * {@code notes} explaining precisely what is missing. A missing row would
 * look like an oversight; this row is a correct, queryable, honest answer.
 *
 * <p>The constructor enforces the same guard as
 * {@link io.destinyos.engine.EngineMetadata}: a version whose status permits
 * calculation ({@link MethodologyStatus#mayCalculate()}) must name its
 * school and cite a source. This is the database-level mirror of that
 * Phase 1 check, so the constraint holds however the row gets written, not
 * only through the one code path that happens to reuse the Java type.
 */
@Entity
@Table(name = "methodology_versions")
public class MethodologyVersionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "methodology_id", nullable = false)
    private MethodologyEntity methodology;

    @Column(nullable = false, length = 30)
    private String version;

    @Column(length = 200)
    private String school;

    @Column(columnDefinition = "TEXT")
    private String source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MethodologyStatus status;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    /**
     * Which RESEARCH_BLOCKERS.md items this version is waiting on, or was
     * resolved by. Never fabricated - every id here must correspond to a
     * real entry in that register.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "methodology_version_research_refs",
            joinColumns = @JoinColumn(name = "methodology_version_id"))
    @Column(name = "research_id", length = 20)
    private Set<String> researchIds = new LinkedHashSet<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected MethodologyVersionEntity() {
        // JPA
    }

    public MethodologyVersionEntity(MethodologyEntity methodology, String version,
                                    MethodologyStatus status, String school, String source,
                                    Set<String> researchIds, String notes) {
        this.methodology = Objects.requireNonNull(methodology, "methodology");
        this.version = Objects.requireNonNull(version, "version");
        this.status = Objects.requireNonNull(status, "status");
        this.school = school;
        this.source = source;
        this.researchIds = researchIds == null ? new LinkedHashSet<>() : new LinkedHashSet<>(researchIds);
        this.notes = notes;

        if (status.mayCalculate() && (school == null || school.isBlank())) {
            throw new IllegalArgumentException(
                    "Methodology version " + methodology.methodologyId() + "@" + version
                            + " has status " + status + " but names no school. "
                            + "CLAUDE.md Rule D forbids silently selecting one.");
        }
        if (status.mayCalculate() && (source == null || source.isBlank())) {
            throw new IllegalArgumentException(
                    "Methodology version " + methodology.methodologyId() + "@" + version
                            + " has status " + status + " but cites no source. "
                            + "CLAUDE.md Rule C requires a citation, not a plausible formula.");
        }
    }

    public Long id() {
        return id;
    }

    public MethodologyEntity methodology() {
        return methodology;
    }

    public String version() {
        return version;
    }

    public String school() {
        return school;
    }

    public String source() {
        return source;
    }

    public MethodologyStatus status() {
        return status;
    }

    public String notes() {
        return notes;
    }

    public LocalDate effectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDate effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public Set<String> researchIds() {
        return Set.copyOf(researchIds);
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
