package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.Competitor;
import com.mycompany.myapp.repository.CompetitorRepository;
import com.mycompany.myapp.service.dto.CompetitorDTO;
import com.mycompany.myapp.service.mapper.CompetitorMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.mycompany.myapp.domain.Competitor}.
 */
@Service
@Transactional
public class CompetitorService {

    private static final Logger LOG = LoggerFactory.getLogger(CompetitorService.class);

    private final CompetitorRepository competitorRepository;

    private final CompetitorMapper competitorMapper;

    public CompetitorService(CompetitorRepository competitorRepository, CompetitorMapper competitorMapper) {
        this.competitorRepository = competitorRepository;
        this.competitorMapper = competitorMapper;
    }

    /**
     * Save a competitor.
     *
     * @param competitorDTO the entity to save.
     * @return the persisted entity.
     */
    public CompetitorDTO save(CompetitorDTO competitorDTO) {
        LOG.debug("Request to save Competitor : {}", competitorDTO);
        Competitor competitor = competitorMapper.toEntity(competitorDTO);
        competitor = competitorRepository.save(competitor);
        return competitorMapper.toDto(competitor);
    }

    /**
     * Update a competitor.
     *
     * @param competitorDTO the entity to save.
     * @return the persisted entity.
     */
    public CompetitorDTO update(CompetitorDTO competitorDTO) {
        LOG.debug("Request to update Competitor : {}", competitorDTO);
        Competitor competitor = competitorMapper.toEntity(competitorDTO);
        competitor = competitorRepository.save(competitor);
        return competitorMapper.toDto(competitor);
    }

    /**
     * Partially update a competitor.
     *
     * @param competitorDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<CompetitorDTO> partialUpdate(CompetitorDTO competitorDTO) {
        LOG.debug("Request to partially update Competitor : {}", competitorDTO);

        return competitorRepository
            .findById(competitorDTO.getId())
            .map(existingCompetitor -> {
                competitorMapper.partialUpdate(existingCompetitor, competitorDTO);

                return existingCompetitor;
            })
            .map(competitorRepository::save)
            .map(competitorMapper::toDto);
    }

    /**
     * Get one competitor by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<CompetitorDTO> findOne(Long id) {
        LOG.debug("Request to get Competitor : {}", id);
        return competitorRepository.findById(id).map(competitorMapper::toDto);
    }

    /**
     * Delete the competitor by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Competitor : {}", id);
        competitorRepository.deleteById(id);
    }
}
