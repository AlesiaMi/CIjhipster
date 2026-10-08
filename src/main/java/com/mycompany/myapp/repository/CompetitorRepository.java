package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Competitor;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Competitor entity.
 */
@Repository
public interface CompetitorRepository extends JpaRepository<Competitor, Long>, JpaSpecificationExecutor<Competitor> {
    @Query("select competitor from Competitor competitor where competitor.owner.login = ?#{authentication.name}")
    List<Competitor> findByOwnerIsCurrentUser();

    default Optional<Competitor> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Competitor> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Competitor> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select competitor from Competitor competitor left join fetch competitor.owner",
        countQuery = "select count(competitor) from Competitor competitor"
    )
    Page<Competitor> findAllWithToOneRelationships(Pageable pageable);

    @Query("select competitor from Competitor competitor left join fetch competitor.owner")
    List<Competitor> findAllWithToOneRelationships();

    @Query("select competitor from Competitor competitor left join fetch competitor.owner where competitor.id =:id")
    Optional<Competitor> findOneWithToOneRelationships(@Param("id") Long id);
}
