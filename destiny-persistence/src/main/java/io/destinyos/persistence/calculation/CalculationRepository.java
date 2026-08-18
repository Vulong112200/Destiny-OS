package io.destinyos.persistence.calculation;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CalculationRepository extends JpaRepository<CalculationEntity, String> {
}
