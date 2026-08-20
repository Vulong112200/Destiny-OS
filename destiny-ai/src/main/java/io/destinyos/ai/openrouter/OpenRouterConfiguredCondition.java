package io.destinyos.ai.openrouter;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * True only when {@code destiny.ai.enabled=true} AND both an API key and a
 * model are configured. Deliberately stricter than
 * {@code @ConditionalOnProperty(destiny.ai.enabled)} alone: an operator who
 * sets {@code enabled=true} but forgets the API key must get the safe
 * fallback path (ADR D8), not a bean that fails on its first real call.
 */
final class OpenRouterConfiguredCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        var environment = context.getEnvironment();
        boolean enabled = environment.getProperty("destiny.ai.enabled", Boolean.class, false);
        String apiKey = environment.getProperty("destiny.ai.openrouter.api-key", "");
        String model = environment.getProperty("destiny.ai.openrouter.model", "");
        return enabled && !apiKey.isBlank() && !model.isBlank();
    }
}
