package io.destinyos.ai;

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

    ProviderCallResult call(NarrativePrompt prompt);
}
