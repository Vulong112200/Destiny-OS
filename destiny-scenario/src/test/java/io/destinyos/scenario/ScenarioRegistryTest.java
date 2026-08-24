package io.destinyos.scenario;

import static org.assertj.core.api.Assertions.assertThat;

import io.destinyos.core.signal.Applicability;
import io.destinyos.core.signal.Dimension;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * The seven applicability policies the project owner added on 2026-08-23
 * (docs/DECISION_LOG.md), on top of BUSINESS and DAILY_ACTION.
 *
 * <p>Every expected value here is pinned exactly, not asserted loosely, on
 * purpose: these numbers came from a specific evidence-gathering pass
 * (docs/research_drafts/scenario_scope_reference.md) and an owner decision on
 * top of it, not from a formula this test could re-derive. If a future change
 * alters one, it should fail a specific assertion naming exactly which
 * scenario/engine pair moved — not be silently absorbed by a loose "contains
 * some entries" check.
 */
class ScenarioRegistryTest {

    @Test
    @DisplayName("Nine of ten scenarios now have a real policy; only COMPATIBILITY stays undefined")
    void onlyCompatibilityRemainsUndefined() {
        for (ScenarioType type : ScenarioType.values()) {
            boolean expectedDefined = type != ScenarioType.COMPATIBILITY;
            assertThat(ScenarioRegistry.get(type).policyDefined())
                    .as("policyDefined for %s", type)
                    .isEqualTo(expectedDefined);
        }
    }

    @Test
    @DisplayName("COMPATIBILITY stays undefined: its evidence is all dual-chart")
    void compatibilityStaysUndefined() {
        ScenarioDefinition compatibility = ScenarioRegistry.get(ScenarioType.COMPATIBILITY);
        assertThat(compatibility.policyDefined()).isFalse();
        assertThat(compatibility.applicableEngines()).isEmpty();
        assertThat(compatibility.dimensions()).isEmpty();
    }

    @Nested
    @DisplayName("CAREER")
    class Career {
        private final ScenarioDefinition definition = ScenarioRegistry.get(ScenarioType.CAREER);

        @Test
        void engineWeights() {
            assertThat(definition.applicableEngines()).containsExactlyInAnyOrderEntriesOf(java.util.Map.of(
                    "BAZI", Applicability.HIGH,
                    "ZIWEI", Applicability.HIGH,
                    "WESTERN_ASTROLOGY", Applicability.MEDIUM,
                    "TAROT", Applicability.LOW,
                    "NUMEROLOGY_PYTHAGOREAN", Applicability.LOW,
                    "FENGSHUI_KUA", Applicability.LOW));
        }

        @Test
        void dimensions() {
            assertThat(definition.dimensions()).containsExactlyInAnyOrder(
                    Dimension.CAREER, Dimension.DECISION);
        }
    }

    @Nested
    @DisplayName("FINANCE")
    class Finance {
        private final ScenarioDefinition definition = ScenarioRegistry.get(ScenarioType.FINANCE);

        @Test
        void engineWeights() {
            assertThat(definition.applicableEngines()).containsExactlyInAnyOrderEntriesOf(java.util.Map.of(
                    "BAZI", Applicability.HIGH,
                    "ZIWEI", Applicability.HIGH,
                    "WESTERN_ASTROLOGY", Applicability.MEDIUM,
                    "TAROT", Applicability.LOW,
                    "NUMEROLOGY_PYTHAGOREAN", Applicability.LOW,
                    "FENGSHUI_KUA", Applicability.MEDIUM));
        }

        @Test
        @DisplayName("Bát Trạch's wealth-corner practice makes it MEDIUM here, unlike CAREER's LOW")
        void fengshuiOutranksCareer() {
            // Applicability's declared order is HIGH, MEDIUM, LOW,
            // NOT_APPLICABLE, so a *lower* ordinal is *more* relevant -
            // isLessThan here means "more applicable", not a typo.
            assertThat(definition.applicabilityFor("FENGSHUI_KUA"))
                    .isLessThan(ScenarioRegistry.get(ScenarioType.CAREER)
                            .applicabilityFor("FENGSHUI_KUA"));
        }

        @Test
        void dimensions() {
            assertThat(definition.dimensions()).containsExactlyInAnyOrder(
                    Dimension.FINANCE, Dimension.DECISION);
        }
    }

    @Nested
    @DisplayName("RELATIONSHIP")
    class Relationship {
        private final ScenarioDefinition definition = ScenarioRegistry.get(ScenarioType.RELATIONSHIP);

        @Test
        void engineWeights() {
            assertThat(definition.applicableEngines()).containsExactlyInAnyOrderEntriesOf(java.util.Map.of(
                    "BAZI", Applicability.HIGH,
                    "ZIWEI", Applicability.HIGH,
                    "WESTERN_ASTROLOGY", Applicability.MEDIUM,
                    "TAROT", Applicability.LOW,
                    "NUMEROLOGY_PYTHAGOREAN", Applicability.LOW));
        }

        @Test
        @DisplayName("Bát Trạch has no named branch here, unlike FINANCE — omitted, not LOW")
        void fengshuiIsNotApplicable() {
            assertThat(definition.applicabilityFor("FENGSHUI_KUA"))
                    .isEqualTo(Applicability.NOT_APPLICABLE);
        }

        @Test
        void dimensions() {
            assertThat(definition.dimensions()).containsExactly(Dimension.RELATIONSHIP);
        }
    }

    @Nested
    @DisplayName("PURCHASE and TRAVEL share a shape: only Tử Vi and Bát Trạch have named branches")
    class PurchaseAndTravel {

        @Test
        void purchaseEngineWeights() {
            var definition = ScenarioRegistry.get(ScenarioType.PURCHASE);
            assertThat(definition.applicableEngines()).containsExactlyInAnyOrderEntriesOf(java.util.Map.of(
                    "ZIWEI", Applicability.HIGH,
                    "WESTERN_ASTROLOGY", Applicability.LOW,
                    "FENGSHUI_KUA", Applicability.HIGH));
            assertThat(definition.dimensions()).containsExactlyInAnyOrder(
                    Dimension.HOME, Dimension.FINANCE, Dimension.DECISION);
        }

        @Test
        void travelEngineWeights() {
            var definition = ScenarioRegistry.get(ScenarioType.TRAVEL);
            assertThat(definition.applicableEngines()).containsExactlyInAnyOrderEntriesOf(java.util.Map.of(
                    "ZIWEI", Applicability.HIGH,
                    "WESTERN_ASTROLOGY", Applicability.LOW,
                    "FENGSHUI_KUA", Applicability.HIGH));
            assertThat(definition.dimensions()).containsExactlyInAnyOrder(
                    Dimension.TRAVEL, Dimension.DECISION);
        }

        @Test
        @DisplayName("Bát Tự, Tarot and Numerology have no named branch for either — omitted from both")
        void neitherIncludesBaziTarotOrNumerology() {
            for (ScenarioType type : Set.of(ScenarioType.PURCHASE, ScenarioType.TRAVEL)) {
                var definition = ScenarioRegistry.get(type);
                assertThat(definition.applicabilityFor("BAZI"))
                        .as("BAZI for %s", type).isEqualTo(Applicability.NOT_APPLICABLE);
                assertThat(definition.applicabilityFor("TAROT"))
                        .as("TAROT for %s", type).isEqualTo(Applicability.NOT_APPLICABLE);
                assertThat(definition.applicabilityFor("NUMEROLOGY_PYTHAGOREAN"))
                        .as("NUMEROLOGY_PYTHAGOREAN for %s", type)
                        .isEqualTo(Applicability.NOT_APPLICABLE);
            }
        }
    }

    @Nested
    @DisplayName("PROJECT")
    class Project {
        private final ScenarioDefinition definition = ScenarioRegistry.get(ScenarioType.PROJECT);

        @Test
        void engineWeights() {
            assertThat(definition.applicableEngines()).containsExactlyInAnyOrderEntriesOf(java.util.Map.of(
                    "BAZI", Applicability.MEDIUM,
                    "ZIWEI", Applicability.MEDIUM,
                    "WESTERN_ASTROLOGY", Applicability.LOW,
                    "TAROT", Applicability.LOW,
                    "NUMEROLOGY_PYTHAGOREAN", Applicability.LOW,
                    "FENGSHUI_KUA", Applicability.MEDIUM));
        }

        @Test
        @DisplayName("Same engines as BUSINESS, each one notch lower — no distinguishing evidence exists")
        void sameEnginesAsBusinessOneNotchLower() {
            var business = ScenarioRegistry.get(ScenarioType.BUSINESS);
            assertThat(definition.applicableEngines().keySet())
                    .isEqualTo(business.applicableEngines().keySet());
            for (String engineId : business.applicableEngines().keySet()) {
                // Higher ordinal = less relevant (HIGH, MEDIUM, LOW,
                // NOT_APPLICABLE), so "one notch lower" is isGreaterThan.
                assertThat(definition.applicabilityFor(engineId))
                        .as("%s should be one notch below BUSINESS's %s", engineId,
                                business.applicabilityFor(engineId))
                        .isGreaterThan(business.applicabilityFor(engineId));
            }
        }

        @Test
        void dimensions() {
            assertThat(definition.dimensions()).containsExactlyInAnyOrder(
                    Dimension.FINANCE, Dimension.CAREER, Dimension.DECISION);
        }
    }

    @Nested
    @DisplayName("GENERAL_DECISION")
    class GeneralDecision {
        private final ScenarioDefinition definition = ScenarioRegistry.get(ScenarioType.GENERAL_DECISION);

        @Test
        void engineWeights() {
            assertThat(definition.applicableEngines()).containsExactlyInAnyOrderEntriesOf(java.util.Map.of(
                    "TAROT", Applicability.HIGH,
                    "ICHING", Applicability.HIGH,
                    "BAZI", Applicability.LOW,
                    "WESTERN_ASTROLOGY", Applicability.LOW,
                    "NUMEROLOGY_PYTHAGOREAN", Applicability.LOW));
        }

        @Test
        @DisplayName("Tarot's classical open-question spread and Kinh Dịch's own decision-oracle use case earn HIGH here")
        void tarotAndIChingAreHigh() {
            assertThat(definition.applicabilityFor("TAROT")).isEqualTo(Applicability.HIGH);
            assertThat(definition.applicabilityFor("ICHING")).isEqualTo(Applicability.HIGH);
            for (String other : java.util.List.of("BAZI", "WESTERN_ASTROLOGY", "NUMEROLOGY_PYTHAGOREAN")) {
                assertThat(definition.applicabilityFor(other))
                        .as(other).isEqualTo(Applicability.LOW);
            }
        }

        @Test
        @DisplayName("Tử Vi and Bát Trạch have no branch for an open, moment-specific question")
        void ziweiAndFengshuiAreNotApplicable() {
            assertThat(definition.applicabilityFor("ZIWEI")).isEqualTo(Applicability.NOT_APPLICABLE);
            assertThat(definition.applicabilityFor("FENGSHUI_KUA")).isEqualTo(Applicability.NOT_APPLICABLE);
        }

        @Test
        void dimensions() {
            assertThat(definition.dimensions()).containsExactlyInAnyOrder(
                    Dimension.DECISION, Dimension.OTHER);
        }
    }

    @Test
    @DisplayName("Every defined policy names at least one engine — a policy with zero engines is a bug, not a valid state")
    void everyDefinedPolicyNamesAtLeastOneEngine() {
        for (ScenarioType type : ScenarioType.values()) {
            ScenarioDefinition definition = ScenarioRegistry.get(type);
            if (definition.policyDefined()) {
                assertThat(definition.applicableEngines())
                        .as("engines for %s", type)
                        .isNotEmpty();
            }
        }
    }
}
