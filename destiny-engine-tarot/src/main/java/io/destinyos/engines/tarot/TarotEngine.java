package io.destinyos.engines.tarot;

import io.destinyos.core.context.CalculationContext;
import io.destinyos.core.evidence.Evidence;
import io.destinyos.core.result.EngineResult;
import io.destinyos.core.signal.Dimension;
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
 * <p><strong>What this engine does not do:</strong> assign meaning. Without
 * the Vietnamese meaning corpus (research item R11, {@code CONTENT_REQUIRED}),
 * there is no basis to assign a {@code Dimension} or {@code Polarity} to a
 * card, so this engine returns an empty signal list. Producing a
 * {@code FINANCE_SUPPORT}-style signal from a card with no sourced meaning
 * would be exactly the fabrication CLAUDE.md Rule C forbids — the mechanical
 * draw is complete and honest; the interpretive layer is not, and says so by
 * omission rather than by invention.
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

        List<String> positions = input.spread().positions();
        List<TarotCardDraw> draws = new ArrayList<>(positions.size());
        List<Evidence> evidence = new ArrayList<>(positions.size());
        String drawGroupId = UUID.randomUUID().toString();

        for (int i = 0; i < positions.size(); i++) {
            TarotCard card = shuffled.get(i);
            TarotOrientation orientation = decideOrientation(input.orientationPolicy(), random);
            String position = positions.get(i);

            draws.add(new TarotCardDraw(position, card, orientation));
            evidence.add(buildEvidence(position, card, orientation, drawGroupId));
        }

        var reading = new TarotReading(input.spread(), draws, seed, TarotDeck.DECK_VERSION,
                SHUFFLE_ALGORITHM_VERSION, input.orientationPolicy());

        // No signals: see class Javadoc. The draw is fully valid; assigning
        // a dimension/polarity to it is not possible without R11's content.
        return EngineResult.success(reading, evidence, List.of());
    }

    private static Evidence buildEvidence(String position, TarotCard card,
                                          TarotOrientation orientation, String groupId) {
        Map<String, Object> fact = new LinkedHashMap<>();
        fact.put("position", position);
        fact.put("cardId", card.id());
        fact.put("cardName", card.name());
        fact.put("orientation", orientation.name());

        return new Evidence(
                UUID.randomUUID().toString(),
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
        return ValidationResult.ok();
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
