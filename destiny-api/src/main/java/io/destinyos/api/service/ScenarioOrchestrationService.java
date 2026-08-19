package io.destinyos.api.service;

import io.destinyos.api.dto.ConflictDto;
import io.destinyos.api.dto.DimensionResultDto;
import io.destinyos.api.dto.EngineOutcomeDto;
import io.destinyos.api.dto.EvidenceDto;
import io.destinyos.api.dto.FusionResultDto;
import io.destinyos.api.dto.LabeledValue;
import io.destinyos.api.dto.ScenarioRunRequest;
import io.destinyos.api.dto.ScenarioRunResponse;
import io.destinyos.api.dto.SignalDto;
import io.destinyos.core.context.BirthTimePrecision;
import io.destinyos.core.context.CalculationContext;
import io.destinyos.core.version.MethodologyVersions;
import io.destinyos.execution.EngineExecution;
import io.destinyos.execution.EngineTask;
import io.destinyos.fusion.Conflict;
import io.destinyos.fusion.DimensionAnalysis;
import io.destinyos.fusion.FusionResult;
import io.destinyos.i18n.VietnameseLabels;
import io.destinyos.persistence.calculation.CalculationRecorder;
import io.destinyos.scenario.ScenarioEngine;
import io.destinyos.scenario.ScenarioType;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Orchestrates one scenario request end to end: build engine tasks (via
 * {@link EngineTaskFactory}, never a concrete engine type directly), run
 * them through {@link ScenarioEngine}, persist the run through
 * {@link CalculationRecorder}, and map the result to the Vietnamese-labeled
 * response the Decision Center flow needs (UI_UX_VIETNAMESE_SPEC section 3).
 */
@Service
public class ScenarioOrchestrationService {

    private final ScenarioEngine scenarioEngine;
    private final CalculationRecorder recorder;
    private final EngineTaskFactoryRegistry taskFactories;

    public ScenarioOrchestrationService(ScenarioEngine scenarioEngine, CalculationRecorder recorder,
                                        EngineTaskFactoryRegistry taskFactories) {
        this.scenarioEngine = scenarioEngine;
        this.recorder = recorder;
        this.taskFactories = taskFactories;
    }

    public ScenarioRunResponse run(ScenarioType scenarioType, ScenarioRunRequest request) {
        Map<String, EngineTask<?, ?>> tasks = new java.util.LinkedHashMap<>();
        Long seed = null;
        for (Map.Entry<String, EngineTaskFactory> entry : taskFactories.all().entrySet()) {
            entry.getValue().createTask(request).ifPresent(task -> tasks.put(entry.getKey(), task));
        }
        if (request.tarot() != null) {
            seed = request.tarot().seed();
        }

        CalculationContext context = new CalculationContext(
                "calc-" + UUID.randomUUID(),
                null, // no single school applies across a whole scenario's engines (Rule D)
                new MethodologyVersions("1.0", "1.0", "1.0", null),
                ZoneId.of("Asia/Ho_Chi_Minh"),
                Locale.forLanguageTag("vi-VN"),
                seed,
                Instant.now(),
                null,
                null,
                BirthTimePrecision.UNKNOWN,
                List.of());

        var scenarioResult = scenarioEngine.run(scenarioType, tasks, context);
        var calculation = recorder.record(context, scenarioType.name(),
                scenarioResult.execution(), scenarioResult.fusion());

        List<EngineOutcomeDto> engineDtos = scenarioResult.execution().executions().stream()
                .map(this::toEngineOutcomeDto)
                .toList();

        List<EvidenceDto> evidenceDtos = scenarioResult.execution().flatMap(exec ->
                exec.result().evidence().stream().map(this::toEvidenceDto).toList());

        List<SignalDto> signalDtos = scenarioResult.execution().flatMap(exec ->
                exec.result().signals().stream().map(this::toSignalDto).toList());

        FusionResultDto fusionDto = scenarioResult.fusion() == null
                ? null : toFusionResultDto(scenarioResult.fusion());

        return new ScenarioRunResponse(
                calculation.calculationId(),
                scenarioType.name(),
                scenarioResult.policyDefined(),
                engineDtos,
                scenarioResult.unavailableEngines(),
                evidenceDtos,
                signalDtos,
                fusionDto,
                calculation.resultHash());
    }

    private EngineOutcomeDto toEngineOutcomeDto(EngineExecution exec) {
        return new EngineOutcomeDto(
                exec.engineId(),
                LabeledValue.of(exec.status(), VietnameseLabels.of(exec.status())),
                exec.timedOut(),
                exec.duration().toMillis());
    }

    private EvidenceDto toEvidenceDto(io.destinyos.core.evidence.Evidence evidence) {
        return new EvidenceDto(
                evidence.evidenceId(),
                evidence.engine(),
                evidence.school(),
                evidence.ruleId(),
                evidence.ruleVersion(),
                LabeledValue.ofNullable(evidence.dimension(), () -> VietnameseLabels.of(evidence.dimension())),
                evidence.fact(),
                evidence.source());
    }

    private SignalDto toSignalDto(io.destinyos.core.signal.Signal signal) {
        return new SignalDto(
                signal.signalId(),
                signal.engine(),
                signal.school(),
                LabeledValue.of(signal.dimension(), VietnameseLabels.of(signal.dimension())),
                signal.tag(),
                LabeledValue.of(signal.polarity(), VietnameseLabels.of(signal.polarity())),
                LabeledValue.of(signal.strength(), VietnameseLabels.of(signal.strength())),
                LabeledValue.of(signal.applicability(), VietnameseLabels.of(signal.applicability())),
                signal.critical(),
                signal.evidenceIds());
    }

    private FusionResultDto toFusionResultDto(FusionResult fusion) {
        List<DimensionResultDto> dimensionDtos = fusion.dimensions().stream()
                .map(this::toDimensionResultDto).toList();
        List<ConflictDto> conflictDtos = fusion.conflicts().stream()
                .map(this::toConflictDto).toList();

        return new FusionResultDto(
                LabeledValue.of(fusion.overallOutcome(), VietnameseLabels.of(fusion.overallOutcome())),
                dimensionDtos,
                conflictDtos,
                fusion.rulesApplied(),
                new ArrayList<>(fusion.supportingSources()),
                new ArrayList<>(fusion.cautionSources()));
    }

    private DimensionResultDto toDimensionResultDto(DimensionAnalysis analysis) {
        return new DimensionResultDto(
                LabeledValue.of(analysis.dimension(), VietnameseLabels.of(analysis.dimension())),
                LabeledValue.of(analysis.state(), VietnameseLabels.of(analysis.state())),
                new ArrayList<>(analysis.supportingEngines()),
                new ArrayList<>(analysis.cautionEngines()),
                new ArrayList<>(analysis.negativeEngines()),
                analysis.rulesApplied());
    }

    private ConflictDto toConflictDto(Conflict conflict) {
        return new ConflictDto(
                LabeledValue.of(conflict.type(), VietnameseLabels.of(conflict.type())),
                LabeledValue.ofNullable(conflict.dimension(), () -> VietnameseLabels.of(conflict.dimension())),
                conflict.involvedEngines(),
                conflict.description());
    }
}
