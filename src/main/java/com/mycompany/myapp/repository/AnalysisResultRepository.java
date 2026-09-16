package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.AnalysisResult;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the AnalysisResult entity.
 */
@Repository
public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, Long>, JpaSpecificationExecutor<AnalysisResult> {
    default Optional<AnalysisResult> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<AnalysisResult> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<AnalysisResult> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select analysisResult from AnalysisResult analysisResult left join fetch analysisResult.newsItem",
        countQuery = "select count(analysisResult) from AnalysisResult analysisResult"
    )
    Page<AnalysisResult> findAllWithToOneRelationships(Pageable pageable);

    @Query("select analysisResult from AnalysisResult analysisResult left join fetch analysisResult.newsItem")
    List<AnalysisResult> findAllWithToOneRelationships();

    @Query("select analysisResult from AnalysisResult analysisResult left join fetch analysisResult.newsItem where analysisResult.id =:id")
    Optional<AnalysisResult> findOneWithToOneRelationships(@Param("id") Long id);

    @Query(
        """
        select analysisResult
        from AnalysisResult analysisResult
        left join fetch analysisResult.newsItem newsItem
        join newsItem.competitor competitor
        join competitor.owner owner
        where analysisResult.id = :id
        and owner.id = :ownerId
        """
    )
    Optional<AnalysisResult> findOneByIdAndNewsItemCompetitorOwnerId(@Param("id") Long id, @Param("ownerId") Long ownerId);

    @Query(
        """
        select count(analysisResult) > 0
        from AnalysisResult analysisResult
        join analysisResult.newsItem newsItem
        join newsItem.competitor competitor
        join competitor.owner owner
        where analysisResult.id = :id
        and owner.id = :ownerId
        """
    )
    boolean existsByIdAndNewsItemCompetitorOwnerId(@Param("id") Long id, @Param("ownerId") Long ownerId);

    @Query(
        """
        select owner.id
        from AnalysisResult analysisResult
        join analysisResult.newsItem newsItem
        join newsItem.competitor competitor
        join competitor.owner owner
        where analysisResult.id = :id
        """
    )
    Optional<Long> findOwnerIdById(@Param("id") Long id);
}
