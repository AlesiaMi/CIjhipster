package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.AnalystProfile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the AnalystProfile entity.
 *
 * When extending this class, extend AnalystProfileRepositoryWithBagRelationships too.
 * For more information refer to https://github.com/jhipster/generator-jhipster/issues/17990.
 */
@Repository
public interface AnalystProfileRepository
    extends AnalystProfileRepositoryWithBagRelationships, JpaRepository<AnalystProfile, Long>, JpaSpecificationExecutor<AnalystProfile>
{
    default Optional<AnalystProfile> findOneWithEagerRelationships(Long id) {
        return this.fetchBagRelationships(this.findOneWithToOneRelationships(id));
    }

    default List<AnalystProfile> findAllWithEagerRelationships() {
        return this.fetchBagRelationships(this.findAllWithToOneRelationships());
    }

    default Page<AnalystProfile> findAllWithEagerRelationships(Pageable pageable) {
        return this.fetchBagRelationships(this.findAllWithToOneRelationships(pageable));
    }

    @Query(
        value = "select analystProfile from AnalystProfile analystProfile left join fetch analystProfile.user",
        countQuery = "select count(analystProfile) from AnalystProfile analystProfile"
    )
    Page<AnalystProfile> findAllWithToOneRelationships(Pageable pageable);

    @Query("select analystProfile from AnalystProfile analystProfile left join fetch analystProfile.user")
    List<AnalystProfile> findAllWithToOneRelationships();

    @Query("select analystProfile from AnalystProfile analystProfile left join fetch analystProfile.user where analystProfile.id =:id")
    Optional<AnalystProfile> findOneWithToOneRelationships(@Param("id") Long id);
}
