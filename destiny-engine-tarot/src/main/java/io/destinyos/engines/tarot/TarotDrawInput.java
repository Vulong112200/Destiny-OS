package io.destinyos.engines.tarot;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * One tarot draw request.
 *
 * <h2>Who picks the cards</h2>
 *
 * <p>The deck is always shuffled deterministically from {@code seed}, so the
 * order is unknown to the querent and reproducible for audit. What
 * {@link #pickedPositions()} changes is <em>which slots of that shuffled deck
 * are turned over</em>:
 *
 * <ul>
 *   <li><strong>empty</strong> — the engine takes from the top, positions
 *       1, 2, 3… This is the original behaviour and stays the default.</li>
 *   <li><strong>supplied</strong> — the querent names slots of a face-down
 *       78-card deck and those are the cards. This is what a physical reading
 *       is: the deck is shuffled by chance, and the person choosing points at
 *       cards they cannot see.</li>
 * </ul>
 *
 * <p>The distinction is real rather than cosmetic even though both paths draw
 * from the same shuffle. Nothing about the outcome is more or less random —
 * but who exercised the choice is different, and the reading records which it
 * was rather than presenting both as the same event.
 *
 * <p>Note what this is <em>not</em>: it is not naming the card you want. The
 * querent picks slot 47 without knowing what slot 47 holds. A mode that let a
 * caller ask for The Fool by name would not be a draw at all, and is not
 * offered here.
 *
 * @param spread          the layout; see {@link TarotSpread}
 * @param question        the querent's question, optional, carried for context
 * @param seed            shuffle seed; generated with a CSPRNG when absent
 * @param orientationPolicy how upright/reversed is decided
 * @param cardCount       required by {@link TarotSpread#FREE_FORM}, which has
 *                        no count of its own; ignored by every fixed spread,
 *                        whose count is a property of the spread
 * @param pickedPositions 1-based slots of the shuffled deck, distinct, as many
 *                        as the spread draws; empty means take from the top
 */
public record TarotDrawInput(
        TarotSpread spread,
        String question,
        Long seed,
        TarotOrientationPolicy orientationPolicy,
        Integer cardCount,
        List<Integer> pickedPositions
) {

    /** Slots a querent may point at — the whole deck. */
    public static final int DECK_SIZE = 78;

    public TarotDrawInput {
        Objects.requireNonNull(spread, "spread");
        orientationPolicy = orientationPolicy == null
                ? TarotOrientationPolicy.RANDOM_INDEPENDENT_PER_CARD
                : orientationPolicy;
        pickedPositions = pickedPositions == null ? List.of() : List.copyOf(pickedPositions);
    }

    /** Kept for the existing callers and tests that predate the added fields. */
    public TarotDrawInput(TarotSpread spread, String question, Long seed,
                          TarotOrientationPolicy orientationPolicy) {
        this(spread, question, seed, orientationPolicy, null, List.of());
    }

    public static TarotDrawInput of(TarotSpread spread) {
        return new TarotDrawInput(spread, null, null, null, null, List.of());
    }

    public static TarotDrawInput withSeed(TarotSpread spread, long seed) {
        return new TarotDrawInput(spread, null, seed, null, null, List.of());
    }

    /** A FREE_FORM draw of {@code cardCount} cards, taken from the top. */
    public static TarotDrawInput freeForm(int cardCount, Long seed) {
        return new TarotDrawInput(TarotSpread.FREE_FORM, null, seed, null, cardCount, List.of());
    }

    public Optional<Long> seedIfPresent() {
        return Optional.ofNullable(seed);
    }

    /** How many cards this draw turns over. */
    public int resolvedCardCount() {
        return spread.resolveCardCount(cardCount);
    }

    /** True if the querent chose the slots rather than taking from the top. */
    public boolean pickedByQuerent() {
        return !pickedPositions.isEmpty();
    }

    /**
     * Why a set of picked slots cannot be used, or empty if they can.
     *
     * <p>Returns a reason rather than throwing so the engine can fail
     * validation with a message the caller can act on, which is what
     * {@code ValidationResult} is for — an invalid pick is a caller mistake,
     * not an engine fault.
     */
    public Optional<String> pickedPositionsProblem() {
        if (pickedPositions.isEmpty()) {
            return Optional.empty();
        }
        int expected;
        try {
            expected = resolvedCardCount();
        } catch (IllegalArgumentException e) {
            return Optional.of(e.getMessage());
        }
        if (pickedPositions.size() != expected) {
            return Optional.of("Spread " + spread + " turns over " + expected
                    + " cards, but " + pickedPositions.size() + " slots were picked.");
        }
        Set<Integer> distinct = new LinkedHashSet<>(pickedPositions);
        if (distinct.size() != pickedPositions.size()) {
            return Optional.of("The same slot was picked more than once; a card "
                    + "cannot appear twice in one draw.");
        }
        for (Integer position : pickedPositions) {
            if (position == null || position < 1 || position > DECK_SIZE) {
                return Optional.of("Picked slots must be 1-" + DECK_SIZE + ", got " + position + ".");
            }
        }
        return Optional.empty();
    }
}
