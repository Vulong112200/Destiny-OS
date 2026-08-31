package io.destinyos.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * The AI narrative stage's single entry point (AI_NARRATIVE_SPEC.md,
 * ADR D8). Always returns a renderable {@link NarrativeResult} - never
 * throws, never blocks the caller waiting on a retry loop beyond what the
 * injected {@link AiNarrativeProvider} itself bounds.
 *
 * <p>The provider is {@link Optional} on purpose: with no provider bean
 * configured (no API key, feature disabled), this service is still fully
 * constructible and fully functional, always taking the
 * {@link HardDataNarrativeFallback} path. Nothing about wiring this bean
 * requires an LLM to exist.
 */
@Service
public class NarrativeService {

    private final AiProperties properties;
    private final Optional<AiNarrativeProvider> provider;
    private final NarrativeResponseParser parser;

    public NarrativeService(AiProperties properties, Optional<AiNarrativeProvider> provider) {
        this.properties = properties;
        this.provider = provider;
        this.parser = new NarrativeResponseParser(new ObjectMapper());
    }

    public NarrativeResult generate(NarrativeInput rawInput) {
        NarrativeInput pruned = NarrativePruner.prune(rawInput);

        if (!properties.isEnabled()) {
            return NarrativeResult.fallback(
                    HardDataNarrativeFallback.build(pruned, FallbackReason.AI_DISABLED), FallbackReason.AI_DISABLED);
        }
        if (provider.isEmpty()) {
            return NarrativeResult.fallback(
                    HardDataNarrativeFallback.build(pruned, FallbackReason.NO_API_KEY), FallbackReason.NO_API_KEY);
        }

        AiNarrativeProvider activeProvider = provider.get();
        NarrativePrompt prompt = NarrativePromptBuilder.build(pruned);

        // The provider is handed this service's own acceptance test so that a
        // model returning unusable content counts as a failed model rather than
        // a finished call. A provider with a fallback chain can then move on to
        // the next model, which is the only place that decision can be made -
        // by the time a result reaches the code below, the chain is over.
        ProviderCallResult callResult = activeProvider.call(prompt, raw -> parser.parse(raw).isPresent());

        if (!callResult.success()) {
            return NarrativeResult.fallback(
                    HardDataNarrativeFallback.build(pruned, callResult.failureReason()), callResult.failureReason());
        }

        // Re-parsed rather than trusting the predicate's verdict. Honouring
        // usableContent is optional for a provider (see AiNarrativeProvider),
        // so this stays the real gate; a provider that ignored the hook, or a
        // future one with no chain to walk, is still validated here exactly as
        // before. The cost is one repeat parse of a small JSON object on the
        // success path only.
        Optional<NarrativeResponse> parsed = parser.parse(callResult.rawContent());
        if (parsed.isEmpty()) {
            return NarrativeResult.fallback(
                    HardDataNarrativeFallback.build(pruned, FallbackReason.MALFORMED_JSON),
                    FallbackReason.MALFORMED_JSON);
        }

        return NarrativeResult.aiGenerated(parsed.get(), activeProvider.name(), callResult.modelUsed());
    }
}
