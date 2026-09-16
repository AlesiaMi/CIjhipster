package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Competitor;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Competitor entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CompetitorRepository extends JpaRepository<Competitor, Long>, JpaSpecificationExecutor<Competitor> {
    Optional<Competitor> findOneByIdAndOwnerId(Long id, Long ownerId);

    boolean existsByIdAndOwnerId(Long id, Long ownerId);
}
