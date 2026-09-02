package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.*; // for static metamodels
import com.mycompany.myapp.repository.NewsItemRepository;
import com.mycompany.myapp.service.criteria.NewsItemCriteria;
import com.mycompany.myapp.service.dto.NewsItemDTO;
import com.mycompany.myapp.service.mapper.NewsItemMapper;
import jakarta.persistence.criteria.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

@Service
@Transactional(readOnly = true)
public class NewsItemQueryService extends QueryService<NewsItem> {

    private static final Logger LOG = LoggerFactory.getLogger(NewsItemQueryService.class);

    private final NewsItemRepository newsItemRepository;

    private final NewsItemMapper newsItemMapper;

    public NewsItemQueryService(NewsItemRepository newsItemRepository, NewsItemMapper newsItemMapper) {
        this.newsItemRepository = newsItemRepository;
        this.newsItemMapper = newsItemMapper;
    }

    @Cacheable(
        cacheNames = "newsItemsPages",
        key = "#criteria.toString() + '|' + " + "#page.pageNumber + '|' + " + "#page.pageSize + '|' + " + "#page.sort.toString()"
    )
    @Transactional(readOnly = true)
    public Page<NewsItemDTO> findByCriteria(NewsItemCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<NewsItem> specification = createSpecification(criteria);
        return newsItemRepository.findAll(specification, page).map(newsItemMapper::toDto);
    }

    @Transactional(readOnly = true)
    public long countByCriteria(NewsItemCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<NewsItem> specification = createSpecification(criteria);
        return newsItemRepository.count(specification);
    }

    protected Specification<NewsItem> createSpecification(NewsItemCriteria criteria) {
        Specification<NewsItem> specification = Specification.unrestricted();
        specification = specification.and((root, query, builder) -> {
            if (Long.class != query.getResultType()) {
                root.fetch(NewsItem_.dataSource, JoinType.LEFT);
                root.fetch(NewsItem_.competitor, JoinType.LEFT);
                root.fetch(NewsItem_.collectionRun, JoinType.LEFT);
            }
            return null;
        });
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildRangeSpecification(criteria.getId(), NewsItem_.id),
                    buildStringSpecification(criteria.getExternalId(), NewsItem_.externalId),
                    buildStringSpecification(criteria.getTitle(), NewsItem_.title),
                    buildStringSpecification(criteria.getUrl(), NewsItem_.url),
                    buildStringSpecification(criteria.getOriginalText(), NewsItem_.originalText),
                    buildRangeSpecification(criteria.getPublishedAt(), NewsItem_.publishedAt),
                    buildRangeSpecification(criteria.getCollectedAt(), NewsItem_.collectedAt),
                    buildSpecification(criteria.getIsDuplicate(), NewsItem_.isDuplicate),
                    buildSpecification(criteria.getDataSourceId(), root ->
                        root.join(NewsItem_.dataSource, JoinType.LEFT).get(DataSource_.id)
                    ),
                    buildSpecification(criteria.getCompetitorId(), root ->
                        root.join(NewsItem_.competitor, JoinType.LEFT).get(Competitor_.id)
                    ),
                    buildSpecification(criteria.getCollectionRunId(), root ->
                        root.join(NewsItem_.collectionRun, JoinType.LEFT).get(CollectionRun_.id)
                    ),
                    buildSpecification(criteria.getAnalysisResultId(), root ->
                        root.join(NewsItem_.analysisResult, JoinType.LEFT).get(AnalysisResult_.id)
                    )
                )
            );
        }
        return specification;
    }
}
