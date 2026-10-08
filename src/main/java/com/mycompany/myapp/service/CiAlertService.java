package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.CiAlert;
import com.mycompany.myapp.repository.CiAlertRepository;
import com.mycompany.myapp.service.dto.CiAlertDTO;
import com.mycompany.myapp.service.mapper.CiAlertMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.mycompany.myapp.domain.CiAlert}.
 */
@Service
@Transactional
public class CiAlertService {

    private static final Logger LOG = LoggerFactory.getLogger(CiAlertService.class);

    private final CiAlertRepository ciAlertRepository;

    private final CiAlertMapper ciAlertMapper;

    public CiAlertService(CiAlertRepository ciAlertRepository, CiAlertMapper ciAlertMapper) {
        this.ciAlertRepository = ciAlertRepository;
        this.ciAlertMapper = ciAlertMapper;
    }

    /**
     * Save a ciAlert.
     *
     * @param ciAlertDTO the entity to save.
     * @return the persisted entity.
     */
    public CiAlertDTO save(CiAlertDTO ciAlertDTO) {
        LOG.debug("Request to save CiAlert : {}", ciAlertDTO);
        CiAlert ciAlert = ciAlertMapper.toEntity(ciAlertDTO);
        ciAlert = ciAlertRepository.save(ciAlert);
        return ciAlertMapper.toDto(ciAlert);
    }

    /**
     * Update a ciAlert.
     *
     * @param ciAlertDTO the entity to save.
     * @return the persisted entity.
     */
    public CiAlertDTO update(CiAlertDTO ciAlertDTO) {
        LOG.debug("Request to update CiAlert : {}", ciAlertDTO);
        CiAlert ciAlert = ciAlertMapper.toEntity(ciAlertDTO);
        ciAlert = ciAlertRepository.save(ciAlert);
        return ciAlertMapper.toDto(ciAlert);
    }

    /**
     * Partially update a ciAlert.
     *
     * @param ciAlertDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<CiAlertDTO> partialUpdate(CiAlertDTO ciAlertDTO) {
        LOG.debug("Request to partially update CiAlert : {}", ciAlertDTO);

        return ciAlertRepository
            .findById(ciAlertDTO.getId())
            .map(existingCiAlert -> {
                ciAlertMapper.partialUpdate(existingCiAlert, ciAlertDTO);

                return existingCiAlert;
            })
            .map(ciAlertRepository::save)
            .map(ciAlertMapper::toDto);
    }

    /**
     * Get all the ciAlerts with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<CiAlertDTO> findAllWithEagerRelationships(Pageable pageable) {
        return ciAlertRepository.findAllWithEagerRelationships(pageable).map(ciAlertMapper::toDto);
    }

    /**
     * Get one ciAlert by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<CiAlertDTO> findOne(Long id) {
        LOG.debug("Request to get CiAlert : {}", id);
        return ciAlertRepository.findOneWithEagerRelationships(id).map(ciAlertMapper::toDto);
    }

    /**
     * Delete the ciAlert by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete CiAlert : {}", id);
        ciAlertRepository.deleteById(id);
    }
}
