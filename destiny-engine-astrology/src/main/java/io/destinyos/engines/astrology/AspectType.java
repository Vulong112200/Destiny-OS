package io.destinyos.engines.astrology;

/**
 * The five Ptolemaic aspects and their orb policy — owner decision,
 * {@code docs/DECISION_LOG.md} / {@code docs/RESEARCH_BLOCKERS.md} R6,
 * 2026-08-30: modern flat orbs (Sakoian &amp; Acker, <i>The Astrologer's
 * Handbook</i>, 1973), widened when a luminary (Sun or Moon) is one of the
 * two bodies. The minor aspects (quincunx, semi-sextile, semi-square,
 * sesquiquadrate, quintile series) are deliberately not in v1 — see R6's
 * decision text for why (orb conventions for them are far less consistent
 * across sources than for these five).
 */
public enum AspectType {
    CONJUNCTION(0.0, 8.0, 10.0),
    SEXTILE(60.0, 5.0, 6.0),
    SQUARE(90.0, 7.0, 9.0),
    TRINE(120.0, 8.0, 10.0),
    OPPOSITION(180.0, 8.0, 10.0);

    private final double exactAngleDegrees;
    private final double baseOrbDegrees;
    private final double luminaryOrbDegrees;

    AspectType(double exactAngleDegrees, double baseOrbDegrees, double luminaryOrbDegrees) {
        this.exactAngleDegrees = exactAngleDegrees;
        this.baseOrbDegrees = baseOrbDegrees;
        this.luminaryOrbDegrees = luminaryOrbDegrees;
    }

    public double exactAngleDegrees() {
        return exactAngleDegrees;
    }

    /** The orb to use for this aspect, given whether either body is the Sun or Moon. */
    public double orbDegrees(boolean involvesLuminary) {
        return involvesLuminary ? luminaryOrbDegrees : baseOrbDegrees;
    }
}
