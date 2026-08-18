package io.destinyos.scenario;

import io.destinyos.execution.ExecutionOutcome;
import io.destinyos.fusion.FusionResult;
import java.util.List;
import java.util.Objects;

/**
 * What a scenario run produces: which engines actually ran and how, which
 * ones the scenario wanted but were never available, and the fused
 * conclusion (Master Spec section 11-12).
 *
 * @param scenarioId        which scenario this was
 * @param execution         per-engine execution outcome (destiny-execution)
 * @param unavailableEngines engines the scenario's policy names but that
 *                          were not supplied to run — reported honestly
 *                          rather than silently absent (ADR D7's spirit
 *                          applied to orchestration, not just research
 *                          status)
 * @param fusion            the fused conclusion, or {@code null} if the
 *                          scenario's policy is not yet defined
 *                          ({@link ScenarioDefinition#policyDefined()})
 */
public record ScenarioResult(
        ScenarioType scenarioId,
        boolean policyDefined,
        ExecutionOutcome execution,
        List<String> unavailableEngines,
        FusionResult fusion
) {
    public ScenarioResult {
        Objects.requireNonNull(scenarioId, "scenarioId");
        Objects.requireNonNull(execution, "execution");
        unavailableEngines = unavailableEngines == null ? List.of() : List.copyOf(unavailableEngines);
    }

    public boolean hasFusionResult() {
        return fusion != null;
    }
}
