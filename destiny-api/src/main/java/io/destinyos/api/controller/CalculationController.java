package io.destinyos.api.controller;

import io.destinyos.api.dto.RetentionDto;
import io.destinyos.api.dto.ScenarioRunResponse;
import io.destinyos.api.service.CalculationQueryService;
import io.destinyos.api.service.CalculationSaveService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** {@code History}/{@code Diagnostics} API group (command section 41): replay a past run. */
@RestController
@RequestMapping("/api/v1/calculations")
public class CalculationController {

    private final CalculationQueryService query;
    private final CalculationSaveService save;

    public CalculationController(CalculationQueryService query, CalculationSaveService save) {
        this.query = query;
        this.save = save;
    }

    @GetMapping("/{calculationId}")
    public ResponseEntity<ScenarioRunResponse> find(@PathVariable String calculationId) {
        return query.find(calculationId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Keeps this result indefinitely (CLAUDE.md section 7, retention class
     * {@code USER_SAVED}).
     *
     * <p>{@code POST} rather than {@code PUT} on a sub-resource, and idempotent:
     * saving twice returns the same state rather than an error, because a user
     * pressing the button twice has done nothing wrong. A 404 means the
     * calculation does not exist — possibly because it already expired, which
     * is exactly why the UI shows the expiry date before it arrives.
     */
    @PostMapping("/{calculationId}/save")
    public ResponseEntity<RetentionDto> save(@PathVariable String calculationId) {
        return save.save(calculationId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
