package io.destinyos.api.service;

import io.destinyos.api.dto.RetentionDto;
import io.destinyos.persistence.retention.CalculationRetentionService;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * The write half of retention that a user can trigger: "keep this result".
 *
 * <p>A thin seam rather than letting the controller call
 * {@link CalculationRetentionService} directly — CLAUDE.md section 3 keeps
 * controllers free of domain work, and this is also the boundary where a
 * persistence entity stops and a DTO starts.
 */
@Service
public class CalculationSaveService {

    private final CalculationRetentionService retention;

    public CalculationSaveService(CalculationRetentionService retention) {
        this.retention = retention;
    }

    /** @return the resulting retention state, or empty if no such calculation */
    public Optional<RetentionDto> save(String calculationId) {
        return retention.markUserSaved(calculationId).map(RetentionDtoMapper::toDto);
    }
}
