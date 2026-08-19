package io.destinyos.engines.tarot;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Lookup for every authored {@link TarotCardMeaning} (research item R11),
 * merging the five content batches (Major Arcana + four Minor suits) into
 * one table keyed by card id. {@link TarotDeck} reads from this instead of
 * using {@link TarotCardMeaning#EMPTY}.
 *
 * <p>{@code CONTENT_VERSION} travels with every reading's methodology
 * metadata, the same way {@link TarotDeck#DECK_VERSION} and
 * {@code shuffleAlgorithmVersion} already do (Master Spec section 17) — a
 * future content revision is a version bump, not a silent change.
 */
public final class TarotCardMeanings {

    public static final String CONTENT_VERSION = "1.0";

    private static final Map<String, TarotCardMeaning> ALL = build();

    private TarotCardMeanings() {
    }

    /** The authored meaning for a card id, or empty if not yet authored (should not happen once R11 is complete). */
    public static Optional<TarotCardMeaning> forId(String cardId) {
        return Optional.ofNullable(ALL.get(cardId));
    }

    public static int size() {
        return ALL.size();
    }

    private static Map<String, TarotCardMeaning> build() {
        Map<String, TarotCardMeaning> all = new LinkedHashMap<>();
        all.putAll(TarotCardMeaningsMajor.entries());
        all.putAll(TarotCardMeaningsWands.entries());
        all.putAll(TarotCardMeaningsCups.entries());
        all.putAll(TarotCardMeaningsSwords.entries());
        all.putAll(TarotCardMeaningsPentacles.entries());
        return Map.copyOf(all);
    }
}
