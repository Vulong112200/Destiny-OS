package io.destinyos.persistence.calculation;

/**
 * What the user asked, as {@link CalculationRecorder} receives it (V9
 * migration).
 *
 * <p>A record rather than three more {@code String} parameters on
 * {@code record(...)}: three adjacent nullable strings in a signature is the
 * classic setup for silently transposing two of them, and no compiler or test
 * would notice a question stored in {@code focus_label}.
 *
 * <p>This deliberately does <em>not</em> live on {@code CalculationContext}
 * (destiny-core), even though that is where {@code calculationId}, versions,
 * timezone and seed live. {@code CalculationContext} is handed to every engine
 * on every {@code calculate()} call, so a question or a UI focus label placed
 * there would be reachable — and eventually read — from inside a deterministic
 * engine. CLAUDE.md Rule A and Rule D both depend on an engine's output being
 * a function of its declared input and methodology, never of free text the
 * client typed. Keeping this on the recording path only makes that
 * structurally true instead of merely agreed.
 *
 * @param question   the run's authoritative question, already trimmed and
 *                   length-capped by the API layer, or {@code null}
 * @param focusId    opaque UI intent id, or {@code null}. Recorded, never
 *                   acted on — see V9's migration comment
 * @param focusLabel the Vietnamese label the user saw for that intent, or
 *                   {@code null}. Recorded, never acted on
 */
public record CalculationRequestContext(String question, String focusId, String focusLabel) {

    /**
     * "The caller asked nothing in particular." Used by the recording
     * overloads that predate V9 so that a bare engine run does not have to
     * fabricate a context it does not have.
     */
    public static final CalculationRequestContext NONE = new CalculationRequestContext(null, null, null);
}
