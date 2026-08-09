package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.*; // for static metamodels
import com.mycompany.myapp.domain.CollectionRun;
import com.mycompany.myapp.repository.CollectionRunRepository;
import com.mycompany.myapp.service.criteria.CollectionRunCriteria;
import com.mycompany.myapp.service.dto.CollectionRunDTO;
import com.mycompany.myapp.service.mapper.CollectionRunMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link CollectionRun} entities in the database.
 * The main input is a {@link CollectionRunCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link CollectionRunDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class CollectionRunQueryService extends QueryService<CollectionRun> {

    private static final Logger LOG = LoggerFactory.getLogger(CollectionRunQueryService.class);

    private final CollectionRunRepository collectionRunRepository;

    private final CollectionRunMapper collectionRunMapper;

    public CollectionRunQueryService(CollectionRunRepository collectionRunRepository, CollectionRunMapper collectionRunMapper) {
        this.collectionRunRepository = collectionRunRepository;
        this.collectionRunMapper = collectionRunMapper;
    }

    /**
     * Return a {@link Page} of {@link CollectionRunDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<CollectionRunDTO> findByCriteria(CollectionRunCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<CollectionRun> specification = createSpecification(criteria);
        return collectionRunRepository.findAll(specification, page).map(collectionRunMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(CollectionRunCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<CollectionRun> specification = createSpecification(criteria);
        return collectionRunRepository.count(specification);
    }

    /**
     * Function to convert {@link CollectionRunCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<CollectionRun> createSpecification(CollectionRunCriteria criteria) {
        Specification<CollectionRun> specification = Specification.unrestricted();
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildRangeSpecification(criteria.getId(), CollectionRun_.id),
                    buildRangeSpecification(criteria.getStartedAt(), CollectionRun_.startedAt),
                    buildRangeSpecification(criteria.getFinishedAt(), CollectionRun_.finishedAt),
                    buildSpecification(criteria.getStatus(), CollectionRun_.status),
                    buildRangeSpecification(criteria.getFoundCount(), CollectionRun_.foundCount),
                    buildRangeSpecification(criteria.getProcessedCount(), CollectionRun_.processedCount),
                    buildStringSpecification(criteria.getErrorMessage(), CollectionRun_.errorMessage)
                )
            );
        }
        return specification;
    }
}
