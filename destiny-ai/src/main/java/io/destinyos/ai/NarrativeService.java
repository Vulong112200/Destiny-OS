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
        ProviderCallResult callResult = activeProvider.call(prompt);

        if (!callResult.success()) {
            return NarrativeResult.fallback(
                    HardDataNarrativeFallback.build(pruned, callResult.failureReason()), callResult.failureReason());
        }

        Optional<NarrativeResponse> parsed = parser.parse(callResult.rawContent());
        if (parsed.isEmpty()) {
            return NarrativeResult.fallback(
                    HardDataNarrativeFallback.build(pruned, FallbackReason.MALFORMED_JSON),
                    FallbackReason.MALFORMED_JSON);
        }

        return NarrativeResult.aiGenerated(parsed.get(), activeProvider.name(), callResult.modelUsed());
    }
}
