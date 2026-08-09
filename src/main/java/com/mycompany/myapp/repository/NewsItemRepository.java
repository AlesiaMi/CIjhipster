package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.NewsItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the NewsItem entity.
 */
@Repository
public interface NewsItemRepository extends JpaRepository<NewsItem, Long>, JpaSpecificationExecutor<NewsItem> {
    boolean existsByUrl(String url);
    boolean existsByExternalId(String externalId);

    default Optional<NewsItem> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<NewsItem> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<NewsItem> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select newsItem from NewsItem newsItem left join fetch newsItem.dataSource left join fetch newsItem.competitor",
        countQuery = "select count(newsItem) from NewsItem newsItem"
    )
    Page<NewsItem> findAllWithToOneRelationships(Pageable pageable);

    @Query("select newsItem from NewsItem newsItem left join fetch newsItem.dataSource left join fetch newsItem.competitor")
    List<NewsItem> findAllWithToOneRelationships();

    @Query(
        "select newsItem from NewsItem newsItem left join fetch newsItem.dataSource left join fetch newsItem.competitor where newsItem.id =:id"
    )
    Optional<NewsItem> findOneWithToOneRelationships(@Param("id") Long id);
}
