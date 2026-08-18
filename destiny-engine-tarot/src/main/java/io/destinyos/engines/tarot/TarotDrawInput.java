package io.destinyos.engines.tarot;

import java.util.Objects;
import java.util.Optional;

/**
 * Input to one Tarot draw.
 *
 * @param spread            which spread to fill
 * @param question          optional; Tarot may run on a bare question with
 *                          no birth data at all (Master Spec section 2)
 * @param seed              caller-supplied seed for reproducibility (tests,
 *                          golden vectors, or replaying a past reading). When
 *                          absent, {@link TarotEngine} generates one via
 *                          CSPRNG (DECISION_LOG C6) and reports it in the result
 * @param orientationPolicy defaults to {@link TarotOrientationPolicy#RANDOM_INDEPENDENT_PER_CARD}
 */
public record TarotDrawInput(
        TarotSpread spread,
        String question,
        Long seed,
        TarotOrientationPolicy orientationPolicy
) {
    public TarotDrawInput {
        Objects.requireNonNull(spread, "spread");
        orientationPolicy = orientationPolicy == null
                ? TarotOrientationPolicy.RANDOM_INDEPENDENT_PER_CARD
                : orientationPolicy;
    }

    public static TarotDrawInput of(TarotSpread spread) {
        return new TarotDrawInput(spread, null, null, null);
    }

    public static TarotDrawInput withSeed(TarotSpread spread, long seed) {
        return new TarotDrawInput(spread, null, seed, null);
    }

    public Optional<Long> seedIfPresent() {
        return Optional.ofNullable(seed);
    }
}
