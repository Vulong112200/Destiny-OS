package io.destinyos.persistence.calculation;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConflictRepository extends JpaRepository<ConflictEntity, Long> {

    List<ConflictEntity> findByCalculationId(String calculationId);
}
