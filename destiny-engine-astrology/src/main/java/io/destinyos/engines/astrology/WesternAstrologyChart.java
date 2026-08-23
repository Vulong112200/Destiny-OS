package io.destinyos.engines.astrology;

import io.destinyos.core.context.Uncertainty;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A constructed Western astrology chart — the hard data this phase can
 * compute, with the parts it cannot named as blocked rather than absent
 * (ADR D7).
 *
 * <p><strong>What this is, by analogy with Bát Tự's Phase 8a.</strong> The
 * Tứ Trụ shipped chart construction (pillars, elements, Ten Gods) without
 * interpretation (Dụng Thần, R1) because construction was verifiable and
 * interpretation was not. This chart is the same move one level earlier:
 * it ships the angles and the Sun's position — pure spherical astronomy,
 * verified against a textbook worked example and first-principles
 * derivation — without the other nine planets or aspects, because those
 * need data (VSOP87/ELP2000 coefficients) or a decision (R6's orb policy)
 * this phase does not yet have.
 *
 * @param sun               the Sun's tropical position (reused from
 *                          {@code destiny-calendar}'s golden-tested
 *                          {@code SolarPosition})
 * @param midheaven         the MC
 * @param ascendant         the Ascendant, i.e. House 1's cusp under Whole Sign
 * @param houses            all twelve Whole Sign houses, keyed by house number
 * @param obliquityDegrees  the obliquity of the ecliptic used, for audit
 * @param ramcDegrees       the right ascension of the meridian used, for audit
 * @param zodiacSystem      always {@code "TROPICAL"} for now (R6) — carried
 *                          on the chart itself, not only in engine metadata,
 *                          since Master Spec §15 requires it travel with
 *                          every chart
 * @param houseSystem       always {@code "WHOLE_SIGN"} for now (R6)
 * @param blockedSections   reading sections this engine refuses to
 *                          approximate (planets beyond the Sun; aspects)
 * @param uncertainties     conditions that must reach the user (ADR D3)
 */
public record WesternAstrologyChart(
        ChartPoint sun,
        ChartPoint midheaven,
        ChartPoint ascendant,
        Map<AstrologicalHouse, ZodiacSign> houses,
        double obliquityDegrees,
        double ramcDegrees,
        String zodiacSystem,
        String houseSystem,
        List<BlockedSection> blockedSections,
        List<Uncertainty> uncertainties
) {
    public WesternAstrologyChart {
        Objects.requireNonNull(sun, "sun");
        Objects.requireNonNull(midheaven, "midheaven");
        Objects.requireNonNull(ascendant, "ascendant");
        Objects.requireNonNull(zodiacSystem, "zodiacSystem");
        Objects.requireNonNull(houseSystem, "houseSystem");
        houses = houses == null ? Map.of() : Map.copyOf(houses);
        blockedSections = blockedSections == null ? List.of() : List.copyOf(blockedSections);
        uncertainties = uncertainties == null ? List.of() : List.copyOf(uncertainties);
    }

    /** Which house a given sign falls in, under this chart's Whole Sign houses. */
    public AstrologicalHouse houseOf(ZodiacSign sign) {
        return WholeSignHouses.houseOf(ascendant.sign(), sign);
    }
}
