package io.destinyos.engines.tarot;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Verifies the deck is structurally exactly the RWS 78-card deck
 * (Master Spec section 17) — the fact that is safe to assert without any
 * research gate, unlike card meanings (R11).
 */
class TarotDeckTest {

    @Test
    @DisplayName("The deck has exactly 78 cards")
    void hasSeventyEightCards() {
        assertThat(TarotDeck.size()).isEqualTo(78);
        assertThat(TarotDeck.allCards()).hasSize(78);
    }

    @Test
    @DisplayName("22 Major Arcana, 56 Minor Arcana")
    void arcanaSplitIsCorrect() {
        Map<TarotArcana, Long> counts = TarotDeck.allCards().stream()
                .collect(Collectors.groupingBy(TarotCard::arcana, Collectors.counting()));

        assertThat(counts.get(TarotArcana.MAJOR)).isEqualTo(22);
        assertThat(counts.get(TarotArcana.MINOR)).isEqualTo(56);
    }

    @Test
    @DisplayName("Each of the four suits has exactly 14 cards")
    void eachSuitHasFourteenCards() {
        for (TarotSuit suit : TarotSuit.values()) {
            long count = TarotDeck.allCards().stream()
                    .filter(c -> c.suit() == suit)
                    .count();
            assertThat(count).as("suit %s", suit).isEqualTo(14);
        }
    }

    @Test
    @DisplayName("All 78 card ids are unique")
    void allIdsAreUnique() {
        Set<String> ids = TarotDeck.allCards().stream()
                .map(TarotCard::id)
                .collect(Collectors.toSet());

        assertThat(ids).hasSize(78);
    }

    @Test
    @DisplayName("Strength is the 8th trump and Justice the 11th (RWS ordering, not Marseille)")
    void rwsMajorArcanaOrderingIsCorrect() {
        // This is the specific detail that distinguishes Rider-Waite-Smith
        // numbering from the older Tarot de Marseille ordering, where the
        // two are swapped. Getting this backwards would be a real, checkable
        // structural error, not a matter of interpretation.
        TarotCard strength = findMajor(8);
        TarotCard justice = findMajor(11);

        assertThat(strength.name()).isEqualTo("Strength");
        assertThat(justice.name()).isEqualTo("Justice");
    }

    @Test
    @DisplayName("The Fool is 0 and The World is 21")
    void majorArcanaBounds() {
        assertThat(findMajor(0).name()).isEqualTo("The Fool");
        assertThat(findMajor(21).name()).isEqualTo("The World");
    }

    @Test
    @DisplayName("Every suit has an Ace (1) through King (14), including four court cards")
    void minorArcanaRanksAreComplete() {
        for (TarotSuit suit : TarotSuit.values()) {
            List<Integer> ranks = TarotDeck.allCards().stream()
                    .filter(c -> c.suit() == suit)
                    .map(TarotCard::number)
                    .sorted()
                    .toList();

            assertThat(ranks).containsExactlyElementsOf(
                    java.util.stream.IntStream.rangeClosed(1, 14).boxed().toList());
        }
    }

    @Test
    @DisplayName("Court cards (Page, Knight, Queen, King) are ranks 11-14")
    void courtCardsAreIdentified() {
        long courtCount = TarotDeck.allCards().stream()
                .filter(TarotCard::isCourtCard)
                .count();

        assertThat(courtCount).isEqualTo(16); // 4 court ranks x 4 suits
    }

    @Test
    @DisplayName("Every one of the 78 cards has authored meaning content (R11, resolved)")
    void everyCardHasAuthoredMeaning() {
        for (TarotCard card : TarotDeck.allCards()) {
            assertThat(card.meaning().isEmpty())
                    .as("card %s must have authored meaning content, not EMPTY", card.id())
                    .isFalse();
            assertThat(card.meaning().uprightPolarity())
                    .as("card %s must have an authored upright polarity", card.id())
                    .isNotNull();
            assertThat(card.meaning().reversedPolarity())
                    .as("card %s must have an authored reversed polarity", card.id())
                    .isNotNull();
            assertThat(card.meaning().uprightKeywords())
                    .as("card %s must have upright keywords", card.id())
                    .isNotEmpty();
            assertThat(card.meaning().reversedKeywords())
                    .as("card %s must have reversed keywords", card.id())
                    .isNotEmpty();
            assertThat(card.meaning().generalMeaning())
                    .as("card %s must have a general meaning", card.id())
                    .isNotBlank();
        }
    }

    @Test
    @DisplayName("Major Arcana never has a suit; Minor Arcana always does")
    void suitAssignmentMatchesArcana() {
        for (TarotCard card : TarotDeck.allCards()) {
            if (card.arcana() == TarotArcana.MAJOR) {
                assertThat(card.suit()).as("card %s", card.id()).isNull();
            } else {
                assertThat(card.suit()).as("card %s", card.id()).isNotNull();
            }
        }
    }

    private static TarotCard findMajor(int number) {
        return TarotDeck.allCards().stream()
                .filter(c -> c.arcana() == TarotArcana.MAJOR && c.number() == number)
                .findFirst()
                .orElseThrow(() -> new AssertionError("No Major Arcana card numbered " + number));
    }
}
