package io.destinyos.engines.tarot;

import java.util.List;

/**
 * The three MVP spreads (Master Spec section 17). Each spread is a fixed,
 * ordered list of position labels; the number of cards drawn equals the
 * number of positions.
 */
public enum TarotSpread {
    PAST_PRESENT_FUTURE(List.of("PAST", "PRESENT", "FUTURE")),
    CHOICE_A_B(List.of("CHOICE_A", "CHOICE_B")),
    SITUATION_CHALLENGE_ADVICE(List.of("SITUATION", "CHALLENGE", "ADVICE"));

    private final List<String> positions;

    TarotSpread(List<String> positions) {
        this.positions = positions;
    }

    public List<String> positions() {
        return positions;
    }

    public int cardCount() {
        return positions.size();
    }
}
