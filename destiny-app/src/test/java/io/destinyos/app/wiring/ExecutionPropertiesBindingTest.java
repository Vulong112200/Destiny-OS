package io.destinyos.app.wiring;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/**
 * Pins the {@code destiny.execution.*} property names.
 *
 * <p><strong>The absence of this test is why that configuration was dead.</strong>
 * {@code application.yml} declared {@code default-timeout} and
 * {@code max-concurrency} from the beginning, but no {@code @ConfigurationProperties}
 * class bound the prefix and {@code EngineWiringConfig} hardcoded
 * {@code ExecutionPolicy.defaults()}. Every existing test passed, because they
 * all construct an {@code ExecutionPolicy} directly and never ask whether
 * configuration reaches one. Nothing observable distinguished "the operator's
 * timeout is in effect" from "the operator's timeout is being ignored", which
 * is precisely the sort of silent divergence this project's own rules are
 * written against.
 *
 * <p>Same reasoning and same shape as
 * {@code OpenRouterPropertiesBindingTest} in {@code destiny-ai}.
 */
class ExecutionPropertiesBindingTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
            .withUserConfiguration(EnableProperties.class);

    @EnableConfigurationProperties(ExecutionProperties.class)
    static class EnableProperties {
    }

    @Test
    @DisplayName("default-timeout and max-concurrency bind, and reach the policy")
    void scalarPropertiesBind() {
        runner.withPropertyValues(
                        "destiny.execution.default-timeout=7s",
                        "destiny.execution.max-concurrency=4")
                .run(context -> {
                    ExecutionProperties properties = context.getBean(ExecutionProperties.class);
                    assertThat(properties.toPolicy().defaultTimeout()).isEqualTo(Duration.ofSeconds(7));
                    assertThat(properties.toPolicy().maxConcurrency()).isEqualTo(4);
                });
    }

    @Test
    @DisplayName("A per-engine override binds under its engine id and beats the default")
    void perEngineOverrideBindsUnderTheEngineId() {
        // The key must be the engine id ExecutionPolicy.timeoutFor() looks up -
        // the same string EngineWiringConfig registers. A key matching no
        // engine binds perfectly happily and then silently does nothing, so
        // asserting the lookup rather than the map is the whole point.
        runner.withPropertyValues(
                        "destiny.execution.default-timeout=5s",
                        "destiny.execution.per-engine-timeouts.TAROT=2s")
                .run(context -> {
                    var policy = context.getBean(ExecutionProperties.class).toPolicy();
                    assertThat(policy.timeoutFor("TAROT")).isEqualTo(Duration.ofSeconds(2));
                    assertThat(policy.timeoutFor("BAZI")).isEqualTo(Duration.ofSeconds(5));
                });
    }

    @Test
    @DisplayName("Binding nothing keeps exactly the behaviour of ExecutionPolicy.defaults()")
    void absentConfigurationKeepsTheOldDefaults() {
        runner.run(context -> {
            var policy = context.getBean(ExecutionProperties.class).toPolicy();
            assertThat(policy.defaultTimeout()).isEqualTo(Duration.ofSeconds(5));
            assertThat(policy.maxConcurrency()).isEqualTo(16);
            assertThat(policy.perEngineTimeouts()).isEmpty();
        });
    }
}
