package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.CollectionRun;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the CollectionRun entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CollectionRunRepository extends JpaRepository<CollectionRun, Long>, JpaSpecificationExecutor<CollectionRun> {}
