package io.destinyos.fusion;

import io.destinyos.core.signal.Signal;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * The full explainability record (FUSION_ENGINE_SPEC.md section 10). The UI
 * must be able to answer, from this alone: what was the conclusion, who
 * contributed to it, where did methods disagree, which rules fired.
 *
 * @param overallOutcome    the scenario-level result (DECISION_LOG C2)
 * @param dimensions        per-dimension analysis (DECISION_LOG C5)
 * @param conflicts         every detected conflict, unresolved and named
 * @param criticalSignals   every active critical signal across all dimensions
 * @param rulesApplied      every Fusion rule (R1-R8) that fired anywhere, in
 *                          the order first triggered
 * @param supportingSources union of supporting engines across all dimensions
 * @param cautionSources    union of cautioning engines across all dimensions
 */
public record FusionResult(
        FusionOutcome overallOutcome,
        List<DimensionAnalysis> dimensions,
        List<Conflict> conflicts,
        List<Signal> criticalSignals,
        List<String> rulesApplied,
        Set<String> supportingSources,
        Set<String> cautionSources
) {
    public FusionResult {
        Objects.requireNonNull(overallOutcome, "overallOutcome");
        dimensions = dimensions == null ? List.of() : List.copyOf(dimensions);
        conflicts = conflicts == null ? List.of() : List.copyOf(conflicts);
        criticalSignals = criticalSignals == null ? List.of() : List.copyOf(criticalSignals);
        rulesApplied = rulesApplied == null ? List.of() : List.copyOf(rulesApplied);
        supportingSources = supportingSources == null ? Set.of() : Set.copyOf(supportingSources);
        cautionSources = cautionSources == null ? Set.of() : Set.copyOf(cautionSources);
    }

    public boolean hasCriticalSignal() {
        return !criticalSignals.isEmpty();
    }

    public boolean hasConflict() {
        return !conflicts.isEmpty();
    }
}
