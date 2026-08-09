package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.CiAlert;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the CiAlert entity.
 */
@Repository
public interface CiAlertRepository extends JpaRepository<CiAlert, Long>, JpaSpecificationExecutor<CiAlert> {
    default Optional<CiAlert> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<CiAlert> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<CiAlert> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select ciAlert from CiAlert ciAlert left join fetch ciAlert.analystProfile",
        countQuery = "select count(ciAlert) from CiAlert ciAlert"
    )
    Page<CiAlert> findAllWithToOneRelationships(Pageable pageable);

    @Query("select ciAlert from CiAlert ciAlert left join fetch ciAlert.analystProfile")
    List<CiAlert> findAllWithToOneRelationships();

    @Query("select ciAlert from CiAlert ciAlert left join fetch ciAlert.analystProfile where ciAlert.id =:id")
    Optional<CiAlert> findOneWithToOneRelationships(@Param("id") Long id);
}
