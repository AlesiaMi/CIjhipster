package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.*; // for static metamodels
import com.mycompany.myapp.domain.DataSource;
import com.mycompany.myapp.repository.DataSourceRepository;
import com.mycompany.myapp.service.criteria.DataSourceCriteria;
import com.mycompany.myapp.service.dto.DataSourceDTO;
import com.mycompany.myapp.service.mapper.DataSourceMapper;
import jakarta.persistence.criteria.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link DataSource} entities in the database.
 * The main input is a {@link DataSourceCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link DataSourceDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class DataSourceQueryService extends QueryService<DataSource> {

    private static final Logger LOG = LoggerFactory.getLogger(DataSourceQueryService.class);

    private final DataSourceRepository dataSourceRepository;

    private final DataSourceMapper dataSourceMapper;

    public DataSourceQueryService(DataSourceRepository dataSourceRepository, DataSourceMapper dataSourceMapper) {
        this.dataSourceRepository = dataSourceRepository;
        this.dataSourceMapper = dataSourceMapper;
    }

    /**
     * Return a {@link Page} of {@link DataSourceDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<DataSourceDTO> findByCriteria(DataSourceCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<DataSource> specification = createSpecification(criteria);
        return dataSourceRepository.findAll(specification, page).map(dataSourceMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(DataSourceCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<DataSource> specification = createSpecification(criteria);
        return dataSourceRepository.count(specification);
    }

    /**
     * Function to convert {@link DataSourceCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<DataSource> createSpecification(DataSourceCriteria criteria) {
        Specification<DataSource> specification = Specification.unrestricted();
        specification = specification.and((root, query, builder) -> {
            if (Long.class != query.getResultType()) {
                root.fetch(DataSource_.competitor, JoinType.LEFT);
            }
            return null;
        });
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
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
                    buildSpecification(criteria.getCompetitorId(), root ->
                        root.join(DataSource_.competitor, JoinType.LEFT).get(Competitor_.id)
                    )
                )
            );
        }
        return specification;
    }
}
