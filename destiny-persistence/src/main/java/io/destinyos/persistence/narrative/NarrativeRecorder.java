package io.destinyos.persistence.narrative;

import io.destinyos.ai.NarrativeResult;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Upserts one {@link NarrativeEntity} per calculation (V7 migration's own
 * comment explains why this is an upsert table, not an append-only history
 * like {@code fusion_results}).
 */
@Service
public class NarrativeRecorder {

    private final NarrativeRepository repository;

    public NarrativeRecorder(NarrativeRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public NarrativeEntity record(String calculationId, NarrativeResult result) {
        Objects.requireNonNull(calculationId, "calculationId");
        Objects.requireNonNull(result, "result");

        return repository.findById(calculationId)
                .map(existing -> {
                    existing.update(result);
                    return repository.save(existing);
                })
                .orElseGet(() -> repository.save(new NarrativeEntity(calculationId, result)));
    }

    @Transactional(readOnly = true)
    public Optional<NarrativeEntity> find(String calculationId) {
        return repository.findById(calculationId);
    }
}
