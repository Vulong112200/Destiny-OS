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
            assertStatus("NUMEROLOGY_PYTHAGOREAN", MethodologyStatus.PRODUCTION_READY, "R8");
            assertStatus("NUMEROLOGY_CHALDEAN", MethodologyStatus.RESEARCH_REQUIRED, "R8");
            assertStatus("TAROT_RWS", MethodologyStatus.PRODUCTION_READY, "R11");
            // Phase 8 is two entries with genuinely different statuses.
            assertStatus("BAZI_TUBINH_CHART", MethodologyStatus.CONTENT_REQUIRED, "R18", "R19");
            assertStatus("BAZI", MethodologyStatus.RESEARCH_REQUIRED, "R1", "R2", "R3");
            assertStatus("ZIWEI", MethodologyStatus.RESEARCH_REQUIRED, "R4");
            assertStatus("WESTERN_ASTROLOGY", MethodologyStatus.DECISION_REQUIRED, "R5", "R6");
            // Phase 10: four of R7's five items are closed, and the fifth (the
            // year boundary) is represented per calculation rather than guessed -
            // the same model CALENDAR_VN_TRADITIONAL uses for R14b.
            assertStatus("FENGSHUI_KUA", MethodologyStatus.PRODUCTION_READY, "R7");
            assertStatus("ICHING", MethodologyStatus.RESEARCH_REQUIRED, "R12");
            assertStatus("MAIHOA", MethodologyStatus.RESEARCH_REQUIRED, "R12");
            assertStatus("QIMEN", MethodologyStatus.OUT_OF_SCOPE, "R13");
            assertStatus("CALENDAR_VN_TRADITIONAL", MethodologyStatus.PRODUCTION_READY,
                    "R14b", "R17");
        }

        @Test
        @DisplayName("Only content-gated and production-ready entries are calculable")
        void onlyContentGatedEntriesAreCalculable() {
            // TAROT_RWS and NUMEROLOGY_PYTHAGOREAN are PRODUCTION_READY:
            // algorithms golden-tested and Vietnamese interpretive content
            // now authored (R11, R8) - both emit real signals.
            // CALENDAR_VN_TRADITIONAL is PRODUCTION_READY (destiny-calendar,
            // golden-tested against Ho Ngoc Duc's published tables) despite
            // R14b/R17 remaining open - those affect specific (date, region)
            // calculations, not the methodology as a whole.
            // Every other seeded entry is RESEARCH_REQUIRED,
            // DECISION_REQUIRED or OUT_OF_SCOPE and must not be calculable.
            seeder.seed();
            entityManager.flush();
            entityManager.clear();

            // BAZI_TUBINH_CHART is CONTENT_REQUIRED: the chart algorithm is
            // verified and calculable while the interpretive content that would
            // make it a reading is not - the same state TAROT_RWS was in before
            // its meaning corpus was authored. BAZI itself (the interpretive
            // half) stays non-calculable, which is the point of the split.
            Set<String> calculable = Set.of("TAROT_RWS", "NUMEROLOGY_PYTHAGOREAN",
                    "CALENDAR_VN_TRADITIONAL", "BAZI_TUBINH_CHART", "FENGSHUI_KUA");

            for (String id : calculable) {
                assertThat(registry.isCalculable(id)).as("%s should be calculable", id).isTrue();
            }

            for (var methodology : registry.allMethodologies()) {
                if (calculable.contains(methodology.methodologyId())) {
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
            assertThat(registry.allVersions("BAZI_TUBINH_CHART")).hasSize(1);
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
