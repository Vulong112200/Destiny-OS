package io.destinyos.api.service;

import io.destinyos.ai.NarrativeConflictItem;
import io.destinyos.ai.NarrativeInput;
import io.destinyos.ai.NarrativeResponse;
import io.destinyos.ai.NarrativeResult;
import io.destinyos.ai.NarrativeService;
import io.destinyos.ai.NarrativeSignalItem;
import io.destinyos.api.dto.LabeledValue;
import io.destinyos.api.dto.NarrativeResponseDto;
import io.destinyos.core.signal.Dimension;
import io.destinyos.fusion.DimensionState;
import io.destinyos.i18n.VietnameseLabels;
import io.destinyos.persistence.calculation.CalculationRepository;
import io.destinyos.persistence.calculation.ConflictEntity;
import io.destinyos.persistence.calculation.ConflictRepository;
import io.destinyos.persistence.calculation.DimensionAnalysisSnapshot;
import io.destinyos.persistence.calculation.FusionResultEntity;
import io.destinyos.persistence.calculation.FusionResultRepository;
import io.destinyos.persistence.calculation.SignalEntity;
import io.destinyos.persistence.calculation.SignalRepository;
import io.destinyos.persistence.narrative.NarrativeEntity;
import io.destinyos.persistence.narrative.NarrativeRecorder;
import io.destinyos.scenario.ScenarioDefinition;
import io.destinyos.scenario.ScenarioRegistry;
import io.destinyos.scenario.ScenarioType;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * Bridges the persisted calculation record to {@code destiny-ai}'s
 * {@link NarrativeService} (Phase 12, ADR D8): builds a {@link NarrativeInput}
 * from what {@code destiny-persistence} (V4-V6) already has, calls the AI
 * narrative stage, records the result (V7), and returns a Vietnamese-labeled
 * DTO. This is the one place allowed to know both the persistence entities
 * and the {@code destiny-ai} contract - neither module depends on the other.
 *
 * <p>Reads {@code SignalEntity}/{@code ConflictEntity}/{@code FusionResultEntity}
 * directly (not through {@link CalculationQueryService}'s already-labeled
 * DTOs) specifically to keep the real {@code Dimension}/{@code Polarity}/
 * {@code Strength} enums {@link io.destinyos.ai.NarrativePruner} needs for its
 * priority rules, rather than round-tripping them through
 * {@code LabeledValue.technical()}.
 */
@Service
public class NarrativeOrchestrationService {

    private final CalculationRepository calculations;
    private final SignalRepository signalRepo;
    private final FusionResultRepository fusionResultRepo;
    private final ConflictRepository conflictRepo;
    private final NarrativeService narrativeService;
    private final NarrativeRecorder narrativeRecorder;

    public NarrativeOrchestrationService(CalculationRepository calculations, SignalRepository signalRepo,
            FusionResultRepository fusionResultRepo, ConflictRepository conflictRepo,
            NarrativeService narrativeService, NarrativeRecorder narrativeRecorder) {
        this.calculations = calculations;
        this.signalRepo = signalRepo;
        this.fusionResultRepo = fusionResultRepo;
        this.conflictRepo = conflictRepo;
        this.narrativeService = narrativeService;
        this.narrativeRecorder = narrativeRecorder;
    }

    /** Generates (or regenerates) a narrative for an existing calculation and persists it. */
    public Optional<NarrativeResponseDto> generate(String calculationId) {
        return calculations.findById(calculationId).map(calculation -> {
            NarrativeInput input = buildInput(calculationId, calculation.scenarioId());
            NarrativeResult result = narrativeService.generate(input);
            narrativeRecorder.record(calculationId, result);
            return toDto(calculationId, result);
        });
    }

    /** Returns the last-generated narrative for a calculation, if one exists. */
    public Optional<NarrativeResponseDto> find(String calculationId) {
        return narrativeRecorder.find(calculationId).map(entity -> toDto(calculationId, entity));
    }

    private NarrativeInput buildInput(String calculationId, String scenarioId) {
        ScenarioDefinition definition = resolveScenarioDefinition(scenarioId);
        String scenarioNameVi = definition != null ? definition.displayNameVi() : "Kết quả tính toán";
        Set<Dimension> relevantDimensions = definition != null ? definition.dimensions() : Set.of();

        List<NarrativeSignalItem> signals = signalRepo.findByCalculationId(calculationId).stream()
                .map(this::toNarrativeSignalItem)
                .toList();
        List<NarrativeConflictItem> conflicts = conflictRepo.findByCalculationId(calculationId).stream()
                .map(this::toNarrativeConflictItem)
                .toList();
        Map<String, Object> hardDataSummary = fusionResultRepo.findByCalculationId(calculationId)
                .map(this::toHardDataSummary)
                .orElse(Map.of());

        // Uncertainty (CalculationContext.uncertainties()) is not persisted
        // anywhere by CalculationRecorder (V4-V6) - a pre-existing gap this
        // service does not paper over. warnings/limitations are honestly
        // empty rather than fabricated until that gap is closed.
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("calculationId", calculationId);
        if (scenarioId != null) {
            metadata.put("scenarioId", scenarioId);
        }

        return new NarrativeInput(scenarioNameVi, relevantDimensions, hardDataSummary, signals, conflicts,
                List.of(), List.of(), metadata);
    }

    private ScenarioDefinition resolveScenarioDefinition(String scenarioId) {
        if (scenarioId == null) {
            return null;
        }
        try {
            return ScenarioRegistry.get(ScenarioType.valueOf(scenarioId));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private NarrativeSignalItem toNarrativeSignalItem(SignalEntity entity) {
        return new NarrativeSignalItem(
                entity.engine(),
                entity.dimension(), VietnameseLabels.of(entity.dimension()),
                entity.polarity(), VietnameseLabels.of(entity.polarity()),
                entity.strength(), VietnameseLabels.of(entity.strength()),
                entity.critical(),
                entity.tag());
    }

    private NarrativeConflictItem toNarrativeConflictItem(ConflictEntity entity) {
        return new NarrativeConflictItem(
                VietnameseLabels.of(entity.type()),
                entity.dimension() == null ? null : VietnameseLabels.of(entity.dimension()),
                entity.involvedEngines(),
                entity.description());
    }

    private Map<String, Object> toHardDataSummary(FusionResultEntity entity) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("overallOutcome", VietnameseLabels.of(entity.overallOutcome()));

        Map<String, String> dimensionStates = new LinkedHashMap<>();
        for (DimensionAnalysisSnapshot snapshot : entity.dimensions()) {
            Dimension dimension = Dimension.valueOf(snapshot.dimension());
            DimensionState state = DimensionState.valueOf(snapshot.state());
            dimensionStates.put(VietnameseLabels.of(dimension), VietnameseLabels.of(state));
        }
        summary.put("dimensionStates", dimensionStates);
        return summary;
    }

    private NarrativeResponseDto toDto(String calculationId, NarrativeResult result) {
        NarrativeResponse response = result.response();
        return new NarrativeResponseDto(
                calculationId,
                LabeledValue.of(result.source(), VietnameseLabels.of(result.source())),
                LabeledValue.of(result.fallbackReason(), VietnameseLabels.of(result.fallbackReason())),
                response.summary(),
                response.keySignals(),
                response.conflicts(),
                response.cautions(),
                response.reflectionQuestions(),
                result.providerName(),
                result.model(),
                null);
    }

    private NarrativeResponseDto toDto(String calculationId, NarrativeEntity entity) {
        return new NarrativeResponseDto(
                calculationId,
                LabeledValue.of(entity.source(), VietnameseLabels.of(entity.source())),
                LabeledValue.of(entity.fallbackReason(), VietnameseLabels.of(entity.fallbackReason())),
                entity.summary(),
                entity.keySignals(),
                entity.conflicts(),
                entity.cautions(),
                entity.reflectionQuestions(),
                entity.providerName(),
                entity.model(),
                entity.generatedAt() == null ? null : entity.generatedAt().toString());
    }
}
