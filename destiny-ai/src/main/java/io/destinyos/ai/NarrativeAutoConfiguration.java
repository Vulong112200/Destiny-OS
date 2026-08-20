package io.destinyos.ai;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** Registers {@link AiProperties} so {@code destiny.ai.enabled} binds from the environment. */
@Configuration
@EnableConfigurationProperties(AiProperties.class)
class NarrativeAutoConfiguration {
}
