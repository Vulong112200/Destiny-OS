package io.destinyos.api.dto;

/**
 * What the user actually asked, carried at request level rather than inside
 * one engine's input.
 *
 * <p>Before this type existed the question was accepted by
 * {@link TarotRequest#question()}, handed to {@code TarotDrawInput}, and then
 * dropped: no engine read it, nothing persisted it, no response returned it,
 * and the narrative layer had no field for it. A user typed
 * "Tôi có nên đổi việc không?" and got a paragraph that never mentioned
 * changing jobs. Putting it here instead of on another per-engine request is
 * deliberate — the question belongs to the <em>run</em>, not to whichever
 * engine happened to be asked for first, and every stage downstream (persistence,
 * response, AI narrative, deterministic fallback) needs it equally.
 *
 * <p><strong>{@code focusId} / {@code focusLabel} are presentation and
 * narrative framing only.</strong> They are an opaque UI intent label the
 * frontend attaches to say which of its own shortcut buttons the user pressed
 * ({@code "doi-viec"} / "Đổi việc / nhảy việc"). They MUST NOT:
 * <ul>
 *   <li>select a school or methodology (CLAUDE.md Rule D — a school is chosen
 *       by an engine's own declared methodology, never by a UI label);</li>
 *   <li>change any engine input;</li>
 *   <li>change applicability, or which engines run;</li>
 *   <li>alter any calculation, score, signal or fused outcome.</li>
 * </ul>
 * Nothing in the deterministic path may branch on them. If a future change
 * makes a focus value influence what is <em>computed</em> rather than how the
 * result is <em>worded</em>, that is a scenario definition
 * ({@code ScenarioDefinition}) with a researched, sourced policy — not a
 * string from the client. This paragraph exists so that the next reader does
 * not "helpfully" wire these into an engine.
 *
 * <p>All three fields are optional. Blank input normalizes to {@code null}
 * rather than {@code ""} so that every downstream stage has exactly one
 * "absent" value to test for, and the question is capped at
 * {@link #MAX_QUESTION_LENGTH} characters — the length of the persisted
 * column, and a bound on how much free text reaches an LLM prompt.
 *
 * @param question   free text the user typed, nullable
 * @param focusId    opaque UI intent id, e.g. {@code "doi-viec"}, nullable
 * @param focusLabel the Vietnamese label the user actually saw for that
 *                   intent, e.g. {@code "Đổi việc / nhảy việc"}, nullable
 */
public record ScenarioContextRequest(String question, String focusId, String focusLabel) {

    /** Matches the {@code calculations.question} column added in V9. */
    public static final int MAX_QUESTION_LENGTH = 500;

    /** Matches the {@code calculations.focus_id} column added in V9. */
    public static final int MAX_FOCUS_ID_LENGTH = 100;

    /** Matches the {@code calculations.focus_label} column added in V9. */
    public static final int MAX_FOCUS_LABEL_LENGTH = 200;

    public ScenarioContextRequest {
        question = normalizeQuestion(question);
        focusId = normalize(focusId, MAX_FOCUS_ID_LENGTH);
        focusLabel = normalize(focusLabel, MAX_FOCUS_LABEL_LENGTH);
    }

    /**
     * Trims, maps blank to {@code null}, and truncates to
     * {@link #MAX_QUESTION_LENGTH}.
     *
     * <p>Public and static because {@link ScenarioRunRequest#effectiveQuestion()}
     * has to apply exactly the same normalization to the legacy
     * {@link TarotRequest#question()} fallback. Two normalizers would mean a
     * question could be stored one way and prompted another.
     *
     * <p>Truncation is silent rather than a {@code 400}: an over-long question
     * is still a real question, and refusing the whole run over it would lose
     * the calculation as well. The kept prefix is what the user began with,
     * which is where people put the actual ask.
     */
    public static String normalizeQuestion(String raw) {
        return normalize(raw, MAX_QUESTION_LENGTH);
    }

    private static String normalize(String raw, int maxLength) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength);
    }
}
