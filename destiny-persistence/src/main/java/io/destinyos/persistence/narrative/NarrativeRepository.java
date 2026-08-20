package io.destinyos.persistence.narrative;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NarrativeRepository extends JpaRepository<NarrativeEntity, String> {
}
