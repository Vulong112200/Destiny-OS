package io.destinyos.api.dto;

/**
 * Request body for {@code POST /api/v1/scenarios/{scenarioType}}.
 *
 * <p>Both {@code numerology} and {@code tarot} are optional — the caller
 * supplies whichever engines they actually want to participate. An engine
 * not supplied here is never invoked, the same way
 * {@code ScenarioEngine#run} never invokes an engine the caller's
 * {@code availableTasks} map does not contain.
 */
public record ScenarioRunRequest(NumerologyRequest numerology, TarotRequest tarot) {
}
