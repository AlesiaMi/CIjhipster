package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.DataSource;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the DataSource entity.
 */
@Repository
public interface DataSourceRepository extends JpaRepository<DataSource, Long>, JpaSpecificationExecutor<DataSource> {
    default Optional<DataSource> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<DataSource> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<DataSource> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select dataSource from DataSource dataSource left join fetch dataSource.competitor",
        countQuery = "select count(dataSource) from DataSource dataSource"
    )
    Page<DataSource> findAllWithToOneRelationships(Pageable pageable);

    @Query("select dataSource from DataSource dataSource left join fetch dataSource.competitor")
    List<DataSource> findAllWithToOneRelationships();

    @Query("select dataSource from DataSource dataSource left join fetch dataSource.competitor where dataSource.id =:id")
    Optional<DataSource> findOneWithToOneRelationships(@Param("id") Long id);
}
