package io.destinyos.core.context;

/**
 * Birth gender, where a methodology's own rules genuinely branch on it.
 *
 * <p>Two methodologies need this, and both for reasons internal to the
 * tradition rather than to this project:
 *
 * <ul>
 *   <li><strong>Phong Thủy Bát Trạch (R7)</strong> — the male and female Kua
 *       formulas are different and <em>not</em> symmetric: the male formula
 *       subtracts from a constant, the female adds to one.</li>
 *   <li><strong>Bát Tự Đại Vận (R2)</strong> — the luck cycle runs forward or
 *       backward depending on gender combined with the year stem's polarity.
 *       Flip it and every luck period after the first is wrong.</li>
 * </ul>
 *
 * <p><strong>Why it lives here.</strong> It began package-private inside
 * {@code destiny-engine-fengshui}. The moment Bát Tự needed the same concept,
 * leaving it there would have forced one engine to depend on another, which
 * {@code ArchitectureRulesTest.enginesStayIndependent} forbids — the same move
 * {@code SolarYear} made into {@code destiny-calendar} for the same reason.
 * It sits beside {@link BirthTimePrecision} because it is the same kind of
 * thing: an attribute of the birth input that engines branch on.
 *
 * <p><strong>Two values, and no default.</strong> That is the extent of what
 * the sourced formulas cover; the traditions supply no third case. Nor is
 * there a defensible fallback — averaging the two is meaningless, and
 * defaulting to male produces a confident answer that looks exactly like a
 * correct one (CLAUDE.md Rule C). A caller who cannot supply this gets a
 * declined result naming why, not a guess.
 */
public enum Gender {
    MALE,
    FEMALE
}
