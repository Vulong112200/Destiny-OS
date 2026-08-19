package io.destinyos.api.dto;

import java.util.List;

/** Response form of {@link io.destinyos.fusion.DimensionAnalysis} (DECISION_LOG C5). */
public record DimensionResultDto(
        LabeledValue dimension,
        LabeledValue state,
        List<String> supportingEngines,
        List<String> cautionEngines,
        List<String> negativeEngines,
        List<String> rulesApplied
) {
}
