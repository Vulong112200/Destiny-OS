package io.destinyos.api.dto;

/**
 * Input for the Tarot engine.
 *
 * @param spread   {@code PAST_PRESENT_FUTURE}, {@code CHOICE_A_B}, or
 *                 {@code SITUATION_CHALLENGE_ADVICE} (case-insensitive)
 * @param seed     optional; supply to replay a past draw exactly
 *                 (DECISION_LOG C6). Omit to let the engine generate one
 *                 via CSPRNG
 * @param question optional free-text question (Master Spec section 2:
 *                 Tarot may run on a bare question, no birth data needed).
 *                 <p><strong>Superseded by
 *                 {@link ScenarioRunRequest#context()}'s
 *                 {@link ScenarioContextRequest#question()}, which wins when
 *                 both are sent.</strong> A question is about the run, not
 *                 about one engine, and every stage that needs it (persistence,
 *                 the response, the AI narrative, the deterministic fallback)
 *                 would otherwise have to know to look inside the Tarot
 *                 request specifically. Kept, not removed, because clients
 *                 already in the field send it here and must keep working:
 *                 {@link ScenarioRunRequest#effectiveQuestion()} falls back to
 *                 this field when the request context carries no question.
 *                 New callers should use the request context
 */
public record TarotRequest(String spread, Long seed, String question) {
}
