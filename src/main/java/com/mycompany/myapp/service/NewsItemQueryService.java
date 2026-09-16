package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.*; // for static metamodels
import com.mycompany.myapp.repository.NewsItemRepository;
import com.mycompany.myapp.service.criteria.NewsItemCriteria;
import com.mycompany.myapp.service.dto.NewsItemDTO;
import com.mycompany.myapp.service.dto.NewsItemPageCacheDTO;
import com.mycompany.myapp.service.mapper.NewsItemMapper;
import jakarta.persistence.criteria.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
    private final CacheManager cacheManager;
    private final TenantCacheVersionService tenantCacheVersionService;

    public NewsItemQueryService(
        NewsItemRepository newsItemRepository,
        NewsItemMapper newsItemMapper,
        CacheManager cacheManager,
        TenantCacheVersionService tenantCacheVersionService
    ) {
        this.newsItemRepository = newsItemRepository;
        this.newsItemMapper = newsItemMapper;
        this.cacheManager = cacheManager;
        this.tenantCacheVersionService = tenantCacheVersionService;
    }

    @Transactional(readOnly = true)
    public Page<NewsItemDTO> findByCriteria(NewsItemCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);

        String cacheKey = buildCacheKey(criteria, page);

        Cache cache = cacheManager.getCache("newsItemsPages");

        if (cache != null) {
            NewsItemPageCacheDTO cachedPage = cache.get(cacheKey, NewsItemPageCacheDTO.class);

            if (cachedPage != null) {
                LOG.debug("Redis CACHE HIT for NewsItem page: {}", cacheKey);

                return new PageImpl<>(cachedPage.getContent(), page, cachedPage.getTotalElements());
            }
        }

        LOG.debug("Redis CACHE MISS for NewsItem page: {}", cacheKey);

        final Specification<NewsItem> specification = createSpecification(criteria);

        Page<NewsItemDTO> result = newsItemRepository.findAll(specification, page).map(newsItemMapper::toDto);

        if (cache != null) {
            NewsItemPageCacheDTO cacheValue = new NewsItemPageCacheDTO(
                result.getContent(),
                page.getPageNumber(),
                page.getPageSize(),
                result.getTotalElements()
            );

            cache.put(cacheKey, cacheValue);
        }

        return result;
    }

    private String buildCacheKey(NewsItemCriteria criteria, Pageable page) {
        Long ownerId = extractOwnerId(criteria);

        String namespace = tenantCacheVersionService.namespace(ownerId);

        String criteriaKey = criteria != null ? criteria.toString() : "null";

        return namespace + "|" + criteriaKey + "|" + page.getPageNumber() + "|" + page.getPageSize() + "|" + page.getSort();
    }

    private Long extractOwnerId(NewsItemCriteria criteria) {
        if (criteria == null || criteria.getOwnerId() == null) {
            return null;
        }

        return criteria.getOwnerId().getEquals();
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
                    ),
                    buildSpecification(criteria.getOwnerId(), root ->
                        root.join("competitor", JoinType.LEFT).join("owner", JoinType.LEFT).<Long>get("id")
                    )
                )
            );
        }
        return specification;
    }
}
