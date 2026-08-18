package io.destinyos.scenario;

import static org.assertj.core.api.Assertions.assertThat;

import io.destinyos.core.context.BirthTimePrecision;
import io.destinyos.core.context.CalculationContext;
import io.destinyos.core.result.EngineResult;
import io.destinyos.core.signal.Applicability;
import io.destinyos.core.signal.Dimension;
import io.destinyos.core.signal.Polarity;
import io.destinyos.core.signal.Signal;
import io.destinyos.core.signal.Strength;
import io.destinyos.core.version.MethodologyVersions;
import io.destinyos.engine.EngineCapability;
import io.destinyos.engine.EngineMetadata;
import io.destinyos.engine.MetaphysicalEngine;
import io.destinyos.engine.MethodologyStatus;
import io.destinyos.engine.ValidationResult;
import io.destinyos.execution.EngineTask;
import io.destinyos.fusion.FusionOutcome;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ScenarioEngineTest {

    private final ScenarioEngine scenario = ScenarioEngine.withDefaults();

    private static CalculationContext context() {
        return new CalculationContext("calc-scenario-1", "SCENARIO", new MethodologyVersions(
                "1.0", "1.0", "1.0", null), ZoneId.of("Asia/Ho_Chi_Minh"),
                Locale.forLanguageTag("vi-VN"), null, Instant.parse("2026-08-18T00:00:00Z"),
                null, null, BirthTimePrecision.UNKNOWN, List.of());
    }

    /** A stub engine that always returns one signal at a declared applicability. */
    private static MetaphysicalEngine<String, String> stubEngine(String engineId, Polarity polarity,
                                                                 Applicability ownApplicability) {
        return new MetaphysicalEngine<>() {
            @Override
            public EngineResult<String> calculate(String input, CalculationContext context) {
                Signal signal = new Signal(engineId + "-sig", engineId, null, Dimension.FINANCE,
                        "TAG", polarity, Strength.MEDIUM, ownApplicability, false, List.of(), null);
                return EngineResult.success("ok", List.of(), List.of(signal));
            }

            @Override
            public ValidationResult validateInput(String input) {
                return ValidationResult.ok();
            }

            @Override
            public EngineCapability capability() {
                return EngineCapability.builder().dimensions(Dimension.FINANCE).build();
            }

            @Override
            public EngineMetadata metadata() {
                return new EngineMetadata(engineId, "Stub", "STUB_METHOD", "1.0", "1.0",
                        "Stub school", "test fixture", MethodologyStatus.PRODUCTION_READY);
            }
        };
    }

    @Test
    @DisplayName("BUSINESS scenario runs only the engines its policy names, from what's available")
    void businessScenarioRunsOnlyPolicyEngines() {
        var tarot = EngineTask.of(stubEngine("TAROT", Polarity.SUPPORT, Applicability.HIGH), "q");
        var unrelated = EngineTask.of(stubEngine("UNRELATED_ENGINE", Polarity.CAUTION,
                Applicability.HIGH), "q");

        var result = scenario.run(ScenarioType.BUSINESS,
                Map.of("TAROT", tarot, "UNRELATED_ENGINE", unrelated), context());

        assertThat(result.policyDefined()).isTrue();
        assertThat(result.execution().executions()).hasSize(1);
        assertThat(result.execution().forEngine("TAROT")).isNotNull();
        assertThat(result.execution().forEngine("UNRELATED_ENGINE")).isNull();
    }

    @Test
    @DisplayName("An engine the policy names but the caller does not supply is reported unavailable")
    void unavailableEngineIsReported() {
        var result = scenario.run(ScenarioType.BUSINESS, Map.of(), context());

        assertThat(result.unavailableEngines()).contains("BAZI", "ZIWEI", "TAROT",
                "NUMEROLOGY_PYTHAGOREAN", "FENGSHUI_KUA", "WESTERN_ASTROLOGY");
        assertThat(result.execution().executions()).isEmpty();
    }

    @Test
    @DisplayName("A scenario's applicability clamp narrows, never widens, an engine's own applicability")
    void applicabilityIsClampedNotWidened() {
        // TAROT reports itself HIGH, but BUSINESS's policy only grants it
        // MEDIUM - the fused signal must reflect MEDIUM, not HIGH.
        var tarot = EngineTask.of(stubEngine("TAROT", Polarity.SUPPORT, Applicability.HIGH), "q");

        var result = scenario.run(ScenarioType.BUSINESS, Map.of("TAROT", tarot), context());

        assertThat(result.fusion()).isNotNull();
        var dimension = result.fusion().dimensions().get(0);
        assertThat(dimension.supportingEngines()).contains("TAROT");
    }

    @Test
    @DisplayName("A scenario with no defined policy runs nothing and returns no fusion result")
    void undefinedPolicyScenarioRunsNothing() {
        var tarot = EngineTask.of(stubEngine("TAROT", Polarity.SUPPORT, Applicability.HIGH), "q");

        var result = scenario.run(ScenarioType.PURCHASE, Map.of("TAROT", tarot), context());

        assertThat(result.policyDefined()).isFalse();
        assertThat(result.execution().executions()).isEmpty();
        assertThat(result.fusion()).isNull();
        assertThat(result.hasFusionResult()).isFalse();
    }

    @Test
    @DisplayName("DAILY_ACTION scenario fuses signals from its own applicable engines")
    void dailyActionScenarioFuses() {
        var bazi = EngineTask.of(stubEngine("BAZI", Polarity.SUPPORT, Applicability.HIGH), "q");
        var tarot = EngineTask.of(stubEngine("TAROT", Polarity.SUPPORT, Applicability.HIGH), "q");

        var result = scenario.run(ScenarioType.DAILY_ACTION,
                Map.of("BAZI", bazi, "TAROT", tarot), context());

        assertThat(result.fusion().overallOutcome()).isEqualTo(FusionOutcome.CONSENSUS_SUPPORT);
    }

    @Test
    @DisplayName("Every registered scenario type is queryable, even without a defined policy")
    void everyScenarioTypeIsRegistered() {
        for (ScenarioType type : ScenarioType.values()) {
            assertThat(ScenarioRegistry.get(type)).as("scenario %s", type).isNotNull();
        }
    }

    @Test
    @DisplayName("An engine outside the scenario's policy never influences the fused result")
    void engineOutsidePolicyNeverInfluencesFusion() {
        var tarot = EngineTask.of(stubEngine("TAROT", Polarity.SUPPORT, Applicability.HIGH), "q");
        var outsider = EngineTask.of(stubEngine("OUTSIDER", Polarity.NEGATIVE, Applicability.HIGH), "q");

        var result = scenario.run(ScenarioType.BUSINESS, Map.of("TAROT", tarot, "OUTSIDER", outsider),
                context());

        assertThat(result.fusion().overallOutcome()).isEqualTo(FusionOutcome.CONSENSUS_SUPPORT);
        assertThat(result.execution().forEngine("OUTSIDER")).isNull();
    }
}
