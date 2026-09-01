package io.destinyos.engines.tarot;

import io.destinyos.core.context.CalculationContext;
import io.destinyos.core.evidence.Evidence;
import io.destinyos.core.result.EngineResult;
import io.destinyos.core.signal.Applicability;
import io.destinyos.core.signal.Dimension;
import io.destinyos.core.signal.Polarity;
import io.destinyos.core.signal.Signal;
import io.destinyos.core.signal.Strength;
import io.destinyos.engine.EngineCapability;
import io.destinyos.engine.EngineMetadata;
import io.destinyos.engine.MetaphysicalEngine;
import io.destinyos.engine.MethodologyStatus;
import io.destinyos.engine.SupportedDateRange;
import io.destinyos.engine.ValidationResult;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

/**
 * Seeded, reproducible Tarot draws against the Rider-Waite-Smith deck
 * (Master Spec section 17, DECISION_LOG C6).
 *
 * <p><strong>Randomness policy (DECISION_LOG C6):</strong> a seed is
 * generated with a CSPRNG at draw time if the caller does not supply one,
 * which satisfies Master Spec section 17's "random thực." That seed is then
 * used to seed a deterministic {@link Random} for the shuffle and for
 * orientation, and is always reported in the result, which satisfies
 * CLAUDE.md section 6's reproducibility requirement. A draw that could not be
 * replayed could not be audited, so the two properties are sequential steps,
 * not alternatives.
 *
 * <p><strong>Signals (research item R11, resolved):</strong> each drawn card
 * emits up to five signals, one per non-empty {@link TarotCardMeaning} field
 * ({@link TarotCardMeaningsMajor} and its four suit counterparts carry the
 * authored Vietnamese content, versioned as {@link TarotCardMeanings#CONTENT_VERSION}).
 * A card with no authored meaning yields no signal for that draw — an honest
 * omission, never a fabricated one. Polarity is authored once per orientation
 * (not per dimension, see {@link TarotCardMeaning#polarityFor}); strength
 * follows the standard convention that Major Arcana carry major life themes,
 * court cards a secondary significance, and numbered Minor cards day-to-day
 * matters.
 */
public final class TarotEngine implements MetaphysicalEngine<TarotDrawInput, TarotReading> {

    public static final String ENGINE_ID = "TAROT";
    public static final String SHUFFLE_ALGORITHM_VERSION = "FISHER_YATES-1.0";

    private static final EngineMetadata METADATA = new EngineMetadata(
            ENGINE_ID,
            "Tarot",
            "TAROT_RWS",
            "1.0",
            "1.0",
            "Rider-Waite-Smith (RWS) - 78 cards: 22 Major Arcana, 56 Minor Arcana",
            "DESTINY_OS_MASTER_SPECIFICATION.md section 17",
            MethodologyStatus.CONTENT_REQUIRED
    );

    private static final EngineCapability CAPABILITY = EngineCapability.builder()
            .dimensions(Dimension.values())
            .requiresBirthTime(false)
            .requiresLocation(false)
            .requiresName(false)
            .requiresCalendar(false)
            .deterministic(true)
            .requiresSeed(true)
            .supportedDateRange(SupportedDateRange.unbounded())
            .build();

    @Override
    public EngineResult<TarotReading> calculate(TarotDrawInput input, CalculationContext context) {
        Objects.requireNonNull(input, "input");

        long seed = input.seedIfPresent().orElseGet(TarotEngine::generateSeed);
        Random random = new Random(seed);

        List<TarotCard> shuffled = new ArrayList<>(TarotDeck.allCards());
        fisherYatesShuffle(shuffled, random);

        int cardCount = input.resolvedCardCount();
        List<String> positions = input.spread().positions(cardCount);

        // Which slots of the shuffled deck get turned over. Taking from the top
        // and letting the querent point at face-down slots draw from the same
        // shuffle and are equally chance-determined; what differs is who made
        // the choice, and the reading records which it was rather than
        // presenting the two as the same event.
        List<Integer> slots = new ArrayList<>(cardCount);
        if (input.pickedByQuerent()) {
            slots.addAll(input.pickedPositions());
        } else {
            for (int i = 1; i <= cardCount; i++) {
                slots.add(i);
            }
        }

        List<TarotCardDraw> draws = new ArrayList<>(cardCount);
        List<Evidence> evidence = new ArrayList<>(cardCount);
        List<Signal> signals = new ArrayList<>();
        String drawGroupId = UUID.randomUUID().toString();

        for (int i = 0; i < cardCount; i++) {
            int slot = slots.get(i);
            TarotCard card = shuffled.get(slot - 1);
            TarotOrientation orientation = decideOrientation(input.orientationPolicy(), random);
            String position = positions.get(i);
            String evidenceId = UUID.randomUUID().toString();

            draws.add(new TarotCardDraw(position, card, orientation));
            evidence.add(buildEvidence(evidenceId, position, card, orientation, drawGroupId,
                    slot, input.pickedByQuerent(), input.spread().hasPositionMeanings()));
            signals.addAll(buildSignals(evidenceId, card, orientation, drawGroupId));
        }

        var reading = new TarotReading(input.spread(), draws, seed, TarotDeck.DECK_VERSION,
                SHUFFLE_ALGORITHM_VERSION, input.orientationPolicy());

        return EngineResult.success(reading, evidence, signals);
    }

    /**
     * One signal per non-empty meaning field on the drawn card (up to 5:
     * career, finance, relationship, decision, general), per R11's now-authored
     * content. A card with no authored meaning ({@link TarotCardMeaning#EMPTY})
     * yields no signals for that draw — an honest omission, not a fabricated one.
     *
     * <p>Polarity is authored once per orientation, not per dimension
     * ({@link TarotCardMeaning#polarityFor}) — a documented simplification,
     * see that method's Javadoc. Strength follows the standard Tarot
     * convention that Major Arcana carry the deck's major life themes, court
     * cards a secondary significance, and numbered Minor cards day-to-day
     * matters: {@code MAJOR -> STRONG}, court card -> {@code MEDIUM},
     * numbered Minor -> {@code WEAK}.
     */
    private static List<Signal> buildSignals(String evidenceId, TarotCard card,
                                             TarotOrientation orientation, String groupId) {
        TarotCardMeaning meaning = card.meaning();
        Polarity polarity = meaning.polarityFor(orientation);
        if (polarity == null) {
            return List.of();
        }
        Strength strength = card.arcana() == TarotArcana.MAJOR ? Strength.STRONG
                : card.isCourtCard() ? Strength.MEDIUM : Strength.WEAK;

        List<Signal> signals = new ArrayList<>();
        addSignalIfPresent(signals, card, Dimension.CAREER, meaning.careerMeaning(),
                polarity, strength, evidenceId, groupId);
        addSignalIfPresent(signals, card, Dimension.FINANCE, meaning.financeMeaning(),
                polarity, strength, evidenceId, groupId);
        addSignalIfPresent(signals, card, Dimension.RELATIONSHIP, meaning.relationshipMeaning(),
                polarity, strength, evidenceId, groupId);
        addSignalIfPresent(signals, card, Dimension.DECISION, meaning.decisionMeaning(),
                polarity, strength, evidenceId, groupId);
        addSignalIfPresent(signals, card, Dimension.OTHER, meaning.generalMeaning(),
                polarity, strength, evidenceId, groupId);
        return signals;
    }

    private static void addSignalIfPresent(List<Signal> signals, TarotCard card, Dimension dimension,
                                           String text, Polarity polarity, Strength strength,
                                           String evidenceId, String groupId) {
        if (text == null) {
            return;
        }
        signals.add(new Signal(
                UUID.randomUUID().toString(),
                ENGINE_ID,
                METADATA.school(),
                dimension,
                card.id() + "_" + dimension.name(),
                polarity,
                strength,
                Applicability.HIGH,
                false,
                List.of(evidenceId),
                groupId
        ));
    }

    private static Evidence buildEvidence(String evidenceId, String position, TarotCard card,
                                          TarotOrientation orientation, String groupId,
                                          int deckSlot, boolean pickedByQuerent,
                                          boolean positionHasMeaning) {
        Map<String, Object> fact = new LinkedHashMap<>();
        fact.put("position", position);
        // Whether `position` means anything. FREE_FORM reports CARD_1, CARD_2 …
        // which is an index, not an interpretation, and a renderer that treats
        // it like PAST or OUTCOME would be inventing a claim the spread
        // deliberately declines to make.
        fact.put("positionHasMeaning", positionHasMeaning);
        // Which slot of the shuffled deck this card came from, and who chose
        // it. Recorded because "the engine took the top three" and "the querent
        // pointed at slots 12, 47 and 63" are different events, and a reading
        // that cannot tell them apart cannot be audited or honestly described.
        fact.put("deckSlot", deckSlot);
        fact.put("selectionMode", pickedByQuerent ? "PICKED_BY_QUERENT" : "TOP_OF_DECK");
        fact.put("cardId", card.id());
        fact.put("cardName", card.name());
        fact.put("arcana", card.arcana().name());
        if (card.suit() != null) {
            fact.put("suit", card.suit().name());
        }
        fact.put("orientation", orientation.name());
        // The authored meaning text itself (TarotCardMeaningsMajor/Wands/Cups/
        // Swords/Pentacles) was previously only consumed internally to derive
        // Signal polarity/strength, then discarded - never reaching the API
        // response, so the frontend had nothing to render beyond the bare card
        // name. Exposing the already-authored, already research-gated (R11)
        // text here is not new content, just no longer throwing away content
        // that exists. Evidence.fact goes through Map.copyOf (disallows null
        // values), so a card with no authored meaning yet omits the "meaning"
        // key entirely rather than mapping it to null - the nested map below
        // is never copyOf'd, so its own per-dimension nulls (a real, honest
        // "not authored for this dimension") are fine.
        TarotCardMeaning meaning = card.meaning();
        if (!meaning.isEmpty()) {
            Map<String, Object> meaningFact = new LinkedHashMap<>();
            meaningFact.put("uprightKeywords", meaning.uprightKeywords());
            meaningFact.put("reversedKeywords", meaning.reversedKeywords());
            meaningFact.put("career", meaning.careerMeaning());
            meaningFact.put("finance", meaning.financeMeaning());
            meaningFact.put("relationship", meaning.relationshipMeaning());
            meaningFact.put("decision", meaning.decisionMeaning());
            meaningFact.put("general", meaning.generalMeaning());
            fact.put("meaning", meaningFact);
        }

        return new Evidence(
                evidenceId,
                TarotEngine.ENGINE_ID,
                METADATA.school(),
                "TAROT_SEEDED_DRAW",
                SHUFFLE_ALGORITHM_VERSION,
                Dimension.OTHER,
                fact,
                "seeded-draw",
                groupId,
                null
        );
    }

    /**
     * Fisher-Yates, unbiased for a fair {@link Random}. Iterating from the
     * end and drawing an inclusive [0, i] index each step is the textbook
     * form; deviating from it (e.g. always drawing [0, n)) is a well-known
     * source of shuffle bias and would be exactly the kind of "looks right"
     * mistake this class's own Javadoc warns against elsewhere in this
     * codebase.
     */
    private static void fisherYatesShuffle(List<TarotCard> cards, Random random) {
        for (int i = cards.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            TarotCard tmp = cards.get(i);
            cards.set(i, cards.get(j));
            cards.set(j, tmp);
        }
    }

    private static TarotOrientation decideOrientation(TarotOrientationPolicy policy, Random random) {
        return switch (policy) {
            case UPRIGHT_ONLY -> TarotOrientation.UPRIGHT;
            case RANDOM_INDEPENDENT_PER_CARD ->
                    random.nextBoolean() ? TarotOrientation.UPRIGHT : TarotOrientation.REVERSED;
        };
    }

    private static long generateSeed() {
        return new SecureRandom().nextLong();
    }

    @Override
    public ValidationResult validateInput(TarotDrawInput input) {
        if (input == null) {
            return ValidationResult.failed("NULL_INPUT", "Tarot draw input is required.", ENGINE_ID);
        }
        // FREE_FORM has no card count of its own, so a missing count is a
        // caller mistake rather than something to default silently - picking a
        // number here would be the engine inventing the shape of the reading.
        if (input.spread().requiresCardCount() && input.cardCount() == null) {
            return ValidationResult.failed("MISSING_CARD_COUNT",
                    "Spread FREE_FORM cần số lá muốn bốc (1-10); hệ thống không tự chọn.",
                    ENGINE_ID);
        }
        try {
            input.resolvedCardCount();
        } catch (IllegalArgumentException e) {
            return ValidationResult.failed("INVALID_CARD_COUNT", e.getMessage(), ENGINE_ID);
        }
        // An invalid pick is the caller's error and has to say what is wrong,
        // because the querent is holding a deck and will want to re-pick.
        return input.pickedPositionsProblem()
                .map(problem -> ValidationResult.failed("INVALID_PICKED_POSITIONS",
                        problem, ENGINE_ID))
                .orElseGet(ValidationResult::ok);
    }

    @Override
    public EngineCapability capability() {
        return CAPABILITY;
    }

    @Override
    public EngineMetadata metadata() {
        return METADATA;
    }
}
