package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.*; // for static metamodels
import com.mycompany.myapp.domain.AnalystProfile;
import com.mycompany.myapp.repository.AnalystProfileRepository;
import com.mycompany.myapp.service.criteria.AnalystProfileCriteria;
import com.mycompany.myapp.service.dto.AnalystProfileDTO;
import com.mycompany.myapp.service.mapper.AnalystProfileMapper;
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
 * Service for executing complex queries for {@link AnalystProfile} entities in the database.
 * The main input is a {@link AnalystProfileCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link AnalystProfileDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class AnalystProfileQueryService extends QueryService<AnalystProfile> {

    private static final Logger LOG = LoggerFactory.getLogger(AnalystProfileQueryService.class);

    private final AnalystProfileRepository analystProfileRepository;

    private final AnalystProfileMapper analystProfileMapper;

    public AnalystProfileQueryService(AnalystProfileRepository analystProfileRepository, AnalystProfileMapper analystProfileMapper) {
        this.analystProfileRepository = analystProfileRepository;
        this.analystProfileMapper = analystProfileMapper;
    }

    /**
     * Return a {@link Page} of {@link AnalystProfileDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<AnalystProfileDTO> findByCriteria(AnalystProfileCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<AnalystProfile> specification = createSpecification(criteria);
        return analystProfileRepository
            .fetchBagRelationships(analystProfileRepository.findAll(specification, page))
            .map(analystProfileMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(AnalystProfileCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<AnalystProfile> specification = createSpecification(criteria);
        return analystProfileRepository.count(specification);
    }

    /**
     * Function to convert {@link AnalystProfileCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<AnalystProfile> createSpecification(AnalystProfileCriteria criteria) {
        Specification<AnalystProfile> specification = Specification.unrestricted();
        specification = specification.and((root, query, builder) -> {
            if (Long.class != query.getResultType()) {
                root.fetch(AnalystProfile_.user, JoinType.LEFT);
            }
            return null;
        });
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildRangeSpecification(criteria.getId(), AnalystProfile_.id),
                    buildStringSpecification(criteria.getDisplayName(), AnalystProfile_.displayName),
                    buildStringSpecification(criteria.getTelegramChatId(), AnalystProfile_.telegramChatId),
                    buildSpecification(criteria.getNotificationEnabled(), AnalystProfile_.notificationEnabled),
                    buildRangeSpecification(criteria.getCreatedAt(), AnalystProfile_.createdAt),
                    buildSpecification(criteria.getUserId(), root -> root.join(AnalystProfile_.user, JoinType.LEFT).get(User_.id)),
                    buildSpecification(criteria.getCompetitorsId(), root ->
                        root.join(AnalystProfile_.competitorses, JoinType.LEFT).get(Competitor_.id)
                    )
                )
            );
        }
        return specification;
    }
}
