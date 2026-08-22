package io.destinyos.api.controller;

import io.destinyos.api.dto.ScenarioRunRequest;
import io.destinyos.api.dto.ScenarioRunResponse;
import io.destinyos.api.service.ScenarioOrchestrationService;
import io.destinyos.scenario.ScenarioType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code Calculation} + {@code Scenario} API groups (command section 41:
 * distinct endpoint groups, not one large endpoint).
 *
 * <p>Contains no domain calculation (CLAUDE.md section 3) — every field here
 * is request/response mapping; the actual orchestration is
 * {@link ScenarioOrchestrationService}, which itself never imports a
 * concrete engine (see {@code EngineTaskFactory}'s Javadoc).
 */
@RestController
@RequestMapping("/api/v1/scenarios")
public class ScenarioController {

    private final ScenarioOrchestrationService orchestration;

    public ScenarioController(ScenarioOrchestrationService orchestration) {
        this.orchestration = orchestration;
    }

    @PostMapping("/{scenarioType}")
    public ResponseEntity<ScenarioRunResponse> run(@PathVariable String scenarioType,
                                                    @RequestBody(required = false) ScenarioRunRequest request) {
        ScenarioType type = ScenarioType.valueOf(scenarioType.toUpperCase(java.util.Locale.ROOT));
        ScenarioRunRequest body = request == null ? new ScenarioRunRequest(null, null, null, null) : request;
        return ResponseEntity.ok(orchestration.run(type, body));
    }
}
