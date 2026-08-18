package io.destinyos.engines.numerology;

/**
 * The five Pythagorean numbers this engine computes (Master Spec section 16).
 *
 * <p>Master Spec section 16 also lists Maturity, Personal Year, Personal
 * Month and Personal Day. Those are deliberately not implemented yet: each
 * needs its own formula verified with the same research discipline applied
 * here, and none was researched in this pass. They are absent rather than
 * approximated.
 */
public enum NumerologyNumberType {
    /** From the birth date alone; no name required. */
    LIFE_PATH,
    /** Sum of every letter in the full name. */
    EXPRESSION,
    /** Sum of the vowels in the full name (research item R8 addendum: Y is always a consonant). */
    SOUL_URGE,
    /** Sum of the consonants in the full name (same Y treatment as Soul Urge). */
    PERSONALITY,
    /** The day-of-month component, reduced the same way as Life Path's day component. */
    BIRTHDAY
}
