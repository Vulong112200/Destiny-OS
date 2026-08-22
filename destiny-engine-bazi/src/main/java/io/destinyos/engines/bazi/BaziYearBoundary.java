package io.destinyos.engines.bazi;

/**
 * Which convention decides when the Bát Tự year pillar advances
 * (research item R18).
 *
 * <p>The two conventions genuinely differ, and the difference is not
 * cosmetic: for a birth between Tết and Lập Xuân they produce different year
 * pillars, and therefore — through the Ngũ Hổ Độn rule — a different month
 * stem as well. {@code MethodologyRegistrySeeder} already records the same
 * dispute for Phong Thủy's Kua number (R7), so this project has no basis for
 * treating it as settled here.
 *
 * <p>{@link #LAP_XUAN} is the convention this engine implements, declared in
 * {@code BaziEngine}'s metadata rather than assumed. It is the Tử Bình
 * convention, and it is what published Four Pillars tables actually do —
 * verified against two independent sources for the 1984 transition, where
 * 4 February 1984 (before Lập Xuân at 23:18 Beijing) yields year pillar Quý
 * Hợi and 5 February yields Giáp Tý, even though Tết 1984 had already
 * passed on 2 February.
 *
 * <p>{@link #LUNAR_NEW_YEAR} is <strong>not implemented</strong>. It is named
 * here so that the engine can detect when the two conventions would disagree
 * for a specific birth and say so, rather than presenting one answer as the
 * only answer.
 */
public enum BaziYearBoundary {
    /** Year advances at the Lập Xuân solar-term instant (315° solar longitude). */
    LAP_XUAN,
    /**
     * Year advances at Tết (lunar new year). Recognised, deliberately not
     * implemented — see this enum's Javadoc and research item R18.
     */
    LUNAR_NEW_YEAR
}
