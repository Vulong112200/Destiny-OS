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
 * @param context            what the user asked, echoed back (never
 *                           {@code null}; its fields may be). Returned so the
 *                           UI can show the reading next to the question that
 *                           produced it, and so a result read back later still
 *                           knows what it was an answer to
 * @param dimensions         the dimensions this scenario's definition declares
 *                           it cares about ({@code ScenarioDefinition#dimensions()}),
 *                           technical value plus Vietnamese label. This is the
 *                           scenario's declared scope, <em>not</em> a filter
 *                           applied to anything below: {@code signals} and
 *                           {@code fusion} are complete and unfiltered, and a
 *                           client is free to use this set to decide what to
 *                           foreground. Emitted in {@code Dimension} declaration
 *                           order purely so the payload is stable across runs
 * @param policyDefined      whether this scenario has a real applicability
 *                           policy (every scenario except COMPATIBILITY does,
 *                           which is undefined on purpose — its evidence is
 *                           entirely dual-chart and this system is single-chart)
 * @param engines            per-engine execution outcome
 * @param unavailableEngines engines the scenario's policy names but the
 *                           request did not supply data for — reported
 *                           honestly rather than silently skipped
 * @param enginesOutsideScenario engines the request supplied input for that
 *                           this scenario's policy does not name, so they did
 *                           not run. Separate from {@code unavailableEngines},
 *                           which is the opposite case: the scenario wanted the
 *                           engine and the request had no data for it. Added
 *                           2026-09-01 because the two had been conflated by
 *                           being silent about one of them — a user who chose
 *                           Tarot for "Mua sắm" had their draw discarded with
 *                           nothing in the payload saying so, and the page they
 *                           got back showed only engines that emit no
 *                           interpretation. The client should tell the user
 *                           plainly, ideally before they submit
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
        ScenarioContextDto context,
        List<LabeledValue> dimensions,
        boolean policyDefined,
        List<EngineOutcomeDto> engines,
        List<String> unavailableEngines,
        List<String> enginesOutsideScenario,
        List<EvidenceDto> evidence,
        List<SignalDto> signals,
        FusionResultDto fusion,
        String resultHash,
        RetentionDto retention
) {

    /**
     * Kept so callers written before {@code enginesOutsideScenario} existed
     * keep compiling; it defaults to empty, which is the correct value for a
     * caller that has no notion of engines outside the scenario.
     */
    public ScenarioRunResponse(String calculationId, String scenarioId, ScenarioContextDto context,
                               List<LabeledValue> dimensions, boolean policyDefined,
                               List<EngineOutcomeDto> engines, List<String> unavailableEngines,
                               List<EvidenceDto> evidence, List<SignalDto> signals,
                               FusionResultDto fusion, String resultHash, RetentionDto retention) {
        this(calculationId, scenarioId, context, dimensions, policyDefined, engines,
                unavailableEngines, List.of(), evidence, signals, fusion, resultHash, retention);
    }
}
