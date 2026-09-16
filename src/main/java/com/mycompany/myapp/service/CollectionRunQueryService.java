package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.*; // for static metamodels
import com.mycompany.myapp.repository.CollectionRunRepository;
import com.mycompany.myapp.service.criteria.CollectionRunCriteria;
import com.mycompany.myapp.service.dto.CollectionRunDTO;
import com.mycompany.myapp.service.mapper.CollectionRunMapper;
import jakarta.persistence.criteria.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

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

    @Transactional(readOnly = true)
    public Page<CollectionRunDTO> findByCriteria(CollectionRunCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<CollectionRun> specification = createSpecification(criteria);
        return collectionRunRepository.findAll(specification, page).map(collectionRunMapper::toDto);
    }

    @Transactional(readOnly = true)
    public long countByCriteria(CollectionRunCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<CollectionRun> specification = createSpecification(criteria);
        return collectionRunRepository.count(specification);
    }

    protected Specification<CollectionRun> createSpecification(CollectionRunCriteria criteria) {
        Specification<CollectionRun> specification = Specification.unrestricted();
        if (criteria != null) {
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildRangeSpecification(criteria.getId(), CollectionRun_.id),
                    buildRangeSpecification(criteria.getStartedAt(), CollectionRun_.startedAt),
                    buildRangeSpecification(criteria.getFinishedAt(), CollectionRun_.finishedAt),
                    buildSpecification(criteria.getStatus(), CollectionRun_.status),
                    buildRangeSpecification(criteria.getFoundCount(), CollectionRun_.foundCount),
                    buildRangeSpecification(criteria.getProcessedCount(), CollectionRun_.processedCount),
                    buildStringSpecification(criteria.getErrorMessage(), CollectionRun_.errorMessage),
                    buildSpecification(criteria.getOwnerId(), root -> root.join("owner", JoinType.LEFT).<Long>get("id"))
                )
            );
        }
        return specification;
    }
}
