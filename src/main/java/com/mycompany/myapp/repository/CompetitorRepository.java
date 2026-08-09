package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Competitor;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Competitor entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CompetitorRepository extends JpaRepository<Competitor, Long>, JpaSpecificationExecutor<Competitor> {}
