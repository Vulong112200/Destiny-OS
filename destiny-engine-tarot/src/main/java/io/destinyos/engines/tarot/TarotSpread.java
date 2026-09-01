package io.destinyos.engines.tarot;

import java.util.ArrayList;
import java.util.List;

/**
 * The spreads this engine supports.
 *
 * <p>A spread is an ordered list of position labels, and the number of cards
 * drawn equals the number of positions — except {@link #FREE_FORM}, which has
 * a card count but deliberately no position meanings at all.
 *
 * <h2>Every spread here is a modern construct, and that is declared (Rule D)</h2>
 *
 * <p>{@code docs/research_drafts/scenario_scope_reference.md} found no named
 * classical branch for topic-specific tarot spreads: they are
 * <em>"cấu trúc hiện đại"</em>. The one exception it recorded is the three-card
 * spread for an open question, which it called <em>"bằng chứng MẠNH … spread
 * phổ biến và LÂU ĐỜI"</em>. So {@link #PAST_PRESENT_FUTURE} and
 * {@link #SITUATION_CHALLENGE_ADVICE} rest on that finding, and the larger
 * spreads below rest on contemporary practice, which is stated rather than
 * dressed up as classical.
 *
 * <h2>Why FREE_FORM exists</h2>
 *
 * <p>The project owner's objection, and it is a fair one: a spread that labels
 * a card "PAST" is asserting that the system knows the querent's past. It does
 * not. {@link #FREE_FORM} draws a chosen number of cards and assigns them no
 * position semantics whatsoever, so the reading says what the cards are and
 * stops there. It is the honest default for anyone who does not want the
 * layout making claims on their behalf.
 */
public enum TarotSpread {

    /**
     * Three cards, past / present / future. The spread with the strongest
     * evidence behind it, per the research note above — but also the one whose
     * labels make the largest claim, which is why FREE_FORM exists.
     */
    PAST_PRESENT_FUTURE(List.of("PAST", "PRESENT", "FUTURE")),

    /** Two cards, one per option, for a decision between two courses. */
    CHOICE_A_B(List.of("CHOICE_A", "CHOICE_B")),

    /**
     * Three cards. Same count as PAST_PRESENT_FUTURE but the labels describe
     * the question rather than the querent's history, so it claims less.
     */
    SITUATION_CHALLENGE_ADVICE(List.of("SITUATION", "CHALLENGE", "ADVICE")),

    /**
     * Five cards, the "horseshoe" shape of contemporary practice: where things
     * stand, what obstructs, what helps, what action is open, where it tends.
     */
    HORSESHOE_FIVE(List.of("PRESENT", "OBSTACLE", "SUPPORT", "ACTION", "TENDENCY")),

    /**
     * Ten cards, Celtic Cross — the most widely used spread in contemporary
     * tarot practice, and the answer to "three cards is not how most tarot
     * readings actually work".
     *
     * <p>Position names follow the common contemporary ordering. They are not
     * a classical citation, and the engine does not weight them differently
     * from one another; they are labels for a layout.
     */
    CELTIC_CROSS(List.of(
            "SIGNIFICATOR", "CROSSING", "FOUNDATION", "RECENT_PAST", "CROWN",
            "NEAR_FUTURE", "SELF", "ENVIRONMENT", "HOPES_FEARS", "OUTCOME")),

    /**
     * A chosen number of cards, 1 to 10, with <strong>no position meanings</strong>.
     *
     * <p>Positions are reported as {@code CARD_1 … CARD_n} — an index, not an
     * interpretation. Nothing in the output claims a card stands for the past,
     * the outcome, or anything else. See the class Javadoc for why this is
     * here rather than being the odd one out.
     */
    FREE_FORM(List.of());

    private static final int FREE_FORM_MIN_CARDS = 1;
    private static final int FREE_FORM_MAX_CARDS = 10;

    private final List<String> positions;

    TarotSpread(List<String> positions) {
        this.positions = positions;
    }

    /**
     * Position labels for a fixed spread. Empty for {@link #FREE_FORM}, whose
     * labels depend on the card count — use {@link #positions(int)} instead.
     */
    public List<String> positions() {
        return positions;
    }

    /**
     * Position labels for this spread at the given card count.
     *
     * <p>{@code cardCount} is ignored by every fixed spread — their count is a
     * property of the spread, not a caller's choice — and is honoured only by
     * {@link #FREE_FORM}, where it produces {@code CARD_1 … CARD_n}.
     */
    public List<String> positions(int cardCount) {
        if (this != FREE_FORM) {
            return positions;
        }
        requireValidFreeFormCount(cardCount);
        List<String> labels = new ArrayList<>(cardCount);
        for (int i = 1; i <= cardCount; i++) {
            labels.add("CARD_" + i);
        }
        return List.copyOf(labels);
    }

    /** Cards this spread draws; 0 for {@link #FREE_FORM}, whose count the caller chooses. */
    public int cardCount() {
        return positions.size();
    }

    /** True if the caller must supply a card count because the spread has none of its own. */
    public boolean requiresCardCount() {
        return this == FREE_FORM;
    }

    /**
     * True if this spread assigns meanings to positions.
     *
     * <p>Lets the UI and the narrative layer avoid presenting {@code CARD_3} as
     * though it meant something.
     */
    public boolean hasPositionMeanings() {
        return this != FREE_FORM;
    }

    /** How many cards this spread will draw, given a caller-supplied count. */
    public int resolveCardCount(Integer requestedCardCount) {
        if (this != FREE_FORM) {
            return positions.size();
        }
        if (requestedCardCount == null) {
            throw new IllegalArgumentException(
                    "FREE_FORM needs a card count; it has no count of its own.");
        }
        requireValidFreeFormCount(requestedCardCount);
        return requestedCardCount;
    }

    private static void requireValidFreeFormCount(int cardCount) {
        if (cardCount < FREE_FORM_MIN_CARDS || cardCount > FREE_FORM_MAX_CARDS) {
            throw new IllegalArgumentException("FREE_FORM card count must be "
                    + FREE_FORM_MIN_CARDS + "-" + FREE_FORM_MAX_CARDS + ", got " + cardCount);
        }
    }
}
