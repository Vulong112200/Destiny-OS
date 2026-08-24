package io.destinyos.engines.iching;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Input to one hexagram casting.
 *
 * <p>Only the fields relevant to {@link #method} need be supplied; the rest
 * are ignored. See {@link IChingEngine#validateInput} for exactly which
 * fields each method requires.
 *
 * @param method       which classical procedure to use
 * @param seed         caller-supplied seed for {@link CastingMethod#THREE_COINS}
 *                     or {@link CastingMethod#YARROW}, for reproducibility
 *                     (tests, golden vectors, replaying a past reading). When
 *                     absent, {@link IChingEngine} generates one via CSPRNG
 *                     (mirroring {@code TarotEngine}, DECISION_LOG C6) and
 *                     reports it in the result
 * @param upperNumber  for {@link CastingMethod#MAI_HOA_NUMBER}: the number
 *                     forming the upper trigram (reduced mod 8; a single
 *                     multi-digit number is not accepted — see
 *                     {@link IChingEngine}'s class Javadoc)
 * @param lowerNumber  for {@link CastingMethod#MAI_HOA_NUMBER}: the number
 *                     forming the lower trigram
 * @param instant      for {@link CastingMethod#MAI_HOA_TIME}: the casting
 *                     instant. When absent, the engine uses
 *                     {@code CalculationContext.calculatedAt()} — "cast right
 *                     now" is the natural reading of this method
 */
public record IChingCastInput(
        CastingMethod method,
        Long seed,
        Integer upperNumber,
        Integer lowerNumber,
        Instant instant
) {
    public IChingCastInput {
        Objects.requireNonNull(method, "method");
    }

    public static IChingCastInput threeCoins() {
        return new IChingCastInput(CastingMethod.THREE_COINS, null, null, null, null);
    }

    public static IChingCastInput threeCoins(long seed) {
        return new IChingCastInput(CastingMethod.THREE_COINS, seed, null, null, null);
    }

    public static IChingCastInput yarrow() {
        return new IChingCastInput(CastingMethod.YARROW, null, null, null, null);
    }

    public static IChingCastInput yarrow(long seed) {
        return new IChingCastInput(CastingMethod.YARROW, seed, null, null, null);
    }

    public static IChingCastInput fromNumbers(int upperNumber, int lowerNumber) {
        return new IChingCastInput(CastingMethod.MAI_HOA_NUMBER, null, upperNumber, lowerNumber, null);
    }

    public static IChingCastInput now() {
        return new IChingCastInput(CastingMethod.MAI_HOA_TIME, null, null, null, null);
    }

    public static IChingCastInput atInstant(Instant instant) {
        return new IChingCastInput(CastingMethod.MAI_HOA_TIME, null, null, null, instant);
    }

    public Optional<Long> seedIfPresent() {
        return Optional.ofNullable(seed);
    }

    public Optional<Instant> instantIfPresent() {
        return Optional.ofNullable(instant);
    }
}
