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

    /**
     * The most recently created version row for a methodology, if any is
     * registered.
     *
     * <p>{@code @Transactional(readOnly = true)} on every read here is not
     * decoration. With {@code open-in-view: false} an untransacted repository
     * call opens its own connection, begins, selects and commits - so a caller
     * that asked about eighteen methodologies paid eighteen begin/commit round
     * trips on top of the eighteen selects. Against a database in another
     * region that was most of the six seconds {@code GET /api/v1/methodologies}
     * took to answer.
     */
    @Transactional(readOnly = true)
    public Optional<MethodologyVersionEntity> latestVersion(String methodologyId) {
        return versions.findByMethodology_MethodologyId(methodologyId).stream()
                .max(java.util.Comparator.comparing(MethodologyVersionEntity::createdAt));
    }

    @Transactional(readOnly = true)
    public List<MethodologyVersionEntity> allVersions(String methodologyId) {
        return versions.findByMethodology_MethodologyId(methodologyId);
    }

    @Transactional(readOnly = true)
    public List<MethodologyEntity> allMethodologies() {
        return methodologies.findAll();
    }

    /**
     * Every registered version row, in one query.
     *
     * <p>Exists so callers that need the latest version of <em>every</em>
     * methodology stop asking one methodology at a time. {@link #latestVersion}
     * in a loop is the N+1 that made the registry endpoint the slowest thing
     * in the system despite reading eighteen rows of static configuration.
     */
    @Transactional(readOnly = true)
    public List<MethodologyVersionEntity> allVersions() {
        return versions.findAll();
    }

    /**
     * Every registered {@code methodologyId@version} pair, in one query.
     *
     * <p>Returns plain strings rather than entities on purpose. The caller is
     * {@code MethodologyRegistrySeeder}, whose {@code seed()} is reached by
     * self-invocation from its {@code ApplicationReadyEvent} listener - so
     * Spring's transactional proxy does not apply and there is no session open
     * on the way back. Handing it entities would mean it had to touch
     * {@code MethodologyVersionEntity#methodology}, a lazy association, outside
     * any transaction. Doing the association walk here, where the transaction
     * is real, is what keeps that from being a
     * {@code LazyInitializationException} at startup.
     */
    @Transactional(readOnly = true)
    public Set<String> seededVersionKeys() {
        return versions.findAll().stream()
                .map(v -> v.methodology().methodologyId() + "@" + v.version())
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
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
    @Transactional(readOnly = true)
    public boolean isCalculable(String methodologyId) {
        return latestVersion(methodologyId)
                .map(v -> v.status().mayCalculate())
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public List<String> researchIdsBlocking(String methodologyId) {
        return latestVersion(methodologyId)
                .filter(v -> !v.status().mayCalculate())
                .map(v -> List.copyOf(v.researchIds()))
                .orElse(List.of());
    }
}
