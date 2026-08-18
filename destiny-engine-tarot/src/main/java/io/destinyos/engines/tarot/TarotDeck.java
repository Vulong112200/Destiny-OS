package io.destinyos.engines.tarot;

import java.util.ArrayList;
import java.util.List;

/**
 * The standard 78-card Rider-Waite-Smith deck (Master Spec section 17): 22
 * Major Arcana plus 56 Minor Arcana across four suits of 14 ranks each.
 *
 * <p>Card names and numbering follow Waite's original ordering, in which
 * Strength is the 8th trump and Justice the 11th — the detail that
 * distinguishes RWS numbering from the older Tarot de Marseille ordering
 * (Justice 8th, Strength 11th). Getting this swap right is exactly the kind
 * of specific, checkable fact that must be correct rather than approximate,
 * even though — unlike a Bát Tự school dispute — it carries no research gate,
 * since it is a fixed, well-documented, public-domain design.
 *
 * <p>{@code DECK_VERSION} travels with every draw for reproducibility
 * (Master Spec section 17: seed + deckVersion + shuffleAlgorithmVersion).
 */
public final class TarotDeck {

    public static final String DECK_VERSION = "RWS-1.0";

    private static final String[] MAJOR_ARCANA_NAMES = {
            "The Fool", "The Magician", "The High Priestess", "The Empress",
            "The Emperor", "The Hierophant", "The Lovers", "The Chariot",
            "Strength", "The Hermit", "Wheel of Fortune", "Justice",
            "The Hanged Man", "Death", "Temperance", "The Devil",
            "The Tower", "The Star", "The Moon", "The Sun",
            "Judgement", "The World"
    };

    private static final String[] MINOR_RANK_NAMES = {
            "Ace", "Two", "Three", "Four", "Five", "Six", "Seven",
            "Eight", "Nine", "Ten", "Page", "Knight", "Queen", "King"
    };

    private static final List<TarotCard> ALL_CARDS = buildDeck();

    private TarotDeck() {
    }

    /** All 78 cards, in canonical order (22 Major, then Wands/Cups/Swords/Pentacles Ace-King). */
    public static List<TarotCard> allCards() {
        return ALL_CARDS;
    }

    public static int size() {
        return ALL_CARDS.size();
    }

    private static List<TarotCard> buildDeck() {
        List<TarotCard> cards = new ArrayList<>(78);

        for (int i = 0; i < MAJOR_ARCANA_NAMES.length; i++) {
            String id = "MAJOR_%02d_%s".formatted(i, slug(MAJOR_ARCANA_NAMES[i]));
            cards.add(new TarotCard(id, MAJOR_ARCANA_NAMES[i], i, TarotArcana.MAJOR, null,
                    TarotCardMeaning.EMPTY));
        }

        for (TarotSuit suit : TarotSuit.values()) {
            for (int rank = 1; rank <= 14; rank++) {
                String rankName = MINOR_RANK_NAMES[rank - 1];
                String id = "MINOR_%s_%02d_%s".formatted(suit.name(), rank, slug(rankName));
                String name = rankName + " of " + capitalize(suit.name());
                cards.add(new TarotCard(id, name, rank, TarotArcana.MINOR, suit,
                        TarotCardMeaning.EMPTY));
            }
        }

        return List.copyOf(cards);
    }

    private static String slug(String name) {
        return name.toUpperCase(java.util.Locale.ROOT).replace(' ', '_');
    }

    private static String capitalize(String s) {
        return s.charAt(0) + s.substring(1).toLowerCase(java.util.Locale.ROOT);
    }
}
