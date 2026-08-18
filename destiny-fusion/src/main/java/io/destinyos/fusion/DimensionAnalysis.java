package io.destinyos.fusion;

import io.destinyos.core.signal.Dimension;
import io.destinyos.core.signal.Signal;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * The voting matrix and resulting state for one dimension
 * (FUSION_ENGINE_SPEC.md section 6). Every engine set here is a set of
 * <strong>distinct engine ids</strong>, never a signal or evidence count —
 * five signals from one engine is one source, not five (section 5, audit
 * risk RK6).
 *
 * @param dimension          which dimension this is
 * @param state              the resulting per-dimension state (DECISION_LOG C5)
 * @param supportingEngines  distinct engines whose applicable signal was SUPPORT
 * @param cautionEngines     distinct engines whose applicable signal was CAUTION
 * @param negativeEngines    distinct engines whose applicable signal was NEGATIVE
 * @param neutralEngines     distinct engines whose applicable signal was NEUTRAL
 * @param criticalSignals    every active critical signal in this dimension —
 *                           these must survive majority voting (section 9)
 * @param rulesApplied       which Fusion rules (R1-R8) produced {@code state}
 */
public record DimensionAnalysis(
        Dimension dimension,
        DimensionState state,
        Set<String> supportingEngines,
        Set<String> cautionEngines,
        Set<String> negativeEngines,
        Set<String> neutralEngines,
        List<Signal> criticalSignals,
        List<String> rulesApplied
) {
    public DimensionAnalysis {
        Objects.requireNonNull(dimension, "dimension");
        Objects.requireNonNull(state, "state");
        supportingEngines = supportingEngines == null ? Set.of() : Set.copyOf(supportingEngines);
        cautionEngines = cautionEngines == null ? Set.of() : Set.copyOf(cautionEngines);
        negativeEngines = negativeEngines == null ? Set.of() : Set.copyOf(negativeEngines);
        neutralEngines = neutralEngines == null ? Set.of() : Set.copyOf(neutralEngines);
        criticalSignals = criticalSignals == null ? List.of() : List.copyOf(criticalSignals);
        rulesApplied = rulesApplied == null ? List.of() : List.copyOf(rulesApplied);
    }

    /** Distinct applicable sources across every polarity — the diversity count (section 8.3). */
    public int sourceCount() {
        var all = new java.util.HashSet<String>();
        all.addAll(supportingEngines);
        all.addAll(cautionEngines);
        all.addAll(negativeEngines);
        all.addAll(neutralEngines);
        return all.size();
    }

    public boolean hasCriticalSignal() {
        return !criticalSignals.isEmpty();
    }
}
