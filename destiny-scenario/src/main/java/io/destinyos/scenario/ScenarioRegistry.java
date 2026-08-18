package io.destinyos.scenario;

import io.destinyos.core.signal.Applicability;
import io.destinyos.core.signal.Dimension;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * The ten MVP scenario definitions (Master Spec section 11).
 *
 * <p>Only {@code BUSINESS} and {@code DAILY_ACTION} carry a real
 * applicability policy, transcribed directly from Master Spec section 7's
 * two worked examples ("Mở rộng kinh doanh" and "Hôm nay nên làm gì"). The
 * other eight scenario types are registered — so the type is real,
 * queryable, and extensible — but their policy is honestly
 * {@link ScenarioDefinition#policyDefined()} {@code == false}: Master Spec
 * never specifies which engines matter for, say, COMPATIBILITY or PURCHASE,
 * and inventing one would not be a software engineering judgment call the
 * way Fusion's vote thresholds are — it would be a product/domain decision
 * with no source, exactly what this project declines to fabricate elsewhere.
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

        map.put(ScenarioType.CAREER, ScenarioDefinition.undefinedPolicy(
                ScenarioType.CAREER, "Sự nghiệp"));
        map.put(ScenarioType.FINANCE, ScenarioDefinition.undefinedPolicy(
                ScenarioType.FINANCE, "Tài chính"));
        map.put(ScenarioType.RELATIONSHIP, ScenarioDefinition.undefinedPolicy(
                ScenarioType.RELATIONSHIP, "Quan hệ"));
        map.put(ScenarioType.COMPATIBILITY, ScenarioDefinition.undefinedPolicy(
                ScenarioType.COMPATIBILITY, "Tương hợp"));
        map.put(ScenarioType.PURCHASE, ScenarioDefinition.undefinedPolicy(
                ScenarioType.PURCHASE, "Mua sắm"));
        map.put(ScenarioType.TRAVEL, ScenarioDefinition.undefinedPolicy(
                ScenarioType.TRAVEL, "Di chuyển"));
        map.put(ScenarioType.PROJECT, ScenarioDefinition.undefinedPolicy(
                ScenarioType.PROJECT, "Dự án"));
        map.put(ScenarioType.GENERAL_DECISION, ScenarioDefinition.undefinedPolicy(
                ScenarioType.GENERAL_DECISION, "Quyết định chung"));

        return Map.copyOf(map);
    }
}
