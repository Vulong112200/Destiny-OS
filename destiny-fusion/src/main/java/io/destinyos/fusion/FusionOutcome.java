package io.destinyos.fusion;

/**
 * The overall scenario-level result (DECISION_LOG C2) — the union of Master
 * Spec section 9's ten outcomes and {@code FUSION_ENGINE_SPEC.md} section
 * 7's twelve, since each document specified at least one outcome the other
 * lacked (e.g. Master Spec omitted {@code METHODOLOGY_CONFLICT} even though
 * its own section 10 Rule F7 requires it to exist). Confirmed by the project
 * owner 2026-08-18: no state is dropped, because Rule D forbids collapsing
 * two specified-but-different states into one.
 *
 * <p>None of these values is a probability (ADR D6, Fusion section 11). They
 * describe which rule fired, not "how likely."
 */
public enum FusionOutcome {
    CONSENSUS_SUPPORT,
    CONSENSUS_CAUTION,
    CONSENSUS_NEGATIVE,
    SUPPORT_WITH_CAUTION,
    /** A critical caution survives even where support is otherwise unanimous (Fusion section 9). */
    SUPPORT_WITH_CRITICAL_CAUTION,
    CAUTION_WITH_SUPPORT,
    /** The mirror case: a critical support signal survives even where caution otherwise dominates. */
    CAUTION_WITH_CRITICAL_SUPPORT,
    MIXED,
    MAJOR_CONFLICT,
    /** Two schools of the same system disagree. Never auto-resolved (Fusion rule R8, Rule F7). */
    METHODOLOGY_CONFLICT,
    INSUFFICIENT_EVIDENCE,
    NOT_APPLICABLE
}
