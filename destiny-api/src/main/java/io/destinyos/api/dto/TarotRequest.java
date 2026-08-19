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
 *                 Tarot may run on a bare question, no birth data needed)
 */
public record TarotRequest(String spread, Long seed, String question) {
}
