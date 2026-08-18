package io.destinyos.persistence.calculation;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FusionResultRepository extends JpaRepository<FusionResultEntity, Long> {

    Optional<FusionResultEntity> findByCalculationId(String calculationId);
}
