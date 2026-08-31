package io.destinyos.api.dto;

/**
 * The request context, echoed back on {@link ScenarioRunResponse}.
 *
 * <p>Separate from {@link ScenarioContextRequest} for the same reason every
 * other pair in this package is ({@code TarotRequest} / {@code EvidenceDto}):
 * a request type is free to normalize, truncate and default its input, and a
 * response type must state only what was actually recorded. Reusing the
 * request record here would mean the read path re-runs the write path's
 * normalization on data that has already been normalized once and persisted —
 * a second, silent chance to change what the user sees compared to what the
 * database holds.
 *
 * <p>Always present on a response, even when the caller supplied nothing: a
 * client can then read {@code context.question} unconditionally instead of
 * null-checking the container first. The three fields themselves are nullable
 * and mean "not supplied".
 *
 * <p>{@code focusId}/{@code focusLabel} carry the same
 * presentation-and-narrative-only meaning documented on
 * {@link ScenarioContextRequest} — nothing deterministic was branched on them
 * to produce this result, and a client must not present them as if something
 * had been.
 *
 * @param question   the question this run was recorded against — the request
 *                   context's question, or the legacy
 *                   {@link TarotRequest#question()} when only that was sent
 *                   (see {@link ScenarioRunRequest#effectiveQuestion()})
 * @param focusId    the opaque UI intent id the caller sent, or {@code null}
 * @param focusLabel the Vietnamese label the caller sent, or {@code null}
 */
public record ScenarioContextDto(String question, String focusId, String focusLabel) {

    /** The "caller supplied no context at all" value, so callers never build it inline. */
    public static final ScenarioContextDto EMPTY = new ScenarioContextDto(null, null, null);
}
