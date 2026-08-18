package io.destinyos.persistence.identity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

/**
 * Identity foundation (DATA_MODEL_AND_RETENTION.md section 2, V1 migration).
 *
 * <p>Deliberately minimal. Authentication - password hashing, OAuth, JWT
 * (Master Spec section 28) - is not specified anywhere in the current
 * documentation, so it is not designed here. This entity exists only so a
 * {@link BirthProfileEntity} has an owner to reference.
 *
 * <p>Named {@code UserEntity} rather than {@code User} to avoid colliding with
 * {@code java.security.Principal}-adjacent framework types once authentication
 * is actually designed.
 */
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 320)
    private String email;

    @Column(name = "display_name", length = 200)
    private String displayName;

    @Column(nullable = false, length = 20)
    private String locale = "vi-VN";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected UserEntity() {
        // JPA
    }

    public UserEntity(String email, String displayName) {
        this.email = Objects.requireNonNull(email, "email");
        this.displayName = displayName;
        // CLAUDE.md section 9: production UI defaults to Vietnamese.
        this.locale = Locale.forLanguageTag("vi-VN").toLanguageTag();
    }

    public Long id() {
        return id;
    }

    public String email() {
        return email;
    }

    public String displayName() {
        return displayName;
    }

    public String locale() {
        return locale;
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
