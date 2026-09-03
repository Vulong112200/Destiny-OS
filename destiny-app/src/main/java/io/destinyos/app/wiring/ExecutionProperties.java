package io.destinyos.app.wiring;

import io.destinyos.execution.ExecutionPolicy;
import java.time.Duration;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds {@code destiny.execution.*} onto {@link ExecutionPolicy}
 * (CLAUDE.md §5).
 *
 * <p><strong>This class exists because that configuration did nothing.</strong>
 * {@code application.yml} has declared {@code default-timeout} and
 * {@code max-concurrency} since the harness was written, but nothing ever bound
 * them - {@code EngineWiringConfig} called {@code ExecutionPolicy.defaults()}
 * and the YAML was decoration. An operator raising the budget to diagnose a
 * slow engine would have changed nothing and concluded the timeout was not the
 * problem. Dead configuration is worse than no configuration, because it
 * answers questions falsely.
 *
 * <p><strong>Why it lives in {@code destiny-app} and not in
 * {@code destiny-execution}.</strong> That module depends on the engine SPI and
 * slf4j and nothing else, deliberately - the same reason
 * {@link MicrometerEngineMetrics} is here rather than there. The harness is the
 * piece most likely to be exercised in a plain unit test, and dragging Spring
 * into it would make every such test pay for a framework it does not use.
 * {@link #toPolicy()} is the seam: configuration is a Spring concern, the
 * policy is not.
 *
 * @param defaultTimeout    budget for any engine without an override
 * @param perEngineTimeouts overrides keyed by <strong>engine id</strong> - the
 *                          same strings {@code EngineWiringConfig} registers
 *                          ({@code TAROT}, {@code BAZI}, …). A key that matches
 *                          no engine is silently unused, which looks exactly
 *                          like a working override; {@code ExecutionPropertiesBindingTest}
 *                          pins the ones that ship.
 * @param maxConcurrency    upper bound on simultaneous engines
 */
@ConfigurationProperties(prefix = "destiny.execution")
public record ExecutionProperties(
        Duration defaultTimeout,
        Map<String, Duration> perEngineTimeouts,
        int maxConcurrency
) {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);
    private static final int DEFAULT_MAX_CONCURRENCY = 16;

    /**
     * Absent properties fall back to the values {@code ExecutionPolicy.defaults()}
     * already used, so binding nothing behaves exactly as before this class
     * existed.
     */
    public ExecutionProperties {
        defaultTimeout = defaultTimeout == null ? DEFAULT_TIMEOUT : defaultTimeout;
        perEngineTimeouts = perEngineTimeouts == null ? Map.of() : Map.copyOf(perEngineTimeouts);
        maxConcurrency = maxConcurrency < 1 ? DEFAULT_MAX_CONCURRENCY : maxConcurrency;
    }

    /** The framework-free policy the execution harness actually consumes. */
    public ExecutionPolicy toPolicy() {
        return new ExecutionPolicy(defaultTimeout, perEngineTimeouts, maxConcurrency);
    }
}
