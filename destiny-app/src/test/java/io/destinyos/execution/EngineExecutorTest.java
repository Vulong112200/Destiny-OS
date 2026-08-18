package io.destinyos.execution;

import static org.assertj.core.api.Assertions.assertThat;

import io.destinyos.core.context.BirthTimePrecision;
import io.destinyos.core.context.CalculationContext;
import io.destinyos.core.result.EngineStatus;
import io.destinyos.core.version.MethodologyVersions;
import io.destinyos.testing.StubEngines;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The behavioural contract of the execution harness (PHASE_1_PLAN section 5).
 *
 * <p>These tests are the reason Phase 1 is worth doing carefully rather than
 * quickly: they pin down failure isolation before any real engine exists, so
 * every engine added later inherits a harness whose guarantees are already
 * proven rather than assumed.
 */
class EngineExecutorTest {

    private static CalculationContext context() {
        return new CalculationContext(
                "calc-test-1",
                "TEST_SCHOOL",
                new MethodologyVersions("1.0", "1.0", "1.0", "1.0"),
                ZoneId.of("Asia/Ho_Chi_Minh"),
                Locale.forLanguageTag("vi-VN"),
                null,
                Instant.parse("2026-08-18T00:00:00Z"),
                "VN_SOUTH",
                null,
                BirthTimePrecision.EXACT,
                List.of());
    }

    private static EngineExecutor fastTimeout() {
        return new EngineExecutor(new ExecutionPolicy(
                Duration.ofMillis(300), Map.of(), 16));
    }

    @Test
    @DisplayName("An empty batch yields NOT_APPLICABLE, not a failure")
    void emptyBatch() {
        var outcome = EngineExecutor.withDefaults().runAll(List.of(), context());

        assertThat(outcome.executions()).isEmpty();
        assertThat(outcome.overallStatus()).isEqualTo(EngineStatus.NOT_APPLICABLE);
        assertThat(outcome.isIncomplete()).isFalse();
    }

    @Test
    @DisplayName("All engines succeeding yields SUCCESS")
    void allSucceed() {
        var tasks = List.<EngineTask<?, ?>>of(
                EngineTask.of(StubEngines.succeeding("A"), "in"),
                EngineTask.of(StubEngines.succeeding("B"), "in"));

        var outcome = EngineExecutor.withDefaults().runAll(tasks, context());

        assertThat(outcome.overallStatus()).isEqualTo(EngineStatus.SUCCESS);
        assertThat(outcome.contributing()).hasSize(2);
        assertThat(outcome.isIncomplete()).isFalse();
    }

    @Test
    @DisplayName("A timeout is isolated: the batch completes as PARTIAL")
    void timeoutIsIsolated() {
        var started = new CountDownLatch(1);
        var tasks = List.<EngineTask<?, ?>>of(
                EngineTask.of(StubEngines.succeeding("FAST"), "in"),
                EngineTask.of(StubEngines.hanging("SLOW", started), "in"));

        var outcome = fastTimeout().runAll(tasks, context());

        assertThat(outcome.overallStatus()).isEqualTo(EngineStatus.PARTIAL);
        assertThat(outcome.forEngine("FAST").status()).isEqualTo(EngineStatus.SUCCESS);
        assertThat(outcome.forEngine("SLOW").status()).isEqualTo(EngineStatus.FAILED_RECOVERABLE);
        assertThat(outcome.forEngine("SLOW").timedOut()).isTrue();
        assertThat(outcome.timedOut()).hasSize(1);
    }

    @Test
    @DisplayName("An exception in one engine leaves the others untouched (Rule F)")
    void exceptionIsIsolated() {
        var tasks = List.<EngineTask<?, ?>>of(
                EngineTask.of(StubEngines.throwing("BOOM"), "in"),
                EngineTask.of(StubEngines.succeeding("OK1"), "in"),
                EngineTask.of(StubEngines.succeeding("OK2"), "in"));

        var outcome = EngineExecutor.withDefaults().runAll(tasks, context());

        assertThat(outcome.overallStatus()).isEqualTo(EngineStatus.PARTIAL);
        assertThat(outcome.forEngine("BOOM").status()).isEqualTo(EngineStatus.FAILED_RECOVERABLE);
        assertThat(outcome.contributing()).hasSize(2);
        assertThat(outcome.forEngine("BOOM").result().errors())
                .singleElement()
                .satisfies(err -> assertThat(err.code()).isEqualTo("ENGINE_EXCEPTION"));
    }

    @Test
    @DisplayName("Every engine failing still returns a result, never an exception")
    void allFailStillReturns() {
        var tasks = List.<EngineTask<?, ?>>of(
                EngineTask.of(StubEngines.throwing("X"), "in"),
                EngineTask.of(StubEngines.throwing("Y"), "in"));

        var outcome = EngineExecutor.withDefaults().runAll(tasks, context());

        assertThat(outcome.executions()).hasSize(2);
        assertThat(outcome.failed()).hasSize(2);
        assertThat(outcome.overallStatus()).isEqualTo(EngineStatus.PARTIAL);
    }

    @Test
    @DisplayName("A defective engine returning null is isolated, not propagated")
    void nullResultIsIsolated() {
        var tasks = List.<EngineTask<?, ?>>of(
                EngineTask.of(StubEngines.returningNull("NULLY"), "in"),
                EngineTask.of(StubEngines.succeeding("OK"), "in"));

        var outcome = EngineExecutor.withDefaults().runAll(tasks, context());

        assertThat(outcome.forEngine("NULLY").result().errors())
                .singleElement()
                .satisfies(err -> assertThat(err.code()).isEqualTo("ENGINE_RETURNED_NULL"));
        assertThat(outcome.forEngine("OK").status()).isEqualTo(EngineStatus.SUCCESS);
    }

    @Test
    @DisplayName("RESEARCH_REQUIRED propagates intact and is not a failure")
    void researchRequiredPropagates() {
        var tasks = List.<EngineTask<?, ?>>of(
                EngineTask.of(StubEngines.researchBlocked("BAZI", "R1"), "in"),
                EngineTask.of(StubEngines.succeeding("TAROT"), "in"));

        var outcome = EngineExecutor.withDefaults().runAll(tasks, context());

        var bazi = outcome.forEngine("BAZI");
        assertThat(bazi.status()).isEqualTo(EngineStatus.RESEARCH_REQUIRED);
        assertThat(bazi.result().researchReferenceIfPresent()).isPresent();
        assertThat(bazi.result().researchReference().researchId()).isEqualTo("R1");

        // An honest non-answer is not a malfunction, so the picture is not
        // "incomplete" - the batch simply has fewer contributors.
        assertThat(outcome.failed()).isEmpty();
        assertThat(outcome.isIncomplete()).isFalse();
        assertThat(outcome.honestNonAnswers()).hasSize(1);
        assertThat(outcome.overallStatus()).isEqualTo(EngineStatus.SUCCESS);
    }

    @Test
    @DisplayName("NOT_APPLICABLE never counts as a contributing engine")
    void notApplicableDoesNotContribute() {
        var tasks = List.<EngineTask<?, ?>>of(
                EngineTask.of(StubEngines.notApplicable("FENGSHUI"), "in"),
                EngineTask.of(StubEngines.succeeding("NUMEROLOGY"), "in"));

        var outcome = EngineExecutor.withDefaults().runAll(tasks, context());

        assertThat(outcome.contributing())
                .extracting(EngineExecution::engineId)
                .containsExactly("NUMEROLOGY");
        assertThat(outcome.forEngine("FENGSHUI").status()).isEqualTo(EngineStatus.NOT_APPLICABLE);
    }

    @Test
    @DisplayName("Concurrency is bounded by the policy")
    void concurrencyIsBounded() {
        var policy = new ExecutionPolicy(Duration.ofSeconds(5), Map.of(), 2);
        var executor = new EngineExecutor(policy);

        var tasks = List.<EngineTask<?, ?>>of(
                EngineTask.of(StubEngines.succeeding("A"), "in"),
                EngineTask.of(StubEngines.succeeding("B"), "in"),
                EngineTask.of(StubEngines.succeeding("C"), "in"),
                EngineTask.of(StubEngines.succeeding("D"), "in"));

        var outcome = executor.runAll(tasks, context());

        assertThat(outcome.executions()).hasSize(4);
        assertThat(outcome.overallStatus()).isEqualTo(EngineStatus.SUCCESS);
    }

    @Test
    @DisplayName("Per-engine timeout overrides the default")
    void perEngineTimeoutOverride() {
        var started = new CountDownLatch(1);
        var policy = new ExecutionPolicy(
                Duration.ofSeconds(10),
                Map.of("SLOW", Duration.ofMillis(200)),
                16);

        var tasks = List.<EngineTask<?, ?>>of(
                EngineTask.of(StubEngines.hanging("SLOW", started), "in"));

        var outcome = new EngineExecutor(policy).runAll(tasks, context());

        assertThat(outcome.forEngine("SLOW").timedOut()).isTrue();
        assertThat(outcome.forEngine("SLOW").duration()).isLessThan(Duration.ofSeconds(5));
    }
}
