package io.destinyos.engines.astrology;

/**
 * The Midheaven (MC) and Ascendant — the two ecliptic longitudes every house
 * system agrees on, since they are defined by the meridian and the horizon
 * respectively, not by any house-division convention. Whole Sign houses
 * ({@link WholeSignHouses}) need only the Ascendant; MC is still computed and
 * reported because it is one of the four angular points a chart is
 * conventionally read against.
 *
 * <p><strong>Why this class carries a full derivation instead of a citation.</strong>
 * Multiple web sources state the Ascendant's tangent identically —
 * {@code tan(Asc) = -cos(RAMC) / (sin(RAMC)·cos(ε) + tan(φ)·sin(ε))} — but
 * disagree on which two quadrant-resolving {@code atan2} arguments to feed
 * that ratio through, and the two conventions found during this project's own
 * research differ by exactly 180° — one gives the Ascendant, the other the
 * Descendant. A formula that is algebraically right but 180° off is exactly
 * the "confident but wrong" failure this project exists to avoid, and no
 * citation alone could distinguish the two candidates from each other.
 *
 * <p>The formulas below were therefore re-derived from the horizon condition
 * {@code sin(h) = 0} applied to a point on the ecliptic, rather than trusted
 * from either source, and confirmed against two independently-reasoned
 * cases before being trusted:
 * <ul>
 *   <li>RAMC = 90°, φ = 0°: at the equator the rising condition reduces to
 *       hour angle H = −90° regardless of declination, which requires right
 *       ascension α = RAMC + 90° = 180°. The only ecliptic point with α = 180°
 *       is λ = 180° (Libra point, declination 0 by construction). The formula
 *       below gives exactly 180°.</li>
 *   <li>RAMC = 0°, φ = 0°: by the same reasoning α must be 90°, which on the
 *       ecliptic is λ = 90° (the summer-solstice point, the only other point
 *       with declination equal to ±ε and right ascension 90°/270°). The
 *       formula below gives exactly 90°.</li>
 * </ul>
 * Both are worked out in full in {@code ChartAnglesTest}'s Javadoc-adjacent
 * comments, not merely asserted.
 */
final class ChartAngles {

    private ChartAngles() {
    }

    /**
     * Midheaven — the ecliptic point currently on the local meridian.
     *
     * <p>Derivation: a point on the ecliptic at longitude λ has right
     * ascension α satisfying {@code cos(δ)cos(α) = cos(λ)} and
     * {@code cos(δ)sin(α) = cos(ε)sin(λ)}. MC is the point where α = RAMC
     * (hour angle zero), which — solved for λ with quadrant preserved — gives
     * the form below. Independent of latitude, unlike the Ascendant.
     */
    static double midheavenDegrees(double ramcDegrees, double obliquityDegrees) {
        double ramc = Math.toRadians(ramcDegrees);
        double eps = Math.toRadians(obliquityDegrees);
        double mc = Math.atan2(Math.sin(ramc), Math.cos(ramc) * Math.cos(eps));
        return normalizeDegrees(Math.toDegrees(mc));
    }

    /**
     * Ascendant — the ecliptic point currently rising on the eastern horizon.
     *
     * <p>Derivation: substituting the ecliptic-to-equatorial relations into
     * the horizon condition {@code sin(φ)sin(δ) + cos(φ)cos(δ)cos(H) = 0}
     * (with {@code H = RAMC − α}) and resolving which of the two horizon
     * crossings (rising vs. setting, 180° apart on the ecliptic — any two
     * points where one great circle crosses another are antipodal) is the
     * rising one via the sign of {@code sin(H)}, yields
     * {@code λ = atan2(cos(RAMC), −sin(RAMC)·cos(ε) − tan(φ)·sin(ε))}. See
     * this class's own Javadoc for the two numerical checks that confirmed
     * this over the 180°-rotated alternative found during research.
     */
    static double ascendantDegrees(double ramcDegrees, double latitudeDegrees,
                                   double obliquityDegrees) {
        double ramc = Math.toRadians(ramcDegrees);
        double phi = Math.toRadians(latitudeDegrees);
        double eps = Math.toRadians(obliquityDegrees);
        double numerator = Math.cos(ramc);
        double denominator = -Math.sin(ramc) * Math.cos(eps) - Math.tan(phi) * Math.sin(eps);
        double asc = Math.atan2(numerator, denominator);
        return normalizeDegrees(Math.toDegrees(asc));
    }

    private static double normalizeDegrees(double degrees) {
        double normalized = degrees % 360.0;
        return normalized < 0 ? normalized + 360.0 : normalized;
    }
}
