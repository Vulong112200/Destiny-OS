package io.destinyos.api.service;

import io.destinyos.api.dto.LabeledValue;
import io.destinyos.api.dto.MethodologyDto;
import io.destinyos.i18n.VietnameseLabels;
import io.destinyos.persistence.registry.MethodologyEntity;
import io.destinyos.persistence.registry.MethodologyRegistryService;
import io.destinyos.persistence.registry.MethodologyVersionEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Read side of the methodology registry (ADR D7) for
 * {@code GET /api/v1/methodologies}. A blocked methodology is a normal row
 * in this list with an honest status, never a 404 or an omission.
 */
@Service
public class MethodologyQueryService {

    private final MethodologyRegistryService registry;

    public MethodologyQueryService(MethodologyRegistryService registry) {
        this.registry = registry;
    }

    public List<MethodologyDto> listAll() {
        return registry.allMethodologies().stream()
                .map(this::toDto)
                .toList();
    }

    public Optional<MethodologyDto> find(String methodologyId) {
        return registry.allMethodologies().stream()
                .filter(m -> m.methodologyId().equals(methodologyId))
                .findFirst()
                .map(this::toDto);
    }

    private MethodologyDto toDto(MethodologyEntity methodology) {
        Optional<MethodologyVersionEntity> latest = registry.latestVersion(methodology.methodologyId());

        return latest.map(v -> new MethodologyDto(
                        methodology.methodologyId(),
                        methodology.displayNameVi(),
                        methodology.domain(),
                        v.version(),
                        LabeledValue.of(v.status(), VietnameseLabels.of(v.status())),
                        v.status().mayCalculate(),
                        v.school(),
                        v.source(),
                        List.copyOf(v.researchIds()),
                        v.notes()))
                .orElseGet(() -> new MethodologyDto(
                        methodology.methodologyId(), methodology.displayNameVi(), methodology.domain(),
                        null, null, false, null, null, List.of(), null));
    }
}
