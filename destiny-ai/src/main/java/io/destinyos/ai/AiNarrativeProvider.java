package io.destinyos.ai;

import java.util.function.Predicate;

/**
 * Model-independence seam (AI_NARRATIVE_SPEC.md section 7: "Khong phu thuoc
 * mot model cu the"). {@link NarrativeService} depends only on this
 * interface, never on a concrete provider - OpenRouter is one implementation
 * ({@code io.destinyos.ai.openrouter.OpenRouterNarrativeProvider}), and a
 * test double is another. Implementations own their own timeout and bounded
 * retry (CLAUDE.md section 5: never infinite retry) and must never throw -
 * every failure mode is reported as a {@link ProviderCallResult} so
 * {@link NarrativeService} can fall back deterministically instead of
 * propagating an exception into the request path.
 */
public interface AiNarrativeProvider {

    /** Stable identifier surfaced in {@link NarrativeResult#providerName()}, e.g. {@code "openrouter"}. */
    String name();

    /**
     * Obtains narrative content the caller is willing to use.
     *
     * <p>{@code usableContent} is the caller's own acceptance test, applied by
     * the implementation to each candidate response <em>before</em> reporting
     * success. It exists because a provider may have somewhere else to go: the
     * OpenRouter implementation walks a chain of models, and a model that
     * returns a 200 carrying unusable content has failed just as surely as one
     * that returned 429. Without this hook the chain could only react to
     * transport failures, so a model that answers fluently with junk - a free
     * model echoing the response schema back verbatim, measured in production -
     * would end the chain, and the caller would discover the content was
     * useless only after every remaining model had been skipped.
     *
     * <p>The predicate keeps this interface schema-agnostic. A provider never
     * learns what makes a narrative acceptable, only whether this one was; the
     * JSON schema and its rules stay in {@link NarrativeResponseParser}, which
     * is the caller's business.
     *
     * <p>Implementations that have nowhere else to go may simply ignore it -
     * the caller re-validates whatever comes back regardless, so honouring it
     * is an optimisation of <em>which</em> model answers, never a substitute
     * for the caller's own gate.
     *
     * @param prompt        the built two-message prompt
     * @param usableContent returns {@code true} if the raw content is
     *                      something the caller can render; never null
     */
    ProviderCallResult call(NarrativePrompt prompt, Predicate<String> usableContent);
}
