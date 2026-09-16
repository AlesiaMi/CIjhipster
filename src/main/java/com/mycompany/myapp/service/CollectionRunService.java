package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.CollectionRun;
import com.mycompany.myapp.repository.CollectionRunRepository;
import com.mycompany.myapp.security.AuthoritiesConstants;
import com.mycompany.myapp.security.SecurityUtils;
import com.mycompany.myapp.service.dto.CollectionRunDTO;
import com.mycompany.myapp.service.mapper.CollectionRunMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service Implementation for managing {@link com.mycompany.myapp.domain.CollectionRun}.
 */
@Service
@Transactional
public class CollectionRunService {

    private static final Logger LOG = LoggerFactory.getLogger(CollectionRunService.class);

    private final CollectionRunRepository collectionRunRepository;

    private final CollectionRunMapper collectionRunMapper;

    public CollectionRunService(CollectionRunRepository collectionRunRepository, CollectionRunMapper collectionRunMapper) {
        this.collectionRunRepository = collectionRunRepository;
        this.collectionRunMapper = collectionRunMapper;
    }

    /**
     * Save a collectionRun.
     *
     * @param collectionRunDTO the entity to save.
     * @return the persisted entity.
     */
    public CollectionRunDTO save(CollectionRunDTO collectionRunDTO) {
        LOG.debug("Request to save CollectionRun : {}", collectionRunDTO);
        CollectionRun collectionRun = collectionRunMapper.toEntity(collectionRunDTO);
        collectionRun = collectionRunRepository.save(collectionRun);
        return collectionRunMapper.toDto(collectionRun);
    }

    /**
     * Update a collectionRun.
     *
     * @param collectionRunDTO the entity to save.
     * @return the persisted entity.
     */
    public CollectionRunDTO update(CollectionRunDTO collectionRunDTO) {
        LOG.debug("Request to update CollectionRun : {}", collectionRunDTO);
        CollectionRun collectionRun = collectionRunMapper.toEntity(collectionRunDTO);
        collectionRun = collectionRunRepository.save(collectionRun);
        return collectionRunMapper.toDto(collectionRun);
    }

    /**
     * Partially update a collectionRun.
     *
     * @param collectionRunDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<CollectionRunDTO> partialUpdate(CollectionRunDTO collectionRunDTO) {
        LOG.debug("Request to partially update CollectionRun : {}", collectionRunDTO);

        return collectionRunRepository
            .findById(collectionRunDTO.getId())
            .map(existingCollectionRun -> {
                collectionRunMapper.partialUpdate(existingCollectionRun, collectionRunDTO);

                return existingCollectionRun;
            })
            .map(collectionRunRepository::save)
            .map(collectionRunMapper::toDto);
    }

    /**
     * Get one collectionRun by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<CollectionRunDTO> findOne(Long id) {
        LOG.debug("Request to get CollectionRun : {}", id);

        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
            return collectionRunRepository.findById(id).map(collectionRunMapper::toDto);
        }

        Long currentUserId = SecurityUtils.getCurrentUserId().orElseThrow(() ->
            new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Current user id not found")
        );

        return collectionRunRepository.findOneByIdAndOwnerId(id, currentUserId).map(collectionRunMapper::toDto);
    }

    /**
     * Delete the collectionRun by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete CollectionRun : {}", id);
        collectionRunRepository.deleteById(id);
    }
}
