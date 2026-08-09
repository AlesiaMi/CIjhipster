package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.*; // for static metamodels
import com.mycompany.myapp.domain.Keyword;
import com.mycompany.myapp.repository.KeywordRepository;
import com.mycompany.myapp.service.criteria.KeywordCriteria;
import com.mycompany.myapp.service.dto.KeywordDTO;
import com.mycompany.myapp.service.mapper.KeywordMapper;
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
 * Service for executing complex queries for {@link Keyword} entities in the database.
 * The main input is a {@link KeywordCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link KeywordDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class KeywordQueryService extends QueryService<Keyword> {

    private static final Logger LOG = LoggerFactory.getLogger(KeywordQueryService.class);

    private final KeywordRepository keywordRepository;

    private final KeywordMapper keywordMapper;

    public KeywordQueryService(KeywordRepository keywordRepository, KeywordMapper keywordMapper) {
        this.keywordRepository = keywordRepository;
        this.keywordMapper = keywordMapper;
    }

    /**
     * Return a {@link Page} of {@link KeywordDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<KeywordDTO> findByCriteria(KeywordCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Keyword> specification = createSpecification(criteria);
        return keywordRepository.findAll(specification, page).map(keywordMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(KeywordCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Keyword> specification = createSpecification(criteria);
        return keywordRepository.count(specification);
    }

    /**
     * Function to convert {@link KeywordCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Keyword> createSpecification(KeywordCriteria criteria) {
        Specification<Keyword> specification = Specification.unrestricted();
        specification = specification.and((root, query, builder) -> {
            if (Long.class != query.getResultType()) {
                root.fetch(Keyword_.competitor, JoinType.LEFT);
            }
            return null;
        });
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildRangeSpecification(criteria.getId(), Keyword_.id),
                    buildStringSpecification(criteria.getValue(), Keyword_.value),
                    buildSpecification(criteria.getIsActive(), Keyword_.isActive),
                    buildRangeSpecification(criteria.getCreatedAt(), Keyword_.createdAt),
                    buildSpecification(criteria.getCompetitorId(), root ->
                        root.join(Keyword_.competitor, JoinType.LEFT).get(Competitor_.id)
                    )
                )
            );
        }
        return specification;
    }
}
