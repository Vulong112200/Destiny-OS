package io.destinyos.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@code destiny.ai.enabled} - the single operator-facing kill switch.
 * Defaults to {@code false}: per ADR D8 the system must be fully usable
 * with AI disabled, and a feature that calls a third-party service should
 * never turn itself on by the mere presence of a library on the classpath.
 */
@ConfigurationProperties(prefix = "destiny.ai")
public class AiProperties {

    private boolean enabled = false;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
