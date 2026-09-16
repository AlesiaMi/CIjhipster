package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.*; // for static metamodels
import com.mycompany.myapp.repository.CompetitorRepository;
import com.mycompany.myapp.service.criteria.CompetitorCriteria;
import com.mycompany.myapp.service.dto.CompetitorDTO;
import com.mycompany.myapp.service.mapper.CompetitorMapper;
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
public class CompetitorQueryService extends QueryService<Competitor> {

    private static final Logger LOG = LoggerFactory.getLogger(CompetitorQueryService.class);

    private final CompetitorRepository competitorRepository;

    private final CompetitorMapper competitorMapper;

    public CompetitorQueryService(CompetitorRepository competitorRepository, CompetitorMapper competitorMapper) {
        this.competitorRepository = competitorRepository;
        this.competitorMapper = competitorMapper;
    }

    @Transactional(readOnly = true)
    public Page<CompetitorDTO> findByCriteria(CompetitorCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Competitor> specification = createSpecification(criteria);
        return competitorRepository.findAll(specification, page).map(competitorMapper::toDto);
    }

    @Transactional(readOnly = true)
    public long countByCriteria(CompetitorCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Competitor> specification = createSpecification(criteria);
        return competitorRepository.count(specification);
    }

    protected Specification<Competitor> createSpecification(CompetitorCriteria criteria) {
        Specification<Competitor> specification = Specification.unrestricted();
        if (criteria != null) {
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildRangeSpecification(criteria.getId(), Competitor_.id),
                    buildStringSpecification(criteria.getCompetitorName(), Competitor_.competitorName),
                    buildStringSpecification(criteria.getWebsiteUrl(), Competitor_.websiteUrl),
                    buildStringSpecification(criteria.getIndustry(), Competitor_.industry),
                    buildStringSpecification(criteria.getDescription(), Competitor_.description),
                    buildSpecification(criteria.getIsActive(), Competitor_.isActive),
                    buildSpecification(criteria.getOwnerId(), root -> root.join("owner", JoinType.LEFT).<Long>get("id")),
                    buildSpecification(criteria.getAnalystProfilesId(), root ->
                        root.join(Competitor_.analystProfileses, JoinType.LEFT).get(AnalystProfile_.id)
                    )
                )
            );
        }
        return specification;
    }
}
