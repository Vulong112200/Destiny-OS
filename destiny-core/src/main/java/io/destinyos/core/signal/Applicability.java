package io.destinyos.core.signal;

/**
 * How far an engine's output bears on the scenario at hand
 * (Master Spec §7, FUSION_ENGINE_SPEC §4).
 *
 * <p>{@code NOT_APPLICABLE} lives on this axis rather than on {@link Polarity},
 * which makes "a non-applicable engine counted as a neutral vote" not merely
 * discouraged but <em>unrepresentable</em>.
 */
public enum Applicability {
    HIGH,
    MEDIUM,
    LOW,
    /**
     * The engine does not participate. Fusion MUST exclude these entirely
     * rather than counting them as NEUTRAL (FUSION_ENGINE_SPEC §4).
     */
    NOT_APPLICABLE;

    /** Whether a signal at this applicability takes part in fusion at all. */
    public boolean participates() {
        return this != NOT_APPLICABLE;
    }
}
