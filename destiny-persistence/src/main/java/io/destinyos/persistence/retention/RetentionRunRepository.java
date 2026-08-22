package io.destinyos.persistence.retention;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RetentionRunRepository extends JpaRepository<RetentionRunEntity, Long> {

    /** Most recent runs first — what an operator actually wants to see. */
    List<RetentionRunEntity> findAllByOrderByStartedAtDesc(Pageable pageable);
}
