package io.destinyos.engines.tarot;

import java.util.Objects;

/**
 * One card as it landed in a spread: which card, which orientation, which
 * position.
 *
 * @param position    the spread position label, e.g. {@code "PAST"}
 * @param card        the card drawn
 * @param orientation upright or reversed at draw time
 */
public record TarotCardDraw(String position, TarotCard card, TarotOrientation orientation) {
    public TarotCardDraw {
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(card, "card");
        Objects.requireNonNull(orientation, "orientation");
    }
}
