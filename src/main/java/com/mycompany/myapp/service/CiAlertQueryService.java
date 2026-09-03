package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.*; // for static metamodels
import com.mycompany.myapp.domain.CiAlert;
import com.mycompany.myapp.repository.CiAlertRepository;
import com.mycompany.myapp.service.criteria.CiAlertCriteria;
import com.mycompany.myapp.service.dto.CiAlertDTO;
import com.mycompany.myapp.service.mapper.CiAlertMapper;
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
public class CiAlertQueryService extends QueryService<CiAlert> {

    private static final Logger LOG = LoggerFactory.getLogger(CiAlertQueryService.class);

    private final CiAlertRepository ciAlertRepository;

    private final CiAlertMapper ciAlertMapper;

    public CiAlertQueryService(CiAlertRepository ciAlertRepository, CiAlertMapper ciAlertMapper) {
        this.ciAlertRepository = ciAlertRepository;
        this.ciAlertMapper = ciAlertMapper;
    }

    @Transactional(readOnly = true)
    public Page<CiAlertDTO> findByCriteria(CiAlertCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<CiAlert> specification = createSpecification(criteria);
        return ciAlertRepository.findAll(specification, page).map(ciAlertMapper::toDto);
    }

    @Transactional(readOnly = true)
    public long countByCriteria(CiAlertCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<CiAlert> specification = createSpecification(criteria);
        return ciAlertRepository.count(specification);
    }

    protected Specification<CiAlert> createSpecification(CiAlertCriteria criteria) {
        Specification<CiAlert> specification = Specification.unrestricted();
        specification = specification.and((root, query, builder) -> {
            if (Long.class != query.getResultType()) {
                root.fetch(CiAlert_.analysisResult, JoinType.LEFT);
                root.fetch(CiAlert_.analystProfile, JoinType.LEFT);
            }
            return null;
        });
        if (criteria != null) {
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildRangeSpecification(criteria.getId(), CiAlert_.id),
                    buildStringSpecification(criteria.getTitle(), CiAlert_.title),
                    buildStringSpecification(criteria.getMessage(), CiAlert_.message),
                    buildSpecification(criteria.getSeverity(), CiAlert_.severity),
                    buildSpecification(criteria.getStatus(), CiAlert_.status),
                    buildRangeSpecification(criteria.getCreatedAt(), CiAlert_.createdAt),
                    buildRangeSpecification(criteria.getReadAt(), CiAlert_.readAt),
                    buildSpecification(criteria.getAnalysisResultId(), root ->
                        root.join(CiAlert_.analysisResult, JoinType.LEFT).get(AnalysisResult_.id)
                    ),
                    buildSpecification(criteria.getAnalystProfileId(), root ->
                        root.join(CiAlert_.analystProfile, JoinType.LEFT).get(AnalystProfile_.id)
                    )
                )
            );
        }
        return specification;
    }
}
