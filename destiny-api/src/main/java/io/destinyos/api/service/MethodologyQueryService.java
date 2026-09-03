package io.destinyos.api.service;

import io.destinyos.api.dto.LabeledValue;
import io.destinyos.api.dto.MethodologyDto;
import io.destinyos.i18n.VietnameseLabels;
import io.destinyos.persistence.registry.MethodologyEntity;
import io.destinyos.persistence.registry.MethodologyRegistryService;
import io.destinyos.persistence.registry.MethodologyVersionEntity;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * Every registered methodology with its latest version.
     *
     * <p>Reads all methodologies and all version rows in two queries, then
     * pairs them in memory. It used to call {@code registry.latestVersion(id)}
     * once per methodology inside {@link #toDto}: eighteen extra queries, each
     * in its own transaction because none of the registry's read methods were
     * transactional, plus one more per row for the eagerly fetched
     * {@code researchIds}. That is roughly forty round trips to read eighteen
     * rows of configuration that only change when the seeder runs, and it made
     * this endpoint the slowest thing in the system at ~6.7s against a
     * database in another region.
     *
     * <p>Transactional because {@link #latestVersionByMethodologyId} walks
     * {@code MethodologyVersionEntity#methodology}, a lazy association, and
     * {@code open-in-view} is off. One read transaction for the whole mapping
     * is also what lets {@code default_batch_fetch_size} collapse the eager
     * {@code researchIds} collection into a single query instead of one per
     * row.
     */
    @Transactional(readOnly = true)
    public List<MethodologyDto> listAll() {
        Map<String, MethodologyVersionEntity> latest = latestVersionByMethodologyId();
        return registry.allMethodologies().stream()
                .map(m -> toDto(m, Optional.ofNullable(latest.get(m.methodologyId()))))
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<MethodologyDto> find(String methodologyId) {
        return registry.allMethodologies().stream()
                .filter(m -> m.methodologyId().equals(methodologyId))
                .findFirst()
                .map(m -> toDto(m, registry.latestVersion(methodologyId)));
    }

    /**
     * The newest version row per methodology, from one query.
     *
     * <p>"Newest" is {@code max(createdAt)}, matching
     * {@code MethodologyRegistryService#latestVersion} exactly - the two must
     * not disagree about which row is current, or the same methodology would
     * report one status in the list and another when fetched by id.
     */
    private Map<String, MethodologyVersionEntity> latestVersionByMethodologyId() {
        return registry.allVersions().stream()
                .collect(Collectors.toMap(
                        v -> v.methodology().methodologyId(),
                        Function.identity(),
                        (a, b) -> a.createdAt().isAfter(b.createdAt()) ? a : b));
    }

    private MethodologyDto toDto(MethodologyEntity methodology,
                                 Optional<MethodologyVersionEntity> latest) {
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
