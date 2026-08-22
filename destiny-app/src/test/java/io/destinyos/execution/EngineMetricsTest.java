package io.destinyos.execution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import io.destinyos.core.context.BirthTimePrecision;
import io.destinyos.core.context.CalculationContext;
import io.destinyos.core.result.EngineStatus;
import io.destinyos.core.version.MethodologyVersions;
import io.destinyos.testing.StubEngines;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Metrics are the fourth thing CLAUDE.md §5 requires of every engine, alongside
 * timeout, cancellation and error isolation — the three that
 * {@link EngineExecutorTest} already covers.
 *
 * <p>The emphasis here is on the paths that are easy to leave unmeasured. A
 * happy-path counter is the part anyone would remember to write; a timeout, a
 * thrown exception and an honest non-answer are the ones worth pinning, because
 * they are exactly the executions an operator most needs to see and the ones a
 * {@code return} in the wrong branch would silently drop.
 */
class EngineMetricsTest {

    /** Records what it was told, and nothing else. */
    private static final class RecordingMetrics implements EngineMetrics {
        record Execution(String engineId, EngineStatus status, Duration duration, boolean timedOut) { }

        final List<Execution> executions = new CopyOnWriteArrayList<>();
        final List<String> waitsByEngine = new CopyOnWriteArrayList<>();

        @Override
        public void recordExecution(String engineId, EngineStatus status, Duration duration,
                                    boolean timedOut) {
            executions.add(new Execution(engineId, status, duration, timedOut));
        }

        @Override
        public void recordConcurrencyWait(String engineId, Duration waited) {
            waitsByEngine.add(engineId);
        }
    }

    private static CalculationContext context() {
        return new CalculationContext("calc-metrics", "TEST",
                new MethodologyVersions("1.0", "1.0", "1.0", null),
                ZoneId.of("Asia/Ho_Chi_Minh"), null, null, Instant.EPOCH,
                null, null, BirthTimePrecision.EXACT, List.of());
    }

    private static EngineExecutor executor(RecordingMetrics metrics) {
        return new EngineExecutor(new ExecutionPolicy(Duration.ofMillis(200), Map.of(), 4), metrics);
    }

    @Nested
    @DisplayName("Every outcome is measured")
    class EveryOutcome {

        @Test
        @DisplayName("A successful engine is recorded with its status and a real duration")
        void successIsRecorded() {
            var metrics = new RecordingMetrics();
            executor(metrics).runAll(
                    List.of(EngineTask.of(StubEngines.succeeding("OK_ENGINE"), "input")),
                    context());

            assertThat(metrics.executions).hasSize(1);
            var recorded = metrics.executions.get(0);
            assertThat(recorded.engineId()).isEqualTo("OK_ENGINE");
            assertThat(recorded.status()).isEqualTo(EngineStatus.SUCCESS);
            assertThat(recorded.timedOut()).isFalse();
            assertThat(recorded.duration()).isPositive();
        }

        @Test
        @DisplayName("A timeout is recorded, and the timedOut flag distinguishes it from a throw")
        void timeoutIsRecordedAndDistinguishable() {
            // Both a timeout and a thrown exception surface as
            // FAILED_RECOVERABLE, so without the flag the one failure mode with
            // a known remedy would be invisible in the metrics.
            var metrics = new RecordingMetrics();
            executor(metrics).runAll(
                    List.of(EngineTask.of(
                            StubEngines.hanging("SLOW_ENGINE", new CountDownLatch(1)), "input")),
                    context());

            assertThat(metrics.executions).hasSize(1);
            var recorded = metrics.executions.get(0);
            assertThat(recorded.status()).isEqualTo(EngineStatus.FAILED_RECOVERABLE);
            assertThat(recorded.timedOut()).isTrue();
        }

        @Test
        @DisplayName("An engine that throws is recorded, not swallowed")
        void throwingEngineIsRecorded() {
            var metrics = new RecordingMetrics();
            executor(metrics).runAll(
                    List.of(EngineTask.of(StubEngines.throwing("BOOM_ENGINE"), "input")),
                    context());

            assertThat(metrics.executions).hasSize(1);
            assertThat(metrics.executions.get(0).status())
                    .isEqualTo(EngineStatus.FAILED_RECOVERABLE);
            assertThat(metrics.executions.get(0).timedOut()).isFalse();
        }

        @Test
        @DisplayName("An honest non-answer is recorded with its own status, not as a failure")
        void researchRequiredKeepsItsOwnStatus() {
            // This project expects a great many RESEARCH_REQUIRED results, so
            // recording the real status - rather than a boolean success flag - is
            // what keeps the failure rate meaningful.
            var declining = StubEngines.researchBlocked("BLOCKED_ENGINE", "R4");
            var metrics = new RecordingMetrics();

            executor(metrics).runAll(List.of(EngineTask.of(declining, "input")), context());

            assertThat(metrics.executions).hasSize(1);
            assertThat(metrics.executions.get(0).status())
                    .isEqualTo(EngineStatus.RESEARCH_REQUIRED);
            assertThat(metrics.executions.get(0).timedOut()).isFalse();
        }

        @Test
        @DisplayName("A mixed batch records one entry per engine, and isolation still holds")
        void mixedBatchRecordsEveryEngine() {
            var metrics = new RecordingMetrics();
            var outcome = executor(metrics).runAll(List.of(
                    EngineTask.of(StubEngines.succeeding("A"), "in"),
                    EngineTask.of(StubEngines.throwing("B"), "in"),
                    EngineTask.of(StubEngines.succeeding("C"), "in")),
                    context());

            assertThat(metrics.executions).hasSize(3);
            assertThat(metrics.executions).extracting(RecordingMetrics.Execution::engineId)
                    .containsExactlyInAnyOrder("A", "B", "C");
            // Rule F still holds - measuring did not change the outcome.
            assertThat(outcome.executions()).hasSize(3);
        }

        @Test
        @DisplayName("Concurrency wait is recorded for every engine, even when there is no queue")
        void concurrencyWaitIsAlwaysRecorded() {
            // Usually ~0, and that is the point: a series that only appears
            // under load is a series nobody has a baseline for.
            var metrics = new RecordingMetrics();
            executor(metrics).runAll(List.of(
                    EngineTask.of(StubEngines.succeeding("A"), "in"),
                    EngineTask.of(StubEngines.succeeding("B"), "in")),
                    context());

            assertThat(metrics.waitsByEngine).containsExactlyInAnyOrder("A", "B");
        }
    }

    @Nested
    @DisplayName("Measuring never breaks calculating")
    class Robustness {

        @Test
        @DisplayName("A metrics backend that throws cannot fail the calculation")
        void throwingMetricsAreSwallowed() {
            // The consequence of getting this wrong would be absurd: a
            // misconfigured metrics backend turning a correct calculation into a
            // failed one. EngineMetrics forbids implementations from throwing,
            // but a contract is not a guarantee.
            EngineMetrics hostile = new EngineMetrics() {
                @Override
                public void recordExecution(String engineId, EngineStatus status,
                                            Duration duration, boolean timedOut) {
                    throw new IllegalStateException("metrics backend is down");
                }

                @Override
                public void recordConcurrencyWait(String engineId, Duration waited) {
                    throw new IllegalStateException("metrics backend is down");
                }
            };

            var harness = new EngineExecutor(
                    new ExecutionPolicy(Duration.ofMillis(200), Map.of(), 4), hostile);

            var outcomes = new ArrayList<ExecutionOutcome>();
            assertThatCode(() -> outcomes.add(harness.runAll(
                    List.of(EngineTask.of(StubEngines.succeeding("OK"), "in")), context())))
                    .doesNotThrowAnyException();

            assertThat(outcomes).hasSize(1);
            assertThat(outcomes.get(0).executions()).hasSize(1);
            assertThat(outcomes.get(0).executions().get(0).status())
                    .isEqualTo(EngineStatus.SUCCESS);
        }

        @Test
        @DisplayName("The default harness records nothing and needs no backend")
        void noOpIsTheDefault() {
            // withDefaults() is what a unit test uses, and it must not require a
            // metrics registry to exist.
            assertThatCode(() -> EngineExecutor.withDefaults().runAll(
                    List.of(EngineTask.of(StubEngines.succeeding("OK"), "in")), context()))
                    .doesNotThrowAnyException();

            assertThatCode(() -> {
                EngineMetrics.NO_OP.recordExecution("X", EngineStatus.SUCCESS, Duration.ZERO, false);
                EngineMetrics.NO_OP.recordConcurrencyWait("X", Duration.ZERO);
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("A null metrics reference degrades to no-op rather than exploding later")
        void nullMetricsBecomesNoOp() {
            assertThatCode(() -> new EngineExecutor(ExecutionPolicy.defaults(), null)
                    .runAll(List.of(EngineTask.of(StubEngines.succeeding("OK"), "in")), context()))
                    .doesNotThrowAnyException();
        }
    }
}
