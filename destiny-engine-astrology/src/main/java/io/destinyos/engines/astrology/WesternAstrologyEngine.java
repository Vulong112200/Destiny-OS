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
import java.util.UUID;

/**
 * Western astrology — chart angles and the Sun's position (Phase 11,
 * research items R5/R6).
 *
 * <p><strong>School (Rule D).</strong> Tropical zodiac, Whole Sign houses —
 * both owner decisions recorded 2026-08-23 in {@code docs/RESEARCH_BLOCKERS.md}
 * R6. Nothing here computes a sidereal position or an ayanamsa, and nothing
 * here divides a house by time (as Placidus/Koch would); nothing here
 * fabricates an aspect set either, since the orb policy is not yet decided.
 *
 * <p><strong>What is verified and what is not.</strong> Three things are
 * pure spherical astronomy, each independently checkable and checked before
 * being trusted here:
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
 *       orb, so no additional precision work was needed to reuse it here.</li>
 * </ul>
 *
 * <p><strong>What is deliberately not computed, and reported as blocked
 * rather than silently absent</strong> (ADR D7, the same device Bát Tự uses
 * for Dụng Thần and Day Master strength):
 * <ul>
 *   <li>the Moon and the seven other planets — these need VSOP87 (planets)
 *       and ELP2000 (Moon) coefficient tables sourced and cross-checked with
 *       the same two-independent-source rigor {@code SolarPosition} already
 *       has, which has not been done;</li>
 *   <li>aspects between chart points — blocked on R6's aspect-set/orb
 *       decision, which remains open.</li>
 * </ul>
 *
 * <p>Emits no signals for the same reason Bát Tự's Phase 8a does not: a
 * signal needs interpretive content (favourable/unfavourable meaning per
 * sign/house), which has not been authored, and a natal chart missing nine
 * of ten planetary positions has nothing to interpret yet regardless.
 */
public final class WesternAstrologyEngine
        implements MetaphysicalEngine<WesternAstrologyInput, WesternAstrologyChart> {

    public static final String ENGINE_ID = "WESTERN_ASTROLOGY";
    public static final String METHODOLOGY_ID = "WESTERN_ASTROLOGY_CHART_ANGLES";
    public static final String METHODOLOGY_VERSION = "1.0";
    public static final String RULE_VERSION = "1.0";
    public static final String SCHOOL =
            "Chiêm tinh phương Tây — Hoàng đạo Tropical, hệ nhà Whole Sign";

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
                    + "'Western Astrology' methodology name, which needs no ayanamsa).";

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

    private static final List<BlockedSection> BLOCKED_SECTIONS = List.of(
            new BlockedSection("PLANETS_BEYOND_SUN",
                    "Vị trí Mặt Trăng và 7 hành tinh còn lại", "R5",
                    "Cần bảng hệ số VSOP87 (hành tinh) và ELP2000 (Mặt Trăng) được đối chiếu "
                            + "độc lập với cùng mức nghiêm ngặt như vị trí Mặt Trời đã có — việc "
                            + "này chưa làm. Tự chép lại các bảng hệ số đó từ tóm tắt web có rủi "
                            + "ro sai số cao đúng loại kết quả tự tin nhưng sai mà hệ thống này "
                            + "từ chối tạo ra.",
                    List.of("VSOP87 rút gọn (độ chính xác ~1 giây cung, đủ dùng)",
                            "VSOP87 đầy đủ (~0.1 giây cung, dư thừa)",
                            "Swiss Ephemeris (cân nhắc rồi bị loại vì AGPL, xem R5)")),
            new BlockedSection("ASPECTS",
                    "Góc chiếu giữa các điểm trong lá số (hợp, đối, vuông góc…)", "R6",
                    "Bộ góc chiếu chính (hợp/lục hợp/vuông/tam hợp/đối) đã đồng thuận rộng "
                            + "rãi, nhưng độ rộng orb (biên độ sai số cho phép) chưa chốt — hai "
                            + "nguồn tìm được cho hai cách tiếp cận khác hẳn nhau (orb cố định "
                            + "theo kiểu trình bày, hay orb khác nhau theo từng loại góc chiếu "
                            + "và theo từng thiên thể). Chưa kể góc chiếu giữa hai điểm nào cũng "
                            + "cần cả hai điểm đó được tính trước — 9/10 thiên thể còn thiếu.",
                    List.of("Orb cố định theo kiểu trình bày (vd 10° cho mọi góc chính)",
                            "Orb phân cấp theo loại góc chiếu + rộng hơn cho Mặt Trời/Mặt Trăng")));

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

        var chart = new WesternAstrologyChart(sun, midheaven, ascendant, houses,
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
                        "Góc lá số (Ascendant, Midheaven, 12 nhà) và vị trí Mặt Trời đã lập "
                                + "xong và là dữ liệu thật. Mặt Trăng, 7 hành tinh còn lại và "
                                + "góc chiếu chưa có nên engine không phát sinh tín hiệu nào cho "
                                + "Fusion.",
                        "docs/RESEARCH_BLOCKERS.md R5/R6",
                        List.of("R5 nguồn ephemeris cho các hành tinh", "R6 bộ orb góc chiếu")),
                Map.of("methodologyId", METHODOLOGY_ID,
                        "zodiacSystem", "TROPICAL",
                        "houseSystem", "WHOLE_SIGN"));
    }

    private static List<Evidence> buildEvidence(WesternAstrologyChart chart) {
        List<Evidence> evidence = new ArrayList<>();
        String groupId = "WESTERN_ASTROLOGY_CHART";

        evidence.add(pointEvidence(chart, "ASTROLOGY_SUN", chart.sun(), groupId));
        evidence.add(pointEvidence(chart, "ASTROLOGY_MIDHEAVEN", chart.midheaven(), groupId));
        evidence.add(pointEvidence(chart, "ASTROLOGY_ASCENDANT", chart.ascendant(), groupId));

        Map<String, Object> housesFact = new LinkedHashMap<>();
        chart.houses().forEach((house, sign) -> housesFact.put(house.name(), sign.name()));
        evidence.add(new Evidence(UUID.randomUUID().toString(), ENGINE_ID, SCHOOL,
                "ASTROLOGY_WHOLE_SIGN_HOUSES", RULE_VERSION, Dimension.OTHER, housesFact,
                "whole-sign-derivation", groupId, null));

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
        if (point != chart.ascendant()) {
            // House 1's cusp is the Ascendant itself, so reporting a house
            // for it would be trivially circular; every other point's house
            // is real information under Whole Sign.
            fact.put("house", chart.houseOf(point.sign()).name());
        }
        return new Evidence(UUID.randomUUID().toString(), ENGINE_ID, SCHOOL, ruleId, RULE_VERSION,
                Dimension.OTHER, fact, "spherical-astronomy", groupId, null);
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
