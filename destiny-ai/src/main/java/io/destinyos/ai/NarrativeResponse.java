package io.destinyos.ai;

import java.util.List;

/**
 * Output schema (AI_NARRATIVE_SPEC.md section 5):
 *
 * <pre>{@code
 * {
 *   "summary": "",
 *   "keySignals": [],
 *   "conflicts": [],
 *   "cautions": [],
 *   "reflectionQuestions": []
 * }
 * }</pre>
 *
 * <p>Whether this came from a real LLM call or the deterministic fallback is
 * tracked separately by {@link NarrativeResult} - this record is the
 * rendered content either way, since the UI must treat both uniformly
 * (D8: the fallback IS the report when AI is unavailable, not an error state).
 */
public record NarrativeResponse(
        String summary,
        List<String> keySignals,
        List<String> conflicts,
        List<String> cautions,
        List<String> reflectionQuestions) {

    public NarrativeResponse {
        keySignals = keySignals == null ? List.of() : List.copyOf(keySignals);
        conflicts = conflicts == null ? List.of() : List.copyOf(conflicts);
        cautions = cautions == null ? List.of() : List.copyOf(cautions);
        reflectionQuestions = reflectionQuestions == null ? List.of() : List.copyOf(reflectionQuestions);
    }

    /**
     * Schema validation gate (AI_NARRATIVE_SPEC.md section 5: "Validate
     * schema truoc khi render"). A response with no summary is not a
     * narrative - malformed output must fall back, never render blank.
     */
    public boolean isWellFormed() {
        return summary != null && !summary.isBlank();
    }
}
