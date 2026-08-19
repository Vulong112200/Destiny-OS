package io.destinyos.api.dto;

import java.util.List;

/**
 * Response form of {@link io.destinyos.fusion.FusionResult}
 * (FUSION_ENGINE_SPEC.md section 10 explainability contract). {@code null}
 * on the enclosing {@link ScenarioRunResponse} when the scenario has no
 * defined applicability policy — never fabricated to fill the gap
 * (ADR-equivalent decision recorded in {@code destiny-scenario}'s
 * {@code ScenarioDefinition#policyDefined()}).
 */
public record FusionResultDto(
        LabeledValue overallOutcome,
        List<DimensionResultDto> dimensions,
        List<ConflictDto> conflicts,
        List<String> rulesApplied,
        List<String> supportingSources,
        List<String> cautionSources
) {
}
