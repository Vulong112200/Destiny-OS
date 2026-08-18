package io.destinyos.engines.tarot;

import java.util.Objects;

/**
 * One of the 78 Rider-Waite-Smith cards (Master Spec section 17).
 *
 * <p>This is structural fact about a well-documented, public-domain deck
 * design — the card names, numbers, suits and arcana are not in dispute the
 * way a Bát Tự school's rules are, so no research gate applies to this part.
 * What is legitimately missing is {@link #meaning()}, gated by research item
 * R11.
 *
 * @param id        stable identifier, e.g. {@code MAJOR_00_THE_FOOL},
 *                  {@code MINOR_WANDS_01_ACE}
 * @param name      canonical English name (Vietnamese naming is content, R11)
 * @param number    0-21 for Major Arcana; 1 (Ace) - 14 (King) for Minor Arcana
 * @param arcana    Major or Minor
 * @param suit      {@code null} for Major Arcana
 * @param meaning   interpretive content; {@link TarotCardMeaning#EMPTY} until R11 lands
 */
public record TarotCard(
        String id,
        String name,
        int number,
        TarotArcana arcana,
        TarotSuit suit,
        TarotCardMeaning meaning
) {
    public TarotCard {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(arcana, "arcana");
        meaning = meaning == null ? TarotCardMeaning.EMPTY : meaning;

        if (arcana == TarotArcana.MAJOR && suit != null) {
            throw new IllegalArgumentException("Major Arcana card " + id + " must not have a suit.");
        }
        if (arcana == TarotArcana.MINOR && suit == null) {
            throw new IllegalArgumentException("Minor Arcana card " + id + " requires a suit.");
        }
        if (arcana == TarotArcana.MAJOR && (number < 0 || number > 21)) {
            throw new IllegalArgumentException("Major Arcana number must be 0-21, got " + number);
        }
        if (arcana == TarotArcana.MINOR && (number < 1 || number > 14)) {
            throw new IllegalArgumentException("Minor Arcana number must be 1-14, got " + number);
        }
    }

    public boolean isCourtCard() {
        return arcana == TarotArcana.MINOR && number >= 11;
    }
}
