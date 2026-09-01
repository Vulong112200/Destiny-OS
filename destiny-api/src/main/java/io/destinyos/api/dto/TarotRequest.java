package io.destinyos.api.dto;

/**
 * Input for the Tarot engine.
 *
 * @param spread   one of {@code PAST_PRESENT_FUTURE}, {@code CHOICE_A_B},
 *                 {@code SITUATION_CHALLENGE_ADVICE}, {@code HORSESHOE_FIVE},
 *                 {@code CELTIC_CROSS}, {@code FREE_FORM} (case-insensitive)
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
 * @param cardCount required by {@code FREE_FORM} (1-10), which has no count of
 *                 its own; ignored by every other spread, whose count is a
 *                 property of the spread rather than a caller's choice
 * @param pickedPositions optional 1-based slots of the shuffled 78-card deck,
 *                 distinct, as many as the spread turns over. Omit to have the
 *                 engine take from the top.
 *                 <p>This is the querent pointing at face-down cards: the deck
 *                 is still shuffled from the seed and the slot's contents are
 *                 still unknown when it is chosen, so the draw is no less
 *                 chance-determined — but the choice was the querent's, and the
 *                 evidence records which of the two happened rather than
 *                 reporting both identically. It is <em>not</em> a way to name
 *                 a card: picking slot 47 does not say what slot 47 holds
 */
public record TarotRequest(
        String spread,
        Long seed,
        String question,
        Integer cardCount,
        java.util.List<Integer> pickedPositions
) {

    /** Kept so clients already in the field keep compiling and working. */
    public TarotRequest(String spread, Long seed, String question) {
        this(spread, seed, question, null, null);
    }
}
