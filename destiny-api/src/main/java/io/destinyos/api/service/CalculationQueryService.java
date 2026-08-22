package io.destinyos.api.service;

import io.destinyos.api.dto.ConflictDto;
import io.destinyos.api.dto.DimensionResultDto;
import io.destinyos.api.dto.EngineOutcomeDto;
import io.destinyos.api.dto.EvidenceDto;
import io.destinyos.api.dto.FusionResultDto;
import io.destinyos.api.dto.LabeledValue;
import io.destinyos.api.dto.ScenarioRunResponse;
import io.destinyos.api.dto.SignalDto;
import io.destinyos.core.signal.Dimension;
import io.destinyos.i18n.VietnameseLabels;
import io.destinyos.persistence.calculation.CalculationEngineResultEntity;
import io.destinyos.persistence.calculation.CalculationEngineResultRepository;
import io.destinyos.persistence.calculation.CalculationRepository;
import io.destinyos.persistence.calculation.ConflictEntity;
import io.destinyos.persistence.calculation.ConflictRepository;
import io.destinyos.persistence.calculation.DimensionAnalysisSnapshot;
import io.destinyos.persistence.calculation.EvidenceEntity;
import io.destinyos.persistence.calculation.EvidenceRepository;
import io.destinyos.persistence.calculation.FusionResultEntity;
import io.destinyos.persistence.calculation.FusionResultRepository;
import io.destinyos.persistence.calculation.SignalEntity;
import io.destinyos.persistence.calculation.SignalRepository;
import io.destinyos.fusion.DimensionState;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Read side for {@code GET /api/v1/calculations/{id}} — rebuilds the same
 * explainability shape {@link ScenarioOrchestrationService} returns at run
 * time, but from what was actually persisted (V4-V6), so a past result is
 * exactly as reproducible to *read back* as CLAUDE.md section 6 requires it
 * to be to *recompute*.
 */
@Service
public class CalculationQueryService {

    private final CalculationRepository calculations;
    private final CalculationEngineResultRepository engineResults;
    private final EvidenceRepository evidenceRepo;
    private final SignalRepository signalRepo;
    private final FusionResultRepository fusionResultRepo;
    private final ConflictRepository conflictRepo;

    public CalculationQueryService(CalculationRepository calculations,
                                   CalculationEngineResultRepository engineResults,
                                   EvidenceRepository evidenceRepo, SignalRepository signalRepo,
                                   FusionResultRepository fusionResultRepo,
                                   ConflictRepository conflictRepo) {
        this.calculations = calculations;
        this.engineResults = engineResults;
        this.evidenceRepo = evidenceRepo;
        this.signalRepo = signalRepo;
        this.fusionResultRepo = fusionResultRepo;
        this.conflictRepo = conflictRepo;
    }

    public Optional<ScenarioRunResponse> find(String calculationId) {
        return calculations.findById(calculationId).map(calculation -> {
            List<EngineOutcomeDto> engineDtos = engineResults.findByCalculationId(calculationId).stream()
                    .map(this::toEngineOutcomeDto).toList();
            List<EvidenceDto> evidenceDtos = evidenceRepo.findByCalculationId(calculationId).stream()
                    .map(this::toEvidenceDto).toList();
            List<SignalDto> signalDtos = signalRepo.findByCalculationId(calculationId).stream()
                    .map(this::toSignalDto).toList();
            FusionResultDto fusionDto = fusionResultRepo.findByCalculationId(calculationId)
                    .map(fr -> toFusionResultDto(fr, conflictRepo.findByCalculationId(calculationId)))
                    .orElse(null);

            return new ScenarioRunResponse(
                    calculation.calculationId(),
                    calculation.scenarioId(),
                    fusionDto != null,
                    engineDtos,
                    List.of(), // not persisted separately; only relevant at run time
                    evidenceDtos,
                    signalDtos,
                    fusionDto,
                    calculation.resultHash(),
                    RetentionDtoMapper.toDto(calculation));
        });
    }

    private EngineOutcomeDto toEngineOutcomeDto(CalculationEngineResultEntity entity) {
        return new EngineOutcomeDto(
                entity.engine(),
                LabeledValue.of(entity.status(), VietnameseLabels.of(entity.status())),
                entity.timedOut(),
                entity.durationMs() == null ? 0 : entity.durationMs());
    }

    private EvidenceDto toEvidenceDto(EvidenceEntity entity) {
        return new EvidenceDto(
                entity.evidenceId(),
                entity.engine(),
                entity.school(),
                entity.ruleId(),
                entity.ruleVersion(),
                LabeledValue.ofNullable(entity.dimension(), () -> VietnameseLabels.of(entity.dimension())),
                entity.fact(),
                entity.source());
    }

    private SignalDto toSignalDto(SignalEntity entity) {
        return new SignalDto(
                entity.signalId(),
                entity.engine(),
                entity.school(),
                LabeledValue.of(entity.dimension(), VietnameseLabels.of(entity.dimension())),
                entity.tag(),
                LabeledValue.of(entity.polarity(), VietnameseLabels.of(entity.polarity())),
                LabeledValue.of(entity.strength(), VietnameseLabels.of(entity.strength())),
                LabeledValue.of(entity.applicability(), VietnameseLabels.of(entity.applicability())),
                entity.critical(),
                List.copyOf(entity.evidenceIds()));
    }

    private FusionResultDto toFusionResultDto(FusionResultEntity entity, List<ConflictEntity> conflictEntities) {
        List<DimensionResultDto> dimensionDtos = entity.dimensions().stream()
                .map(this::toDimensionResultDto).toList();
        List<ConflictDto> conflictDtos = conflictEntities.stream().map(this::toConflictDto).toList();

        return new FusionResultDto(
                LabeledValue.of(entity.overallOutcome(), VietnameseLabels.of(entity.overallOutcome())),
                dimensionDtos,
                conflictDtos,
                entity.rulesApplied(),
                List.copyOf(entity.supportingSources()),
                List.copyOf(entity.cautionSources()));
    }

    private DimensionResultDto toDimensionResultDto(DimensionAnalysisSnapshot snapshot) {
        Dimension dimension = Dimension.valueOf(snapshot.dimension());
        DimensionState state = DimensionState.valueOf(snapshot.state());

        return new DimensionResultDto(
                LabeledValue.of(dimension, VietnameseLabels.of(dimension)),
                LabeledValue.of(state, VietnameseLabels.of(state)),
                List.copyOf(snapshot.supportingEngines()),
                List.copyOf(snapshot.cautionEngines()),
                List.copyOf(snapshot.negativeEngines()),
                snapshot.rulesApplied());
    }

    private ConflictDto toConflictDto(ConflictEntity entity) {
        return new ConflictDto(
                LabeledValue.of(entity.type(), VietnameseLabels.of(entity.type())),
                LabeledValue.ofNullable(entity.dimension(), () -> VietnameseLabels.of(entity.dimension())),
                entity.involvedEngines(),
                entity.description());
    }
}
