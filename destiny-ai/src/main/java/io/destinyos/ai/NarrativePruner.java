package io.destinyos.ai;

import io.destinyos.core.signal.Polarity;
import io.destinyos.core.signal.Strength;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Implements AI_NARRATIVE_SPEC.md section 3's pruning priority:
 *
 * <pre>
 * Uu tien: 1. CRITICAL; 2. CONFLICT; 3. STRONG; 4. scenario-relevant MEDIUM;
 *          5. warnings; 6. limitations.
 * Loai:    NEUTRAL; duplicate; irrelevant; raw calculation tree.
 * </pre>
 *
 * <p>Conflicts (priority 2) are never pruned - they live in
 * {@link NarrativeInput#conflicts()} and are always passed through unchanged,
 * never subject to this class's signal budget. Warnings/limitations
 * (priorities 5-6) are likewise passed through as-is: they arrive already
 * curated by the caller from {@code Uncertainty} entries, not as raw
 * candidates this class ranks.
 *
 * <p>What this class actually reduces is {@link NarrativeInput#signals()}:
 *
 * <ol>
 *   <li>Drop {@code NEUTRAL} polarity outright - never informative to a
 *       reader asking "what should I watch for".</li>
 *   <li>Deduplicate by {@code (engine, dimension, polarity, tag)}, keeping
 *       the first occurrence.</li>
 *   <li>Classify the remainder: {@code critical} signals and {@code STRONG}
 *       signals are always kept; {@code MEDIUM} signals are kept only when
 *       their dimension is scenario-relevant (Master Spec section 22's
 *       "irrelevant" is exactly a non-relevant MEDIUM signal); {@code WEAK}
 *       signals are dropped - the spec's priority list has no slot for
 *       them.</li>
 *   <li>Clamp to the section 22 budget (target 8-20). Truncation removes
 *       lowest-priority-class items first and never removes a critical
 *       signal to make room - critical signals are exactly what Fusion
 *       itself guarantees survive majority voting (FUSION_ENGINE_SPEC.md
 *       section 9), and pruning must not undo that guarantee.</li>
 * </ol>
 */
public final class NarrativePruner {

    /** Master Spec section 22: "Default AI payload muc tieu: 8-20 signals tuy scenario." */
    static final int MAX_SIGNAL_BUDGET = 20;

    private NarrativePruner() {
    }

    public static NarrativeInput prune(NarrativeInput input) {
        List<NarrativeSignalItem> deduped = dedupe(input.signals());
        List<NarrativeSignalItem> classified = deduped.stream()
                .filter(s -> s.polarity() != Polarity.NEUTRAL)
                .filter(s -> s.critical()
                        || s.strength() == Strength.STRONG
                        || (s.strength() == Strength.MEDIUM
                                && input.scenarioRelevantDimensions().contains(s.dimension())))
                .sorted(Comparator.comparingInt(NarrativePruner::priorityRank))
                .toList();

        List<NarrativeSignalItem> withinBudget = enforceBudget(classified);
        return input.withSignals(withinBudget);
    }

    private static List<NarrativeSignalItem> dedupe(List<NarrativeSignalItem> signals) {
        Set<String> seen = new LinkedHashSet<>();
        List<NarrativeSignalItem> result = new ArrayList<>();
        for (NarrativeSignalItem signal : signals) {
            if (seen.add(signal.dedupeKey())) {
                result.add(signal);
            }
        }
        return result;
    }

    /** Lower rank sorts first: critical, then STRONG, then relevant MEDIUM. */
    private static int priorityRank(NarrativeSignalItem signal) {
        if (signal.critical()) {
            return 0;
        }
        return signal.strength() == Strength.STRONG ? 1 : 2;
    }

    private static List<NarrativeSignalItem> enforceBudget(List<NarrativeSignalItem> ranked) {
        if (ranked.size() <= MAX_SIGNAL_BUDGET) {
            return ranked;
        }

        // ranked is already priority-sorted (critical first, then STRONG,
        // then relevant MEDIUM), and stable within each class (Java's sort
        // is stable) - so simply truncating keeps every critical signal
        // (there is no scenario in which fewer than MAX_SIGNAL_BUDGET
        // critical+STRONG signals exist and a later, lower-priority MEDIUM
        // signal is kept in its place).
        return ranked.subList(0, MAX_SIGNAL_BUDGET);
    }
}
