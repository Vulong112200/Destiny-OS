package io.destinyos.engines.tarot;

/**
 * How a card's orientation is decided at draw time (DECISION_LOG C9).
 *
 * <p>Versioned rather than hard-coded because, unlike deck structure, this is
 * a genuine style choice some traditions omit entirely — recording it as a
 * policy keeps that choice visible and changeable without breaking
 * reproducibility of past draws (a past reading's policy travels with it).
 */
public enum TarotOrientationPolicy {
    /**
     * Every card is drawn upright. The simpler, no-reversal tradition some
     * readers prefer.
     */
    UPRIGHT_ONLY,

    /**
     * Each card's orientation is an independent 50/50 draw from the same
     * seeded random stream as the shuffle, taken at the moment that card is
     * placed into the spread. This is the adopted default (DECISION_LOG C9).
     */
    RANDOM_INDEPENDENT_PER_CARD
}
