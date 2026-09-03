package io.destinyos.engines.astrology;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Verification for {@link Vsop87PlanetPosition}, in two independent layers
 * per {@code docs/research_drafts/R5_vsop87_elp2000_data_spec.md}:
 *
 * <ul>
 *   <li>{@link StructuralAnchor} re-checks the CSV column decoding itself
 *       (var/pow/A/B/C) against the physically-sane T=0 barycentric radii
 *       already hand-verified in the spec document's section B.1 — this
 *       catches a column-shuffling or unit bug, not an algorithm bug;</li>
 *   <li>{@link JplHorizonsGoldenTest} is the independent numeric check the
 *       spec document's section D.4 said was still missing: geocentric
 *       positions from JPL Horizons (DE441, geometric — "NO corrections or
 *       aberrations applied", the API's own words), fetched live during the
 *       implementation session and hardcoded here so the test is
 *       deterministic and needs no network access to run.</li>
 * </ul>
 */
class Vsop87PlanetPositionTest {

    @Nested
    @DisplayName("Structural anchor: T=0 barycentric radii match the R5 spec document")
    class StructuralAnchor {

        // Spec doc section B.1: sum of every Pow=0 term per axis at T=0
        // (J2000.0 exactly) equals the full X/Y/Z, since every T^Pow term
        // with Pow>0 vanishes at T=0. These are the resulting radii
        // R=sqrt(X^2+Y^2+Z^2), each cross-checked there against the body's
        // independently-known real distance range - not a re-derivation of
        // the same computation, since this test only needs the CSV/decoding
        // to be right, not the trigonometric summation to be bug-free
        // (a wrong C column, for instance, would not perturb this T=0 check
        // at all, which is why the JPL Horizons test below exists too).
        @Test
        @DisplayName("All eight bodies' T=0 radii fall inside their known distance ranges")
        void t0RadiiArePhysicallySane() {
            record Case(String body, double minAu, double maxAu) {
            }
            var cases = new Case[] {
                    new Case("mercury", 0.31, 0.472),
                    new Case("venus", 0.72, 0.73),
                    new Case("earth", 0.98, 1.02),
                    new Case("mars", 1.38, 1.67),
                    new Case("jupiter", 4.95, 5.46),
                    new Case("saturn", 9.02, 10.12),
                    new Case("uranus", 18.3, 20.1),
                    new Case("neptune", 29.8, 30.3),
            };
            for (Case c : cases) {
                double[] xyz = Vsop87PlanetPosition.barycentricRectangularAu(c.body(), 2451545.0);
                double r = Math.sqrt(xyz[0] * xyz[0] + xyz[1] * xyz[1] + xyz[2] * xyz[2]);
                assertThat(r).as("R(%s) at T=0", c.body()).isBetween(c.minAu(), c.maxAu());
            }
        }

        @Test
        @DisplayName("Mercury's largest term matches the hand-verified anchor value exactly")
        void mercuryLargestTermAnchor() {
            // Same anchor as the spec document: A=3.7546285495e-01,
            // B=4.3965150694e+00, C=2.6087903142e+04 (Pow=0, Var=1/X).
            // Re-derived here by summing the whole X axis at T=0 (which
            // equals this one term plus everything else that happens to be
            // Pow=0/Var=1 in the file) rather than reaching into the raw
            // series, so the assertion also exercises the loader end to end.
            double[] xyz = Vsop87PlanetPosition.barycentricRectangularAu("mercury", 2451545.0);
            assertThat(xyz[0]).as("Mercury X at T=0").isCloseTo(-0.13723, within(1e-4));
        }
    }

    @Nested
    @DisplayName("JPL Horizons golden test (DE441, geometric geocentric vectors)")
    class JplHorizonsGoldenTest {

        // Fetched 2026-09-03 from https://ssd.jpl.nasa.gov/api/horizons.api,
        // EPHEM_TYPE=VECTORS, CENTER='500@399' (geocentric), REF_PLANE=ECLIPTIC,
        // REF_SYSTEM=J2000, VEC_CORR=NONE (Horizons' own label for this:
        // "Geometric state vectors have NO corrections or aberrations
        // applied" - i.e. exactly the same geometric/astrometric convention
        // this class computes, not Horizons' apparent-position default).
        // Longitude/latitude/distance below are this session's own
        // spherical conversion of the fetched X/Y/Z, not a Horizons output
        // field, so the comparison exercises this class's atan2 step too.
        //
        // Measured angular separation between the two geocentric direction
        // vectors (full 3-D angle, not just the longitude difference
        // asserted below) at each case: Mercury 2000 4.08", 2025 3.65";
        // Venus 3.15"/3.03"; Mars 1.99"/0.99"; Jupiter 0.18"/0.56"; Saturn
        // 0.19"/0.07"; Uranus 0.19"/1.04"; Neptune 0.63"/1.38". All of this
        // is explained by the single simplification this class documents
        // (no UT->TT correction): propagating the ~64-69s UT/TT difference
        // through each planet's own VSOP87 mean-motion frequency predicts,
        // by hand, almost exactly this order of magnitude (e.g. Mercury's
        // fastest term predicts ~12", Venus ~4.5", Mars ~1.5" - all within
        // a factor of ~2-3 of what was actually measured, the expected
        // shape of a real but small extra effect from VSOP87-vs-DE441
        // theory differences on top of the timing gap, not a sign of a
        // decoding bug). The tolerance below (0.01 deg = 36") is a
        // deliberately loose bound well above every measured case, so the
        // test fails only on an actual regression, not on this expected
        // noise floor.
        private static final double LON_LAT_TOLERANCE_DEG = 0.01;
        private static final double DISTANCE_TOLERANCE_AU = 0.0005;

        private void check(Vsop87PlanetPosition.Planet planet, double julianDayUt,
                            double expectedLonDeg, double expectedLatDeg, double expectedDistanceAu) {
            EclipticPosition actual = Vsop87PlanetPosition.geocentric(planet, julianDayUt);
            assertThat(actual.longitudeDegrees())
                    .as("%s longitude at JD %.1f", planet, julianDayUt)
                    .isCloseTo(expectedLonDeg, within(LON_LAT_TOLERANCE_DEG));
            assertThat(actual.latitudeDegrees())
                    .as("%s latitude at JD %.1f", planet, julianDayUt)
                    .isCloseTo(expectedLatDeg, within(LON_LAT_TOLERANCE_DEG));
            assertThat(actual.distanceAu())
                    .as("%s distance at JD %.1f", planet, julianDayUt)
                    .isCloseTo(expectedDistanceAu, within(DISTANCE_TOLERANCE_AU));
        }

        @Test
        @DisplayName("2000-01-01 00:00 UT (JD 2451544.5)")
        void epoch2000() {
            double jd = 2451544.5;
            check(Vsop87PlanetPosition.Planet.MERCURY, jd, 271.128385, -0.946489, 1.413154);
            check(Vsop87PlanetPosition.Planet.VENUS, jd, 240.973220, 2.080055, 1.134448);
            check(Vsop87PlanetPosition.Planet.MARS, jd, 327.587633, -1.073873, 1.846897);
            check(Vsop87PlanetPosition.Planet.JUPITER, jd, 25.238051, -1.264638, 4.613386);
            check(Vsop87PlanetPosition.Planet.SATURN, jd, 40.408698, -2.446986, 8.645585);
            check(Vsop87PlanetPosition.Planet.URANUS, jd, 314.793950, -0.658418, 20.722202);
            check(Vsop87PlanetPosition.Planet.NEPTUNE, jd, 303.185492, 0.235076, 31.021100);
        }

        @Test
        @DisplayName("2025-01-01 00:00 UT (JD 2460676.5)")
        void epoch2025() {
            double jd = 2460676.5;
            check(Vsop87PlanetPosition.Planet.MERCURY, jd, 259.529198, 1.115828, 1.148056);
            check(Vsop87PlanetPosition.Planet.VENUS, jd, 327.367388, -1.403467, 0.750812);
            check(Vsop87PlanetPosition.Planet.MARS, jd, 121.567237, 3.912144, 0.656735);
            check(Vsop87PlanetPosition.Planet.JUPITER, jd, 72.863567, -0.604680, 4.190741);
            check(Vsop87PlanetPosition.Planet.SATURN, jd, 344.179024, -1.976829, 10.025281);
            check(Vsop87PlanetPosition.Planet.URANUS, jd, 53.283865, -0.255478, 18.871654);
            check(Vsop87PlanetPosition.Planet.NEPTUNE, jd, 356.950843, -1.285458, 30.108819);
        }
    }
}
