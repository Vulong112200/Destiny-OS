package io.destinyos.scenario;

import io.destinyos.core.signal.Applicability;
import io.destinyos.core.signal.Dimension;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * What Master Spec section 11 calls a Scenario Definition, scoped to the
 * fields that are actually specified anywhere: {@code scenarioId},
 * {@code applicableEngines} and {@code dimensions}. {@code requiredInputs}
 * is deliberately not a separate declared field — it is derived at run time
 * from the {@link io.destinyos.engine.EngineCapability} of whichever engines
 * are actually applicable, so it can never drift out of sync with what the
 * engines themselves declare. {@code signal rules} and {@code fusion
 * policy} are listed in Master Spec section 11 but never given content
 * anywhere in the specification, so they are not modelled here rather than
 * invented.
 *
 * @param scenarioId        which scenario this is
 * @param displayNameVi     Vietnamese name (CLAUDE.md section 9)
 * @param policyDefined     whether {@code applicableEngines} reflects a real,
 *                          sourced policy ({@code true} only for the two
 *                          scenarios Master Spec section 7 gives a worked
 *                          example for) or is empty/undefined
 * @param applicableEngines engineId to applicability, e.g.
 *                          {@code {"BAZI": HIGH, "TAROT": MEDIUM}} — engines
 *                          not present here are treated as NOT_APPLICABLE
 *                          to this scenario regardless of what they
 *                          themselves would otherwise report
 * @param dimensions        which dimensions this scenario cares about
 */
public record ScenarioDefinition(
        ScenarioType scenarioId,
        String displayNameVi,
        boolean policyDefined,
        Map<String, Applicability> applicableEngines,
        Set<Dimension> dimensions
) {
    public ScenarioDefinition {
        Objects.requireNonNull(scenarioId, "scenarioId");
        Objects.requireNonNull(displayNameVi, "displayNameVi");
        applicableEngines = applicableEngines == null ? Map.of() : Map.copyOf(applicableEngines);
        dimensions = dimensions == null ? Set.of() : Set.copyOf(dimensions);
    }

    /** A scenario with no policy at all — registered by name, honestly undefined. */
    public static ScenarioDefinition undefinedPolicy(ScenarioType type, String displayNameVi) {
        return new ScenarioDefinition(type, displayNameVi, false, Map.of(), Set.of());
    }

    public Applicability applicabilityFor(String engineId) {
        return applicableEngines.getOrDefault(engineId, Applicability.NOT_APPLICABLE);
    }
}
