package io.destinyos.execution;

import io.destinyos.core.result.EngineStatus;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Result of running a batch of engines (Master Spec §23, command §18).
 *
 * <p>The whole point of this type is that a batch <em>never</em> fails as a
 * unit. If Bát Tự succeeds, Tử Vi succeeds, Tarot times out and Astrology
 * fails, the outcome is {@code PARTIAL} with four distinct statuses — not an
 * error. Command §18 is emphatic about the consequence: with an engine missing,
 * nothing downstream may claim "tất cả hệ thống đều đồng thuận".
 *
 * @param executions per-engine results, in completion-independent order
 */
public record ExecutionOutcome(List<EngineExecution> executions) {

    public ExecutionOutcome {
        executions = executions == null ? List.of() : List.copyOf(executions);
    }

    public Map<String, EngineStatus> statusByEngine() {
        return executions.stream().collect(Collectors.toMap(
                EngineExecution::engineId, EngineExecution::status,
                (a, b) -> a, java.util.LinkedHashMap::new));
    }

    /** Engines that produced usable findings. */
    public List<EngineExecution> contributing() {
        return executions.stream().filter(EngineExecution::contributedData).toList();
    }

    /** Engines that malfunctioned, as distinct from those that declined to answer. */
    public List<EngineExecution> failed() {
        return executions.stream().filter(e -> e.status().isFailure()).toList();
    }

    /** Engines that gave an honest non-answer: NOT_APPLICABLE / RESEARCH_REQUIRED / NOT_IMPLEMENTED. */
    public List<EngineExecution> honestNonAnswers() {
        return executions.stream().filter(e -> e.status().isHonestNonAnswer()).toList();
    }

    public List<EngineExecution> timedOut() {
        return executions.stream().filter(EngineExecution::timedOut).toList();
    }

    /**
     * Whether the picture is incomplete because something went wrong.
     *
     * <p>Note what does NOT count: an engine that honestly declined
     * ({@code NOT_APPLICABLE}, {@code RESEARCH_REQUIRED}) is not a gap in the
     * data — it is a correct, complete answer of "this does not apply". Only
     * genuine failures make the picture incomplete.
     *
     * <p>Downstream narration MUST consult this before describing consensus
     * (command §18).
     */
    public boolean isIncomplete() {
        return !failed().isEmpty();
    }

    /** Overall status: SUCCESS only when nothing failed and something contributed. */
    public EngineStatus overallStatus() {
        if (executions.isEmpty()) {
            return EngineStatus.NOT_APPLICABLE;
        }
        if (contributing().isEmpty()) {
            return failed().isEmpty() ? EngineStatus.NOT_APPLICABLE : EngineStatus.PARTIAL;
        }
        return isIncomplete() ? EngineStatus.PARTIAL : EngineStatus.SUCCESS;
    }

    public <T> List<T> flatMap(Function<EngineExecution, List<T>> mapper) {
        return executions.stream().map(mapper).flatMap(List::stream).toList();
    }

    public EngineExecution forEngine(String engineId) {
        Objects.requireNonNull(engineId, "engineId");
        return executions.stream()
                .filter(e -> e.engineId().equals(engineId))
                .findFirst()
                .orElse(null);
    }
}
