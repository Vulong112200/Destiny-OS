package io.destinyos.persistence.registry;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MethodologyVersionRepository extends JpaRepository<MethodologyVersionEntity, Long> {

    List<MethodologyVersionEntity> findByMethodology_MethodologyId(String methodologyId);

    Optional<MethodologyVersionEntity> findByMethodology_MethodologyIdAndVersion(
            String methodologyId, String version);
}
