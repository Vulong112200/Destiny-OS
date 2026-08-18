package io.destinyos.engine;

/**
 * Lifecycle state of a methodology in the registry (ADR D7).
 *
 * <p>Blocked methodologies are <em>registered and visible</em>, not omitted.
 * Registering them means the UI can honestly show a user which systems
 * participated and which did not, and research progress becomes visible in the
 * product rather than buried in a document.
 */
public enum MethodologyStatus {
    /** Verified, sourced, golden-tested. Safe to produce results. */
    PRODUCTION_READY,
    /** Algorithm not verified. MUST NOT calculate (CLAUDE.md Rule C). */
    RESEARCH_REQUIRED,
    /** Algorithm known; a school choice must be recorded first (Rule D). */
    DECISION_REQUIRED,
    /** Algorithm fine, reference content missing. May calculate; content degrades. */
    CONTENT_REQUIRED,
    /** Specified but not yet built. */
    NOT_IMPLEMENTED,
    /** Deliberately not implemented. */
    OUT_OF_SCOPE;

    /** Whether an engine in this state may produce a real result. */
    public boolean mayCalculate() {
        return this == PRODUCTION_READY || this == CONTENT_REQUIRED;
    }
}
