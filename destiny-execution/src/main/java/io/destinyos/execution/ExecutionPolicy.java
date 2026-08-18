package io.destinyos.execution;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/**
 * Timeout and concurrency limits (CLAUDE.md §5, command §19).
 *
 * <p>There is no "no timeout" option, and no retry count. Both are deliberate:
 * CLAUDE.md §5 forbids infinite retry, and an engine without a time budget can
 * hold the whole request open — the exact failure Rule F exists to prevent.
 *
 * @param defaultTimeout    budget applied to any engine without an override
 * @param perEngineTimeouts overrides by engineId
 * @param maxConcurrency    upper bound on simultaneous engines; no unbounded pools
 */
public record ExecutionPolicy(
        Duration defaultTimeout,
        Map<String, Duration> perEngineTimeouts,
        int maxConcurrency
) {
    public ExecutionPolicy {
        Objects.requireNonNull(defaultTimeout, "defaultTimeout");
        if (defaultTimeout.isNegative() || defaultTimeout.isZero()) {
            throw new IllegalArgumentException("defaultTimeout must be positive");
        }
        if (maxConcurrency < 1) {
            throw new IllegalArgumentException("maxConcurrency must be at least 1");
        }
        perEngineTimeouts = perEngineTimeouts == null ? Map.of() : Map.copyOf(perEngineTimeouts);
    }

    public static ExecutionPolicy defaults() {
        return new ExecutionPolicy(Duration.ofSeconds(5), Map.of(), 16);
    }

    public Duration timeoutFor(String engineId) {
        return perEngineTimeouts.getOrDefault(engineId, defaultTimeout);
    }
}
