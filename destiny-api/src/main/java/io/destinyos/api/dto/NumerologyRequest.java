package io.destinyos.api.dto;

import java.time.LocalDate;

/**
 * Input for the Numerology (Pythagorean) engine. Present in a
 * {@link ScenarioRunRequest} only when the caller wants Numerology to
 * participate — its absence is not an error, it simply means Numerology
 * contributes nothing to this run (research item R8's addressed part;
 * Chaldean remains unavailable regardless, per the registry).
 */
public record NumerologyRequest(String fullName, LocalDate birthDate) {
}
