package io.destinyos.persistence.calculation;

import io.destinyos.fusion.DimensionAnalysis;
import java.util.List;
import java.util.Set;

/**
 * A JSON-serializable summary of one {@link DimensionAnalysis}, persisted as
 * part of {@code fusion_results.dimensions_json} (V6).
 *
 * <p>Deliberately lighter than the domain type: {@link DimensionAnalysis}
 * carries full {@code Signal} objects for its critical signals, which would
 * duplicate rows already written to the {@code signals} table. This
 * snapshot references them by id instead — the full signal is one join
 * away, in the table that is its source of truth.
 */
public record DimensionAnalysisSnapshot(
        String dimension,
        String state,
        Set<String> supportingEngines,
        Set<String> cautionEngines,
        Set<String> negativeEngines,
        Set<String> neutralEngines,
        List<String> criticalSignalIds,
        List<String> rulesApplied
) {
    public static DimensionAnalysisSnapshot from(DimensionAnalysis analysis) {
        return new DimensionAnalysisSnapshot(
                analysis.dimension().name(),
                analysis.state().name(),
                analysis.supportingEngines(),
                analysis.cautionEngines(),
                analysis.negativeEngines(),
                analysis.neutralEngines(),
                analysis.criticalSignals().stream().map(s -> s.signalId()).toList(),
                analysis.rulesApplied());
    }
}
