package io.destinyos.core.evidence;

/**
 * OPTIONAL technical data-quality marker. Absent unless a specific methodology
 * defines it and states its meaning (DECISION_LOG C8, ADR D6).
 *
 * <p>Master Spec §5's example carried a field literally named {@code confidence}
 * with the value {@code "NOT_PROBABILITY"} — a comment wearing the costume of
 * data — while the surrounding prose warned against exactly that name. This
 * enum is the resolution: the field is {@code dataConfidence}, it is an enum
 * rather than a number, and the guarantee lives in the type system instead of
 * in a magic string.
 *
 * <p>This describes <em>how good the input data was</em>. It says nothing about
 * whether a traditional reading is true, and must never be rendered as a
 * percentage.
 */
public enum DataConfidence {
    /** Input was exact and the rule applied cleanly. */
    EXACT,
    /** Input was approximate — e.g. birth time precision APPROXIMATE. */
    APPROXIMATE,
    /** A rule applied but its inputs sit near a boundary. */
    BOUNDARY,
    /** Derived under a methodology assumption that is itself unresolved. */
    ASSUMED
}
