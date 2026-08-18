package io.destinyos.core.signal;

/**
 * Magnitude of a {@link Signal}. <strong>Not a probability</strong>
 * (FUSION_ENGINE_SPEC §3, ADR D6).
 *
 * <p>{@code CRITICAL} is deliberately NOT a member here. FUSION_ENGINE_SPEC §3
 * listed it as a strength while its own example carried a separate
 * {@code critical} boolean — criticality encoded twice, with no stated
 * relationship. DECISION_LOG C3 resolves this: strength carries magnitude only,
 * and {@link Signal#critical()} is the sole encoding of criticality.
 *
 * <p>The two are genuinely orthogonal. A WEAK signal can be critical (a
 * methodology warning), and a STRONG signal need not be.
 */
public enum Strength {
    WEAK,
    MEDIUM,
    STRONG
}
