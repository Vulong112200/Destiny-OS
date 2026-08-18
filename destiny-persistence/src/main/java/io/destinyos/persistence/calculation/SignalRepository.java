package io.destinyos.persistence.calculation;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SignalRepository extends JpaRepository<SignalEntity, String> {

    List<SignalEntity> findByCalculationId(String calculationId);
}
