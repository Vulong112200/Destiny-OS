package io.destinyos.engine;

import java.util.Objects;

/**
 * Identity and provenance of an engine (command §4, CLAUDE.md Rule D).
 *
 * <p>{@code school} and {@code source} are required rather than optional
 * because Rule D forbids silently selecting a school: an engine that cannot
 * name its tradition and cite where its rules come from has no business
 * producing output.
 *
 * @param engineId           stable id, e.g. {@code TAROT}
 * @param displayNameVi      Vietnamese name for the UI (CLAUDE.md §9)
 * @param methodologyId      e.g. {@code NUMEROLOGY_PYTHAGOREAN}
 * @param methodologyVersion methodology version
 * @param engineVersion      implementation version
 * @param school             the tradition this follows
 * @param source             citation for the rules
 * @param status             lifecycle state
 */
public record EngineMetadata(
        String engineId,
        String displayNameVi,
        String methodologyId,
        String methodologyVersion,
        String engineVersion,
        String school,
        String source,
        MethodologyStatus status
) {
    public EngineMetadata {
        Objects.requireNonNull(engineId, "engineId");
        Objects.requireNonNull(displayNameVi, "displayNameVi");
        Objects.requireNonNull(methodologyId, "methodologyId");
        Objects.requireNonNull(methodologyVersion, "methodologyVersion");
        Objects.requireNonNull(engineVersion, "engineVersion");
        Objects.requireNonNull(status, "status");

        if (status.mayCalculate() && (school == null || school.isBlank())) {
            throw new IllegalArgumentException(
                    "Engine " + engineId + " may calculate but names no school. "
                            + "CLAUDE.md Rule D forbids silently selecting one.");
        }
        if (status.mayCalculate() && (source == null || source.isBlank())) {
            throw new IllegalArgumentException(
                    "Engine " + engineId + " may calculate but cites no source. "
                            + "CLAUDE.md Rule C requires a citation, not a plausible formula.");
        }
    }
}
