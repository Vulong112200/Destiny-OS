package io.destinyos.api.controller;

import io.destinyos.api.dto.ScenarioRunResponse;
import io.destinyos.api.service.CalculationQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** {@code History}/{@code Diagnostics} API group (command section 41): replay a past run. */
@RestController
@RequestMapping("/api/v1/calculations")
public class CalculationController {

    private final CalculationQueryService query;

    public CalculationController(CalculationQueryService query) {
        this.query = query;
    }

    @GetMapping("/{calculationId}")
    public ResponseEntity<ScenarioRunResponse> find(@PathVariable String calculationId) {
        return query.find(calculationId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
