package io.destinyos.persistence.identity;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BirthProfileRepository extends JpaRepository<BirthProfileEntity, Long> {

    List<BirthProfileEntity> findByUser(UserEntity user);
}
