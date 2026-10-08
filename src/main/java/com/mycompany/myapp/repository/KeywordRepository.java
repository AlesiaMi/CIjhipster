package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Keyword;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Keyword entity.
 */
@Repository
public interface KeywordRepository extends JpaRepository<Keyword, Long>, JpaSpecificationExecutor<Keyword> {
    default Optional<Keyword> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Keyword> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Keyword> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select keyword from Keyword keyword left join fetch keyword.competitor",
        countQuery = "select count(keyword) from Keyword keyword"
    )
    Page<Keyword> findAllWithToOneRelationships(Pageable pageable);

    @Query("select keyword from Keyword keyword left join fetch keyword.competitor")
    List<Keyword> findAllWithToOneRelationships();

    @Query("select keyword from Keyword keyword left join fetch keyword.competitor where keyword.id =:id")
    Optional<Keyword> findOneWithToOneRelationships(@Param("id") Long id);
}
