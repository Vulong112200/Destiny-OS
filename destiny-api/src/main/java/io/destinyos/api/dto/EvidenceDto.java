package io.destinyos.api.dto;

import java.util.Map;

/**
 * Response form of {@link io.destinyos.core.evidence.Evidence} — the
 * "Vì sao có kết quả này?" panel's raw material (UI_UX_VIETNAMESE_SPEC
 * section 7).
 */
public record EvidenceDto(
        String evidenceId,
        String engine,
        String school,
        String ruleId,
        String ruleVersion,
        LabeledValue dimension,
        Map<String, Object> fact,
        String source
) {
}
