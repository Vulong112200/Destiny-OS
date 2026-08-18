package io.destinyos.engine;

import io.destinyos.core.context.CalculationContext;
import io.destinyos.core.result.EngineResult;

/**
 * The contract every methodology engine implements (CLAUDE.md §4, command §4).
 *
 * <p>Implementations MUST be deterministic where the methodology permits: the
 * same input, versions and seed must yield the same result (Master Spec §25).
 *
 * <p>Implementations MUST NOT call another engine. Master Spec §0 and command
 * §3 require engines to stay independent, and an ArchUnit rule enforces it —
 * cross-engine calls would make source diversity meaningless, since two
 * "independent" sources would share a derivation.
 *
 * <p>Implementations MUST NOT invent an algorithm. Where the methodology is
 * unresolved, return {@link EngineResult#researchRequired} with a reference
 * naming what is missing. CLAUDE.md Rule C admits no exception, and the
 * FINAL PRINCIPLE is explicit: no result beats an unverified result.
 *
 * @param <I> engine-specific input
 * @param <O> engine-specific payload
 */
public interface MetaphysicalEngine<I, O> {

    /** Stable identifier. Convenience accessor over {@link #metadata()}. */
    default String engineId() {
        return metadata().engineId();
    }

    /**
     * Run the calculation.
     *
     * <p>MUST NOT throw for domain conditions — an unsupported date, an
     * unresolved methodology and a missing input are all results, not
     * exceptions. The execution harness isolates genuine faults (Rule F), but
     * an engine that throws where it should have returned
     * {@code NOT_APPLICABLE} loses the explanation the user needed.
     */
    EngineResult<O> calculate(I input, CalculationContext context);

    /** Check input before spending a thread on it. */
    ValidationResult validateInput(I input);

    /** What this engine can do and what it needs. */
    EngineCapability capability();

    /** Identity, versions, school and source. */
    EngineMetadata metadata();
}
