package io.destinyos.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.destinyos.api.dto.ScenarioContextRequest;
import io.destinyos.api.dto.ScenarioRunRequest;
import io.destinyos.api.dto.ScenarioRunResponse;
import io.destinyos.api.testing.StubEngine;
import io.destinyos.core.context.CalculationContext;
import io.destinyos.execution.EngineExecutor;
import io.destinyos.execution.EngineTask;
import io.destinyos.fusion.FusionEngine;
import io.destinyos.fusion.FusionResult;
import io.destinyos.persistence.calculation.CalculationEntity;
import io.destinyos.persistence.calculation.CalculationRecorder;
import io.destinyos.persistence.calculation.CalculationRequestContext;
import io.destinyos.scenario.ScenarioEngine;
import io.destinyos.scenario.ScenarioType;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Exercises the orchestration path end to end (task assembly -> ScenarioEngine
 * -> Fusion -> DTO mapping) using a {@link StubEngine} instead of a concrete
 * {@code destiny-engine-*} module — this service must never depend on one
 * (see {@link EngineTaskFactory}'s Javadoc), and a real signal-emitting stub
 * proves the DTO/label mapping paths that today's real engines don't yet
 * exercise (Tarot and Numerology currently emit evidence but no signals,
 * per research items R11/R8's content gap).
 *
 * <p>{@link CalculationRecorder} is mocked: it requires a live JPA/database
 * context, which is exactly what a persistence-layer test (already covered
 * in {@code destiny-persistence}) is for, not this orchestration test.
 */
class ScenarioOrchestrationServiceTest {

    private static final String STUB_ENGINE_ID = "TAROT";

    private final CalculationRecorder recorder = mock(CalculationRecorder.class);
    private final ScenarioEngine scenarioEngine = new ScenarioEngine(EngineExecutor.withDefaults(), new FusionEngine());

    @Test
    @DisplayName("A single supporting signal from one engine reaches the response as CONSENSUS_SUPPORT")
    void mapsSingleEngineRunToResponse() {
        Map<String, EngineTaskFactory> factories = new LinkedHashMap<>();
        factories.put(STUB_ENGINE_ID, request -> Optional.of(EngineTask.of(new StubEngine(STUB_ENGINE_ID), "input")));
        var registry = new EngineTaskFactoryRegistry(factories);

        var recordedCalculation = new CalculationEntity("calc-fixture-1", "input-hash",
                "1.0", "1.0", "1.0", "Asia/Ho_Chi_Minh", Instant.now());
        recordedCalculation.markCompleted(io.destinyos.core.result.EngineStatus.SUCCESS,
                "deadbeef", Instant.now());
        when(recorder.record(any(CalculationContext.class), any(), any(CalculationRequestContext.class),
                any(), any(FusionResult.class)))
                .thenReturn(recordedCalculation);

        var service = new ScenarioOrchestrationService(scenarioEngine, recorder, registry);
        ScenarioRunResponse response = service.run(ScenarioType.BUSINESS, new ScenarioRunRequest(null, null, null, null, null, null));

        assertThat(response.calculationId()).isEqualTo("calc-fixture-1");
        assertThat(response.resultHash()).isEqualTo("deadbeef");
        assertThat(response.policyDefined()).isTrue();
        assertThat(response.engines()).hasSize(1);
        assertThat(response.engines().get(0).status().technical()).isEqualTo("SUCCESS");
        assertThat(response.engines().get(0).status().labelVi()).isEqualTo("Thành công");

        assertThat(response.signals()).hasSize(1);
        var signal = response.signals().get(0);
        assertThat(signal.polarity().labelVi()).isEqualTo("Thuận lợi");
        assertThat(signal.dimension().labelVi()).isEqualTo("Quyết định");

        assertThat(response.fusion()).isNotNull();
        assertThat(response.fusion().overallOutcome().technical()).isEqualTo("CONSENSUS_SUPPORT");
        assertThat(response.fusion().overallOutcome().labelVi()).isEqualTo("Đồng thuận thuận lợi");
        assertThat(response.fusion().dimensions()).hasSize(1);
        assertThat(response.fusion().dimensions().get(0).supportingEngines()).containsExactly(STUB_ENGINE_ID);
    }

    @Test
    @DisplayName("A scenario with no defined applicability policy runs no engines and carries no fusion")
    void undefinedPolicyScenarioSkipsFusion() {
        var registry = new EngineTaskFactoryRegistry(Map.of());

        var recordedCalculation = new CalculationEntity("calc-fixture-2", "input-hash",
                "1.0", "1.0", "1.0", "Asia/Ho_Chi_Minh", Instant.now());
        recordedCalculation.markCompleted(io.destinyos.core.result.EngineStatus.NOT_APPLICABLE,
                "cafebabe", Instant.now());
        when(recorder.record(any(CalculationContext.class), any(), any(CalculationRequestContext.class),
                any(), any()))
                .thenReturn(recordedCalculation);

        var service = new ScenarioOrchestrationService(scenarioEngine, recorder, registry);
        // COMPATIBILITY, not CAREER: CAREER gained a real policy on
        // 2026-08-23 (docs/DECISION_LOG.md).
        ScenarioRunResponse response = service.run(ScenarioType.COMPATIBILITY, new ScenarioRunRequest(null, null, null, null, null, null));

        assertThat(response.policyDefined()).isFalse();
        assertThat(response.fusion()).isNull();
        assertThat(response.engines()).isEmpty();
    }

    @Test
    @DisplayName("The user's question and focus are recorded with the run and echoed back")
    void carriesTheRequestContextIntoPersistenceAndTheResponse() {
        // The gap this closes: the question used to be accepted, handed to
        // TarotDrawInput, read by nobody, and never written down. Asserting
        // both halves - what reaches CalculationRecorder and what reaches the
        // caller - because a question that is only echoed is still lost the
        // moment the user reloads the page.
        var service = serviceWith(new EngineTaskFactoryRegistry(Map.of()), "calc-ctx", "hash-ctx");

        var request = new ScenarioRunRequest(
                new ScenarioContextRequest("  Tôi có nên đổi việc không?  ", "doi-viec", "Đổi việc / nhảy việc"),
                null, null, null, null, null, null);
        ScenarioRunResponse response = service.run(ScenarioType.CAREER, request);

        assertThat(response.context().question())
                .as("trimmed by ScenarioContextRequest, and not re-trimmed differently downstream")
                .isEqualTo("Tôi có nên đổi việc không?");
        assertThat(response.context().focusId()).isEqualTo("doi-viec");
        assertThat(response.context().focusLabel()).isEqualTo("Đổi việc / nhảy việc");

        var captor = org.mockito.ArgumentCaptor.forClass(CalculationRequestContext.class);
        verify(recorder).record(any(CalculationContext.class), eq("CAREER"), captor.capture(),
                any(), any());
        assertThat(captor.getValue().question()).isEqualTo("Tôi có nên đổi việc không?");
        assertThat(captor.getValue().focusId()).isEqualTo("doi-viec");
        assertThat(captor.getValue().focusLabel()).isEqualTo("Đổi việc / nhảy việc");
    }

    @Test
    @DisplayName("A legacy caller's TarotRequest.question still works, and the context question wins over it")
    void tarotQuestionRemainsSupportedButLosesToTheRequestContext() {
        // Backward compatibility is the whole reason TarotRequest.question was
        // kept rather than removed: clients already in the field send it there.
        var service = serviceWith(new EngineTaskFactoryRegistry(Map.of()), "calc-legacy", "hash-legacy");

        var legacyOnly = new ScenarioRunRequest(null,
                new io.destinyos.api.dto.TarotRequest("PAST_PRESENT_FUTURE", 1L, "Câu hỏi cũ"),
                null, null, null, null);
        assertThat(service.run(ScenarioType.CAREER, legacyOnly).context().question())
                .isEqualTo("Câu hỏi cũ");

        var both = new ScenarioRunRequest(
                new ScenarioContextRequest("Câu hỏi mới", null, null),
                null, new io.destinyos.api.dto.TarotRequest("PAST_PRESENT_FUTURE", 1L, "Câu hỏi cũ"),
                null, null, null, null);
        assertThat(service.run(ScenarioType.CAREER, both).context().question())
                .as("the request-level context is authoritative")
                .isEqualTo("Câu hỏi mới");
    }

    @Test
    @DisplayName("The response states which dimensions the scenario declares, in a stable order")
    void exposesTheScenarioDeclaredDimensions() {
        // ScenarioDefinition.dimensions() was previously dead outside the AI
        // pruner, so a client had no way to tell what the scenario it just ran
        // is actually about. Order is asserted because the underlying Set's
        // iteration order is unspecified - an unstable response would make
        // this field useless for caching or diffing.
        var service = serviceWith(new EngineTaskFactoryRegistry(Map.of()), "calc-dim", "hash-dim");

        ScenarioRunResponse response = service.run(ScenarioType.CAREER,
                new ScenarioRunRequest(null, null, null, null, null, null));

        assertThat(response.dimensions()).extracting(io.destinyos.api.dto.LabeledValue::technical)
                .containsExactly("CAREER", "DECISION");
        assertThat(response.dimensions()).extracting(io.destinyos.api.dto.LabeledValue::labelVi)
                .containsExactly("Sự nghiệp", "Quyết định");
    }

    /** A service whose recorder returns a fixed, already-completed calculation row. */
    private ScenarioOrchestrationService serviceWith(EngineTaskFactoryRegistry registry,
                                                     String calculationId, String resultHash) {
        var recorded = new CalculationEntity(calculationId, "input-hash",
                "1.0", "1.0", "1.0", "Asia/Ho_Chi_Minh", Instant.now());
        recorded.markCompleted(io.destinyos.core.result.EngineStatus.SUCCESS, resultHash, Instant.now());
        when(recorder.record(any(CalculationContext.class), any(), any(CalculationRequestContext.class),
                any(), any())).thenReturn(recorded);
        return new ScenarioOrchestrationService(scenarioEngine, recorder, registry);
    }
}
