package io.destinyos.engines.fengshui;

/**
 * Needed because the Kua formulas for men and women are genuinely different
 * and <em>not</em> symmetric (research item R7): the male formula subtracts
 * from a constant, the female adds to one.
 *
 * <p>Two values, because that is the extent of what the sourced formulas
 * cover. The tradition supplies no third case, and inventing one — averaging
 * the two, or defaulting to the male formula — would produce a confident
 * number with nothing behind it (CLAUDE.md Rule C). A caller who cannot supply
 * this gets a declined result naming why, not a guess.
 */
public enum Gender {
    MALE,
    FEMALE
}
