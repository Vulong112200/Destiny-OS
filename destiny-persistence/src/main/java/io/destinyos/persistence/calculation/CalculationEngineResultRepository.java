package io.destinyos.persistence.calculation;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CalculationEngineResultRepository extends JpaRepository<CalculationEngineResultEntity, Long> {

    List<CalculationEngineResultEntity> findByCalculationId(String calculationId);
}
