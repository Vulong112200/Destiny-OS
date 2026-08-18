package io.destinyos.engines.tarot;

import java.util.List;
import java.util.Optional;

/**
 * Vietnamese interpretive content for one card (Master Spec section 17).
 *
 * <p>This is <strong>content, not algorithm</strong> (research item R11,
 * status {@code CONTENT_REQUIRED}). Every field here is legitimately absent
 * today, and the deck ships without it rather than waiting for it or
 * inventing placeholder text — Master Spec section 17 lists these fields as
 * required for a complete card, but CLAUDE.md Rule B forbids the AI
 * narrative layer manufacturing them, and this project will not manufacture
 * them in code either. A card with no meaning renders "Chưa có nội dung" in
 * the UI, never invented text.
 *
 * @param uprightKeywords    short keywords for the upright orientation
 * @param reversedKeywords   short keywords for the reversed orientation
 * @param careerMeaning      interpretation for CAREER-dimension questions
 * @param financeMeaning     interpretation for FINANCE-dimension questions
 * @param relationshipMeaning interpretation for RELATIONSHIP-dimension questions
 * @param decisionMeaning    interpretation for DECISION-dimension questions
 * @param generalMeaning     interpretation with no specific dimension
 */
public record TarotCardMeaning(
        List<String> uprightKeywords,
        List<String> reversedKeywords,
        String careerMeaning,
        String financeMeaning,
        String relationshipMeaning,
        String decisionMeaning,
        String generalMeaning
) {
    public TarotCardMeaning {
        uprightKeywords = uprightKeywords == null ? List.of() : List.copyOf(uprightKeywords);
        reversedKeywords = reversedKeywords == null ? List.of() : List.copyOf(reversedKeywords);
    }

    /** No content authored yet for any field (the current state for all 78 cards). */
    public static final TarotCardMeaning EMPTY =
            new TarotCardMeaning(List.of(), List.of(), null, null, null, null, null);

    public boolean isEmpty() {
        return this.equals(EMPTY);
    }

    public Optional<String> generalMeaningIfPresent() {
        return Optional.ofNullable(generalMeaning);
    }
}
