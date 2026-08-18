package io.destinyos.persistence.identity;

import io.destinyos.core.context.BirthTimePrecision;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/**
 * The Person / birth profile model (Master Spec section 2, V1 migration).
 *
 * <p>{@code birthRegion} is a free-form string on purpose, mirroring the
 * V1 migration comment: research item R17 (what granularity "region" should
 * have) is still {@code DECISION_REQUIRED}. This entity does not constrain
 * the value to a fixed set, because doing so would silently make that
 * decision instead of waiting for it (ADR D3, CLAUDE.md Rule D).
 *
 * <p>{@code birthTimePrecision} defaults to {@link BirthTimePrecision#UNKNOWN},
 * matching {@link io.destinyos.core.context.CalculationContext}'s own default
 * and Master Spec section 2's instruction: never treat UNKNOWN as EXACT.
 */
@Entity
@Table(name = "birth_profiles")
public class BirthProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "birth_time")
    private LocalTime birthTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "birth_time_precision", nullable = false, length = 20)
    private BirthTimePrecision birthTimePrecision = BirthTimePrecision.UNKNOWN;

    @Column(name = "birth_timezone", length = 60)
    private String birthTimezone;

    @Column(name = "birth_location", length = 300)
    private String birthLocation;

    /** See class Javadoc: intentionally unconstrained pending research item R17. */
    @Column(name = "birth_region", length = 100)
    private String birthRegion;

    @Column(precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 9, scale = 6)
    private BigDecimal longitude;

    /** "gender/sex only where methodology requires" (Master Spec section 2). */
    @Column(length = 20)
    private String gender;

    @Column(nullable = false, length = 20)
    private String locale = "vi-VN";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected BirthProfileEntity() {
        // JPA
    }

    public BirthProfileEntity(UserEntity user, String fullName, LocalDate birthDate) {
        this.user = Objects.requireNonNull(user, "user");
        this.fullName = Objects.requireNonNull(fullName, "fullName");
        this.birthDate = Objects.requireNonNull(birthDate, "birthDate");
    }

    public Long id() {
        return id;
    }

    public UserEntity user() {
        return user;
    }

    public String fullName() {
        return fullName;
    }

    public LocalDate birthDate() {
        return birthDate;
    }

    public LocalTime birthTime() {
        return birthTime;
    }

    public BirthTimePrecision birthTimePrecision() {
        return birthTimePrecision;
    }

    /**
     * Sets the birth time together with its precision, so the two can never
     * drift apart - a stored time with no recorded precision is exactly the
     * false-EXACT failure Master Spec section 2 warns about.
     */
    public void setBirthTime(LocalTime birthTime, BirthTimePrecision precision) {
        this.birthTime = birthTime;
        this.birthTimePrecision = Objects.requireNonNull(precision, "precision");
    }

    public String birthTimezone() {
        return birthTimezone;
    }

    public void setBirthTimezone(String birthTimezone) {
        this.birthTimezone = birthTimezone;
    }

    public String birthLocation() {
        return birthLocation;
    }

    public void setBirthLocation(String birthLocation) {
        this.birthLocation = birthLocation;
    }

    public String birthRegion() {
        return birthRegion;
    }

    public void setBirthRegion(String birthRegion) {
        this.birthRegion = birthRegion;
    }

    public BigDecimal latitude() {
        return latitude;
    }

    public BigDecimal longitude() {
        return longitude;
    }

    public void setCoordinates(BigDecimal latitude, BigDecimal longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String gender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
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
