package io.destinyos.scenario;

import io.destinyos.core.signal.Applicability;
import io.destinyos.core.signal.Dimension;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * The ten MVP scenario definitions (Master Spec section 11).
 *
 * <p>{@code BUSINESS} and {@code DAILY_ACTION} carry the two applicability
 * policies transcribed directly from Master Spec section 7's worked
 * examples ("Mở rộng kinh doanh" and "Hôm nay nên làm gì"). A prior attempt
 * to <em>derive</em> the remaining eight from each engine's declared
 * {@link io.destinyos.engine.EngineCapability} dimensions failed its own
 * test (see {@code docs/DECISION_LOG.md}, "Rejected: deriving the eight
 * missing scenario policies") — dimension overlap is not evidence of
 * traditional relevance, since {@code TAROT} and
 * {@code NUMEROLOGY_PYTHOGOREAN} declare every dimension there is.
 *
 * <p>The seven policies below instead come from a research pass gathering
 * <em>named, sourced traditional practice</em> per engine per scenario
 * ({@code docs/research_drafts/scenario_scope_reference.md}) — e.g. Bát Tự's
 * Quan Tinh / Tài Tinh for career and finance, Tử Vi's Điền Trạch / Thiên Di
 * for purchase and travel — reviewed and approved by the project owner
 * (2026-08-23, {@code docs/DECISION_LOG.md}). An engine absent from a
 * scenario's map is not an oversight: it means the research pass found no
 * named traditional branch connecting that engine to that scenario, and
 * Rule C treats "no evidence" as a reason to omit, not to guess {@code LOW}.
 *
 * <p>{@code COMPATIBILITY} stays {@link ScenarioDefinition#policyDefined()}
 * {@code == false} on purpose: its evidence is the strongest of all eight,
 * but every strong source (hợp hôn, xem tuổi, synastry) needs <em>two</em>
 * charts, and this system takes one. Declaring a policy would misstate what
 * the system can currently do, not just what tradition says.
 */
public final class ScenarioRegistry {

    private static final Map<ScenarioType, ScenarioDefinition> DEFINITIONS = build();

    private ScenarioRegistry() {
    }

    public static ScenarioDefinition get(ScenarioType type) {
        return DEFINITIONS.get(type);
    }

    public static Map<ScenarioType, ScenarioDefinition> all() {
        return DEFINITIONS;
    }

    private static Map<ScenarioType, ScenarioDefinition> build() {
        Map<ScenarioType, ScenarioDefinition> map = new EnumMap<>(ScenarioType.class);

        // Master Spec section 7: "Mở rộng kinh doanh" (Business expansion).
        map.put(ScenarioType.BUSINESS, new ScenarioDefinition(
                ScenarioType.BUSINESS,
                "Mở rộng kinh doanh",
                true,
                Map.of(
                        "BAZI", Applicability.HIGH,
                        "ZIWEI", Applicability.HIGH,
                        "WESTERN_ASTROLOGY", Applicability.HIGH,
                        "TAROT", Applicability.MEDIUM,
                        "NUMEROLOGY_PYTHAGOREAN", Applicability.MEDIUM,
                        "FENGSHUI_KUA", Applicability.HIGH
                ),
                Set.of(Dimension.FINANCE, Dimension.CAREER, Dimension.DECISION)));

        // Master Spec section 7: "Hôm nay nên làm gì" (What should I do today).
        map.put(ScenarioType.DAILY_ACTION, new ScenarioDefinition(
                ScenarioType.DAILY_ACTION,
                "Hôm nay nên làm gì",
                true,
                Map.of(
                        "BAZI", Applicability.HIGH,
                        "FENGSHUI_KUA", Applicability.MEDIUM,
                        "TAROT", Applicability.MEDIUM,
                        "ZIWEI", Applicability.MEDIUM
                ),
                Set.of(Dimension.DAILY, Dimension.TIMING)));

        // Bát Tự Quan Tinh (Chính Quan/Thất Sát), Tử Vi cung Quan Lộc, and
        // Chiêm tinh Nhà 10/MC are all named classical branches for career.
        // Numerology's Destiny/Expression Number and Bát Trạch's Sinh Khí
        // direction are modern-popular and indirect respectively, hence LOW
        // rather than omitted (docs/research_drafts/scenario_scope_reference.md).
        map.put(ScenarioType.CAREER, new ScenarioDefinition(
                ScenarioType.CAREER, "Sự nghiệp", true,
                Map.of(
                        "BAZI", Applicability.HIGH,
                        "ZIWEI", Applicability.HIGH,
                        "WESTERN_ASTROLOGY", Applicability.MEDIUM,
                        "TAROT", Applicability.LOW,
                        "NUMEROLOGY_PYTHAGOREAN", Applicability.LOW,
                        "FENGSHUI_KUA", Applicability.LOW
                ),
                Set.of(Dimension.CAREER, Dimension.DECISION)));

        // Same shape as CAREER: Bát Tự Tài Tinh, Tử Vi cung Tài Bạch, Chiêm
        // tinh Nhà 2/8 are classical. Bát Trạch's "wealth corner" is a real,
        // widely-practiced technique (unlike its career connection), hence
        // MEDIUM rather than LOW here specifically.
        map.put(ScenarioType.FINANCE, new ScenarioDefinition(
                ScenarioType.FINANCE, "Tài chính", true,
                Map.of(
                        "BAZI", Applicability.HIGH,
                        "ZIWEI", Applicability.HIGH,
                        "WESTERN_ASTROLOGY", Applicability.MEDIUM,
                        "TAROT", Applicability.LOW,
                        "NUMEROLOGY_PYTHAGOREAN", Applicability.LOW,
                        "FENGSHUI_KUA", Applicability.MEDIUM
                ),
                Set.of(Dimension.FINANCE, Dimension.DECISION)));

        // An ongoing relationship, not a match assessment before one starts
        // (see COMPATIBILITY, still undefined). Bát Tự's Spouse Palace/Star,
        // Tử Vi's cung Phu Thê and Chiêm tinh's Nhà 7 read a single chart;
        // Bát Trạch has no named branch here at all (Đào Hoa vị is a
        // different Phong Thủy method, not Bát Trạch), so it is omitted.
        map.put(ScenarioType.RELATIONSHIP, new ScenarioDefinition(
                ScenarioType.RELATIONSHIP, "Quan hệ", true,
                Map.of(
                        "BAZI", Applicability.HIGH,
                        "ZIWEI", Applicability.HIGH,
                        "WESTERN_ASTROLOGY", Applicability.MEDIUM,
                        "TAROT", Applicability.LOW,
                        "NUMEROLOGY_PYTHAGOREAN", Applicability.LOW
                ),
                Set.of(Dimension.RELATIONSHIP)));

        // Bát Trạch's Đông/Tây Tứ Trạch house-direction assessment is this
        // methodology's original purpose; Tử Vi's cung Điền Trạch is a named
        // classical branch for real estate specifically. Bát Tự, Tarot and
        // Numerology have no named branch for a purchase decision as such
        // (Tài Tinh is about wealth generally, not a purchase) - omitted
        // rather than guessed.
        map.put(ScenarioType.PURCHASE, new ScenarioDefinition(
                ScenarioType.PURCHASE, "Mua sắm", true,
                Map.of(
                        "ZIWEI", Applicability.HIGH,
                        "WESTERN_ASTROLOGY", Applicability.LOW,
                        "FENGSHUI_KUA", Applicability.HIGH
                ),
                Set.of(Dimension.HOME, Dimension.FINANCE, Dimension.DECISION)));

        // Same shape as PURCHASE: Tử Vi's cung Thiên Di and Bát Trạch's
        // auspicious-direction-for-travel are named classical branches.
        map.put(ScenarioType.TRAVEL, new ScenarioDefinition(
                ScenarioType.TRAVEL, "Di chuyển", true,
                Map.of(
                        "ZIWEI", Applicability.HIGH,
                        "WESTERN_ASTROLOGY", Applicability.LOW,
                        "FENGSHUI_KUA", Applicability.HIGH
                ),
                Set.of(Dimension.TRAVEL, Dimension.DECISION)));

        // No source distinguishes "a specific project" from "expanding a
        // business" as separate categories - traditional date-selection
        // practice for 開張/動土 treats both as "starting a major undertaking"
        // with the same criteria. Rather than invent a distinction the
        // research pass could not find, this uses BUSINESS's own weights
        // scaled down one notch, reflecting a smaller-scoped undertaking
        // without claiming a different kind of relevance exists.
        map.put(ScenarioType.PROJECT, new ScenarioDefinition(
                ScenarioType.PROJECT, "Dự án", true,
                Map.of(
                        "BAZI", Applicability.MEDIUM,
                        "ZIWEI", Applicability.MEDIUM,
                        "WESTERN_ASTROLOGY", Applicability.LOW,
                        "TAROT", Applicability.LOW,
                        "NUMEROLOGY_PYTHAGOREAN", Applicability.LOW,
                        "FENGSHUI_KUA", Applicability.MEDIUM
                ),
                Set.of(Dimension.FINANCE, Dimension.CAREER, Dimension.DECISION)));

        // An open-ended question with no fixed topic is a real traditional
        // category (I Ching's one-question-one-cast, horary astrology,
        // Tarot's three-card past/present/future spread) - but of the six
        // engines this system has, only Tarot's classical spread structure
        // exists for exactly this purpose. Bát Tự/Tử Vi are lifelong-chart
        // systems with no mechanism for a question asked at one moment, so
        // they carry only the weak general-context relevance any natal
        // reading has; Numerology and Bát Trạch have no branch for this at
        // all and are omitted.
        map.put(ScenarioType.GENERAL_DECISION, new ScenarioDefinition(
                ScenarioType.GENERAL_DECISION, "Quyết định chung", true,
                Map.of(
                        "TAROT", Applicability.HIGH,
                        "BAZI", Applicability.LOW,
                        "WESTERN_ASTROLOGY", Applicability.LOW,
                        "NUMEROLOGY_PYTHAGOREAN", Applicability.LOW
                ),
                Set.of(Dimension.DECISION, Dimension.OTHER)));

        // The one scenario left undefined on purpose: its strongest evidence
        // (Bát Tự hợp hôn, Tử Vi xem tuổi, Chiêm tinh synastry) is entirely
        // dual-chart, and every ScenarioDefinition here — and every engine
        // input this system has — is single-chart. Declaring HIGH/MEDIUM
        // values here would misstate what the system can compute today, not
        // just what tradition recommends. See docs/DECISION_LOG.md.
        map.put(ScenarioType.COMPATIBILITY, ScenarioDefinition.undefinedPolicy(
                ScenarioType.COMPATIBILITY, "Tương hợp"));

        return Map.copyOf(map);
    }
}
