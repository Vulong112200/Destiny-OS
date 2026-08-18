package io.destinyos.core.result;

/**
 * Outcome of one engine invocation (Master Spec §4, CLAUDE.md Rule F).
 *
 * <p>Several of these are <em>honest successes</em>, not failures.
 * {@code RESEARCH_REQUIRED} and {@code NOT_IMPLEMENTED} in particular are
 * legitimate answers that must reach the user as information, never as an
 * HTTP 500 (ADR D7, CLAUDE_CODE_WORKFLOW §8).
 */
public enum EngineStatus {
    /** Completed; results are usable. */
    SUCCESS,
    /** Completed partially; some findings are missing but what is present is valid. */
    PARTIAL,
    /** Engine does not apply to this input or scenario. NOT a neutral vote. */
    NOT_APPLICABLE,
    /**
     * The algorithm is not verified, so no result is produced.
     * CLAUDE.md Rule C: no placeholder formula, no plausible-looking guess.
     */
    RESEARCH_REQUIRED,
    /** Methodology is specified but not yet built. */
    NOT_IMPLEMENTED,
    /** Input failed validation. */
    INVALID_INPUT,
    /** Failed, but the wider request can still complete (e.g. timeout). */
    FAILED_RECOVERABLE,
    /** Failed in a way that cannot be isolated. */
    FAILED_FATAL;

    /** Whether this status carries usable findings. */
    public boolean hasUsableData() {
        return this == SUCCESS || this == PARTIAL;
    }

    /**
     * Whether this is an honest non-answer rather than a malfunction.
     * These must be surfaced to the user with an explanation (ADR D7).
     */
    public boolean isHonestNonAnswer() {
        return this == NOT_APPLICABLE
                || this == RESEARCH_REQUIRED
                || this == NOT_IMPLEMENTED;
    }

    /** Whether the engine malfunctioned, as opposed to declining to answer. */
    public boolean isFailure() {
        return this == FAILED_RECOVERABLE || this == FAILED_FATAL || this == INVALID_INPUT;
    }
}
