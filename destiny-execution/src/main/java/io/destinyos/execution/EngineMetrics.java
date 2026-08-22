package io.destinyos.execution;

import io.destinyos.core.result.EngineStatus;
import java.time.Duration;

/**
 * Per-engine metrics (CLAUDE.md §5, which requires every engine to have
 * <em>timeout, cancellation, error isolation and metrics</em>).
 *
 * <p>The first three were already structural in {@link EngineExecutor}; this is
 * the fourth. Until it existed, an engine that started timing out in production
 * left nothing behind but a log line — and a log line is not something you can
 * alert on or graph.
 *
 * <p><strong>An interface, not a Micrometer dependency.</strong>
 * {@code destiny-execution} depends on nothing but the engine SPI and slf4j, and
 * that is worth keeping: the harness is the piece most likely to be exercised in
 * a plain unit test, and making it drag a metrics framework in would make every
 * such test pay for one. The Micrometer implementation lives in
 * {@code destiny-app}, the module that already exists to do assembly — the same
 * split {@code AiNarrativeProvider} uses for its OpenRouter implementation.
 *
 * <p>Implementations MUST NOT throw. A metrics backend that is misconfigured, or
 * a registry that has been shut down, must not turn a working calculation into a
 * failed one — measuring something is never more important than doing it.
 * {@link EngineExecutor} guards against this anyway, but an implementation that
 * relies on being guarded has the priority backwards.
 */
public interface EngineMetrics {

    /**
     * One engine finished, however it finished.
     *
     * @param engineId  which engine
     * @param status    its outcome — recorded rather than reduced to
     *                  success/failure, because the honest non-answers
     *                  ({@code NOT_APPLICABLE}, {@code RESEARCH_REQUIRED}) are
     *                  normal states this project expects to see a lot of, and
     *                  bucketing them with real failures would make the failure
     *                  rate meaningless
     * @param duration  wall-clock time from submission to completion
     * @param timedOut  whether the harness cut it off. Separate from
     *                  {@code status} on purpose: a timeout surfaces as
     *                  {@code FAILED_RECOVERABLE}, so without this flag the one
     *                  failure mode with a known remedy would be
     *                  indistinguishable from an engine that threw
     */
    void recordExecution(String engineId, EngineStatus status, Duration duration, boolean timedOut);

    /**
     * How long an engine waited for a concurrency permit before starting.
     *
     * <p>The one thing that is otherwise invisible. {@code ExecutionPolicy}
     * bounds concurrency, so under load an engine's measured duration includes
     * time it spent queued — and "the engine is slow" and "we were saturated"
     * need different fixes. Recorded for every execution, usually as ~0.
     */
    void recordConcurrencyWait(String engineId, Duration waited);

    /**
     * Records nothing. The default, so that constructing an
     * {@link EngineExecutor} never requires a metrics backend and no test has
     * to supply one.
     */
    EngineMetrics NO_OP = new EngineMetrics() {
        @Override
        public void recordExecution(String engineId, EngineStatus status, Duration duration,
                                    boolean timedOut) {
            // deliberately nothing
        }

        @Override
        public void recordConcurrencyWait(String engineId, Duration waited) {
            // deliberately nothing
        }
    };
}
