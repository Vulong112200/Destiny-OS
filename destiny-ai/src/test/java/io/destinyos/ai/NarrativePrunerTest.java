package io.destinyos.ai;

import static org.assertj.core.api.Assertions.assertThat;

import io.destinyos.core.signal.Dimension;
import io.destinyos.core.signal.Polarity;
import io.destinyos.core.signal.Strength;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** AI_NARRATIVE_SPEC.md section 3: priority, dedup, and the 8-20 signal budget. */
class NarrativePrunerTest {

    private static NarrativeSignalItem signal(String engine, Dimension dimension, Polarity polarity,
            Strength strength, boolean critical, String tag) {
        return new NarrativeSignalItem(engine, dimension, dimension.name(), polarity, polarity.name(),
                strength, strength.name(), critical, tag);
    }

    private static NarrativeInput inputWith(List<NarrativeSignalItem> signals, Set<Dimension> relevantDimensions) {
        return new NarrativeInput("Kich ban thu", relevantDimensions, Map.of(), signals, List.of(), List.of(),
                List.of(), Map.of());
    }

    @Nested
    @DisplayName("Loai (discard)")
    class Discard {

        @Test
        @DisplayName("NEUTRAL polarity is always dropped")
        void dropsNeutral() {
            var neutral = signal("TAROT", Dimension.CAREER, Polarity.NEUTRAL, Strength.STRONG, false, "t1");
            var kept = signal("TAROT", Dimension.CAREER, Polarity.SUPPORT, Strength.STRONG, false, "t2");

            NarrativeInput result = NarrativePruner.prune(inputWith(List.of(neutral, kept), Set.of()));

            assertThat(result.signals()).containsExactly(kept);
        }

        @Test
        @DisplayName("Duplicate (same engine/dimension/polarity/tag) keeps only the first")
        void dedupesDuplicates() {
            var first = signal("TAROT", Dimension.CAREER, Polarity.SUPPORT, Strength.STRONG, false, "same-tag");
            var duplicate = signal("TAROT", Dimension.CAREER, Polarity.SUPPORT, Strength.STRONG, false, "same-tag");

            NarrativeInput result = NarrativePruner.prune(inputWith(List.of(first, duplicate), Set.of()));

            assertThat(result.signals()).containsExactly(first);
        }

        @Test
        @DisplayName("WEAK signals are dropped even when nothing else competes for the budget")
        void dropsWeak() {
            var weak = signal("NUMEROLOGY", Dimension.CAREER, Polarity.SUPPORT, Strength.WEAK, false, "w1");

            NarrativeInput result = NarrativePruner.prune(inputWith(List.of(weak), Set.of()));

            assertThat(result.signals()).isEmpty();
        }

        @Test
        @DisplayName("MEDIUM signals outside the scenario's relevant dimensions are dropped as irrelevant")
        void dropsIrrelevantMedium() {
            var irrelevant = signal("TAROT", Dimension.TRAVEL, Polarity.SUPPORT, Strength.MEDIUM, false, "m1");

            NarrativeInput result = NarrativePruner.prune(inputWith(List.of(irrelevant), Set.of(Dimension.CAREER)));

            assertThat(result.signals()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Uu tien (keep)")
    class Keep {

        @Test
        @DisplayName("Critical signals are always kept regardless of strength")
        void keepsCritical() {
            var critical = signal("BAZI", Dimension.CAREER, Polarity.CAUTION, Strength.WEAK, true, "c1");

            NarrativeInput result = NarrativePruner.prune(inputWith(List.of(critical), Set.of()));

            assertThat(result.signals()).containsExactly(critical);
        }

        @Test
        @DisplayName("STRONG signals are kept regardless of scenario relevance")
        void keepsStrong() {
            var strong = signal("TAROT", Dimension.TRAVEL, Polarity.SUPPORT, Strength.STRONG, false, "s1");

            NarrativeInput result = NarrativePruner.prune(inputWith(List.of(strong), Set.of(Dimension.CAREER)));

            assertThat(result.signals()).containsExactly(strong);
        }

        @Test
        @DisplayName("Scenario-relevant MEDIUM signals are kept")
        void keepsRelevantMedium() {
            var relevant = signal("NUMEROLOGY", Dimension.CAREER, Polarity.SUPPORT, Strength.MEDIUM, false, "m1");

            NarrativeInput result = NarrativePruner.prune(inputWith(List.of(relevant), Set.of(Dimension.CAREER)));

            assertThat(result.signals()).containsExactly(relevant);
        }

        @Test
        @DisplayName("Priority ordering: critical first, then STRONG, then relevant MEDIUM")
        void ordersByPriority() {
            var medium = signal("A", Dimension.CAREER, Polarity.SUPPORT, Strength.MEDIUM, false, "m");
            var strong = signal("B", Dimension.CAREER, Polarity.SUPPORT, Strength.STRONG, false, "s");
            var critical = signal("C", Dimension.CAREER, Polarity.CAUTION, Strength.WEAK, true, "c");

            NarrativeInput result = NarrativePruner.prune(
                    inputWith(List.of(medium, strong, critical), Set.of(Dimension.CAREER)));

            assertThat(result.signals()).containsExactly(critical, strong, medium);
        }
    }

    @Nested
    @DisplayName("Budget (Master Spec section 22: target 8-20 signals)")
    class Budget {

        @Test
        @DisplayName("Truncates lower-priority signals first, never a critical one")
        void truncatesLowestPriorityFirst() {
            List<NarrativeSignalItem> criticals = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                criticals.add(signal("ENGINE" + i, Dimension.CAREER, Polarity.CAUTION, Strength.WEAK, true, "c" + i));
            }
            List<NarrativeSignalItem> mediums = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                mediums.add(signal("MED" + i, Dimension.CAREER, Polarity.SUPPORT, Strength.MEDIUM, false, "m" + i));
            }
            List<NarrativeSignalItem> all = new ArrayList<>(criticals);
            all.addAll(mediums);

            NarrativeInput result = NarrativePruner.prune(inputWith(all, Set.of(Dimension.CAREER)));

            assertThat(result.signals()).hasSize(NarrativePruner.MAX_SIGNAL_BUDGET);
            assertThat(result.signals()).containsAll(criticals);
        }

        @Test
        @DisplayName("Fewer than the target minimum is not padded with weak signals")
        void doesNotPadBelowMinimum() {
            var onlyCritical = signal("BAZI", Dimension.CAREER, Polarity.CAUTION, Strength.WEAK, true, "c1");

            NarrativeInput result = NarrativePruner.prune(inputWith(List.of(onlyCritical), Set.of()));

            assertThat(result.signals()).hasSize(1);
        }
    }

    @Test
    @DisplayName("Conflicts, warnings and limitations pass through untouched - never pruned")
    void passesThroughNonSignalFields() {
        var conflict = new NarrativeConflictItem("Xung dot truc tiep", "CAREER", List.of("A", "B"), "mo ta");
        NarrativeInput input = new NarrativeInput("Kich ban", Set.of(), Map.of(), List.of(), List.of(conflict),
                List.of("canh bao 1"), List.of("gioi han 1"), Map.of("id", "calc-1"));

        NarrativeInput result = NarrativePruner.prune(input);

        assertThat(result.conflicts()).containsExactly(conflict);
        assertThat(result.warnings()).containsExactly("canh bao 1");
        assertThat(result.limitations()).containsExactly("gioi han 1");
        assertThat(result.calculationMetadata()).containsEntry("id", "calc-1");
    }
}
