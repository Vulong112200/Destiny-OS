package io.destinyos.calendar;

/**
 * Which historical jurisdiction a birth location falls under, for
 * historical timezone resolution (R14a/R14b).
 *
 * <p>Deliberately coarse — {@code NORTH}/{@code SOUTH}/{@code UNKNOWN} only.
 * R17 (region model granularity) explicitly cannot be resolved any finer
 * until R14b (the geographic North/South boundary) has a source; adding a
 * richer type now would be guessing ahead of research that has not
 * happened, across three research rounds, per
 * {@code docs/RESEARCH_BLOCKERS.md}.
 */
public enum VietnameseRegion {
    NORTH,
    SOUTH,
    /** Region not supplied, or not confidently assignable. Never silently treated as one side. */
    UNKNOWN
}
