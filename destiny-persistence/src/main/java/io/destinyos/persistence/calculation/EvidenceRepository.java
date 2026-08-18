package io.destinyos.persistence.calculation;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvidenceRepository extends JpaRepository<EvidenceEntity, String> {

    List<EvidenceEntity> findByCalculationId(String calculationId);
}
