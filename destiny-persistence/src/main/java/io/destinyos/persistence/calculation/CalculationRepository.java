package io.destinyos.persistence.calculation;

import io.destinyos.core.retention.RetentionClass;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CalculationRepository extends JpaRepository<CalculationEntity, String> {

    /**
     * Cleanup candidates, oldest expiry first, bounded by {@code pageable}
     * (DATA_MODEL_AND_RETENTION.md section 11 batch delete).
     *
     * <p>The retention class is a parameter rather than hardcoded to EPHEMERAL
     * so the query cannot quietly widen: a caller has to name the class it
     * means, and {@code RetentionClass.isAutoDeletable()} is what decides
     * whether naming it is legitimate.
     */
    List<CalculationEntity> findByRetentionClassAndExpiresAtLessThanEqualOrderByExpiresAtAsc(
            RetentionClass retentionClass, Instant cutoff, Pageable pageable);

    /** Total candidates, so an audit row can distinguish "found" from "deleted this batch". */
    long countByRetentionClassAndExpiresAtLessThanEqual(RetentionClass retentionClass, Instant cutoff);
}
