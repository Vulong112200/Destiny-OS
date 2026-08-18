package io.destinyos.core.signal;

/**
 * Life area a signal speaks to (Master Spec §6).
 *
 * <p>{@code HEALTH_REFLECTION} is named as it is on purpose: CLAUDE.md §10
 * forbids diagnosis, so this dimension carries material for self-reflection,
 * never a medical claim.
 */
public enum Dimension {
    FINANCE,
    CAREER,
    RELATIONSHIP,
    HEALTH_REFLECTION,
    TIMING,
    TRAVEL,
    DECISION,
    HOME,
    DAILY,
    OTHER
}
