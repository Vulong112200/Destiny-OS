package io.destinyos.execution;

import io.destinyos.core.result.EngineResult;
import io.destinyos.core.result.EngineStatus;
import java.time.Duration;
import java.util.Objects;

/**
 * One engine's result plus how it ran (CLAUDE.md §40 observability).
 *
 * @param engineId   which engine
 * @param result     what it returned — never null, even on failure
 * @param duration   wall-clock time
 * @param timedOut   whether the budget was exceeded
 */
public record EngineExecution(
        String engineId,
        EngineResult<?> result,
        Duration duration,
        boolean timedOut
) {
    public EngineExecution {
        Objects.requireNonNull(engineId, "engineId");
        Objects.requireNonNull(result, "result");
        Objects.requireNonNull(duration, "duration");
    }

    public EngineStatus status() {
        return result.status();
    }

    public boolean contributedData() {
        return result.status().hasUsableData();
    }
}
