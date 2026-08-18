package io.destinyos.core.context;

import java.util.Objects;

/**
 * One unresolved condition attached to a calculation (ADR D3).
 *
 * <p>This type is the mechanism by which D3's requirement — <em>preserve the
 * uncertainty in the calculation metadata</em> — becomes structural rather than
 * aspirational. Without somewhere for uncertainty to live, it gets dropped at
 * the first boundary it crosses, and the user sees a confident answer the
 * system was never entitled to give.
 *
 * <p>Intended propagation chain:
 * <pre>
 *   Calendar methodology gap
 *     → CalculationContext.uncertainties[]
 *     → Evidence limitation
 *     → Signal marked critical
 *     → Fusion INPUT_SENSITIVITY_CONFLICT
 *     → Vietnamese UI warning
 * </pre>
 *
 * @param kind        what sort of uncertainty
 * @param detail      specifics, e.g. the (date, region) not covered
 * @param researchId  register entry, e.g. {@code R14}
 * @param affectsResult whether the result could differ under a different
 *                    resolution. When true the user MUST be told; a silently
 *                    resolved boundary case is the RK3 failure mode
 */
public record Uncertainty(
        UncertaintyKind kind,
        String detail,
        String researchId,
        boolean affectsResult
) {
    public Uncertainty {
        Objects.requireNonNull(kind, "kind");
    }

    public static Uncertainty of(UncertaintyKind kind, String detail, String researchId) {
        return new Uncertainty(kind, detail, researchId, true);
    }

    /** Noted, but cannot change the outcome — e.g. a boundary that both rules agree on. */
    public static Uncertainty informational(UncertaintyKind kind, String detail) {
        return new Uncertainty(kind, detail, null, false);
    }
}
