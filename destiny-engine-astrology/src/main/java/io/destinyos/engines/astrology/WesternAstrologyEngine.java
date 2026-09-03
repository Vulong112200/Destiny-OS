package io.destinyos.engines.astrology;

import io.destinyos.calendar.JulianDay;
import io.destinyos.calendar.SolarPosition;
import io.destinyos.core.context.CalculationContext;
import io.destinyos.core.evidence.Evidence;
import io.destinyos.core.result.EngineResult;
import io.destinyos.core.result.EngineStatus;
import io.destinyos.core.result.EngineWarning;
import io.destinyos.core.result.ResearchReference;
import io.destinyos.core.signal.Dimension;
import io.destinyos.engine.EngineCapability;
import io.destinyos.engine.EngineMetadata;
import io.destinyos.engine.MetaphysicalEngine;
import io.destinyos.engine.MethodologyStatus;
import io.destinyos.engine.SupportedDateRange;
import io.destinyos.engine.ValidationResult;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Western astrology — chart angles, the Sun, the Moon, the seven planets
 * Mercury..Neptune, and the five Ptolemaic aspects between them (Phase 11,
 * research items R5/R6, R5 planets/Moon closed 2026-09-03).
 *
 * <p><strong>School (Rule D).</strong> Tropical zodiac, Whole Sign houses —
 * both owner decisions recorded 2026-08-23 in {@code docs/RESEARCH_BLOCKERS.md}
 * R6. Nothing here computes a sidereal position or an ayanamsa, and nothing
 * here divides a house by time (as Placidus/Koch would).
 *
 * <p><strong>What is verified and what is not.</strong> Every position on this
 * chart is pure spherical astronomy, each independently checkable and checked
 * before being trusted here:
 * <ul>
 *   <li>the obliquity of the ecliptic and Greenwich Mean Sidereal Time
 *       ({@link SiderealTime}), against Jean Meeus's own textbook worked
 *       example (1994-06-16 18ʰ UT → GMST 174.7711135°);</li>
 *   <li>the Midheaven and Ascendant ({@link ChartAngles}), re-derived from
 *       the horizon condition after research found two web sources whose
 *       Ascendant formulas differed by exactly 180° — see that class's
 *       Javadoc for the two independently-reasoned numerical checks that
 *       resolved which was right;</li>
 *   <li>the Sun's tropical position, which is not recomputed here at all —
 *       it is {@code destiny-calendar}'s {@link SolarPosition}, the same
 *       Meeus low-precision series already golden-tested for the lunar
 *       calendar and Bát Tự. Its ~0.01° accuracy limit, which research item
 *       R19 correctly calls disqualifying for Bát Tự's month boundaries, is
 *       roughly two orders of magnitude finer than astrology's narrowest
 *       orb, so no additional precision work was needed to reuse it here;</li>
 *   <li>the Moon and the seven planets ({@link Vsop87PlanetPosition},
 *       {@link Elp2000MoonPosition}) — VSOP87 (IMCCE/Bretagnon &amp; Francou)
 *       and ELP2000-82B (IMCCE/Chapront-Touzé &amp; Chapront), both
 *       cross-checked against JPL Horizons (DE441), an independent
 *       third-party ephemeris; the measured discrepancy (arcseconds to a few
 *       tens of arcseconds for the Moon) matches the discrepancy predicted
 *       from the one accepted simplification (no light-time/aberration
 *       correction), not an unexplained error;</li>
 *   <li>aspects ({@link AspectFinder}) — pure geometry (angular separation
 *       against R6's declared orb table) once every body's longitude is
 *       known, so nothing further to verify beyond the positions above.</li>
 * </ul>
 *
 * <p><strong>What is deliberately not computed, and reported as blocked
 * rather than silently absent</strong> (ADR D7, the same device Bát Tự uses
 * for Dụng Thần and Day Master strength): <strong>Pluto's position.</strong>
 * VSOP87 does not cover Pluto (its orbit needs a different theory — Pluto is
 * in a 3:2 mean-motion resonance with Neptune and is not well modelled by a
 * Fourier-series planetary theory the way the eight VSOP87 bodies are), and
 * no source for one has been evaluated. See
 * {@code BlockedSection("PLUTO_POSITION")}.
 *
 * <p>Emits no signals for the same reason Bát Tự's Phase 8a does not: a
 * signal needs interpretive content (favourable/unfavourable meaning per
 * sign/house/aspect), which has not been authored for anything beyond the
 * Sun and Ascendant ({@link AstrologyMeanings}) — the chart is real hard data
 * now for nine of ten bodies and every aspect among them, but still nothing
 * to fuse into a reading.
 */
public final class WesternAstrologyEngine
        implements MetaphysicalEngine<WesternAstrologyInput, WesternAstrologyChart> {

    public static final String ENGINE_ID = "WESTERN_ASTROLOGY";
    public static final String METHODOLOGY_ID = "WESTERN_ASTROLOGY_CHART_ANGLES";
    public static final String METHODOLOGY_VERSION = "2.0";
    public static final String RULE_VERSION = "1.0";
    public static final String SCHOOL =
            "Chiêm tinh phương Tây — Hoàng đạo Tropical, hệ nhà Whole Sign";

    /** R5's own methodology (Rule D) for the Moon + seven planets — a named data source, not this engine's chart-angle methodology. */
    public static final String PLANETS_METHODOLOGY_ID = "WESTERN_ASTROLOGY_PLANETS_VSOP87_ELP2000";
    public static final String PLANETS_METHODOLOGY_VERSION = "1.0";
    public static final String PLANETS_SCHOOL =
            "VSOP87 (Bretagnon & Francou 1988, qua gói Mahooti BSD-3) cho 7 hành tinh; "
                    + "ELP2000-82B (Chapront-Touzé & Chapront 1988/1983, qua IMCCE) cho Mặt Trăng";

    /** R6's own methodology (Rule D) for aspects — a named orb convention, not this engine's chart-angle methodology. */
    public static final String ASPECTS_METHODOLOGY_ID = "WESTERN_ASTROLOGY_ASPECTS_MODERN_FLAT_ORB";
    public static final String ASPECTS_METHODOLOGY_VERSION = "1.0";
    public static final String ASPECTS_SCHOOL =
            "Năm góc chiếu Ptolemaic, orb phẳng kiểu Sakoian & Acker (1973) với biên độ rộng "
                    + "hơn cho Mặt Trời/Mặt Trăng — không phải moiety cổ điển (Lilly), xem R6";

    private static final Set<String> LUMINARIES = Set.of("SUN", "MOON");

    public static final String SOURCE =
            "Obliquity of the ecliptic and Greenwich Mean Sidereal Time: Jean Meeus, "
                    + "Astronomical Algorithms (1998), equations 22.2 and 12.4; GMST formula "
                    + "independently corroborated by astrogreg.com's IAU-1982 citation and "
                    + "verified end to end against Meeus's own worked example (1994-06-16 18h "
                    + "UT -> GMST 174.7711135 degrees). Midheaven and Ascendant formulas "
                    + "re-derived from the horizon condition (sin(altitude)=0 for a point on "
                    + "the ecliptic) after two independent web sources for the Ascendant were "
                    + "found to disagree by exactly 180 degrees on which atan2 quadrant is "
                    + "correct; resolved via two independently-reasoned numerical cases "
                    + "(RAMC=90/lat=0 -> 180 deg; RAMC=0/lat=0 -> 90 deg), see ChartAngles's "
                    + "Javadoc. Sun position: reused from destiny-calendar's SolarPosition "
                    + "(Meeus low-precision solar series), not recomputed. Whole Sign house "
                    + "system and tropical zodiac: owner decision, 2026-08-22/23, "
                    + "docs/RESEARCH_BLOCKERS.md R6 (Whole Sign chosen for correctness at "
                    + "every latitude, including inside the polar circle where Placidus/Koch "
                    + "are undefined; tropical chosen to match this project's own "
                    + "'Western Astrology' methodology name, which needs no ayanamsa). "
                    + "Moon and seven planets: VSOP87 (Bretagnon & Francou 1988, IMCCE data via "
                    + "the Mahooti/Ofek BSD-3-licensed package) and ELP2000-82B (Chapront-Touze "
                    + "& Chapront 1988/1983, transcribed directly from IMCCE's own reference "
                    + "Fortran, docs/research_drafts/R5_vsop87_elp2000_data_spec.md); geometric "
                    + "geocentric positions, no light-time/aberration correction, cross-checked "
                    + "against JPL Horizons (DE441) with measured error consistent with that "
                    + "one documented simplification, see Vsop87PlanetPosition/"
                    + "Elp2000MoonPosition. Aspects: five Ptolemaic aspects, flat orbs with a "
                    + "luminary allowance, owner decision 2026-08-30 (docs/RESEARCH_BLOCKERS.md "
                    + "R6); applying/separating computed from a finite-difference longitude "
                    + "derivative, not looked up.";

    private static final EngineMetadata METADATA = new EngineMetadata(
            ENGINE_ID,
            "Chiêm tinh phương Tây — Góc lá số",
            METHODOLOGY_ID,
            METHODOLOGY_VERSION,
            "1.0",
            SCHOOL,
            SOURCE,
            // Chart angles and the Sun are verified; the other nine planets
            // and aspects are not - the same CONTENT_REQUIRED-shaped honesty
            // Tarot carried before its meaning corpus was authored, and
            // BAZI_TUBINH_CHART carries today for the same reason.
            MethodologyStatus.CONTENT_REQUIRED
    );

    private static final EngineCapability CAPABILITY = EngineCapability.builder()
            // Declared so the Applicability layer keeps scheduling this
            // engine once interpretive content exists - it contributes no
            // signal to any of them yet, same as BAZI's Phase 8a.
            .dimensions(Dimension.CAREER, Dimension.FINANCE, Dimension.RELATIONSHIP,
                    Dimension.DECISION, Dimension.OTHER)
            .requiresBirthTime(true)
            .requiresLocation(true)
            .requiresName(false)
            .requiresCalendar(false)
            .deterministic(true)
            .requiresSeed(false)
            // Matches SolarPosition's own documented low-precision-series
            // validity window rather than an arbitrarily chosen range.
            .supportedDateRange(SupportedDateRange.of(LocalDate.of(1900, 1, 1),
                    LocalDate.of(2100, 12, 31)))
            .build();

    /** Order fixed here (not alphabetical) so the frontend can render a stable, sensible row order. */
    private static final List<Vsop87PlanetPosition.Planet> PLANET_ORDER = List.of(
            Vsop87PlanetPosition.Planet.MERCURY, Vsop87PlanetPosition.Planet.VENUS,
            Vsop87PlanetPosition.Planet.MARS, Vsop87PlanetPosition.Planet.JUPITER,
            Vsop87PlanetPosition.Planet.SATURN, Vsop87PlanetPosition.Planet.URANUS,
            Vsop87PlanetPosition.Planet.NEPTUNE);

    private static final List<BlockedSection> BLOCKED_SECTIONS = List.of(
            new BlockedSection("PLUTO_POSITION",
                    "Vị trí Sao Diêm Vương (Pluto)", "R5",
                    "VSOP87 không phủ Sao Diêm Vương — quỹ đạo của nó nằm trong cộng hưởng 3:2 "
                            + "với Sao Hải Vương nên không mô hình hóa tốt bằng một chuỗi Fourier "
                            + "kiểu VSOP87 như 8 thiên thể còn lại; cần một lý thuyết/nguồn dữ "
                            + "liệu khác hẳn (ví dụ khớp số từ JPL), chưa được khảo sát. Mặt "
                            + "Trăng và 7 hành tinh Mercury-Neptune đã có (VSOP87/ELP2000-82B, "
                            + "R5 đóng 2026-09-03) nên Sao Diêm Vương là phần duy nhất còn thiếu "
                            + "để đủ 10 thiên thể cổ điển.",
                    List.of("Bộ hệ số khớp số (Chebyshev) kiểu JPL — chưa khảo sát",
                            "Các yếu tố quỹ đạo gần đúng (vd. Meeus ch.37) — độ chính xác thấp "
                                    + "hơn, chưa khảo sát")));

    @Override
    public EngineResult<WesternAstrologyChart> calculate(WesternAstrologyInput input,
                                                          CalculationContext context) {
        Objects.requireNonNull(input, "input");

        LocalDateTime utcLocal = LocalDateTime.ofInstant(input.utcInstant(), ZoneOffset.UTC);
        double julianDateUt = JulianDay.fromLocalDateTime(utcLocal, 0.0);

        double t = SiderealTime.julianCenturies(julianDateUt);
        double obliquity = ObliquityOfEcliptic.meanObliquityDegrees(t);
        double ramc = SiderealTime.localSiderealTimeDegrees(julianDateUt,
                input.longitudeDegreesEast());

        double sunLongitudeDegrees = Math.toDegrees(SolarPosition.longitudeRadians(julianDateUt));
        double mcLongitude = ChartAngles.midheavenDegrees(ramc, obliquity);
        double ascLongitude = ChartAngles.ascendantDegrees(ramc, input.latitudeDegrees(), obliquity);

        ChartPoint sun = ChartPoint.of(sunLongitudeDegrees);
        ChartPoint midheaven = ChartPoint.of(mcLongitude);
        ChartPoint ascendant = ChartPoint.of(ascLongitude);
        Map<AstrologicalHouse, ZodiacSign> houses = WholeSignHouses.cusps(ascendant.sign());

        Map<String, WesternAstrologyChart.BodyPosition> bodies = new LinkedHashMap<>();
        Map<String, Double> longitudesNow = new LinkedHashMap<>();
        Map<String, Double> longitudesShortlyAfter = new LinkedHashMap<>();
        longitudesNow.put("SUN", sunLongitudeDegrees);
        longitudesShortlyAfter.put("SUN",
                Math.toDegrees(SolarPosition.longitudeRadians(julianDateUt + 1.0)));

        EclipticPosition moonNow = Elp2000MoonPosition.geocentric(julianDateUt);
        bodies.put("MOON", new WesternAstrologyChart.BodyPosition(
                ChartPoint.of(moonNow.longitudeDegrees()), moonNow.latitudeDegrees(),
                moonNow.distanceAu()));
        longitudesNow.put("MOON", moonNow.longitudeDegrees());
        longitudesShortlyAfter.put("MOON",
                Elp2000MoonPosition.geocentric(julianDateUt + 1.0).longitudeDegrees());

        for (Vsop87PlanetPosition.Planet planet : PLANET_ORDER) {
            String name = planet.name();
            EclipticPosition position = Vsop87PlanetPosition.geocentric(planet, julianDateUt);
            bodies.put(name, new WesternAstrologyChart.BodyPosition(
                    ChartPoint.of(position.longitudeDegrees()), position.latitudeDegrees(),
                    position.distanceAu()));
            longitudesNow.put(name, position.longitudeDegrees());
            longitudesShortlyAfter.put(name,
                    Vsop87PlanetPosition.geocentric(planet, julianDateUt + 1.0).longitudeDegrees());
        }

        List<Aspect> aspects = AspectFinder.findAll(longitudesNow, longitudesShortlyAfter, LUMINARIES);

        var chart = new WesternAstrologyChart(sun, midheaven, ascendant, bodies, aspects, houses,
                obliquity, ramc, "TROPICAL", "WHOLE_SIGN", BLOCKED_SECTIONS, List.of());

        List<EngineWarning> warnings = new ArrayList<>();
        for (BlockedSection blocked : BLOCKED_SECTIONS) {
            warnings.add(EngineWarning.critical(
                    "ASTROLOGY_SECTION_BLOCKED_" + blocked.sectionId(),
                    blocked.displayNameVi() + ": " + blocked.reasonVi()
                            + " (" + blocked.researchId() + ")"));
        }

        return new EngineResult<>(
                EngineStatus.PARTIAL,
                chart,
                buildEvidence(chart),
                // No signals: see this class's Javadoc.
                List.of(),
                List.copyOf(warnings),
                List.of(),
                new ResearchReference("R5", "Chiêm tinh phương Tây",
                        "Góc lá số (Ascendant, Midheaven, 12 nhà), vị trí Mặt Trời, Mặt Trăng, 7 "
                                + "hành tinh (Mercury-Neptune) và mọi góc chiếu giữa chúng đã lập "
                                + "xong và là dữ liệu thật (R5/R6 đóng 2026-09-03). Còn thiếu Sao "
                                + "Diêm Vương (BlockedSection riêng) và toàn bộ phần luận giải ý "
                                + "nghĩa — engine vẫn không phát sinh tín hiệu nào cho Fusion vì "
                                + "chưa có nội dung diễn giải cho các thiên thể/góc chiếu mới.",
                        "docs/RESEARCH_BLOCKERS.md R5/R6",
                        List.of("Sao Diêm Vương", "Nội dung luận giải cho 8 thiên thể + góc chiếu mới")),
                Map.of("methodologyId", METHODOLOGY_ID,
                        "planetsMethodologyId", PLANETS_METHODOLOGY_ID,
                        "aspectsMethodologyId", ASPECTS_METHODOLOGY_ID,
                        "zodiacSystem", "TROPICAL",
                        "houseSystem", "WHOLE_SIGN"));
    }

    /** Authored meaning of a point itself, or {@code null} where none exists. */
    private static String pointMeaningVi(WesternAstrologyChart chart, ChartPoint point) {
        if (point == chart.sun()) {
            return AstrologyMeanings.SUN_MEANING_VI;
        }
        if (point == chart.ascendant()) {
            return AstrologyMeanings.ASCENDANT_MEANING_VI;
        }
        return null;
    }

    private static List<Evidence> buildEvidence(WesternAstrologyChart chart) {
        List<Evidence> evidence = new ArrayList<>();
        String groupId = "WESTERN_ASTROLOGY_CHART";

        evidence.add(pointEvidence(chart, "ASTROLOGY_SUN", chart.sun(), groupId));
        evidence.add(pointEvidence(chart, "ASTROLOGY_MIDHEAVEN", chart.midheaven(), groupId));
        evidence.add(pointEvidence(chart, "ASTROLOGY_ASCENDANT", chart.ascendant(), groupId));

        // Moon and the seven planets - a different methodology (R5, VSOP87/
        // ELP2000-82B) from the chart-angle Sun/MC/Asc above, so a different
        // school string travels on this evidence, the same device BaziEngine
        // uses to keep BAZI_DAY_MASTER_STRENGTH_TVH's evidence distinguishable
        // from BAZI_TUBINH_CHART's.
        for (Vsop87PlanetPosition.Planet planet : PLANET_ORDER) {
            evidence.add(bodyEvidence(chart, "ASTROLOGY_" + planet.name(),
                    chart.bodies().get(planet.name()), groupId));
        }
        evidence.add(bodyEvidence(chart, "ASTROLOGY_MOON", chart.bodies().get("MOON"), groupId));

        for (Aspect aspect : chart.aspects()) {
            evidence.add(aspectEvidence(aspect, groupId));
        }

        Map<String, Object> housesFact = new LinkedHashMap<>();
        chart.houses().forEach((house, sign) -> housesFact.put(house.name(), sign.name()));
        evidence.add(new Evidence(UUID.randomUUID().toString(), ENGINE_ID, SCHOOL,
                "ASTROLOGY_WHOLE_SIGN_HOUSES", RULE_VERSION, Dimension.OTHER, housesFact,
                "whole-sign-derivation", groupId, null));

        // What the twelve houses are about. Authored content (AstrologyMeanings,
        // from the reviewed draft §B3), carried on evidence the same way the
        // Tarot card meanings are - never generated, never through the AI stage.
        // The house system travels with it because §B3's source requires it:
        // Placidus divides houses differently, so a theme without its system is
        // a claim about a chart the reader may not have.
        Map<String, Object> houseThemesFact = new LinkedHashMap<>();
        Map<String, Object> themes = new LinkedHashMap<>();
        AstrologyMeanings.allHouseThemes()
                .forEach((house, theme) -> themes.put(house.name(), theme));
        houseThemesFact.put("houseThemesVi", themes);
        houseThemesFact.put("houseSystem", chart.houseSystem());
        houseThemesFact.put("houseSystemNoteVi", AstrologyMeanings.HOUSE_SYSTEM_NOTE_VI);
        houseThemesFact.put("sourceNoteVi", AstrologyMeanings.SOURCE_NOTE_VI);
        houseThemesFact.put("contentVersion", AstrologyMeanings.CONTENT_VERSION);
        evidence.add(new Evidence(UUID.randomUUID().toString(), ENGINE_ID, SCHOOL,
                "ASTROLOGY_HOUSE_THEMES", RULE_VERSION, Dimension.OTHER, houseThemesFact,
                "astrology-house-themes", groupId, null));

        Map<String, Object> anglesFact = new LinkedHashMap<>();
        anglesFact.put("obliquityDegrees", chart.obliquityDegrees());
        anglesFact.put("ramcDegrees", chart.ramcDegrees());
        anglesFact.put("zodiacSystem", chart.zodiacSystem());
        anglesFact.put("houseSystem", chart.houseSystem());
        evidence.add(new Evidence(UUID.randomUUID().toString(), ENGINE_ID, SCHOOL,
                "ASTROLOGY_FRAME", RULE_VERSION, Dimension.OTHER, anglesFact,
                "sidereal-time-and-obliquity", groupId, null));

        for (BlockedSection blocked : chart.blockedSections()) {
            Map<String, Object> fact = new LinkedHashMap<>();
            fact.put("sectionId", blocked.sectionId());
            fact.put("displayNameVi", blocked.displayNameVi());
            fact.put("researchId", blocked.researchId());
            fact.put("reasonVi", blocked.reasonVi());
            fact.put("knownVariants", blocked.knownVariants());
            evidence.add(new Evidence(UUID.randomUUID().toString(), ENGINE_ID, SCHOOL,
                    "ASTROLOGY_BLOCKED_" + blocked.sectionId(), RULE_VERSION,
                    Dimension.OTHER, fact, "research-blocker", groupId, null));
        }

        return List.copyOf(evidence);
    }

    private static Evidence pointEvidence(WesternAstrologyChart chart, String ruleId,
                                          ChartPoint point, String groupId) {
        Map<String, Object> fact = new LinkedHashMap<>();
        fact.put("eclipticLongitudeDegrees", point.eclipticLongitudeDegrees());
        fact.put("sign", point.sign().name());
        fact.put("degreesIntoSign", point.degreesIntoSign());

        // The authored meaning of the sign this point falls in, plus - for the
        // Sun and the Ascendant only - what that point itself stands for.
        //
        // The Midheaven gets the sign keywords and `pointMeaningAuthored:
        // false`. The reviewed draft's §B header names the MC, but §B1 and §B2
        // author text only for the Sun and the Ascendant, and writing an MC
        // paragraph to fill the shape would be inventing content (Rule C). An
        // honest absent field beats a plausible sentence nobody reviewed.
        var signMeaning = AstrologyMeanings.ofSign(point.sign());
        if (signMeaning != null) {
            fact.put("signKeywordsVi", signMeaning.keywordsVi());
        }
        String pointMeaning = pointMeaningVi(chart, point);
        fact.put("pointMeaningAuthored", pointMeaning != null);
        if (pointMeaning != null) {
            fact.put("pointMeaningVi", pointMeaning);
        }
        fact.put("meaningSourceNoteVi", AstrologyMeanings.SOURCE_NOTE_VI);
        fact.put("meaningContentVersion", AstrologyMeanings.CONTENT_VERSION);
        if (point != chart.ascendant()) {
            // House 1's cusp is the Ascendant itself, so reporting a house
            // for it would be trivially circular; every other point's house
            // is real information under Whole Sign.
            fact.put("house", chart.houseOf(point.sign()).name());
        }
        return new Evidence(UUID.randomUUID().toString(), ENGINE_ID, SCHOOL, ruleId, RULE_VERSION,
                Dimension.OTHER, fact, "spherical-astronomy", groupId, null);
    }

    /** Same shape as {@link #pointEvidence}, plus latitude/distance, for a Moon/planet body. */
    private static Evidence bodyEvidence(WesternAstrologyChart chart, String ruleId,
                                         WesternAstrologyChart.BodyPosition body, String groupId) {
        ChartPoint point = body.point();
        Map<String, Object> fact = new LinkedHashMap<>();
        fact.put("eclipticLongitudeDegrees", point.eclipticLongitudeDegrees());
        fact.put("eclipticLatitudeDegrees", body.eclipticLatitudeDegrees());
        fact.put("distanceAu", body.distanceAu());
        fact.put("sign", point.sign().name());
        fact.put("degreesIntoSign", point.degreesIntoSign());
        var signMeaning = AstrologyMeanings.ofSign(point.sign());
        if (signMeaning != null) {
            fact.put("signKeywordsVi", signMeaning.keywordsVi());
        }
        // No authored point meaning for these yet (AstrologyMeanings only
        // covers the Sun and Ascendant) - honest absence, not a filled-in
        // placeholder, same device pointEvidence already uses for the MC.
        fact.put("pointMeaningAuthored", false);
        fact.put("meaningSourceNoteVi", AstrologyMeanings.SOURCE_NOTE_VI);
        fact.put("meaningContentVersion", AstrologyMeanings.CONTENT_VERSION);
        fact.put("house", chart.houseOf(point.sign()).name());
        return new Evidence(UUID.randomUUID().toString(), ENGINE_ID, PLANETS_SCHOOL, ruleId,
                PLANETS_METHODOLOGY_VERSION, Dimension.OTHER, fact, "vsop87-elp2000-82b",
                groupId, null);
    }

    private static Evidence aspectEvidence(Aspect aspect, String groupId) {
        Map<String, Object> fact = new LinkedHashMap<>();
        fact.put("bodyA", aspect.bodyA());
        fact.put("bodyB", aspect.bodyB());
        fact.put("aspectType", aspect.type().name());
        fact.put("exactAngleDegrees", aspect.exactAngleDegrees());
        fact.put("actualAngleDegrees", aspect.actualAngleDegrees());
        fact.put("orbDegrees", aspect.orbDegrees());
        fact.put("orbLimitDegrees", aspect.orbLimitDegrees());
        fact.put("applying", aspect.applying());
        return new Evidence(UUID.randomUUID().toString(), ENGINE_ID, ASPECTS_SCHOOL,
                "ASTROLOGY_ASPECT_" + aspect.bodyA() + "_" + aspect.bodyB() + "_" + aspect.type().name(),
                ASPECTS_METHODOLOGY_VERSION, Dimension.OTHER, fact, "ptolemaic-flat-orb",
                groupId, null);
    }

    @Override
    public ValidationResult validateInput(WesternAstrologyInput input) {
        if (input == null) {
            return ValidationResult.failed("NULL_INPUT", "Astrology input is required.", ENGINE_ID);
        }
        LocalDate utcDate = input.utcInstant().atZone(ZoneOffset.UTC).toLocalDate();
        if (!CAPABILITY.supportedDateRange().covers(utcDate)) {
            return ValidationResult.failed("OUTSIDE_SUPPORTED_RANGE",
                    "Birth date " + utcDate + " is outside the supported range "
                            + CAPABILITY.supportedDateRange().describe() + ".", ENGINE_ID);
        }
        if (input.latitudeDegrees() < -90.0 || input.latitudeDegrees() > 90.0) {
            return ValidationResult.failed("INVALID_LATITUDE",
                    "Latitude must be within [-90, 90]; got " + input.latitudeDegrees() + ".",
                    ENGINE_ID);
        }
        if (input.longitudeDegreesEast() < -180.0 || input.longitudeDegreesEast() > 180.0) {
            return ValidationResult.failed("INVALID_LONGITUDE",
                    "Longitude must be within [-180, 180]; got "
                            + input.longitudeDegreesEast() + ".", ENGINE_ID);
        }
        // No polar-latitude rejection: Whole Sign is valid at every
        // latitude, including inside the polar circle (|lat| > 66.5) where
        // Placidus/Koch become mathematically undefined - that correctness
        // is exactly why R6 chose it for this first version.
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
