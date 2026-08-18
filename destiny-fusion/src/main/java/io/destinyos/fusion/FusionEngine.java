package io.destinyos.fusion;

import io.destinyos.core.signal.Dimension;
import io.destinyos.core.signal.Polarity;
import io.destinyos.core.signal.Signal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Evidence-based rule fusion (FUSION_ENGINE_SPEC.md), not a weighted average
 * (ADR D6). Depends only on {@link Signal} - ADR D5 - so it needed no
 * concrete engine to build or test.
 *
 * <p>The qualitative rules R1-R8 come from {@code FUSION_ENGINE_SPEC.md}
 * section 7. That document deliberately does not fix exact vote-count
 * arithmetic ("chiếm ưu thế" is never given a threshold), because Fusion's
 * rules are this project's own design, not an external tradition to cite -
 * unlike Bát Tự, there is no source to research here, only an engineering
 * decision to make and document. The specific thresholds below (strict
 * majority = more sources than every other polarity combined) are that
 * decision, recorded here rather than left implicit.
 *
 * <p>Pipeline (section 2): applicability is already encoded in
 * {@link Signal#participates()}; deduplication is achieved by counting
 * <strong>distinct engines</strong>, never signals or evidence, per source
 * diversity (section 5, section 8.3) — five signals from one engine count
 * as one source, always.
 */
public final class FusionEngine {

    /**
     * Runs the full pipeline over every applicable signal from every engine
     * that ran, regardless of dimension.
     */
    public FusionResult fuse(List<Signal> allSignals) {
        List<Signal> applicable = allSignals == null ? List.of()
                : allSignals.stream().filter(Signal::participates).toList();

        Map<Dimension, List<Signal>> byDimension = applicable.stream()
                .collect(Collectors.groupingBy(Signal::dimension, LinkedHashMap::new, Collectors.toList()));

        List<DimensionAnalysis> analyses = byDimension.entrySet().stream()
                .map(e -> analyzeDimension(e.getKey(), e.getValue()))
                .toList();

        List<Conflict> conflicts = detectConflicts(byDimension, analyses);

        List<Signal> allCriticalSignals = analyses.stream()
                .flatMap(a -> a.criticalSignals().stream())
                .toList();

        Set<String> supportingSources = union(analyses, DimensionAnalysis::supportingEngines);
        Set<String> cautionSources = union(analyses, DimensionAnalysis::cautionEngines);

        List<String> allRules = new ArrayList<>();
        analyses.forEach(a -> a.rulesApplied().forEach(r -> {
            if (!allRules.contains(r)) {
                allRules.add(r);
            }
        }));

        FusionOutcome outcome = synthesizeOutcome(analyses, conflicts, allRules);

        return new FusionResult(outcome, analyses, conflicts, allCriticalSignals, allRules,
                supportingSources, cautionSources);
    }

    // ------------------------------------------------------------------
    // Per-dimension analysis (rules R1-R6; R5's critical override; R8's
    // methodology-conflict detection happens separately in detectConflicts)
    // ------------------------------------------------------------------

    DimensionAnalysis analyzeDimension(Dimension dimension, List<Signal> signals) {
        Set<String> support = enginesWith(signals, Polarity.SUPPORT);
        Set<String> caution = enginesWith(signals, Polarity.CAUTION);
        Set<String> negative = enginesWith(signals, Polarity.NEGATIVE);
        Set<String> neutral = enginesWith(signals, Polarity.NEUTRAL);

        List<Signal> criticalSignals = signals.stream().filter(Signal::isActiveCritical).toList();

        int totalSources = union(support, caution, negative, neutral).size();
        List<String> rules = new ArrayList<>();
        DimensionState state;

        if (totalSources == 0) {
            // R1 - Insufficient: no applicable signal at all.
            state = DimensionState.INSUFFICIENT_EVIDENCE;
            rules.add("R1");
        } else {
            state = voteOnPositionedPolarities(support, caution, negative, rules);
            applyCriticalOverride(state, criticalSignals, rules);
            state = resolveStateAfterCritical(state, criticalSignals);
        }

        return new DimensionAnalysis(dimension, state, support, caution, negative, neutral,
                criticalSignals, rules);
    }

    /** R2/R3/R4/R6/R7: the vote among sources that took a position (SUPPORT/CAUTION/NEGATIVE). */
    private static DimensionState voteOnPositionedPolarities(Set<String> support, Set<String> caution,
                                                             Set<String> negative, List<String> rules) {
        int s = support.size();
        int c = caution.size();
        int n = negative.size();
        int positioned = (s > 0 ? 1 : 0) + (c > 0 ? 1 : 0) + (n > 0 ? 1 : 0);

        if (positioned <= 1) {
            // Unanimous among positioned sources (R2/R3). Neutral-only
            // sources do not count as a position either way.
            if (s > 0) {
                rules.add("R2");
                return DimensionState.POSITIVE;
            }
            if (c > 0) {
                rules.add("R3");
                return DimensionState.CAUTION;
            }
            if (n > 0) {
                // Symmetric extension of R3 for unanimous NEGATIVE, not
                // named separately in FUSION_ENGINE_SPEC.md but required
                // for CONSENSUS_NEGATIVE (Master Spec section 9) to be
                // reachable at all.
                rules.add("R3");
                return DimensionState.NEGATIVE;
            }
            // Only NEUTRAL sources took part - nothing to report.
            rules.add("R1");
            return DimensionState.INSUFFICIENT_EVIDENCE;
        }

        // More than one polarity is positioned. Adopted threshold (see
        // class Javadoc): a "strict majority" is more sources than every
        // other polarity combined.
        int max = Math.max(s, Math.max(c, n));

        if (s == max && s > c + n) {
            rules.add("R4");
            return DimensionState.POSITIVE;
        }
        if (c == max && c > s + n) {
            rules.add("R4");
            return DimensionState.CAUTION;
        }
        if (n == max && n > s + c) {
            rules.add("R4");
            return DimensionState.NEGATIVE;
        }

        // No strict majority. Distinguish a true opposite-pole standoff
        // (SUPPORT vs NEGATIVE both present) from a softer SUPPORT/CAUTION
        // split, since Rule E gives these different severities.
        if (s > 0 && n > 0) {
            if (c == 0 && s == n) {
                rules.add("R7");
                return DimensionState.MAJOR_CONFLICT;
            }
            rules.add("R7");
            return DimensionState.CONFLICT;
        }

        rules.add("R6");
        return DimensionState.MIXED;
    }

    /** R5: a critical signal must survive regardless of majority. Records that R5 fired. */
    private static void applyCriticalOverride(DimensionState baseState, List<Signal> criticalSignals,
                                              List<String> rules) {
        if (!criticalSignals.isEmpty()) {
            rules.add("R5");
        }
    }

    /**
     * R5's actual effect on the reported state: a critical signal opposing
     * the majority downgrades an otherwise-positive reading to caution (or
     * negative), because {@link DimensionState} (Rule E's 8-value
     * vocabulary) has no compound "positive but critically cautioned"
     * member. That finer distinction is preserved losslessly at the
     * {@link FusionOutcome} layer (e.g. {@code SUPPORT_WITH_CRITICAL_CAUTION})
     * via {@link DimensionAnalysis#criticalSignals()}, which always travels
     * with the state - nothing is discarded, only summarized at this layer.
     */
    private static DimensionState resolveStateAfterCritical(DimensionState baseState,
                                                             List<Signal> criticalSignals) {
        if (criticalSignals.isEmpty()) {
            return baseState;
        }
        Set<Polarity> criticalPolarities = criticalSignals.stream()
                .map(Signal::polarity).collect(Collectors.toSet());

        boolean baseIsPositive = baseState == DimensionState.POSITIVE;
        boolean criticalOpposesWithCaution = criticalPolarities.contains(Polarity.CAUTION);
        boolean criticalOpposesWithNegative = criticalPolarities.contains(Polarity.NEGATIVE);

        if (baseIsPositive && criticalOpposesWithNegative) {
            return DimensionState.NEGATIVE;
        }
        if (baseIsPositive && criticalOpposesWithCaution) {
            return DimensionState.CAUTION;
        }
        // Critical signal aligns with the base state (or the base state is
        // already CAUTION/NEGATIVE/MIXED/CONFLICT) - the critical fact is
        // still recorded via rule R5 and criticalSignals, but does not
        // change which of Rule E's 8 states applies.
        return baseState;
    }

    // ------------------------------------------------------------------
    // Conflict detection (section 8): DIRECT_CONFLICT and
    // METHODOLOGY_CONFLICT. SCOPE_CONFLICT is deliberately never raised
    // here - signals in different dimensions are analyzed independently
    // and never compared against each other, which is what makes a
    // cross-dimension disagreement a non-event rather than a conflict.
    // ------------------------------------------------------------------

    private static List<Conflict> detectConflicts(Map<Dimension, List<Signal>> byDimension,
                                                   List<DimensionAnalysis> analyses) {
        List<Conflict> conflicts = new ArrayList<>();

        for (DimensionAnalysis analysis : analyses) {
            if (analysis.state() == DimensionState.CONFLICT
                    || analysis.state() == DimensionState.MAJOR_CONFLICT) {
                Set<String> involved = new LinkedHashSet<>();
                involved.addAll(analysis.supportingEngines());
                involved.addAll(analysis.negativeEngines());
                conflicts.add(new Conflict(ConflictType.DIRECT_CONFLICT, analysis.dimension(),
                        List.copyOf(involved),
                        "Tín hiệu SUPPORT và NEGATIVE cùng xuất hiện ở " + analysis.dimension()
                                + " mà không có bên nào chiếm ưu thế rõ ràng."));
            }

            List<Signal> methodologyConflict = detectMethodologyConflict(
                    byDimension.getOrDefault(analysis.dimension(), List.of()));
            if (!methodologyConflict.isEmpty()) {
                List<String> engines = methodologyConflict.stream()
                        .map(Signal::engine).distinct().toList();
                String schools = methodologyConflict.stream()
                        .map(Signal::school).filter(java.util.Objects::nonNull).distinct()
                        .collect(Collectors.joining(" vs "));
                conflicts.add(new Conflict(ConflictType.METHODOLOGY_CONFLICT, analysis.dimension(),
                        engines,
                        "Hai trường phái trong cùng engine bất đồng ở " + analysis.dimension()
                                + (schools.isBlank() ? "" : " (" + schools + ")")
                                + " - không tự động gộp thành một kết quả."));
            }
        }

        return conflicts;
    }

    /**
     * A methodology conflict, per FUSION_ENGINE_SPEC.md section 8, is two
     * <em>schools of the same engine</em> disagreeing - not two different
     * engines disagreeing (that is an ordinary {@code DIRECT_CONFLICT}).
     * Detected as: the same {@code engine} id producing signals of opposite
     * polarity from two different {@code school} values in one dimension.
     */
    private static List<Signal> detectMethodologyConflict(List<Signal> dimensionSignals) {
        Map<String, List<Signal>> byEngine = dimensionSignals.stream()
                .filter(Signal::participates)
                .collect(Collectors.groupingBy(Signal::engine));

        for (List<Signal> engineSignals : byEngine.values()) {
            Set<String> schools = engineSignals.stream()
                    .map(Signal::school).filter(java.util.Objects::nonNull)
                    .collect(Collectors.toSet());
            if (schools.size() < 2) {
                continue;
            }
            Set<Polarity> polarities = engineSignals.stream().map(Signal::polarity)
                    .collect(Collectors.toSet());
            boolean opposed = polarities.contains(Polarity.SUPPORT)
                    && (polarities.contains(Polarity.CAUTION) || polarities.contains(Polarity.NEGATIVE));
            if (opposed) {
                return engineSignals;
            }
        }
        return List.of();
    }

    // ------------------------------------------------------------------
    // Overall scenario outcome (DECISION_LOG C2's 12-value union)
    // ------------------------------------------------------------------

    private static FusionOutcome synthesizeOutcome(List<DimensionAnalysis> analyses,
                                                    List<Conflict> conflicts, List<String> allRules) {
        if (analyses.isEmpty()) {
            return FusionOutcome.INSUFFICIENT_EVIDENCE;
        }

        boolean anyMethodologyConflict = conflicts.stream()
                .anyMatch(c -> c.type() == ConflictType.METHODOLOGY_CONFLICT);
        if (anyMethodologyConflict) {
            return FusionOutcome.METHODOLOGY_CONFLICT;
        }

        boolean anyMajorConflict = analyses.stream()
                .anyMatch(a -> a.state() == DimensionState.MAJOR_CONFLICT
                        || a.state() == DimensionState.CONFLICT);
        if (anyMajorConflict) {
            // FusionOutcome (C2's union) has no separate "minor conflict"
            // member distinct from MAJOR_CONFLICT; both DimensionState
            // severities compress to this one outcome at the scenario
            // layer. The finer distinction remains visible per-dimension
            // via DimensionAnalysis.state().
            return FusionOutcome.MAJOR_CONFLICT;
        }

        // All remaining dimensions are non-conflicting. If they disagree
        // with each other in aggregate direction (some POSITIVE, some
        // CAUTION/NEGATIVE, none of them individually a conflict), that is
        // a MIXED scenario overall.
        boolean anyMixed = analyses.stream().anyMatch(a -> a.state() == DimensionState.MIXED);
        Set<DimensionState> distinctStates = analyses.stream()
                .map(DimensionAnalysis::state)
                .filter(s -> s != DimensionState.INSUFFICIENT_EVIDENCE)
                .collect(Collectors.toSet());

        if (analyses.size() == 1) {
            return singleDimensionOutcome(analyses.get(0), allRules);
        }

        if (anyMixed || distinctStates.size() > 1) {
            return FusionOutcome.MIXED;
        }

        if (distinctStates.isEmpty()) {
            return FusionOutcome.INSUFFICIENT_EVIDENCE;
        }

        // Every dimension agrees on the same single state - fold through
        // the single-dimension mapping using the first dimension as
        // representative (they are all the same state by construction).
        return singleDimensionOutcome(analyses.get(0), allRules);
    }

    /**
     * Derives the scenario-level outcome for a single dimension.
     *
     * <p>Deliberately does <strong>not</strong> switch on
     * {@link DimensionAnalysis#state()} for the POSITIVE/CAUTION/NEGATIVE
     * cases: {@code state} has already been through
     * {@link #resolveStateAfterCritical}, which downgrades e.g. POSITIVE to
     * CAUTION when a critical caution opposes it — correct for what the
     * per-dimension display should read, but it destroys the information
     * this method needs (which side the plain vote count actually favored)
     * to distinguish {@code SUPPORT_WITH_CRITICAL_CAUTION} from
     * {@code CAUTION_WITH_CRITICAL_SUPPORT}. This method recovers that from
     * the raw engine-count sets instead, which critical adjustment never
     * touches.
     */
    private static FusionOutcome singleDimensionOutcome(DimensionAnalysis analysis, List<String> allRules) {
        DimensionState state = analysis.state();

        if (state == DimensionState.INSUFFICIENT_EVIDENCE) {
            return FusionOutcome.INSUFFICIENT_EVIDENCE;
        }
        if (state == DimensionState.MAJOR_CONFLICT || state == DimensionState.CONFLICT) {
            return FusionOutcome.MAJOR_CONFLICT;
        }
        if (state == DimensionState.MIXED) {
            return FusionOutcome.MIXED;
        }

        int supportCount = analysis.supportingEngines().size();
        int cautionCount = analysis.cautionEngines().size();
        int negativeCount = analysis.negativeEngines().size();
        int opposeSupportCount = cautionCount + negativeCount;

        boolean hasCritical = analysis.hasCriticalSignal();
        boolean criticalIsSupport = analysis.criticalSignals().stream()
                .anyMatch(s -> s.polarity() == Polarity.SUPPORT);
        boolean criticalOpposesSupport = analysis.criticalSignals().stream()
                .anyMatch(s -> s.polarity() == Polarity.CAUTION || s.polarity() == Polarity.NEGATIVE);

        // R5: a critical signal survives regardless of which way the plain
        // vote count leans - checked first, using raw counts rather than
        // the already-adjusted `state`.
        if (hasCritical && criticalOpposesSupport && supportCount > 0 && supportCount >= opposeSupportCount) {
            return FusionOutcome.SUPPORT_WITH_CRITICAL_CAUTION;
        }
        if (hasCritical && criticalIsSupport && opposeSupportCount > 0 && opposeSupportCount >= supportCount) {
            return FusionOutcome.CAUTION_WITH_CRITICAL_SUPPORT;
        }

        // Plain majority reading (R2/R3/R4).
        if (supportCount > 0 && supportCount >= opposeSupportCount) {
            return allRules.contains("R4") ? FusionOutcome.SUPPORT_WITH_CAUTION : FusionOutcome.CONSENSUS_SUPPORT;
        }
        if (negativeCount > 0 && negativeCount >= supportCount + cautionCount) {
            return FusionOutcome.CONSENSUS_NEGATIVE;
        }
        if (cautionCount > 0) {
            return allRules.contains("R4") ? FusionOutcome.CAUTION_WITH_SUPPORT : FusionOutcome.CONSENSUS_CAUTION;
        }
        return FusionOutcome.INSUFFICIENT_EVIDENCE;
    }

    // ------------------------------------------------------------------
    // Small helpers
    // ------------------------------------------------------------------

    private static Set<String> enginesWith(List<Signal> signals, Polarity polarity) {
        return signals.stream()
                .filter(s -> s.polarity() == polarity)
                .map(Signal::engine)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @SafeVarargs
    private static Set<String> union(Set<String>... sets) {
        Set<String> result = new HashSet<>();
        for (Set<String> s : sets) {
            result.addAll(s);
        }
        return result;
    }

    private static Set<String> union(List<DimensionAnalysis> analyses,
                                     java.util.function.Function<DimensionAnalysis, Set<String>> extractor) {
        Set<String> result = new LinkedHashSet<>();
        analyses.forEach(a -> result.addAll(extractor.apply(a)));
        return result;
    }
}
