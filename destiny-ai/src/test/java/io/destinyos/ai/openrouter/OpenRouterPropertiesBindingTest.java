package io.destinyos.ai.openrouter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/**
 * Pins the {@code destiny.ai.openrouter.*} property <em>names</em>, not the
 * behaviour they drive.
 *
 * <p>A misnamed configuration key is the one kind of break in this area that
 * no other test would catch: Spring binds what it recognises, silently ignores
 * what it does not, and the field keeps its default. The result is an operator
 * setting {@code DESTINY_AI_OPENROUTER_FALLBACK_MODELS} in {@code .env},
 * seeing no error anywhere, and getting a chain that never contains what they
 * asked for. ADR D8 then hides the consequence too, because a chain that runs
 * out still renders a perfectly good deterministic report.
 */
class OpenRouterPropertiesBindingTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(org.springframework.boot.autoconfigure.AutoConfigurations.of(
                    ConfigurationPropertiesAutoConfiguration.class))
            .withUserConfiguration(EnableProperties.class);

    @org.springframework.boot.context.properties.EnableConfigurationProperties(OpenRouterProperties.class)
    static class EnableProperties {
    }

    @Test
    @DisplayName("fallback-models binds from a comma-separated value, as an env var supplies it")
    void fallbackModelsBindsFromCommaSeparatedConfiguration() {
        // Exactly the shape DESTINY_AI_OPENROUTER_FALLBACK_MODELS=a,b arrives
        // in. Spring's relaxed binding is what maps the kebab-case key to the
        // camelCase field and splits the delimited string into a List - this
        // asserts that chain actually holds for this property.
        runner.withPropertyValues("destiny.ai.openrouter.fallback-models=openrouter/free,minimax/minimax-m3:free")
                .run(context -> assertThat(context.getBean(OpenRouterProperties.class).getFallbackModels())
                        .containsExactly("openrouter/free", "minimax/minimax-m3:free"));
    }

    @Test
    @DisplayName("An unset fallback-models keeps the openrouter/free default")
    void unsetFallbackModelsKeepsTheDefault() {
        runner.run(context -> assertThat(context.getBean(OpenRouterProperties.class).getFallbackModels())
                .containsExactly("openrouter/free"));
    }

    @Test
    @DisplayName("max-tokens binds, and its default is the measured 2000")
    void maxTokensBindsAndDefaultsToTheMeasuredValue() {
        // 800 truncated 2 of 3 real replies mid-string; see
        // OpenRouterProperties#maxTokens. Pinned so a future "trim the token
        // budget" change has to argue with a test rather than slip through.
        runner.run(context -> assertThat(context.getBean(OpenRouterProperties.class).getMaxTokens())
                .isEqualTo(2000));
        runner.withPropertyValues("destiny.ai.openrouter.max-tokens=1234")
                .run(context -> assertThat(context.getBean(OpenRouterProperties.class).getMaxTokens())
                        .isEqualTo(1234));
    }
}
