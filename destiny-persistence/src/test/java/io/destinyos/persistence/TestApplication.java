package io.destinyos.persistence;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Bootstraps a Spring context for {@code @DataJpaTest} in this module.
 *
 * <p>{@code destiny-persistence} has no {@code @SpringBootApplication} of its
 * own (that lives in {@code destiny-app}); this test-only class exists so the
 * repository and registry tests here can run in isolation, without pulling in
 * the whole application module.
 */
@SpringBootApplication
public class TestApplication {
}
