package io.destinyos.calendar;

/**
 * When the day pillar rolls over relative to Giờ Tý (R10, owner-confirmed
 * 2026-08-19, {@code docs/DECISION_LOG.md}).
 *
 * <p>Versioned as an enum with room to grow, the same way
 * {@code TarotOrientationPolicy} (DECISION_LOG C9) is — not a boolean —
 * because a {@code ZI_HOUR_00_00} variant is a real, named alternative some
 * schools use, even though only the adopted default is implemented today.
 */
public enum ZiHourBoundaryPolicy {
    /**
     * Adopted default. The day pillar changes at 23:00, not midnight: a
     * birth at 23:30 takes the *next* day's day pillar. Sourced to the
     * mainstream Tử Bình convention, converging across multiple independent
     * Vietnamese-language sources (no single classical primary text located
     * — recorded honestly as a converging-secondary-source convention, see
     * DECISION_LOG R10).
     */
    ZI_HOUR_23_00
}
