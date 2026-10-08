package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.AnalystProfile;
import com.mycompany.myapp.repository.AnalystProfileRepository;
import com.mycompany.myapp.service.dto.AnalystProfileDTO;
import com.mycompany.myapp.service.mapper.AnalystProfileMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.mycompany.myapp.domain.AnalystProfile}.
 */
@Service
@Transactional
public class AnalystProfileService {

    private static final Logger LOG = LoggerFactory.getLogger(AnalystProfileService.class);

    private final AnalystProfileRepository analystProfileRepository;

    private final AnalystProfileMapper analystProfileMapper;

    public AnalystProfileService(AnalystProfileRepository analystProfileRepository, AnalystProfileMapper analystProfileMapper) {
        this.analystProfileRepository = analystProfileRepository;
        this.analystProfileMapper = analystProfileMapper;
    }

    /**
     * Save a analystProfile.
     *
     * @param analystProfileDTO the entity to save.
     * @return the persisted entity.
     */
    public AnalystProfileDTO save(AnalystProfileDTO analystProfileDTO) {
        LOG.debug("Request to save AnalystProfile : {}", analystProfileDTO);
        AnalystProfile analystProfile = analystProfileMapper.toEntity(analystProfileDTO);
        analystProfile = analystProfileRepository.save(analystProfile);
        return analystProfileMapper.toDto(analystProfile);
    }

    /**
     * Update a analystProfile.
     *
     * @param analystProfileDTO the entity to save.
     * @return the persisted entity.
     */
    public AnalystProfileDTO update(AnalystProfileDTO analystProfileDTO) {
        LOG.debug("Request to update AnalystProfile : {}", analystProfileDTO);
        AnalystProfile analystProfile = analystProfileMapper.toEntity(analystProfileDTO);
        analystProfile = analystProfileRepository.save(analystProfile);
        return analystProfileMapper.toDto(analystProfile);
    }

    /**
     * Partially update a analystProfile.
     *
     * @param analystProfileDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<AnalystProfileDTO> partialUpdate(AnalystProfileDTO analystProfileDTO) {
        LOG.debug("Request to partially update AnalystProfile : {}", analystProfileDTO);

        return analystProfileRepository
            .findById(analystProfileDTO.getId())
            .map(existingAnalystProfile -> {
                analystProfileMapper.partialUpdate(existingAnalystProfile, analystProfileDTO);

                return existingAnalystProfile;
            })
            .map(analystProfileRepository::save)
            .map(analystProfileMapper::toDto);
    }

    /**
     * Get all the analystProfiles with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<AnalystProfileDTO> findAllWithEagerRelationships(Pageable pageable) {
        return analystProfileRepository.findAllWithEagerRelationships(pageable).map(analystProfileMapper::toDto);
    }

    /**
     * Get one analystProfile by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<AnalystProfileDTO> findOne(Long id) {
        LOG.debug("Request to get AnalystProfile : {}", id);
        return analystProfileRepository.findOneWithEagerRelationships(id).map(analystProfileMapper::toDto);
    }

    /**
     * Delete the analystProfile by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete AnalystProfile : {}", id);
        analystProfileRepository.deleteById(id);
    }
}
