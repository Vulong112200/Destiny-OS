package io.destinyos.persistence.registry;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;

/**
 * A methodology, e.g. {@code BAZI}, {@code NUMEROLOGY}, {@code TAROT}
 * (DATA_MODEL_AND_RETENTION.md section 2, ADR D7, V2 migration).
 *
 * <p>This row is the parent of one or more {@link MethodologyVersionEntity}
 * rows - one per school. CLAUDE.md Rule D forbids merging schools into one
 * average, so a methodology with two disputed schools has two version rows,
 * never one row with a blended answer.
 */
@Entity
@Table(name = "methodologies")
public class MethodologyEntity {

    @Id
    @Column(name = "methodology_id", length = 60)
    private String methodologyId;

    @Column(name = "display_name_vi", nullable = false, length = 200)
    private String displayNameVi;

    @Column(length = 60)
    private String domain;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected MethodologyEntity() {
        // JPA
    }

    public MethodologyEntity(String methodologyId, String displayNameVi, String domain) {
        this.methodologyId = Objects.requireNonNull(methodologyId, "methodologyId");
        this.displayNameVi = Objects.requireNonNull(displayNameVi, "displayNameVi");
        this.domain = domain;
    }

    public String methodologyId() {
        return methodologyId;
    }

    public String displayNameVi() {
        return displayNameVi;
    }

    public String domain() {
        return domain;
    }

    public Instant createdAt() {
        return createdAt;
    }

    @jakarta.persistence.PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
