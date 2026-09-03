package io.destinyos.engines.astrology;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Geocentric ecliptic position of the Moon, from the ELP2000-82B lunar
 * theory — Stage 1 of R5's implementation (data sourcing and structural
 * verification: {@code docs/research_drafts/R5_vsop87_elp2000_data_spec.md},
 * sections A.2 and B.2). Not yet wired into {@link WesternAstrologyEngine}
 * — that is Stage 2, tracked separately so the diff stays reviewable.
 *
 * <p><strong>Source and algorithm.</strong> M. Chapront-Touze, J. Chapront,
 * <i>ELP 2000-85: a semi-analytical lunar ephemeris adequate for historical
 * times</i>, Astron. Astrophys. 190, 342 (1988), and <i>The Lunar Ephemeris
 * ELP 2000</i>, Astron. Astrophys. 124, 50 (1983) — constants fitted to
 * JPL's DE200/LE200. Data: 36 coefficient files ({@code ELP1}-{@code
 * ELP36}) from IMCCE's public distribution
 * ({@code ftp://ftp.imcce.fr/pub/ephem/moon/elp82b/}), converted to the CSV
 * resources under {@code /elp2000/} by a one-off script (not part of the
 * build; see the spec document for the column layout).
 *
 * <p><strong>Every constant and every step of this class is transcribed
 * directly from IMCCE's own reference Fortran subroutine {@code
 * elp82b_1}</strong> ({@code docs/origin_source/ELP2000-82B/elp82b_1}) —
 * nothing here is reconstructed from the PostScript notice or from a
 * secondary description, per Rule C. Variable names below intentionally
 * echo the Fortran (e.g. {@code w1}, {@code eart}, {@code peri}, {@code
 * del}, {@code zeta}, {@code ilu}/{@code ipla}, {@code delnu}) so the two
 * can be read side by side. Arrays are sized one larger than needed and
 * left 1-indexed (index 0 unused) for the same reason.
 *
 * <p><strong>Two simplifications, both inherited from the same precedent
 * {@link Vsop87PlanetPosition} already documents</strong> ({@code
 * SolarPosition}'s own accepted simplifications, not new gaps introduced
 * here):
 * <ol>
 *   <li><strong>No UT→TT correction</strong> — the Julian Day passed in
 *       (UT) is used directly as {@code elp82b_1}'s {@code tjj} (nominally
 *       TDB).</li>
 *   <li><strong>Geometric (astrometric) position</strong> — no light-time
 *       iteration, no aberration/nutation correction beyond what the theory
 *       itself models (the theory's own precession-matrix step, applied
 *       below, is part of the algorithm, not an addition by this
 *       class).</li>
 * </ol>
 *
 * <p><strong>Truncation.</strong> {@code elp82b_1} supports a
 * runtime-configurable truncation level ({@code prec}); this class does not
 * replicate that mechanism because the CSV resources for the twelve
 * largest planetary-perturbation groups (ELP10-ELP21, the only groups
 * large enough to matter — see the spec document's mục 5 truncation
 * table) were already truncated once, at data-preparation time, to
 * {@code |amplitude| >= 0.001} (arcsecond for longitude/latitude, km for
 * distance) — about 18 million times smaller than the narrowest orb this
 * project uses (5 degrees = 18000 arcseconds). All other groups (main
 * problem ELP1-3, and ELP4-9/ELP22-36) are shipped in full.
 */
public final class Elp2000MoonPosition {

    private Elp2000MoonPosition() {
    }

    // -----------------------------------------------------------------
    // Constants (elp82b_1's "ideb.eq.0" initialisation block, transcribed
    // literally).
    // -----------------------------------------------------------------

    private static final double CPI = Math.PI;
    private static final double CPI2 = 2.0 * Math.PI;
    private static final double PIS2 = Math.PI / 2.0;
    private static final double RAD = 648000.0 / Math.PI; // arcsec per radian
    private static final double DEG = Math.PI / 180.0;    // radian per degree
    private static final double C1 = 60.0;
    private static final double C2 = 3600.0;

    private static final double ATH = 384747.9806743165;
    private static final double A0 = 384747.9806448954;
    private static final double AM = 0.074801329518;
    private static final double ALFA = 0.002571881335;
    private static final double DTASM = 2.0 * ALFA / (3.0 * AM);

    // w[body][k]: body 1=W1 (Moon mean longitude), 2=W2 (perigee), 3=W3
    // (ascending node); k=1..5 are the polynomial coefficients of T^0..T^4
    // (degrees for k=1, arcsec/century^(k-1) for k=2..5, pre-divided to
    // radians below - k=1 is in degrees*DEG=radians too).
    private static final double[][] W = new double[4][6];
    private static final double[] EART = new double[6];
    private static final double[] PERI = new double[6];
    private static final double PRECES = 5029.0966 / RAD;

    // p[planet][k]: 1=Mercury..8=Neptune, k=1=constant (rad), k=2=T-coeff (rad/century)
    private static final double[][] P = new double[9][3];

    private static final double DELNU;
    private static final double DELE = 0.01789 / RAD;
    private static final double DELG = -0.08066 / RAD;
    private static final double DELNP;
    private static final double DELEP = -0.12879 / RAD;

    // del[arg][k]: arg 1=D, 2=l', 3=l, 4=F ; k=1..5 polynomial coefficients.
    private static final double[][] DEL = new double[5][6];
    private static final double[] ZETA = new double[3];

    private static final double P1 = 0.10180391e-4;
    private static final double P2 = 0.47020439e-6;
    private static final double P3 = -0.5417367e-9;
    private static final double P4 = -0.2507948e-11;
    private static final double P5 = 0.463486e-14;
    private static final double Q1 = -0.113469002e-3;
    private static final double Q2 = 0.12372674e-6;
    private static final double Q3 = 0.1265417e-8;
    private static final double Q4 = -0.1371808e-11;
    private static final double Q5 = -0.320334e-14;

    static {
        W[1][1] = (218 + 18 / C1 + 59.95571 / C2) * DEG;
        W[2][1] = (83 + 21 / C1 + 11.67475 / C2) * DEG;
        W[3][1] = (125 + 2 / C1 + 40.39816 / C2) * DEG;
        EART[1] = (100 + 27 / C1 + 59.22059 / C2) * DEG;
        PERI[1] = (102 + 56 / C1 + 14.42753 / C2) * DEG;

        W[1][2] = 1732559343.73604 / RAD;
        W[2][2] = 14643420.2632 / RAD;
        W[3][2] = -6967919.3622 / RAD;
        EART[2] = 129597742.2758 / RAD;
        PERI[2] = 1161.2283 / RAD;

        W[1][3] = -5.8883 / RAD;
        W[2][3] = -38.2776 / RAD;
        W[3][3] = 6.3622 / RAD;
        EART[3] = -0.0202 / RAD;
        PERI[3] = 0.5327 / RAD;

        W[1][4] = 0.6604e-2 / RAD;
        W[2][4] = -0.45047e-1 / RAD;
        W[3][4] = 0.7625e-2 / RAD;
        EART[4] = 0.9e-5 / RAD;
        PERI[4] = -0.138e-3 / RAD;

        W[1][5] = -0.3169e-4 / RAD;
        W[2][5] = 0.21301e-3 / RAD;
        W[3][5] = -0.3586e-4 / RAD;
        EART[5] = 0.15e-6 / RAD;
        PERI[5] = 0.0;

        P[1][1] = (252 + 15 / C1 + 3.25986 / C2) * DEG;
        P[2][1] = (181 + 58 / C1 + 47.28305 / C2) * DEG;
        P[3][1] = EART[1];
        P[4][1] = (355 + 25 / C1 + 59.78866 / C2) * DEG;
        P[5][1] = (34 + 21 / C1 + 5.34212 / C2) * DEG;
        P[6][1] = (50 + 4 / C1 + 38.89694 / C2) * DEG;
        P[7][1] = (314 + 3 / C1 + 18.01841 / C2) * DEG;
        P[8][1] = (304 + 20 / C1 + 55.19575 / C2) * DEG;

        P[1][2] = 538101628.68898 / RAD;
        P[2][2] = 210664136.43355 / RAD;
        P[3][2] = EART[2];
        P[4][2] = 68905077.59284 / RAD;
        P[5][2] = 10925660.42861 / RAD;
        P[6][2] = 4399609.65932 / RAD;
        P[7][2] = 1542481.19393 / RAD;
        P[8][2] = 786550.32074 / RAD;

        DELNU = 0.55604 / RAD / W[1][2];
        DELNP = -0.06424 / RAD / W[1][2];

        for (int k = 1; k <= 5; k++) {
            DEL[1][k] = W[1][k] - EART[k];
            DEL[4][k] = W[1][k] - W[3][k];
            DEL[3][k] = W[1][k] - W[2][k];
            DEL[2][k] = EART[k] - PERI[k];
        }
        DEL[1][1] = DEL[1][1] + CPI;
        ZETA[1] = W[1][1];
        ZETA[2] = W[1][2] + PRECES;
    }

    // -----------------------------------------------------------------
    // Data rows, loaded from the CSV resources.
    // -----------------------------------------------------------------

    /** ELP1-3: main problem. {@code d,lp,l,f} are the Delaunay multipliers
     * (ilu in the Fortran); {@code a} is the amplitude; {@code c2..c6} are
     * the five partial derivatives used only by the DE200/LE200 fit
     * correction ({@code coef(2)..coef(6)} in the Fortran). */
    private record MainTerm(int d, int lp, int l, int f,
                             double a, double c2, double c3, double c4, double c5, double c6) {
    }

    /** ELP4-9, ELP22-36: Earth-figure/tidal/Moon-figure/relativistic/solar
     * eccentricity perturbations. */
    private record StdTerm(int iz, int d, int lp, int l, int f, double phaDeg, double amplitude) {
    }

    /** ELP10-21: planetary perturbations; {@code ipla} has 11 multipliers. */
    private record PlanetaryTerm(int[] ipla, double phaDeg, double amplitude) {
    }

    private static final MainTerm[][] MAIN_TERMS = new MainTerm[4][]; // index 1..3
    private static final StdTerm[][] STD_TERMS = new StdTerm[37][];   // index 4..9, 22..36
    private static final PlanetaryTerm[][] PLANETARY_TERMS = new PlanetaryTerm[22][]; // index 10..21

    static {
        for (int n = 1; n <= 3; n++) {
            MAIN_TERMS[n] = loadMain(n);
        }
        for (int n = 4; n <= 9; n++) {
            STD_TERMS[n] = loadStd(n);
        }
        for (int n = 10; n <= 21; n++) {
            PLANETARY_TERMS[n] = loadPlanetary(n);
        }
        for (int n = 22; n <= 36; n++) {
            STD_TERMS[n] = loadStd(n);
        }
    }

    private static BufferedReader open(int group) throws IOException {
        String path = "/elp2000/elp" + group + ".csv";
        InputStream in = Elp2000MoonPosition.class.getResourceAsStream(path);
        if (in == null) {
            throw new IllegalStateException("Missing ELP2000 resource on classpath: " + path);
        }
        return new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
    }

    private static List<String[]> dataRows(int group) {
        try (BufferedReader reader = open(group)) {
            List<String[]> rows = new ArrayList<>();
            String line;
            boolean sawHeaderRow = false;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank() || line.startsWith("#")) {
                    continue;
                }
                if (!sawHeaderRow) {
                    sawHeaderRow = true; // column-name header row
                    continue;
                }
                rows.add(line.split(","));
            }
            return rows;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read ELP2000 resource elp" + group + ".csv", e);
        }
    }

    private static MainTerm[] loadMain(int group) {
        List<String[]> rows = dataRows(group);
        MainTerm[] terms = new MainTerm[rows.size()];
        for (int i = 0; i < rows.size(); i++) {
            String[] f = rows.get(i);
            terms[i] = new MainTerm(
                    Integer.parseInt(f[0]), Integer.parseInt(f[1]),
                    Integer.parseInt(f[2]), Integer.parseInt(f[3]),
                    Double.parseDouble(f[4]), Double.parseDouble(f[5]),
                    Double.parseDouble(f[6]), Double.parseDouble(f[7]),
                    Double.parseDouble(f[8]), Double.parseDouble(f[9]));
        }
        return terms;
    }

    private static StdTerm[] loadStd(int group) {
        List<String[]> rows = dataRows(group);
        StdTerm[] terms = new StdTerm[rows.size()];
        for (int i = 0; i < rows.size(); i++) {
            String[] f = rows.get(i);
            terms[i] = new StdTerm(
                    Integer.parseInt(f[0]), Integer.parseInt(f[1]),
                    Integer.parseInt(f[2]), Integer.parseInt(f[3]), Integer.parseInt(f[4]),
                    Double.parseDouble(f[5]), Double.parseDouble(f[6]));
        }
        return terms;
    }

    private static PlanetaryTerm[] loadPlanetary(int group) {
        List<String[]> rows = dataRows(group);
        PlanetaryTerm[] terms = new PlanetaryTerm[rows.size()];
        for (int i = 0; i < rows.size(); i++) {
            String[] f = rows.get(i);
            int[] ipla = new int[11];
            for (int j = 0; j < 11; j++) {
                ipla[j] = Integer.parseInt(f[j]);
            }
            terms[i] = new PlanetaryTerm(ipla, Double.parseDouble(f[11]), Double.parseDouble(f[12]));
        }
        return terms;
    }

    /** Which spherical coordinate (1=longitude, 2=latitude, 3=distance)
     * group {@code ific} contributes to — {@code mod(ific-1,3)+1} in the
     * Fortran, a pure function of the group number. */
    private static int component(int ific) {
        return ((ific - 1) % 3) + 1;
    }

    /**
     * Geocentric ecliptic longitude, latitude, and distance of the Moon at
     * the given Julian Day.
     *
     * @param julianDay Julian Day, UT (may include a fractional
     *                   time-of-day); used directly, no UT→TT correction
     *                   (see class Javadoc)
     */
    public static EclipticPosition geocentric(double julianDay) {
        double[] t = new double[6]; // t[1]=1, t[2]=T, t[3]=T^2, t[4]=T^3, t[5]=T^4
        t[1] = 1.0;
        t[2] = (julianDay - 2451545.0) / 36525.0;
        t[3] = t[2] * t[2];
        t[4] = t[3] * t[2];
        t[5] = t[4] * t[2];

        double[] r = new double[4]; // r[1]=longitude (arcsec sum), r[2]=latitude (arcsec sum), r[3]=distance (km)

        // --- Main problem (ELP1-3) ---
        for (int ific = 1; ific <= 3; ific++) {
            int iv = component(ific);
            for (MainTerm term : MAIN_TERMS[ific]) {
                double x = term.a();
                if (ific == 3) {
                    x = x - 2.0 * x * DELNU / 3.0;
                }
                double tgv = term.c2() + DTASM * term.c6();
                x = x + tgv * (DELNP - AM * DELNU) + term.c3() * DELG + term.c4() * DELE
                        + term.c5() * DELEP;

                double y = 0.0;
                for (int k = 1; k <= 5; k++) {
                    y += term.d() * DEL[1][k] * t[k];
                    y += term.lp() * DEL[2][k] * t[k];
                    y += term.l() * DEL[3][k] * t[k];
                    y += term.f() * DEL[4][k] * t[k];
                }
                if (iv == 3) {
                    y += PIS2;
                }
                y = y % CPI2;
                r[iv] += x * Math.sin(y);
            }
        }

        // --- Earth figure / tides / Moon figure / relativistic / solar
        //     eccentricity perturbations (ELP4-9, ELP22-36) ---
        int[] stdGroups = {4, 5, 6, 7, 8, 9, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36};
        for (int ific : stdGroups) {
            int iv = component(ific);
            for (StdTerm term : STD_TERMS[ific]) {
                double x = term.amplitude();
                if ((ific >= 7 && ific <= 9) || (ific >= 25 && ific <= 27)) {
                    x = x * t[2];
                }
                if (ific >= 34 && ific <= 36) {
                    x = x * t[3];
                }
                double y = term.phaDeg() * DEG;
                for (int k = 1; k <= 2; k++) {
                    y += term.iz() * ZETA[k] * t[k];
                    y += term.d() * DEL[1][k] * t[k];
                    y += term.lp() * DEL[2][k] * t[k];
                    y += term.l() * DEL[3][k] * t[k];
                    y += term.f() * DEL[4][k] * t[k];
                }
                y = y % CPI2;
                r[iv] += x * Math.sin(y);
            }
        }

        // --- Planetary perturbations (ELP10-21) ---
        for (int ific = 10; ific <= 21; ific++) {
            int iv = component(ific);
            for (PlanetaryTerm term : PLANETARY_TERMS[ific]) {
                double x = term.amplitude();
                if ((ific >= 13 && ific <= 15) || (ific >= 19 && ific <= 21)) {
                    x = x * t[2];
                }
                int[] ipla = term.ipla(); // 0-indexed array, ipla[0]..ipla[10] = Fortran ipla(1)..ipla(11)
                double y = term.phaDeg() * DEG;
                if (ific < 16) {
                    for (int k = 1; k <= 2; k++) {
                        double z = ipla[8] * DEL[1][k] + ipla[9] * DEL[3][k] + ipla[10] * DEL[4][k];
                        y += z * t[k];
                        for (int i = 1; i <= 8; i++) {
                            y += ipla[i - 1] * P[i][k] * t[k];
                        }
                    }
                } else {
                    for (int k = 1; k <= 2; k++) {
                        for (int i = 1; i <= 4; i++) {
                            y += ipla[i + 6] * DEL[i][k] * t[k]; // ipla(i+7) in Fortran, 1-based
                        }
                        for (int i = 1; i <= 7; i++) {
                            y += ipla[i - 1] * P[i][k] * t[k];
                        }
                    }
                }
                y = y % CPI2;
                r[iv] += x * Math.sin(y);
            }
        }

        // --- Change of coordinates (elp82b_1's "500 continue" block) ---
        double longitudeRad = r[1] / RAD + W[1][1] + W[1][2] * t[2] + W[1][3] * t[3]
                + W[1][4] * t[4] + W[1][5] * t[5];
        double latitudeRad = r[2] / RAD;
        double distanceKm = r[3] * A0 / ATH;

        double x1 = distanceKm * Math.cos(latitudeRad);
        double x2 = x1 * Math.sin(longitudeRad);
        x1 = x1 * Math.cos(longitudeRad);
        double x3 = distanceKm * Math.sin(latitudeRad);

        // Precession from the mean dynamical ecliptic of date to the
        // ecliptic and equinox of J2000.0.
        double pw = (P1 + P2 * t[2] + P3 * t[3] + P4 * t[4] + P5 * t[5]) * t[2];
        double qw = (Q1 + Q2 * t[2] + Q3 * t[3] + Q4 * t[4] + Q5 * t[5]) * t[2];
        double ra = 2.0 * Math.sqrt(1 - pw * pw - qw * qw);
        double pwqw = 2.0 * pw * qw;
        double pw2 = 1 - 2.0 * pw * pw;
        double qw2 = 1 - 2.0 * qw * qw;
        pw = pw * ra;
        qw = qw * ra;

        double rx = pw2 * x1 + pwqw * x2 + pw * x3;
        double ry = pwqw * x1 + qw2 * x2 - qw * x3;
        double rz = -pw * x1 + qw * x2 + (pw2 + qw2 - 1) * x3;

        double distanceAu = Math.sqrt(rx * rx + ry * ry + rz * rz) / AU_KM;
        double lonDeg = Math.toDegrees(Math.atan2(ry, rx));
        lonDeg = ((lonDeg % 360.0) + 360.0) % 360.0;
        double latDeg = Math.toDegrees(Math.atan2(rz, Math.sqrt(rx * rx + ry * ry)));

        return new EclipticPosition(lonDeg, latDeg, distanceAu);
    }

    /** IAU 2012 exact conversion, used only to express the Moon's distance
     * in the same unit ({@code EclipticPosition.distanceAu}) the planets
     * use — not part of the ELP2000-82B algorithm itself. */
    private static final double AU_KM = 149597870.7;
}
