package io.destinyos.ai;

/**
 * Why {@link NarrativeService} rendered the deterministic hard-data report
 * instead of a real AI narrative. Enumerates exactly the failure case list
 * in AI_NARRATIVE_SPEC.md section 6, plus the two cases that never reach a
 * provider at all ({@link #AI_DISABLED}, {@link #NO_API_KEY}).
 */
public enum FallbackReason {

    /** Not a failure - {@link NarrativeSource#AI_GENERATED} used this value. */
    NONE,

    /** {@code destiny.ai.enabled=false}, the operator's own choice. */
    AI_DISABLED,

    /** Enabled, but no provider is configured (e.g. no OpenRouter API key). */
    NO_API_KEY,

    TIMEOUT,
    RATE_LIMITED,
    SERVER_ERROR,
    PROVIDER_UNAVAILABLE,
    MALFORMED_JSON,
    EMPTY_RESPONSE
}
