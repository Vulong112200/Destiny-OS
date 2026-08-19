package io.destinyos.app;

import static org.assertj.core.api.Assertions.assertThat;

import io.destinyos.engine.MethodologyStatus;
import io.destinyos.persistence.registry.MethodologyRegistryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * A genuine full-context boot, distinct from the {@code @DataJpaTest} slices
 * in {@code destiny-persistence}.
 *
 * <p>Those slice tests {@code @Import} their beans explicitly, which proves
 * the beans work but says nothing about whether
 * {@link DestinyOsApplication}'s {@code @ComponentScan(basePackages =
 * "io.destinyos")}, {@code @EntityScan} and {@code @EnableJpaRepositories}
 * actually reach into {@code destiny-persistence} the way the real
 * application depends on. This test boots the real application class and
 * checks that they do.
 */
// WebEnvironment.NONE: this test is about persistence/registry wiring, not
// HTTP behaviour (that is destiny-api's ScenarioApiIntegrationTest) - no
// need to start a real embedded servlet container just to check a bean
// exists.
@SpringBootTest(classes = DestinyOsApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class DestinyOsApplicationTest {

    @Autowired
    private MethodologyRegistryService registry;

    @Test
    @DisplayName("The full application context loads with the persistence module wired in")
    void contextLoads() {
        assertThat(registry).isNotNull();
    }

    @Test
    @DisplayName("The methodology registry seeder ran automatically on startup (ADR D7)")
    void seederRanAutomatically() {
        // Proves @EventListener(ApplicationReadyEvent.class) inside
        // MethodologyRegistrySeeder actually fires in the real application -
        // not merely when a test calls seed() directly, as the
        // destiny-persistence slice tests do.
        assertThat(registry.allMethodologies()).isNotEmpty();

        var bazi = registry.latestVersion("BAZI").orElseThrow(
                () -> new AssertionError("BAZI was not auto-registered on startup"));
        assertThat(bazi.status()).isEqualTo(MethodologyStatus.RESEARCH_REQUIRED);

        var tarot = registry.latestVersion("TAROT_RWS").orElseThrow();
        assertThat(tarot.status()).isEqualTo(MethodologyStatus.CONTENT_REQUIRED);
        assertThat(registry.isCalculable("TAROT_RWS")).isTrue();
    }
}
