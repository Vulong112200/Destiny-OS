package io.destinyos.ai;

/** Where {@link NarrativeResult#response()} actually came from. */
public enum NarrativeSource {

    /** A real provider call succeeded and its output passed schema validation. */
    AI_GENERATED,

    /**
     * The deterministic, non-LLM hard-data report (ADR D8). Never an error
     * state by itself - see {@link NarrativeResult#fallbackReason()} for why.
     */
    FALLBACK
}
