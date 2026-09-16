package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.*; // for static metamodels
import com.mycompany.myapp.domain.AnalysisResult;
import com.mycompany.myapp.repository.AnalysisResultRepository;
import com.mycompany.myapp.service.criteria.AnalysisResultCriteria;
import com.mycompany.myapp.service.dto.AnalysisResultDTO;
import com.mycompany.myapp.service.mapper.AnalysisResultMapper;
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
 * Service for executing complex queries for {@link AnalysisResult} entities in the database.
 * The main input is a {@link AnalysisResultCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link AnalysisResultDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class AnalysisResultQueryService extends QueryService<AnalysisResult> {

    private static final Logger LOG = LoggerFactory.getLogger(AnalysisResultQueryService.class);

    private final AnalysisResultRepository analysisResultRepository;

    private final AnalysisResultMapper analysisResultMapper;

    public AnalysisResultQueryService(AnalysisResultRepository analysisResultRepository, AnalysisResultMapper analysisResultMapper) {
        this.analysisResultRepository = analysisResultRepository;
        this.analysisResultMapper = analysisResultMapper;
    }

    /**
     * Return a {@link Page} of {@link AnalysisResultDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<AnalysisResultDTO> findByCriteria(AnalysisResultCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<AnalysisResult> specification = createSpecification(criteria);
        return analysisResultRepository.findAll(specification, page).map(analysisResultMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(AnalysisResultCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<AnalysisResult> specification = createSpecification(criteria);
        return analysisResultRepository.count(specification);
    }

    /**
     * Function to convert {@link AnalysisResultCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<AnalysisResult> createSpecification(AnalysisResultCriteria criteria) {
        Specification<AnalysisResult> specification = Specification.unrestricted();
        specification = specification.and((root, query, builder) -> {
            if (Long.class != query.getResultType()) {
                root.fetch(AnalysisResult_.newsItem, JoinType.LEFT);
            }
            return null;
        });
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildRangeSpecification(criteria.getId(), AnalysisResult_.id),
                    buildStringSpecification(criteria.getSummary(), AnalysisResult_.summary),
                    buildSpecification(criteria.getSentiment(), AnalysisResult_.sentiment),
                    buildStringSpecification(criteria.getTopic(), AnalysisResult_.topic),
                    buildStringSpecification(criteria.getEntities(), AnalysisResult_.entities),
                    buildStringSpecification(criteria.getRiskSource(), AnalysisResult_.riskSource),
                    buildSpecification(criteria.getStatus(), AnalysisResult_.status),
                    buildStringSpecification(criteria.getModelName(), AnalysisResult_.modelName),
                    buildRangeSpecification(criteria.getAnalyzedAt(), AnalysisResult_.analyzedAt),
                    buildStringSpecification(criteria.getErrorMessage(), AnalysisResult_.errorMessage),
                    buildSpecification(criteria.getNewsItemId(), root ->
                        root.join(AnalysisResult_.newsItem, JoinType.LEFT).get(NewsItem_.id)
                    ),
                    buildSpecification(criteria.getOwnerId(), root ->
                        root.join("newsItem", JoinType.LEFT).join("competitor", JoinType.LEFT).join("owner", JoinType.LEFT).<Long>get("id")
                    )
                )
            );
        }
        return specification;
    }
}
