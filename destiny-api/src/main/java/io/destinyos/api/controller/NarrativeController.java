package io.destinyos.api.controller;

import io.destinyos.api.dto.NarrativeResponseDto;
import io.destinyos.api.service.NarrativeOrchestrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Narrative API group (command section 41; Phase 12, ADR D8). Deliberately a
 * separate call from {@code POST /api/v1/scenarios/{scenarioType}}: the AI
 * stage is optional and may call a third-party provider, so a scenario run
 * persists and returns its hard-data result immediately, and a narrative is
 * requested for it afterward, on its own timeout budget.
 *
 * <p>{@code 404} means "no calculation with this id" ({@link #generate}) or
 * "no narrative generated yet" ({@link #find}) - never a 500. A
 * research-blocked or AI-disabled system still returns {@code 200} with the
 * deterministic fallback narrative (ADR D8, D7's same honesty principle
 * applied to this stage).
 */
@RestController
@RequestMapping("/api/v1/calculations")
public class NarrativeController {

    private final NarrativeOrchestrationService orchestration;

    public NarrativeController(NarrativeOrchestrationService orchestration) {
        this.orchestration = orchestration;
    }

    @PostMapping("/{calculationId}/narrative")
    public ResponseEntity<NarrativeResponseDto> generate(@PathVariable String calculationId) {
        return orchestration.generate(calculationId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{calculationId}/narrative")
    public ResponseEntity<NarrativeResponseDto> find(@PathVariable String calculationId) {
        return orchestration.find(calculationId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
