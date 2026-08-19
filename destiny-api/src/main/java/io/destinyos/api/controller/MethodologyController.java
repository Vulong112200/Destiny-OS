package io.destinyos.api.controller;

import io.destinyos.api.dto.MethodologyDto;
import io.destinyos.api.service.MethodologyQueryService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code Methodology} API group (command section 41). Lists every
 * registered methodology, including research-blocked ones, with its honest
 * status (ADR D7) — never a 404 for a methodology that exists but cannot
 * calculate yet.
 */
@RestController
@RequestMapping("/api/v1/methodologies")
public class MethodologyController {

    private final MethodologyQueryService query;

    public MethodologyController(MethodologyQueryService query) {
        this.query = query;
    }

    @GetMapping
    public List<MethodologyDto> listAll() {
        return query.listAll();
    }

    @GetMapping("/{methodologyId}")
    public ResponseEntity<MethodologyDto> find(@PathVariable String methodologyId) {
        return query.find(methodologyId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
