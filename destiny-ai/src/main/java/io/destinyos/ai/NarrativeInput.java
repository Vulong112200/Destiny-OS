package io.destinyos.ai;

import io.destinyos.core.signal.Dimension;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Pruned-and-labeled input to the AI narrative stage
 * (AI_NARRATIVE_SPEC.md section 2):
 *
 * <pre>{@code
 * {
 *   "scenario": {},
 *   "hardData": {},
 *   "signals": [],
 *   "conflicts": [],
 *   "warnings": [],
 *   "limitations": {},
 *   "calculationMetadata": {}
 * }
 * }</pre>
 *
 * <p>{@code limitations} is modelled as {@code List<String>} rather than the
 * spec example's bare {@code {}} - the example illustrates "some structured
 * content", not a literal empty-object schema, and a flat list of Vietnamese
 * sentences is what {@link NarrativePromptBuilder} and
 * {@link HardDataNarrativeFallback} both need to render directly.
 *
 * <p>{@code hardData} is a plain {@code Map<String, Object>} rather than a
 * typed reference to {@code FusionResult} - this module must not depend on
 * destiny-fusion (mirrors ADR D5's isolation of destiny-fusion from any
 * engine). The caller (destiny-api) already has Vietnamese-labeled DTOs and
 * flattens whatever summary fields it wants surfaced (overall outcome,
 * per-dimension states) into this map.
 *
 * <p>{@code question} and {@code focusLabel} are what the user actually asked.
 * They are not in AI_NARRATIVE_SPEC.md section 2's original sketch because
 * nothing carried them this far: the question was accepted by the API and
 * dropped before persistence, so both the LLM prompt and the deterministic
 * fallback wrote about "Sự nghiệp" in general while the user had asked
 * something specific. They are inputs to <em>wording</em> only — this whole
 * module is downstream of every calculation, so there is nothing here they
 * could influence even in principle.
 */
public record NarrativeInput(
        String scenarioNameVi,
        String question,
        String focusLabel,
        Set<Dimension> scenarioRelevantDimensions,
        Map<String, Object> hardDataSummary,
        List<NarrativeSignalItem> signals,
        List<NarrativeConflictItem> conflicts,
        List<String> warnings,
        List<String> limitations,
        Map<String, String> calculationMetadata) {

    public NarrativeInput {
        Objects.requireNonNull(scenarioNameVi, "scenarioNameVi");
        scenarioRelevantDimensions = scenarioRelevantDimensions == null
                ? Set.of() : Set.copyOf(scenarioRelevantDimensions);
        hardDataSummary = hardDataSummary == null ? Map.of() : Map.copyOf(hardDataSummary);
        signals = signals == null ? List.of() : List.copyOf(signals);
        conflicts = conflicts == null ? List.of() : List.copyOf(conflicts);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
        limitations = limitations == null ? List.of() : List.copyOf(limitations);
        calculationMetadata = calculationMetadata == null ? Map.of() : Map.copyOf(calculationMetadata);
    }

    /** Returns a copy with {@code signals} replaced - used by {@link NarrativePruner}. */
    NarrativeInput withSignals(List<NarrativeSignalItem> newSignals) {
        return new NarrativeInput(scenarioNameVi, question, focusLabel, scenarioRelevantDimensions,
                hardDataSummary, newSignals, conflicts, warnings, limitations, calculationMetadata);
    }
}
