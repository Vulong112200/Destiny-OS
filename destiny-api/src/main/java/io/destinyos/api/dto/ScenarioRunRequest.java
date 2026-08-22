package io.destinyos.api.dto;

/**
 * Request body for {@code POST /api/v1/scenarios/{scenarioType}}.
 *
 * <p>Every field is optional — the caller supplies whichever engines they
 * actually want to participate. An engine not supplied here is never invoked,
 * the same way {@code ScenarioEngine#run} never invokes an engine the caller's
 * {@code availableTasks} map does not contain, and the response's
 * {@code unavailableEngines} says which ones the scenario wanted but did not
 * get.
 */
public record ScenarioRunRequest(
        NumerologyRequest numerology,
        TarotRequest tarot,
        BaziRequest bazi,
        FengShuiRequest fengShui
) {
}
