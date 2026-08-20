package io.destinyos.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.destinyos.ai.FallbackReason;
import io.destinyos.ai.NarrativeInput;
import io.destinyos.ai.NarrativeResponse;
import io.destinyos.ai.NarrativeResult;
import io.destinyos.ai.NarrativeService;
import io.destinyos.api.dto.NarrativeResponseDto;
import io.destinyos.core.signal.Dimension;
import io.destinyos.core.signal.Polarity;
import io.destinyos.core.signal.Strength;
import io.destinyos.fusion.ConflictType;
import io.destinyos.fusion.FusionOutcome;
import io.destinyos.persistence.calculation.CalculationEntity;
import io.destinyos.persistence.calculation.CalculationRepository;
import io.destinyos.persistence.calculation.ConflictEntity;
import io.destinyos.persistence.calculation.ConflictRepository;
import io.destinyos.persistence.calculation.FusionResultEntity;
import io.destinyos.persistence.calculation.FusionResultRepository;
import io.destinyos.persistence.calculation.SignalEntity;
import io.destinyos.persistence.calculation.SignalRepository;
import io.destinyos.persistence.narrative.NarrativeEntity;
import io.destinyos.persistence.narrative.NarrativeRecorder;
import io.destinyos.core.signal.Signal;
import io.destinyos.fusion.Conflict;
import io.destinyos.fusion.FusionResult;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * ADR D8's contract at the API boundary: the request path builds a
 * {@link NarrativeInput} from persisted V4-V6 data and always gets a
 * renderable {@link NarrativeResponseDto} back, and an unknown calculation
 * id is reported honestly rather than crashing the mapping layer.
 */
class NarrativeOrchestrationServiceTest {

    private final CalculationRepository calculations = mock(CalculationRepository.class);
    private final SignalRepository signalRepo = mock(SignalRepository.class);
    private final FusionResultRepository fusionResultRepo = mock(FusionResultRepository.class);
    private final ConflictRepository conflictRepo = mock(ConflictRepository.class);
    private final NarrativeService narrativeService = mock(NarrativeService.class);
    private final NarrativeRecorder narrativeRecorder = mock(NarrativeRecorder.class);

    private final NarrativeOrchestrationService service = new NarrativeOrchestrationService(
            calculations, signalRepo, fusionResultRepo, conflictRepo, narrativeService, narrativeRecorder);

    private static CalculationEntity calculation(String id, String scenarioId) {
        var entity = new CalculationEntity(id, "hash", "1.0", "1.0", "1.0", "Asia/Ho_Chi_Minh", Instant.now());
        entity.setScenarioId(scenarioId);
        return entity;
    }

    @Test
    @DisplayName("generate() returns empty for an unknown calculation id, never calls the AI stage")
    void generateReturnsEmptyForUnknownCalculation() {
        when(calculations.findById("no-such-id")).thenReturn(Optional.empty());

        Optional<NarrativeResponseDto> result = service.generate("no-such-id");

        assertThat(result).isEmpty();
        org.mockito.Mockito.verifyNoInteractions(narrativeService);
    }

    @Test
    @DisplayName("generate() builds NarrativeInput from persisted signals/conflicts/fusion, then persists the result")
    void generateBuildsInputFromPersistedDataAndRecords() {
        String calculationId = "calc-1";
        when(calculations.findById(calculationId)).thenReturn(Optional.of(calculation(calculationId, "BUSINESS")));

        var signal = new SignalEntity(calculationId, new Signal("s1", "TAROT", null, Dimension.CAREER, "tag",
                Polarity.SUPPORT, Strength.STRONG, io.destinyos.core.signal.Applicability.HIGH, true, List.of(), null));
        when(signalRepo.findByCalculationId(calculationId)).thenReturn(List.of(signal));

        var conflict = new ConflictEntity(calculationId,
                new Conflict(ConflictType.DIRECT_CONFLICT, Dimension.CAREER, List.of("TAROT", "BAZI"), "mo ta"));
        when(conflictRepo.findByCalculationId(calculationId)).thenReturn(List.of(conflict));

        var fusionResult = new FusionResult(FusionOutcome.MAJOR_CONFLICT, List.of(), List.of(), List.of(),
                List.of(), Set.of(), Set.of());
        var fusionEntity = new FusionResultEntity(calculationId, fusionResult);
        when(fusionResultRepo.findByCalculationId(calculationId)).thenReturn(Optional.of(fusionEntity));

        var response = new NarrativeResponse("Tom tat", List.of(), List.of(), List.of(), List.of());
        var aiResult = NarrativeResult.aiGenerated(response, "openrouter", "model-x");
        when(narrativeService.generate(any(NarrativeInput.class))).thenReturn(aiResult);

        Optional<NarrativeResponseDto> result = service.generate(calculationId);

        assertThat(result).isPresent();
        assertThat(result.get().summary()).isEqualTo("Tom tat");
        assertThat(result.get().source().technical()).isEqualTo("AI_GENERATED");
        assertThat(result.get().providerName()).isEqualTo("openrouter");

        verify(narrativeRecorder).record(eq(calculationId), eq(aiResult));

        org.mockito.ArgumentCaptor<NarrativeInput> captor = org.mockito.ArgumentCaptor.forClass(NarrativeInput.class);
        verify(narrativeService).generate(captor.capture());
        NarrativeInput input = captor.getValue();
        assertThat(input.scenarioNameVi()).isEqualTo("Mở rộng kinh doanh");
        assertThat(input.signals()).hasSize(1);
        assertThat(input.signals().get(0).critical()).isTrue();
        assertThat(input.conflicts()).hasSize(1);
    }

    @Test
    @DisplayName("An unrecognized or missing scenarioId falls back to a generic name, not an exception")
    void unrecognizedScenarioIdFallsBackToGenericName() {
        String calculationId = "calc-2";
        when(calculations.findById(calculationId))
                .thenReturn(Optional.of(calculation(calculationId, "NOT_A_REAL_SCENARIO_TYPE")));
        when(signalRepo.findByCalculationId(calculationId)).thenReturn(List.of());
        when(conflictRepo.findByCalculationId(calculationId)).thenReturn(List.of());
        when(fusionResultRepo.findByCalculationId(calculationId)).thenReturn(Optional.empty());
        var response = new NarrativeResponse("x", List.of(), List.of(), List.of(), List.of());
        when(narrativeService.generate(any())).thenReturn(NarrativeResult.fallback(response, FallbackReason.AI_DISABLED));

        service.generate(calculationId);

        org.mockito.ArgumentCaptor<NarrativeInput> captor = org.mockito.ArgumentCaptor.forClass(NarrativeInput.class);
        verify(narrativeService).generate(captor.capture());
        assertThat(captor.getValue().scenarioNameVi()).isEqualTo("Kết quả tính toán");
    }

    @Test
    @DisplayName("find() reads back a persisted narrative without calling the AI stage")
    void findReadsPersistedNarrativeWithoutRegenerating() {
        var response = new NarrativeResponse("Da luu", List.of("a"), List.of(), List.of(), List.of());
        var stored = new NarrativeEntity("calc-3", NarrativeResult.aiGenerated(response, "openrouter", "m"));
        when(narrativeRecorder.find("calc-3")).thenReturn(Optional.of(stored));

        Optional<NarrativeResponseDto> result = service.find("calc-3");

        assertThat(result).isPresent();
        assertThat(result.get().summary()).isEqualTo("Da luu");
        org.mockito.Mockito.verifyNoInteractions(narrativeService);
    }

    @Test
    @DisplayName("find() returns empty when no narrative has been generated yet")
    void findReturnsEmptyWhenNoneGenerated() {
        when(narrativeRecorder.find("calc-4")).thenReturn(Optional.empty());

        assertThat(service.find("calc-4")).isEmpty();
    }
}
