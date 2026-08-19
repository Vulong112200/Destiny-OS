package io.destinyos.api.service;

import io.destinyos.api.dto.ScenarioRunRequest;
import io.destinyos.execution.EngineTask;
import java.util.Optional;

/**
 * Converts a generic {@link ScenarioRunRequest} into one engine's specific,
 * strongly-typed {@link EngineTask} — or declines to, if the request does
 * not supply that engine's required input.
 *
 * <p>This interface is the seam that keeps {@code destiny-api} from ever
 * depending on a concrete {@code destiny-engine-*} type (CLAUDE.md section 3;
 * an ArchUnit rule in {@code destiny-app} enforces it mechanically, the same
 * way ADR D5 protects {@code destiny-fusion}). Building a
 * {@code TarotDrawInput} or a {@code NumerologyInput} requires knowing that
 * concrete type, so every implementation of this interface lives in
 * {@code destiny-app}, which already depends on the concrete engines and
 * exists precisely for this kind of assembly. {@code destiny-api}'s
 * orchestration service only ever calls through this interface.
 */
public interface EngineTaskFactory {

    /**
     * @return the task to run, or empty if {@code request} does not supply
     *         what this engine needs (not an error — the caller simply
     *         didn't ask for this engine)
     */
    Optional<EngineTask<?, ?>> createTask(ScenarioRunRequest request);
}
