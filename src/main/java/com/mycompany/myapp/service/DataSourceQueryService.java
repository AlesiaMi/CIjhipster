package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.*; // for static metamodels
import com.mycompany.myapp.repository.DataSourceRepository;
import com.mycompany.myapp.service.criteria.DataSourceCriteria;
import com.mycompany.myapp.service.dto.DataSourceDTO;
import com.mycompany.myapp.service.dto.DataSourcePageCacheDTO;
import com.mycompany.myapp.service.mapper.DataSourceMapper;
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
public class DataSourceQueryService extends QueryService<DataSource> {

    private static final Logger LOG = LoggerFactory.getLogger(DataSourceQueryService.class);

    private final DataSourceRepository dataSourceRepository;

    private final DataSourceMapper dataSourceMapper;

    private final CacheManager cacheManager;

    private final TenantCacheVersionService tenantCacheVersionService;

    public DataSourceQueryService(
        DataSourceRepository dataSourceRepository,
        DataSourceMapper dataSourceMapper,
        CacheManager cacheManager,
        TenantCacheVersionService tenantCacheVersionService
    ) {
        this.dataSourceRepository = dataSourceRepository;
        this.dataSourceMapper = dataSourceMapper;
        this.cacheManager = cacheManager;
        this.tenantCacheVersionService = tenantCacheVersionService;
    }

    @Transactional(readOnly = true)
    public Page<DataSourceDTO> findByCriteria(DataSourceCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);

        String cacheKey = buildCacheKey(criteria, page);

        Cache cache = cacheManager.getCache("dataSourcesPages");

        if (cache != null) {
            DataSourcePageCacheDTO cachedPage = cache.get(cacheKey, DataSourcePageCacheDTO.class);

            if (cachedPage != null) {
                LOG.debug("Redis CACHE HIT for DataSource page: {}", cacheKey);

                return new PageImpl<>(cachedPage.getContent(), page, cachedPage.getTotalElements());
            }
        }

        LOG.debug("Redis CACHE MISS for DataSource page: {}", cacheKey);

        final Specification<DataSource> specification = createSpecification(criteria);

        Page<DataSourceDTO> result = dataSourceRepository.findAll(specification, page).map(dataSourceMapper::toDto);

        if (cache != null) {
            DataSourcePageCacheDTO cacheValue = new DataSourcePageCacheDTO(
                result.getContent(),
                page.getPageNumber(),
                page.getPageSize(),
                result.getTotalElements()
            );

            cache.put(cacheKey, cacheValue);
        }

        return result;
    }

    private String buildCacheKey(DataSourceCriteria criteria, Pageable page) {
        Long ownerId = extractOwnerId(criteria);

        String namespace = tenantCacheVersionService.namespace(ownerId);

        String criteriaKey = criteria != null ? criteria.toString() : "null";

        return namespace + "|" + criteriaKey + "|" + page.getPageNumber() + "|" + page.getPageSize() + "|" + page.getSort();
    }

    private Long extractOwnerId(DataSourceCriteria criteria) {
        if (criteria == null || criteria.getOwnerId() == null) {
            return null;
        }

        return criteria.getOwnerId().getEquals();
    }

    @Transactional(readOnly = true)
    public long countByCriteria(DataSourceCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<DataSource> specification = createSpecification(criteria);
        return dataSourceRepository.count(specification);
    }

    protected Specification<DataSource> createSpecification(DataSourceCriteria criteria) {
        Specification<DataSource> specification = Specification.unrestricted();
        specification = specification.and((root, query, builder) -> {
            if (Long.class != query.getResultType()) {
                root.fetch(DataSource_.competitor, JoinType.LEFT);
            }
            return null;
        });
        if (criteria != null) {
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildRangeSpecification(criteria.getId(), DataSource_.id),
                    buildStringSpecification(criteria.getSourceName(), DataSource_.sourceName),
                    buildStringSpecification(criteria.getUrl(), DataSource_.url),
                    buildSpecification(criteria.getSourceType(), DataSource_.sourceType),
                    buildSpecification(criteria.getIsActive(), DataSource_.isActive),
                    buildRangeSpecification(criteria.getLastCheckedAt(), DataSource_.lastCheckedAt),
                    buildRangeSpecification(criteria.getCreatedAt(), DataSource_.createdAt),
                    buildSpecification(criteria.getCompetitorId(), root -> root.join("competitor", JoinType.LEFT).<Long>get("id")),
                    buildSpecification(criteria.getOwnerId(), root ->
                        root.join("competitor", JoinType.LEFT).join("owner", JoinType.LEFT).<Long>get("id")
                    )
                )
            );
        }
        return specification;
    }
}
