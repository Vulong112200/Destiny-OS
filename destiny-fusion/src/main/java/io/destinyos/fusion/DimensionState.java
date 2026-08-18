package io.destinyos.fusion;

/**
 * Per-dimension aggregate state (DECISION_LOG C5) — {@code CLAUDE.md} Rule
 * E's vocabulary, applied at the dimension layer rather than the overall
 * scenario layer.
 *
 * <p>C5 resolved a genuine ambiguity: Rule E's list and
 * {@link FusionOutcome}'s twelve values look like competing enumerations of
 * the same thing, but they are not — this is the state of <em>one
 * dimension</em> after combining every applicable engine's signal for it
 * (e.g. is FINANCE positive, cautionary, conflicted?); {@link FusionOutcome}
 * is the state of the <em>whole scenario</em> after combining every
 * dimension. The layered reading is the only one under which
 * {@code CLAUDE.md}, the Master Specification and {@code
 * FUSION_ENGINE_SPEC.md} are simultaneously true — confirmed by the project
 * owner 2026-08-18.
 */
public enum DimensionState {
    POSITIVE,
    NEUTRAL,
    CAUTION,
    NEGATIVE,
    MIXED,
    CONFLICT,
    MAJOR_CONFLICT,
    INSUFFICIENT_EVIDENCE
}
