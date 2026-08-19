package io.destinyos.api.dto;

/**
 * How one engine fared (CLAUDE.md Rule F). {@code status} carries the
 * honest non-answers ({@code NOT_APPLICABLE}, {@code RESEARCH_REQUIRED})
 * alongside real failures — a client renders both without treating either
 * as an HTTP error, per ADR D7.
 */
public record EngineOutcomeDto(
        String engine,
        LabeledValue status,
        boolean timedOut,
        long durationMs
) {
}
