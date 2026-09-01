package io.destinyos.scenario;

import io.destinyos.core.context.CalculationContext;
import io.destinyos.core.signal.Applicability;
import io.destinyos.core.signal.Signal;
import io.destinyos.execution.EngineExecutor;
import io.destinyos.execution.EngineTask;
import io.destinyos.execution.ExecutionOutcome;
import io.destinyos.fusion.FusionEngine;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Orchestrates one scenario: picks which of the caller's available engines
 * actually apply, runs them, clamps every signal to the scenario's declared
 * applicability, and hands the result to {@link FusionEngine}
 * (Master Spec section 11).
 *
 * <p>Deliberately generic over concrete engines — this class knows only the
 * {@link io.destinyos.engine.MetaphysicalEngine} SPI via
 * {@link EngineTask}, never a concrete {@code destiny-engine-*} type. The
 * caller (eventually an API layer, today {@code destiny-app} or a test)
 * supplies whichever engines it has actually wired up; a scenario whose
 * policy names an engine the caller does not supply simply does not run
 * that engine, and says so in {@link ScenarioResult#unavailableEngines()}
 * rather than silently proceeding as if it did not exist.
 */
public final class ScenarioEngine {

    private final EngineExecutor executor;
    private final FusionEngine fusion;

    public ScenarioEngine(EngineExecutor executor, FusionEngine fusion) {
        this.executor = Objects.requireNonNull(executor, "executor");
        this.fusion = Objects.requireNonNull(fusion, "fusion");
    }

    public static ScenarioEngine withDefaults() {
        return new ScenarioEngine(EngineExecutor.withDefaults(), new FusionEngine());
    }

    /**
     * Runs {@code scenarioType} using whichever of {@code availableTasks}
     * the scenario's policy actually names.
     *
     * @param availableTasks engineId to a ready-to-run task, supplied by
     *                       the caller — only entries whose key appears in
     *                       the scenario's applicability policy are used
     */
    public ScenarioResult run(ScenarioType scenarioType, Map<String, EngineTask<?, ?>> availableTasks,
                              CalculationContext context) {
        Objects.requireNonNull(scenarioType, "scenarioType");
        Objects.requireNonNull(availableTasks, "availableTasks");
        Objects.requireNonNull(context, "context");

        ScenarioDefinition definition = ScenarioRegistry.get(scenarioType);

        if (!definition.policyDefined()) {
            // Master Spec section 11 names this scenario but no document
            // specifies which engines apply to it. Running nothing is the
            // honest choice - guessing an applicability policy here would
            // be an unsourced product decision, not a software judgment
            // call the way Fusion's vote thresholds are.
            // Nothing ran, so everything the caller supplied is input this
            // scenario did not use. It is reported in both lists on purpose:
            // `unavailableEngines` because the run produced no engine results,
            // and `enginesOutsideScenario` because from the caller's side their
            // input went unused, which is the thing they need told.
            return new ScenarioResult(scenarioType, false, new ExecutionOutcome(List.of()),
                    List.copyOf(availableTasks.keySet()),
                    List.copyOf(availableTasks.keySet()), null);
        }

        List<EngineTask<?, ?>> tasksToRun = new ArrayList<>();
        List<String> unavailable = new ArrayList<>();

        for (String engineId : definition.applicableEngines().keySet()) {
            EngineTask<?, ?> task = availableTasks.get(engineId);
            if (task != null) {
                tasksToRun.add(task);
            } else {
                unavailable.add(engineId);
            }
        }

        // The other direction, which this loop used to ignore entirely: input
        // the caller supplied for an engine this scenario's policy does not
        // name. Only policy-named engines run — that part is deliberate, it is
        // what makes a scenario a scenario — but until 2026-09-01 the discarded
        // input was invisible in the result. A user who chose Tarot for "Mua
        // sắm" got back astrology and feng shui, neither of which emits an
        // interpretation, and nothing anywhere said their Tarot draw had been
        // dropped. Running it anyway would break the policy; saying nothing
        // breaks the honesty rule the rest of this class is built on, so the
        // fix is to report it.
        List<String> outsideScenario = new ArrayList<>();
        for (String suppliedEngineId : availableTasks.keySet()) {
            if (!definition.applicableEngines().containsKey(suppliedEngineId)) {
                outsideScenario.add(suppliedEngineId);
            }
        }

        ExecutionOutcome execution = executor.runAll(tasksToRun, context);

        List<Signal> clampedSignals = execution.flatMap(exec -> {
            Applicability scenarioLevel = definition.applicabilityFor(exec.engineId());
            return exec.result().signals().stream()
                    .map(s -> clampApplicability(s, scenarioLevel))
                    .toList();
        });

        var fusionResult = fusion.fuse(clampedSignals);

        return new ScenarioResult(scenarioType, true, execution, unavailable,
                outsideScenario, fusionResult);
    }

    /**
     * A signal's own applicability can only be narrowed by the scenario,
     * never widened — a scenario that considers an engine MEDIUM cannot
     * make an engine that judged itself LOW into something more relevant
     * than the engine itself claimed.
     */
    private static Signal clampApplicability(Signal signal, Applicability scenarioLevel) {
        Applicability effective = moreRestrictive(signal.applicability(), scenarioLevel);
        if (effective == signal.applicability()) {
            return signal;
        }
        return new Signal(signal.signalId(), signal.engine(), signal.school(), signal.dimension(),
                signal.tag(), signal.polarity(), signal.strength(), effective, signal.critical(),
                signal.evidenceIds(), signal.evidenceGroupId());
    }

    /** Applicability's declared order (HIGH..NOT_APPLICABLE) is already most-to-least relevant. */
    private static Applicability moreRestrictive(Applicability a, Applicability b) {
        return a.ordinal() >= b.ordinal() ? a : b;
    }
}
