package io.destinyos.execution;

import io.destinyos.core.context.CalculationContext;
import io.destinyos.core.result.EngineResult;
import io.destinyos.engine.MetaphysicalEngine;
import java.util.Objects;

/**
 * One engine bound to its input, ready to run.
 *
 * <p>Exists because {@link MetaphysicalEngine} is generic in its input type
 * while the executor runs a heterogeneous batch. Binding the input here lets
 * the executor treat every task uniformly, without unchecked casts leaking
 * into the harness.
 *
 * @param <I> engine input type
 * @param <O> engine payload type
 */
public final class EngineTask<I, O> {

    private final MetaphysicalEngine<I, O> engine;
    private final I input;

    private EngineTask(MetaphysicalEngine<I, O> engine, I input) {
        this.engine = Objects.requireNonNull(engine, "engine");
        this.input = input;
    }

    public static <I, O> EngineTask<I, O> of(MetaphysicalEngine<I, O> engine, I input) {
        return new EngineTask<>(engine, input);
    }

    public String engineId() {
        return engine.engineId();
    }

    EngineResult<O> run(CalculationContext context) {
        return engine.calculate(input, context);
    }

    MetaphysicalEngine<I, O> engine() {
        return engine;
    }

    I input() {
        return input;
    }
}
