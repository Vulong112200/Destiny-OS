package io.destinyos.app.wiring;

import io.destinyos.core.result.EngineStatus;
import io.destinyos.execution.EngineMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * Micrometer-backed {@link EngineMetrics} (CLAUDE.md §5, Phase 14).
 *
 * <p>Lives in {@code destiny-app} so that {@code destiny-execution} keeps
 * depending on nothing but the engine SPI and slf4j — the harness is the piece
 * most likely to be exercised in a plain unit test, and dragging a metrics
 * framework into it would make every such test pay for one.
 *
 * <p><strong>Tag cardinality is deliberately small.</strong> Every tag value
 * here comes from a closed set: {@code engine} from the registered engine ids,
 * {@code status} from {@link EngineStatus}, {@code outcome} from three fixed
 * strings. Nothing derived from user input is ever tagged — a per-user or
 * per-calculation tag would multiply time series without bound, which is the
 * standard way a metrics backend gets taken down by the thing meant to watch it.
 */
@Component
public class MicrometerEngineMetrics implements EngineMetrics {

    /** How an execution ended, at a coarser grain than {@link EngineStatus}. */
    private enum Outcome {
        /** Produced usable data, in full or in part. */
        ANSWERED,
        /**
         * Declined honestly — {@code NOT_APPLICABLE}, {@code RESEARCH_REQUIRED},
         * {@code NOT_IMPLEMENTED}, {@code INVALID_INPUT}. Its own bucket because
         * this project expects a great many of these and folding them into
         * failures would make the failure rate meaningless.
         */
        DECLINED,
        /** Actually broke: threw, returned null, or timed out. */
        FAILED
    }

    private static final String EXECUTIONS = "destiny.engine.executions";
    private static final String DURATION = "destiny.engine.duration";
    private static final String CONCURRENCY_WAIT = "destiny.engine.concurrency.wait";

    private final MeterRegistry registry;

    public MicrometerEngineMetrics(MeterRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    @Override
    public void recordExecution(String engineId, EngineStatus status, Duration duration,
                                boolean timedOut) {
        String engine = engineId == null ? "unknown" : engineId;
        String statusTag = status == null ? "UNKNOWN" : status.name();
        String outcome = classify(status, timedOut).name();

        registry.counter(EXECUTIONS,
                "engine", engine,
                "status", statusTag,
                "outcome", outcome,
                // Separate from status because a timeout surfaces as
                // FAILED_RECOVERABLE: without this tag the one failure mode with
                // a known remedy (raise the budget, or make the engine faster)
                // is indistinguishable from an engine that threw.
                "timedOut", Boolean.toString(timedOut)
        ).increment();

        Timer.builder(DURATION)
                .description("Wall-clock time for one engine execution, including queueing")
                .tag("engine", engine)
                .tag("outcome", outcome)
                .register(registry)
                .record(duration == null ? Duration.ZERO : duration);
    }

    @Override
    public void recordConcurrencyWait(String engineId, Duration waited) {
        Timer.builder(CONCURRENCY_WAIT)
                .description("Time an engine waited for a concurrency permit before starting")
                .tag("engine", engineId == null ? "unknown" : engineId)
                .register(registry)
                .record(waited == null ? Duration.ZERO : waited);
    }

    /**
     * A timeout is a failure even though {@code EngineStatus} calls it
     * recoverable, so {@code timedOut} is checked first. Everything else follows
     * the status: usable data means answered, an explicit non-answer means
     * declined, and a genuine fault means failed.
     */
    private static Outcome classify(EngineStatus status, boolean timedOut) {
        if (timedOut) {
            return Outcome.FAILED;
        }
        if (status == null) {
            return Outcome.FAILED;
        }
        return switch (status) {
            case SUCCESS, PARTIAL -> Outcome.ANSWERED;
            case NOT_APPLICABLE, RESEARCH_REQUIRED, NOT_IMPLEMENTED, INVALID_INPUT
                    -> Outcome.DECLINED;
            case FAILED_RECOVERABLE, FAILED_FATAL -> Outcome.FAILED;
        };
    }
}
