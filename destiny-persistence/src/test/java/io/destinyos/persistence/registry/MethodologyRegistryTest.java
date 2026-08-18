package io.destinyos.persistence.registry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.destinyos.engine.MethodologyStatus;
import io.destinyos.persistence.TestApplication;
import jakarta.persistence.EntityManager;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

/**
 * Tests for the methodology registry (ADR D7) against the real V2 migration
 * schema, on H2 in PostgreSQL-compatibility mode (see class-level Javadoc on
 * {@link io.destinyos.persistence.identity.IdentityPersistenceTest} for why).
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestApplication.class, MethodologyRegistryService.class, MethodologyRegistrySeeder.class})
class MethodologyRegistryTest {

    @Autowired
    private MethodologyRegistryService registry;

    @Autowired
    private EntityManager entityManager;

    @Nested
    @DisplayName("Registering a methodology version (ADR D7)")
    class Registration {

        @Test
        @DisplayName("A blocked methodology round-trips with its research references intact")
        void blockedMethodologyRoundTrips() {
            registry.register("TEST_BAZI", "Bát Tự thử nghiệm", "EASTERN", "1.0",
                    MethodologyStatus.RESEARCH_REQUIRED, null, null,
                    Set.of("R1", "R2", "R3"), "Dụng Thần school not yet selected.");

            entityManager.flush();
            entityManager.clear();

            var latest = registry.latestVersion("TEST_BAZI").orElseThrow();

            assertThat(latest.status()).isEqualTo(MethodologyStatus.RESEARCH_REQUIRED);
            assertThat(latest.researchIds()).containsExactlyInAnyOrder("R1", "R2", "R3");
            assertThat(latest.school()).isNull();
            assertThat(latest.source()).isNull();
        }

        @Test
        @DisplayName("A calculable status without a school is rejected (mirrors EngineMetadata)")
        void calculableWithoutSchoolIsRejected() {
            assertThatThrownBy(() -> registry.register("TEST_TAROT", "Tarot thử nghiệm",
                    "WESTERN", "1.0", MethodologyStatus.PRODUCTION_READY, null,
                    "some-source", Set.of(), null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Rule D");
        }

        @Test
        @DisplayName("A calculable status without a source is rejected (mirrors EngineMetadata)")
        void calculableWithoutSourceIsRejected() {
            assertThatThrownBy(() -> registry.register("TEST_TAROT2", "Tarot thử nghiệm 2",
                    "WESTERN", "1.0", MethodologyStatus.CONTENT_REQUIRED, "Rider-Waite-Smith",
                    null, Set.of(), null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Rule C");
        }

        @Test
        @DisplayName("An unregistered methodology is not calculable, same as a blocked one")
        void unregisteredIsNotCalculable() {
            // ADR D7: absence and an honest block must both read as
            // "cannot calculate", never as "assume it is fine".
            assertThat(registry.isCalculable("NEVER_REGISTERED")).isFalse();
        }

        @Test
        @DisplayName("Re-registering the same methodology id does not duplicate the parent row")
        void sameMethodologyIdReusesParent() {
            registry.register("TEST_SHARED", "Chung", "EASTERN", "1.0",
                    MethodologyStatus.RESEARCH_REQUIRED, null, null, Set.of("R12"), "v1");
            registry.register("TEST_SHARED", "Chung", "EASTERN", "2.0",
                    MethodologyStatus.RESEARCH_REQUIRED, null, null, Set.of("R12"), "v2");

            entityManager.flush();
            entityManager.clear();

            assertThat(registry.allMethodologies())
                    .filteredOn(m -> m.methodologyId().equals("TEST_SHARED"))
                    .hasSize(1);
            assertThat(registry.allVersions("TEST_SHARED")).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Seeder accuracy against RESEARCH_BLOCKERS.md")
    class SeederAccuracy {

        @Autowired
        private MethodologyRegistrySeeder seeder;

        @Test
        @DisplayName("Every specified methodology is registered, honestly, exactly once")
        void allMethodologiesRegistered() {
            seeder.seed();
            entityManager.flush();
            entityManager.clear();

            // These statuses are transcribed directly from
            // docs/RESEARCH_BLOCKERS.md's register index. If that document
            // changes, this test - and the seeder - must change with it.
            assertStatus("NUMEROLOGY_PYTHAGOREAN", MethodologyStatus.DECISION_REQUIRED, "R8");
            assertStatus("NUMEROLOGY_CHALDEAN", MethodologyStatus.RESEARCH_REQUIRED, "R8");
            assertStatus("TAROT_RWS", MethodologyStatus.CONTENT_REQUIRED, "R11");
            assertStatus("BAZI", MethodologyStatus.RESEARCH_REQUIRED, "R1", "R2", "R3");
            assertStatus("ZIWEI", MethodologyStatus.RESEARCH_REQUIRED, "R4");
            assertStatus("WESTERN_ASTROLOGY", MethodologyStatus.DECISION_REQUIRED, "R5", "R6");
            assertStatus("FENGSHUI_KUA", MethodologyStatus.RESEARCH_REQUIRED, "R7");
            assertStatus("ICHING", MethodologyStatus.RESEARCH_REQUIRED, "R12");
            assertStatus("MAIHOA", MethodologyStatus.RESEARCH_REQUIRED, "R12");
            assertStatus("QIMEN", MethodologyStatus.OUT_OF_SCOPE, "R13");
            assertStatus("CALENDAR_VN_TRADITIONAL", MethodologyStatus.RESEARCH_REQUIRED,
                    "R9", "R10", "R14", "R15", "R16", "R17");
        }

        @Test
        @DisplayName("Only TAROT_RWS is calculable; every research/decision-blocked entry is not")
        void onlyContentGatedEntriesAreCalculable() {
            // TAROT_RWS is CONTENT_REQUIRED: its algorithm (RWS deck
            // structure, seeded shuffle) is fully specified, so
            // MethodologyStatus.mayCalculate() is true even though the
            // Vietnamese meaning corpus is still missing (R11). Every other
            // seeded entry is RESEARCH_REQUIRED, DECISION_REQUIRED or
            // OUT_OF_SCOPE and must not be calculable.
            seeder.seed();
            entityManager.flush();
            entityManager.clear();

            assertThat(registry.isCalculable("TAROT_RWS")).isTrue();

            for (var methodology : registry.allMethodologies()) {
                if (methodology.methodologyId().equals("TAROT_RWS")) {
                    continue;
                }
                assertThat(registry.isCalculable(methodology.methodologyId()))
                        .as("%s should not be calculable yet", methodology.methodologyId())
                        .isFalse();
            }
        }

        @Test
        @DisplayName("Seeding twice does not duplicate rows (idempotent)")
        void seedingTwiceIsIdempotent() {
            seeder.seed();
            seeder.seed();
            entityManager.flush();
            entityManager.clear();

            assertThat(registry.allVersions("BAZI")).hasSize(1);
        }

        private void assertStatus(String methodologyId, MethodologyStatus expected,
                                  String... expectedResearchIds) {
            var latest = registry.latestVersion(methodologyId)
                    .orElseThrow(() -> new AssertionError(methodologyId + " was not registered"));

            assertThat(latest.status())
                    .as("%s status", methodologyId)
                    .isEqualTo(expected);
            assertThat(latest.researchIds())
                    .as("%s research references", methodologyId)
                    .containsExactlyInAnyOrder(expectedResearchIds);
        }
    }
}
