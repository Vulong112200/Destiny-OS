package io.destinyos.ai.openrouter;

import io.destinyos.ai.AiNarrativeProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Wires an {@link AiNarrativeProvider} bean only when OpenRouter is actually
 * configured. Nothing else in the reactor requires this bean to exist -
 * {@code io.destinyos.ai.NarrativeService} takes
 * {@code Optional<AiNarrativeProvider>} precisely so its absence is a
 * normal, fully-supported state (ADR D8), not a startup failure.
 */
@Configuration
@EnableConfigurationProperties(OpenRouterProperties.class)
class OpenRouterAutoConfiguration {

    @Bean
    @Conditional(OpenRouterConfiguredCondition.class)
    AiNarrativeProvider openRouterNarrativeProvider(RestClient.Builder builder, OpenRouterProperties properties) {
        RestClient restClient = OpenRouterNarrativeProvider.buildRestClient(builder, properties);
        return new OpenRouterNarrativeProvider(restClient, properties);
    }
}
