package io.destinyos.engines.tarot;

import java.util.List;
import java.util.Objects;

/**
 * The result of one Tarot draw: which cards landed where, and everything
 * needed to reproduce the exact same draw again (CLAUDE.md section 6,
 * Master Spec section 17).
 *
 * @param spread                 which spread was drawn
 * @param draws                  one entry per spread position, in position order
 * @param seed                   the seed actually used — always present in the
 *                               result even when the caller did not supply one
 * @param deckVersion            {@link TarotDeck#DECK_VERSION}
 * @param shuffleAlgorithmVersion version of {@link TarotEngine}'s shuffle
 * @param orientationPolicy      the policy actually applied
 */
public record TarotReading(
        TarotSpread spread,
        List<TarotCardDraw> draws,
        long seed,
        String deckVersion,
        String shuffleAlgorithmVersion,
        TarotOrientationPolicy orientationPolicy
) {
    public TarotReading {
        Objects.requireNonNull(spread, "spread");
        Objects.requireNonNull(deckVersion, "deckVersion");
        Objects.requireNonNull(shuffleAlgorithmVersion, "shuffleAlgorithmVersion");
        Objects.requireNonNull(orientationPolicy, "orientationPolicy");
        draws = draws == null ? List.of() : List.copyOf(draws);
    }
}
