package io.destinyos.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.destinyos.api.dto.ScenarioRunResponse;
import io.destinyos.core.evidence.Evidence;
import io.destinyos.core.result.EngineStatus;
import io.destinyos.core.signal.Applicability;
import io.destinyos.core.signal.Dimension;
import io.destinyos.core.signal.Polarity;
import io.destinyos.core.signal.Signal;
import io.destinyos.core.signal.Strength;
import io.destinyos.fusion.Conflict;
import io.destinyos.fusion.ConflictType;
import io.destinyos.fusion.DimensionAnalysis;
import io.destinyos.fusion.DimensionState;
import io.destinyos.fusion.FusionOutcome;
import io.destinyos.fusion.FusionResult;
import io.destinyos.persistence.calculation.CalculationEngineResultEntity;
import io.destinyos.persistence.calculation.CalculationEngineResultRepository;
import io.destinyos.persistence.calculation.CalculationEntity;
import io.destinyos.persistence.calculation.CalculationRepository;
import io.destinyos.persistence.calculation.ConflictEntity;
import io.destinyos.persistence.calculation.ConflictRepository;
import io.destinyos.persistence.calculation.EvidenceEntity;
import io.destinyos.persistence.calculation.EvidenceRepository;
import io.destinyos.persistence.calculation.FusionResultEntity;
import io.destinyos.persistence.calculation.FusionResultRepository;
import io.destinyos.persistence.calculation.SignalEntity;
import io.destinyos.persistence.calculation.SignalRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Verifies that reading a past calculation back rebuilds exactly the same
 * explainability shape {@link ScenarioOrchestrationService} produces at run
 * time — CLAUDE.md section 6's reproducibility applies to reading a result
 * back, not only to recomputing one.
 */
class CalculationQueryServiceTest {

    private static final String CALC_ID = "calc-123";

    private final CalculationRepository calculations = mock(CalculationRepository.class);
    private final CalculationEngineResultRepository engineResults = mock(CalculationEngineResultRepository.class);
    private final EvidenceRepository evidenceRepo = mock(EvidenceRepository.class);
    private final SignalRepository signalRepo = mock(SignalRepository.class);
    private final FusionResultRepository fusionResultRepo = mock(FusionResultRepository.class);
    private final ConflictRepository conflictRepo = mock(ConflictRepository.class);

    private final CalculationQueryService service = new CalculationQueryService(
            calculations, engineResults, evidenceRepo, signalRepo, fusionResultRepo, conflictRepo);

    @Test
    @DisplayName("find() returns empty for an unknown calculation id, never throws")
    void findReturnsEmptyForUnknownId() {
        when(calculations.findById("missing")).thenReturn(Optional.empty());

        assertThat(service.find("missing")).isEmpty();
    }

    @Test
    @DisplayName("A persisted calculation round-trips its evidence, signal, fusion and conflict rows")
    void rebuildsFullExplainabilityRecord() {
        CalculationEntity calculation = new CalculationEntity(CALC_ID, "input-hash",
                "1.0", "1.0", "1.0", "Asia/Ho_Chi_Minh", Instant.now());
        calculation.setScenarioId("BUSINESS");
        calculation.markCompleted(EngineStatus.SUCCESS, "deadbeef", Instant.now());
        when(calculations.findById(CALC_ID)).thenReturn(Optional.of(calculation));

        var engineResult = new CalculationEngineResultEntity(CALC_ID, "TAROT", EngineStatus.SUCCESS,
                null, 42L, false);
        when(engineResults.findByCalculationId(CALC_ID)).thenReturn(List.of(engineResult));

        Evidence evidence = new Evidence("ev-1", "TAROT", "TAROT_RWS", "RULE_DRAW", "1.0",
                Dimension.DECISION, Map.of("card", "The Fool"), "deterministic-draw", null, null);
        when(evidenceRepo.findByCalculationId(CALC_ID))
                .thenReturn(List.of(new EvidenceEntity("ev-1", CALC_ID, evidence)));

        Signal signal = new Signal("sig-1", "TAROT", "TAROT_RWS", Dimension.DECISION, "DECISION_SUPPORT",
                Polarity.SUPPORT, Strength.MEDIUM, Applicability.HIGH, false, List.of("ev-1"), null);
        when(signalRepo.findByCalculationId(CALC_ID))
                .thenReturn(List.of(new SignalEntity(CALC_ID, signal)));

        DimensionAnalysis analysis = new DimensionAnalysis(Dimension.DECISION, DimensionState.POSITIVE,
                Set.of("TAROT"), Set.of(), Set.of(), Set.of(), List.of(), List.of("R1"));
        FusionResult fusion = new FusionResult(FusionOutcome.CONSENSUS_SUPPORT, List.of(analysis),
                List.of(), List.of(), List.of("R1"), Set.of("TAROT"), Set.of());
        when(fusionResultRepo.findByCalculationId(CALC_ID))
                .thenReturn(Optional.of(new FusionResultEntity(CALC_ID, fusion)));

        Conflict conflict = new Conflict(ConflictType.METHODOLOGY_CONFLICT, Dimension.DECISION,
                List.of("TAROT", "BAZI"), "Trường phái khác biệt.");
        when(conflictRepo.findByCalculationId(CALC_ID))
                .thenReturn(List.of(new ConflictEntity(CALC_ID, conflict)));

        ScenarioRunResponse response = service.find(CALC_ID).orElseThrow();

        assertThat(response.calculationId()).isEqualTo(CALC_ID);
        assertThat(response.scenarioId()).isEqualTo("BUSINESS");
        assertThat(response.resultHash()).isEqualTo("deadbeef");

        assertThat(response.engines()).hasSize(1);
        assertThat(response.engines().get(0).status().labelVi()).isEqualTo("Thành công");

        assertThat(response.evidence()).hasSize(1);
        assertThat(response.evidence().get(0).dimension().labelVi()).isEqualTo("Quyết định");

        assertThat(response.signals()).hasSize(1);
        assertThat(response.signals().get(0).polarity().labelVi()).isEqualTo("Thuận lợi");

        assertThat(response.fusion()).isNotNull();
        assertThat(response.fusion().overallOutcome().labelVi()).isEqualTo("Đồng thuận thuận lợi");
        assertThat(response.fusion().dimensions()).hasSize(1);
        assertThat(response.fusion().dimensions().get(0).state().labelVi()).isEqualTo("Thuận lợi");

        assertThat(response.fusion().conflicts()).hasSize(1);
        assertThat(response.fusion().conflicts().get(0).type().labelVi())
                .isEqualTo("Khác biệt giữa các trường phái");
    }
}
