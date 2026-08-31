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
        keySignals = meaningfulOnly(keySignals);
        conflicts = meaningfulOnly(conflicts);
        cautions = meaningfulOnly(cautions);
        reflectionQuestions = meaningfulOnly(reflectionQuestions);
    }

    /**
     * Schema validation gate (AI_NARRATIVE_SPEC.md section 5: "Validate
     * schema truoc khi render"). A response with no summary is not a
     * narrative - malformed output must fall back, never render blank.
     *
     * <p>"No summary" means <em>no letters in the summary</em>, not merely a
     * null or blank one. Measured in production: a free model echoed this
     * record's own schema template back verbatim, producing
     * {@code {"summary": "...", "keySignals": ["..."], ...}}. Under the old
     * {@code !summary.isBlank()} rule that passed every gate, and the user was
     * shown four bullet points reading {@code ...} under the heading
     * "Diễn giải bởi AI". That is worse than useless: it is strictly less
     * informative than the deterministic fallback it displaced, and it labels
     * empty output as a real reading - presenting nothing as if it were a
     * result, which is the one thing this project's honesty rules exist to
     * prevent (Rule C).
     *
     * <p>Deliberately <em>not</em> a minimum word or character count. Vietnamese
     * carries a lot in few words and a legitimately terse summary must render;
     * a length floor would trade a real failure mode for an invented one. The
     * no-letters test catches the observed failure exactly - every template
     * echo and every ellipsis variant has zero letters, and every real
     * sentence has many - so no floor is needed and none is imposed.
     */
    public boolean isWellFormed() {
        return hasMeaningfulText(summary);
    }

    /**
     * Whether {@code text} contains at least one letter in any script.
     *
     * <p>{@link Character#isLetter} over code points rather than a
     * {@code [a-zA-Z]} match: the narrative is Vietnamese, so "ừ", "Đ" and
     * every diacritic-bearing character must count as a letter. Iterating code
     * points rather than {@code char}s keeps that true for anything outside
     * the Basic Multilingual Plane too.
     *
     * <p>Punctuation, digits and whitespace alone are not text: {@code "..."},
     * {@code "…"}, {@code "—"}, {@code "-"} and {@code "  "} all return
     * {@code false}. {@code "N/A"} returns {@code true} - it has letters, and
     * a model that genuinely means "not applicable" has said something, unlike
     * one that emitted the template.
     */
    public static boolean hasMeaningfulText(String text) {
        return text != null && text.codePoints().anyMatch(Character::isLetter);
    }

    /**
     * Copies a list, dropping nulls and entries with no letters.
     *
     * <p>Dropping the bad elements rather than rejecting the whole response is
     * deliberate: a model that writes a real summary but fills
     * {@code keySignals} with {@code ["..."]} has still produced something
     * worth showing, and discarding it would send the reader to the
     * deterministic fallback over a partial defect. What must not happen is
     * rendering a bullet list of dots.
     *
     * <p>Applying this in the compact constructor means it holds for every
     * {@code NarrativeResponse} however it was built - parsed from a provider,
     * assembled by {@link HardDataNarrativeFallback}, or constructed in a test.
     * For the deterministic fallback it is a no-op (its text is always real
     * Vietnamese), so it costs nothing there and cannot be bypassed here.
     *
     * <p>Also the only null-safe path: the previous {@code List.copyOf} threw
     * {@code NullPointerException} on a {@code [null]} array, which the parser
     * then swallowed as "unparsable" - a correct outcome reached by accident.
     */
    private static List<String> meaningfulOnly(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream().filter(NarrativeResponse::hasMeaningfulText).toList();
    }
}
