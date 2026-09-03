package io.destinyos.engines.astrology;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Geocentric ecliptic position of the seven planets beyond the Sun
 * (Mercury, Venus, Mars, Jupiter, Saturn, Uranus, Neptune), from the VSOP87
 * theory of planetary motion — Stage 1 of R5's implementation (data
 * sourcing and structural verification: {@code
 * docs/research_drafts/R5_vsop87_elp2000_data_spec.md}, sections A.1 and
 * B.1). Not yet wired into {@link WesternAstrologyEngine} — that is Stage
 * 2, tracked separately so the diff stays reviewable.
 *
 * <p><strong>Source.</strong> Bretagnon P., Francou G. 1988, <i>Astron.
 * Astrophys.</i> 202, 309 — the original VSOP87 theory published by the
 * Bureau des Longitudes/IMCCE, not Meeus's copyrighted abridged tables (R5
 * decision, 2026-08-22). Coefficients taken from the "SunPosition_VSOP87"
 * v2.0.0 MATLAB package (Meysam Mahooti, BSD-3-Clause,
 * {@code docs/origin_source/SunPosition_VSOP87_2.0.0/license.txt}), VSOP87E
 * variant (barycentric rectangular, ecliptic and equinox of J2000.0),
 * converted to the CSV resources under {@code /vsop87/} by a one-off script
 * (not part of the build); see that spec document, section B.1, for the
 * column decoding this class relies on and a hand-verified anchor value
 * (Mercury's largest term) plus a physical sanity check (all nine bodies'
 * T=0 radii falling inside their known distance ranges).
 *
 * <p><strong>Two simplifications inherited from precedent, not introduced
 * here</strong> — both already accepted by {@code destiny-calendar}'s
 * {@code SolarPosition}, which this class deliberately mirrors rather than
 * re-litigates:
 * <ol>
 *   <li><strong>No UT→TT correction.</strong> The Julian Day passed in (UT)
 *       is used directly as the time argument, exactly as
 *       {@code SolarPosition.longitudeRadians(jd)} does. The few tens of
 *       seconds of difference between UT and TT are far below the precision
 *       this class targets (arcseconds to arcminutes) once expressed as an
 *       angle — see this class's test suite for the measured end-to-end
 *       error against JPL Horizons, which already absorbs this along with
 *       simplification 2 below.</li>
 *   <li><strong>Geometric (astrometric) position, not apparent.</strong> No
 *       light-time iteration (the planet's position is evaluated at the
 *       requested instant, not the instant light left it), and no
 *       aberration or nutation correction. For the outer planets this is
 *       expected to cost a few arcminutes of apparent position; see the
 *       test suite for the measured figure against Horizons at specific
 *       dates.</li>
 * </ol>
 *
 * <p><strong>Geocentric = barycentric planet minus barycentric Earth.</strong>
 * VSOP87E already gives every body (including Earth) as a barycentric
 * rectangular vector in the same frame, so the geocentric vector is a plain
 * vector subtraction, then converted to spherical coordinates — the
 * approach the spec document's section D.1/D.2 identified as the simpler of
 * two options and is what this class implements.
 */
public final class Vsop87PlanetPosition {

    /** The seven planets computed here — Earth (needed only as the
     * subtrahend for geocentric conversion) and the Sun (already covered by
     * {@code SolarPosition}, out of scope for this task) are deliberately
     * not exposed as values of this enum. */
    public enum Planet {
        MERCURY("mercury"), VENUS("venus"), MARS("mars"), JUPITER("jupiter"),
        SATURN("saturn"), URANUS("uranus"), NEPTUNE("neptune");

        private final String resourceKey;

        Planet(String resourceKey) {
            this.resourceKey = resourceKey;
        }
    }

    private static final String EARTH_KEY = "earth";

    /** One VSOP87E series term: contribution to coordinate {@code varIndex}
     * (1=X, 2=Y, 3=Z, AU) is {@code T^powIndex * a * cos(b + c*T)}. */
    private record Term(int varIndex, int powIndex, double a, double b, double c) {
    }

    private static final Map<String, List<Term>> SERIES_BY_BODY = loadAll();

    private Vsop87PlanetPosition() {
    }

    private static Map<String, List<Term>> loadAll() {
        Map<String, List<Term>> map = new HashMap<>();
        map.put(EARTH_KEY, loadSeries(EARTH_KEY));
        for (Planet planet : Planet.values()) {
            map.put(planet.resourceKey, loadSeries(planet.resourceKey));
        }
        return Map.copyOf(map);
    }

    private static List<Term> loadSeries(String bodyResourceKey) {
        String path = "/vsop87/" + bodyResourceKey + ".csv";
        try (InputStream in = Vsop87PlanetPosition.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("Missing VSOP87 resource on classpath: " + path);
            }
            List<Term> terms = new ArrayList<>();
            try (BufferedReader reader =
                    new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                boolean sawHeaderRow = false;
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank() || line.startsWith("#")) {
                        continue;
                    }
                    if (!sawHeaderRow) {
                        // First non-comment line is the "var,pow,A,B,C" header.
                        sawHeaderRow = true;
                        continue;
                    }
                    String[] f = line.split(",");
                    terms.add(new Term(
                            Integer.parseInt(f[0]), Integer.parseInt(f[1]),
                            Double.parseDouble(f[2]), Double.parseDouble(f[3]),
                            Double.parseDouble(f[4])));
                }
            }
            return List.copyOf(terms);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read VSOP87 resource: " + path, e);
        }
    }

    /**
     * Barycentric rectangular position (AU), ecliptic and equinox of
     * J2000.0, of a body identified by its resource key ("mercury" ...
     * "neptune", or "earth"). Package-private (rather than private) only so
     * {@code Vsop87PlanetPositionTest} can re-verify the T=0 physical-radius
     * anchor from the R5 spec document directly, without duplicating the
     * geocentric-subtraction step that isn't part of that check.
     */
    static double[] barycentricRectangularAu(String bodyResourceKey, double julianDay) {
        double t = (julianDay - 2451545.0) / 365250.0; // Julian millennia from J2000.0
        double[] xyz = new double[3];
        for (Term term : SERIES_BY_BODY.get(bodyResourceKey)) {
            double contribution = Math.pow(t, term.powIndex()) * term.a()
                    * Math.cos(term.b() + term.c() * t);
            xyz[term.varIndex() - 1] += contribution;
        }
        return xyz;
    }

    /**
     * Geocentric ecliptic longitude, latitude, and distance of
     * {@code planet} at the given Julian Day.
     *
     * @param julianDay Julian Day, UT (may include a fractional
     *                   time-of-day); used directly, no UT→TT correction
     *                   (see class Javadoc)
     */
    public static EclipticPosition geocentric(Planet planet, double julianDay) {
        Objects.requireNonNull(planet, "planet");
        double[] planetXyz = barycentricRectangularAu(planet.resourceKey, julianDay);
        double[] earthXyz = barycentricRectangularAu(EARTH_KEY, julianDay);
        double x = planetXyz[0] - earthXyz[0];
        double y = planetXyz[1] - earthXyz[1];
        double z = planetXyz[2] - earthXyz[2];

        double lonDeg = Math.toDegrees(Math.atan2(y, x));
        lonDeg = ((lonDeg % 360.0) + 360.0) % 360.0;
        double latDeg = Math.toDegrees(Math.atan2(z, Math.sqrt(x * x + y * y)));
        double distanceAu = Math.sqrt(x * x + y * y + z * z);
        return new EclipticPosition(lonDeg, latDeg, distanceAu);
    }
}
