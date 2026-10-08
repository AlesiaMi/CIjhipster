package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.CollectionRun;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the CollectionRun entity.
 */
@Repository
public interface CollectionRunRepository extends JpaRepository<CollectionRun, Long>, JpaSpecificationExecutor<CollectionRun> {
    @Query("select collectionRun from CollectionRun collectionRun where collectionRun.owner.login = ?#{authentication.name}")
    List<CollectionRun> findByOwnerIsCurrentUser();

    default Optional<CollectionRun> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<CollectionRun> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<CollectionRun> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select collectionRun from CollectionRun collectionRun left join fetch collectionRun.owner",
        countQuery = "select count(collectionRun) from CollectionRun collectionRun"
    )
    Page<CollectionRun> findAllWithToOneRelationships(Pageable pageable);

    @Query("select collectionRun from CollectionRun collectionRun left join fetch collectionRun.owner")
    List<CollectionRun> findAllWithToOneRelationships();

    @Query("select collectionRun from CollectionRun collectionRun left join fetch collectionRun.owner where collectionRun.id =:id")
    Optional<CollectionRun> findOneWithToOneRelationships(@Param("id") Long id);
}
