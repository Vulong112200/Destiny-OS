package io.destinyos.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        when(recorder.record(any(CalculationContext.class), any(), any(), any(FusionResult.class)))
                .thenReturn(recordedCalculation);

        var service = new ScenarioOrchestrationService(scenarioEngine, recorder, registry);
        ScenarioRunResponse response = service.run(ScenarioType.BUSINESS, new ScenarioRunRequest(null, null, null, null));

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
        when(recorder.record(any(CalculationContext.class), any(), any(), any()))
                .thenReturn(recordedCalculation);

        var service = new ScenarioOrchestrationService(scenarioEngine, recorder, registry);
        ScenarioRunResponse response = service.run(ScenarioType.CAREER, new ScenarioRunRequest(null, null, null, null));

        assertThat(response.policyDefined()).isFalse();
        assertThat(response.fusion()).isNull();
        assertThat(response.engines()).isEmpty();
    }
}
