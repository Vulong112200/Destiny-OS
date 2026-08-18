package io.destinyos.fusion;

/**
 * The five conflict categories (FUSION_ENGINE_SPEC.md section 8,
 * Master Spec section 8). Never collapsed into one generic "conflict" —
 * each names a genuinely different situation.
 */
public enum ConflictType {
    /** Same dimension, opposite polarity. */
    DIRECT_CONFLICT,
    /** Different dimensions/scope; not a real contradiction. */
    SCOPE_CONFLICT,
    /** Two schools of the same engine/system disagree. Never auto-resolved. */
    METHODOLOGY_CONFLICT,
    /** Caused by input precision/ambiguity (e.g. approximate birth time). */
    INPUT_SENSITIVITY_CONFLICT,
    /** The conflicting results belong to different points in time. */
    TEMPORAL_CONFLICT
}
