package io.destinyos.ai;

import java.util.Objects;

/**
 * {@link NarrativeService#generate}'s return value: always a renderable
 * {@link NarrativeResponse}, plus honest provenance about how it was
 * produced. There is deliberately no "narrative unavailable" outcome that
 * lacks a response - ADR D8 requires the hard-data report to always render.
 */
public record NarrativeResult(
        NarrativeSource source,
        FallbackReason fallbackReason,
        NarrativeResponse response,
        String providerName,
        String model) {

    public NarrativeResult {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(fallbackReason, "fallbackReason");
        Objects.requireNonNull(response, "response");
        if (source == NarrativeSource.AI_GENERATED && fallbackReason != FallbackReason.NONE) {
            throw new IllegalArgumentException(
                    "AI_GENERATED must carry fallbackReason NONE, got " + fallbackReason);
        }
        if (source == NarrativeSource.FALLBACK && fallbackReason == FallbackReason.NONE) {
            throw new IllegalArgumentException("FALLBACK requires a real fallbackReason, got NONE");
        }
    }

    public static NarrativeResult aiGenerated(NarrativeResponse response, String providerName, String model) {
        return new NarrativeResult(NarrativeSource.AI_GENERATED, FallbackReason.NONE, response, providerName, model);
    }

    public static NarrativeResult fallback(NarrativeResponse response, FallbackReason reason) {
        return new NarrativeResult(NarrativeSource.FALLBACK, reason, response, null, null);
    }
}
