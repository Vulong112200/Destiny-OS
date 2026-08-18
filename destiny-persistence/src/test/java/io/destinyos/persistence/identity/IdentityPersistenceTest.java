package io.destinyos.persistence.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.destinyos.core.context.BirthTimePrecision;
import io.destinyos.persistence.TestApplication;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

/**
 * Round-trip tests against the V1 migration schema (identity foundation).
 *
 * <p>Runs against H2 in PostgreSQL-compatibility mode with real Flyway
 * migrations applied (see {@code src/test/resources/application.yml}) - not
 * Hibernate-generated schema. A successful context load here already proves
 * the V1 migration executes cleanly; the assertions below additionally prove
 * the entity mappings round-trip correctly through it.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestApplication.class)
class IdentityPersistenceTest {

    @Autowired
    private UserRepository users;

    @Autowired
    private BirthProfileRepository profiles;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("A user round-trips with timestamps set by the entity lifecycle callbacks")
    void userRoundTrips() {
        var saved = users.save(new UserEntity("test@example.com", "Nguyễn Văn A"));
        entityManager.flush();
        entityManager.clear();

        var loaded = users.findById(saved.id()).orElseThrow();

        assertThat(loaded.email()).isEqualTo("test@example.com");
        assertThat(loaded.displayName()).isEqualTo("Nguyễn Văn A");
        assertThat(loaded.locale()).isEqualTo("vi-VN");
        assertThat(loaded.createdAt()).isNotNull();
        assertThat(loaded.updatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Duplicate email is rejected by the V1 unique constraint")
    void duplicateEmailRejected() {
        users.saveAndFlush(new UserEntity("dup@example.com", "A"));

        assertThatThrownBy(() ->
                users.saveAndFlush(new UserEntity("dup@example.com", "B")))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Birth time precision defaults to UNKNOWN, never silently EXACT")
    void birthTimePrecisionDefaultsToUnknown() {
        var user = users.save(new UserEntity("owner@example.com", "Chủ hồ sơ"));

        var profile = new BirthProfileEntity(user, "Nguyễn Thị B", LocalDate.of(1990, 5, 12));
        var saved = profiles.save(profile);
        entityManager.flush();
        entityManager.clear();

        var loaded = profiles.findById(saved.id()).orElseThrow();

        // Master Spec section 2: "Không được coi UNKNOWN là EXACT."
        assertThat(loaded.birthTimePrecision()).isEqualTo(BirthTimePrecision.UNKNOWN);
        assertThat(loaded.birthTime()).isNull();
    }

    @Test
    @DisplayName("Setting a birth time also requires stating its precision")
    void birthTimeAndPrecisionTravelTogether() {
        var user = users.save(new UserEntity("owner2@example.com", "Chủ hồ sơ 2"));
        var profile = new BirthProfileEntity(user, "Trần Văn C", LocalDate.of(1985, 3, 1));
        profile.setBirthTime(LocalTime.of(14, 30), BirthTimePrecision.EXACT);
        profile.setBirthRegion("VN_SOUTH");
        profile.setCoordinates(new BigDecimal("10.762622"), new BigDecimal("106.660172"));

        var saved = profiles.save(profile);
        entityManager.flush();
        entityManager.clear();

        var loaded = profiles.findById(saved.id()).orElseThrow();

        assertThat(loaded.birthTime()).isEqualTo(LocalTime.of(14, 30));
        assertThat(loaded.birthTimePrecision()).isEqualTo(BirthTimePrecision.EXACT);
        // ADR D3 / research item R17: region is free-form, not a constrained
        // enum, since the granularity decision is still open.
        assertThat(loaded.birthRegion()).isEqualTo("VN_SOUTH");
        assertThat(loaded.latitude()).isEqualByComparingTo("10.762622");
    }

    @Test
    @DisplayName("A birth profile without an owning user is rejected")
    void profileRequiresAUser() {
        assertThatThrownBy(() -> new BirthProfileEntity(null, "X", LocalDate.now()))
                .isInstanceOf(NullPointerException.class);
    }
}
