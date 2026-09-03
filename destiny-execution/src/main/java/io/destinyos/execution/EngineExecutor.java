package io.destinyos.execution;

import io.destinyos.core.context.CalculationContext;
import io.destinyos.core.result.EngineError;
import io.destinyos.core.result.EngineResult;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs independent engines in parallel on virtual threads, with per-engine
 * timeout, cancellation and failure isolation (CLAUDE.md section 5 and Rule F,
 * Master Spec section 23, command section 19).
 *
 * <p>The contract this class exists to guarantee:
 * <ul>
 *   <li>an exception in one engine cannot affect another;</li>
 *   <li>a timeout in one engine cannot hold the batch open;</li>
 *   <li>a batch never fails as a unit - the result is PARTIAL carrying
 *       per-engine statuses;</li>
 *   <li>concurrency is bounded; no unbounded thread creation;</li>
 *   <li>no retry, ever. CLAUDE.md section 5 forbids it;</li>
 *   <li>every execution is measured ({@link EngineMetrics}), including the time
 *       spent waiting for a concurrency permit - and a metrics backend that
 *       misbehaves can never turn a working calculation into a failed one.</li>
 * </ul>
 *
 * <p>Virtual threads suit this well: engines are CPU-light and IO-adjacent
 * (dataset lookups), so a blocked engine parks its carrier rather than
 * starving the others.
 */
public final class EngineExecutor {

    private static final Logger log = LoggerFactory.getLogger(EngineExecutor.class);

    private final ExecutionPolicy policy;
    private final EngineMetrics metrics;

    public EngineExecutor(ExecutionPolicy policy) {
        this(policy, EngineMetrics.NO_OP);
    }

    public EngineExecutor(ExecutionPolicy policy, EngineMetrics metrics) {
        this.policy = Objects.requireNonNull(policy, "policy");
        this.metrics = metrics == null ? EngineMetrics.NO_OP : metrics;
    }

    public static EngineExecutor withDefaults() {
        return new EngineExecutor(ExecutionPolicy.defaults());
    }

    /**
     * Run every task, returning once all have completed, failed or timed out.
     *
     * <p>Never throws for engine-level problems. The only way out is an
     * {@link ExecutionOutcome} carrying exactly one entry per task.
     */
    public ExecutionOutcome runAll(List<EngineTask<?, ?>> tasks, CalculationContext context) {
        Objects.requireNonNull(tasks, "tasks");
        Objects.requireNonNull(context, "context");

        if (tasks.isEmpty()) {
            return new ExecutionOutcome(List.of());
        }

        var permits = new Semaphore(policy.maxConcurrency());
        var executions = new ArrayList<EngineExecution>(tasks.size());

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var pending = new ArrayList<PendingTask>(tasks.size());

            for (EngineTask<?, ?> task : tasks) {
                String engineId = task.engineId();
                // Seeded with the submit time so a task that never gets a
                // permit still has a deadline; overwritten with the real start
                // the moment one is acquired.
                var startedAt = new AtomicLong(System.nanoTime());
                Callable<EngineResult<?>> callable = () -> {
                    // Measured inside the task, around the acquire only: this is
                    // queueing time, and it is the difference between "the engine
                    // is slow" and "we were saturated". Without it, a duration
                    // under load silently includes both.
                    long queuedAt = System.nanoTime();
                    permits.acquire();
                    long runningAt = System.nanoTime();
                    startedAt.set(runningAt);
                    record(() -> metrics.recordConcurrencyWait(engineId,
                            Duration.ofNanos(runningAt - queuedAt)));
                    try {
                        return task.run(context);
                    } finally {
                        permits.release();
                    }
                };
                pending.add(new PendingTask(engineId, executor.submit(callable), startedAt));
            }

            for (PendingTask task : pending) {
                EngineExecution execution = await(task);
                // One call site for all four outcomes (completed, timed out,
                // threw, interrupted). Recording inside each branch of await()
                // would mean a future fifth branch could silently go unmeasured.
                record(() -> metrics.recordExecution(execution.engineId(),
                        execution.status(), execution.duration(), execution.timedOut()));
                executions.add(execution);
            }
        }

        return new ExecutionOutcome(executions);
    }

    /**
     * Waits for one task, bounded by its own budget measured from the moment
     * it started running.
     *
     * <p><strong>Why an absolute deadline rather than a relative wait.</strong>
     * Tasks are submitted all at once and then awaited in order, so a plain
     * {@code future.get(budget)} restarts the clock for every task: with six
     * engines on a five-second budget the batch could take thirty seconds
     * before returning, even though every engine ran concurrently and none was
     * individually over budget. That is how a request exceeds a caller's
     * timeout with no engine reporting one - the failure looks like nothing at
     * all from inside the harness. Anchoring on {@code startedAtNanos} makes
     * the batch cost roughly one budget, not the sum of them.
     *
     * <p>The anchor is the moment a concurrency permit was taken, not the
     * moment of submission. Those differ only when tasks outnumber
     * {@code maxConcurrency}, and using submission there would report
     * {@code ENGINE_TIMEOUT} for an engine that had not yet been allowed to
     * start - blaming an engine for the harness's queue, and recording that
     * lie in the metrics.
     */
    private EngineExecution await(PendingTask pending) {
        Duration budget = policy.timeoutFor(pending.engineId());
        long startedAt = pending.startedAtNanos().get();
        long remainingNanos = startedAt + budget.toNanos() - System.nanoTime();

        try {
            EngineResult<?> result =
                    pending.future().get(Math.max(0, remainingNanos), TimeUnit.NANOSECONDS);
            Duration elapsed = elapsedSince(pending.startedAtNanos().get());

            // A null return is a defect in that engine. Isolate it here rather
            // than letting a NullPointerException surface somewhere unrelated.
            if (result == null) {
                log.warn("Engine {} returned null; treating as recoverable failure.",
                        pending.engineId());
                return new EngineExecution(pending.engineId(),
                        EngineResult.failedRecoverable(EngineError.of(
                                "ENGINE_RETURNED_NULL",
                                "Engine returned no result object.",
                                pending.engineId())),
                        elapsed, false);
            }
            return new EngineExecution(pending.engineId(), result, elapsed, false);

        } catch (TimeoutException e) {
            // The task may only have acquired its permit while we were waiting,
            // in which case the deadline we just used was the queued one and
            // this engine has not actually had its budget yet. Re-anchor once
            // and wait again rather than reporting a timeout it did not earn.
            long anchorNow = pending.startedAtNanos().get();
            if (anchorNow != startedAt) {
                return await(pending);
            }
            // Cancel with interrupt so a cooperative engine can stop working.
            pending.future().cancel(true);
            Duration elapsed = elapsedSince(startedAt);
            log.warn("Engine {} timed out after {} ms (budget {} ms).",
                    pending.engineId(), elapsed.toMillis(), budget.toMillis());
            return new EngineExecution(pending.engineId(),
                    EngineResult.failedRecoverable(
                            EngineError.timeout(pending.engineId(), budget.toMillis())),
                    elapsed, true);

        } catch (ExecutionException e) {
            // The engine threw. Isolate it - Rule F requires that a fault in
            // one engine leaves the others untouched.
            Duration elapsed = elapsedSince(pending.startedAtNanos().get());
            Throwable cause = e.getCause() == null ? e : e.getCause();
            log.warn("Engine {} threw {}: {}", pending.engineId(),
                    cause.getClass().getSimpleName(), cause.getMessage());
            return new EngineExecution(pending.engineId(),
                    EngineResult.failedRecoverable(EngineError.of(
                            "ENGINE_EXCEPTION",
                            cause.getClass().getSimpleName()
                                    + (cause.getMessage() == null ? "" : ": " + cause.getMessage()),
                            pending.engineId())),
                    elapsed, false);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            pending.future().cancel(true);
            return new EngineExecution(pending.engineId(),
                    EngineResult.failedRecoverable(EngineError.of(
                            "ENGINE_INTERRUPTED",
                            "Execution was interrupted.",
                            pending.engineId())),
                    elapsedSince(pending.startedAtNanos().get()), false);
        }
    }

    private static Duration elapsedSince(long startNanos) {
        return Duration.ofNanos(System.nanoTime() - startNanos);
    }

    /**
     * Runs a metrics call and swallows anything it throws.
     *
     * <p>{@link EngineMetrics} already forbids implementations from throwing,
     * but a contract is not a guarantee, and the consequence of trusting it
     * here would be absurd: a misconfigured metrics backend turning a correct
     * calculation into a failed one. Measuring something is never more
     * important than doing it. Logged at debug because a metrics outage is
     * worth knowing about and not worth alarming about.
     */
    private static void record(Runnable metricCall) {
        try {
            metricCall.run();
        } catch (RuntimeException e) {
            log.debug("Metrics recording failed and was ignored: {}", e.toString());
        }
    }

    /**
     * @param startedAtNanos when this task actually began running, set by the
     *                       task itself the moment it takes a concurrency
     *                       permit. Mutable because the deadline in
     *                       {@link #await} must be measured from that instant
     *                       rather than from submission - see that method.
     */
    private record PendingTask(String engineId, Future<EngineResult<?>> future,
                               AtomicLong startedAtNanos) { }
}
