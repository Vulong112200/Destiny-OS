package io.destinyos.persistence.registry;

import io.destinyos.engine.MethodologyStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The queryable methodology registry (ADR D7).
 *
 * <p>D7 exists to make one guarantee visible in the product rather than
 * buried in a document: every methodology named in the specification -
 * including the eleven that are currently research-blocked - is registered
 * here with its true status. A blocked methodology is a row a caller can
 * query, not a silent absence that looks like an oversight.
 *
 * <p>This service does not decide statuses. It records the status the
 * research register (RESEARCH_BLOCKERS.md) already assigned - see
 * {@link MethodologyRegistrySeeder} for where those values come from and
 * why each one is what it is.
 */
@Service
public class MethodologyRegistryService {

    private final MethodologyRepository methodologies;
    private final MethodologyVersionRepository versions;

    public MethodologyRegistryService(MethodologyRepository methodologies,
                                      MethodologyVersionRepository versions) {
        this.methodologies = methodologies;
        this.versions = versions;
    }

    /**
     * Registers a methodology (idempotent on {@code methodologyId}) and adds
     * one version row to it.
     *
     * <p>Fails if {@code status.mayCalculate()} is true but {@code school} or
     * {@code source} is blank - the same guard
     * {@link MethodologyVersionEntity}'s constructor enforces, restated here
     * so the failure is diagnosed at the call site with full context.
     */
    @Transactional
    public MethodologyVersionEntity register(String methodologyId, String displayNameVi,
                                             String domain, String version,
                                             MethodologyStatus status, String school,
                                             String source, Set<String> researchIds,
                                             String notes) {
        MethodologyEntity methodology = methodologies.findById(methodologyId)
                .orElseGet(() -> methodologies.save(
                        new MethodologyEntity(methodologyId, displayNameVi, domain)));

        var entity = new MethodologyVersionEntity(methodology, version, status, school,
                source, researchIds, notes);
        return versions.save(entity);
    }

    /** The most recently created version row for a methodology, if any is registered. */
    public Optional<MethodologyVersionEntity> latestVersion(String methodologyId) {
        return versions.findByMethodology_MethodologyId(methodologyId).stream()
                .max(java.util.Comparator.comparing(MethodologyVersionEntity::createdAt));
    }

    public List<MethodologyVersionEntity> allVersions(String methodologyId) {
        return versions.findByMethodology_MethodologyId(methodologyId);
    }

    public List<MethodologyEntity> allMethodologies() {
        return methodologies.findAll();
    }

    /**
     * Whether a methodology's latest registered version may currently
     * produce a real result.
     *
     * <p>Returns false for an unregistered methodology - an engine that has
     * never been registered is exactly as unusable as one registered
     * {@code RESEARCH_REQUIRED} (ADR D7: absence and an honest block must
     * both read as "cannot calculate", never as "assume it is fine").
     */
    public boolean isCalculable(String methodologyId) {
        return latestVersion(methodologyId)
                .map(v -> v.status().mayCalculate())
                .orElse(false);
    }

    public List<String> researchIdsBlocking(String methodologyId) {
        return latestVersion(methodologyId)
                .filter(v -> !v.status().mayCalculate())
                .map(v -> List.copyOf(v.researchIds()))
                .orElse(List.of());
    }
}
