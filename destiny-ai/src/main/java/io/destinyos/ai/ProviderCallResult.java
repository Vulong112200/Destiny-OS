package io.destinyos.ai;

/**
 * The outcome of one {@link AiNarrativeProvider#call}. Every failure case
 * from AI_NARRATIVE_SPEC.md section 6 (timeout, 429, 5xx, provider
 * unavailable, malformed JSON, empty response) is representable here -
 * {@code malformed JSON} is only detected later by
 * {@link NarrativeResponseParser}, so a successful HTTP call with non-empty
 * body always reports {@link #ok}; the parser decides {@code MALFORMED_JSON}
 * afterward.
 */
public record ProviderCallResult(boolean success, FallbackReason failureReason, String rawContent, String modelUsed) {

    public static ProviderCallResult ok(String rawContent, String modelUsed) {
        return new ProviderCallResult(true, FallbackReason.NONE, rawContent, modelUsed);
    }

    public static ProviderCallResult failure(FallbackReason reason) {
        if (reason == FallbackReason.NONE || reason == FallbackReason.AI_DISABLED
                || reason == FallbackReason.NO_API_KEY) {
            throw new IllegalArgumentException("Not a provider-call failure reason: " + reason);
        }
        return new ProviderCallResult(false, reason, null, null);
    }
}
