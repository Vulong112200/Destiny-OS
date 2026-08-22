package io.destinyos.api.dto;

import java.util.List;

/**
 * Response body for {@code POST /api/v1/scenarios/{scenarioType}} — the
 * full explainability record the Decision Center flow needs
 * (UI_UX_VIETNAMESE_SPEC section 3): which systems applied, per-system
 * results, evidence, consensus, conflict, synthesis.
 *
 * @param calculationId      the persisted calculation's id — the same id a
 *                           later {@code GET /api/v1/calculations/{id}} uses
 * @param scenarioId         which scenario ran
 * @param policyDefined      whether this scenario has a real applicability
 *                           policy (only BUSINESS and DAILY_ACTION do today)
 * @param engines            per-engine execution outcome
 * @param unavailableEngines engines the scenario's policy names but the
 *                           request did not supply data for — reported
 *                           honestly rather than silently skipped
 * @param evidence           every evidence item produced
 * @param signals            every signal produced
 * @param fusion             the fused conclusion, or {@code null} when
 *                           {@code policyDefined} is {@code false}
 * @param resultHash         reproducibility identity (CLAUDE.md section 6)
 * @param retention          how long this result will be kept (CLAUDE.md
 *                           section 7). Always present: a user is entitled to
 *                           know that a reading is scheduled for deletion
 *                           before the deletion happens, not after
 */
public record ScenarioRunResponse(
        String calculationId,
        String scenarioId,
        boolean policyDefined,
        List<EngineOutcomeDto> engines,
        List<String> unavailableEngines,
        List<EvidenceDto> evidence,
        List<SignalDto> signals,
        FusionResultDto fusion,
        String resultHash,
        RetentionDto retention
) {
}
