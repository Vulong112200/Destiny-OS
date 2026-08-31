package io.destinyos.api.service;

import io.destinyos.api.dto.LabeledValue;
import io.destinyos.core.signal.Dimension;
import io.destinyos.i18n.VietnameseLabels;
import io.destinyos.scenario.ScenarioDefinition;
import io.destinyos.scenario.ScenarioRegistry;
import io.destinyos.scenario.ScenarioType;
import java.util.Arrays;
import java.util.List;

/**
 * Resolving a persisted {@code scenarioId} string back to its
 * {@link ScenarioDefinition}, and rendering that definition's declared
 * dimensions for the API.
 *
 * <p>Exists because three services need exactly this and each had (or would
 * have had) its own copy: {@link ScenarioOrchestrationService} on the write
 * path, {@link CalculationQueryService} and
 * {@link NarrativeOrchestrationService} on two separate read paths. A
 * scenario id that resolves to a real definition in one of them and to
 * {@code null} in another is precisely the kind of drift that shows up as "the
 * reading says one thing and the saved copy says another".
 */
final class ScenarioDefinitions {

    private ScenarioDefinitions() {
    }

    /**
     * The definition for a persisted scenario id, or {@code null} if there is
     * none.
     *
     * <p>Returns {@code null} rather than throwing for an unrecognized id on
     * purpose. {@code calculations.scenario_id} is a plain {@code VARCHAR} that
     * has outlived at least one shape of this enum already; a result recorded
     * under a scenario name that no longer exists is still a real result the
     * user is entitled to read back. Failing the whole read to punish a stale
     * string would destroy data access to punish a naming change.
     */
    static ScenarioDefinition byId(String scenarioId) {
        if (scenarioId == null) {
            return null;
        }
        try {
            return ScenarioRegistry.get(ScenarioType.valueOf(scenarioId));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * The scenario's declared dimensions as technical value + Vietnamese label
     * (UI_UX_VIETNAMESE_SPEC section 1), or an empty list when the scenario is
     * unknown or declares none.
     *
     * <p>Emitted in {@link Dimension} declaration order. That is a
     * serialization decision, not a ranking: {@code ScenarioDefinition#dimensions()}
     * is a {@code Set} built by {@code Set.copyOf}, whose iteration order is
     * explicitly unspecified and in practice varies between JVM runs, so
     * emitting it as-is would give the same scenario a different response every
     * restart. Nothing here filters, weights or reorders by relevance — the
     * whole declared set is exposed exactly as the registry states it, and what
     * to do with it is the client's decision.
     */
    static List<LabeledValue> dimensionLabels(ScenarioDefinition definition) {
        if (definition == null) {
            return List.of();
        }
        return Arrays.stream(Dimension.values())
                .filter(definition.dimensions()::contains)
                .map(dimension -> LabeledValue.of(dimension, VietnameseLabels.of(dimension)))
                .toList();
    }
}
