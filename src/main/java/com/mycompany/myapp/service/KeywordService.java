package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.Keyword;
import com.mycompany.myapp.repository.KeywordRepository;
import com.mycompany.myapp.service.dto.KeywordDTO;
import com.mycompany.myapp.service.mapper.KeywordMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.mycompany.myapp.domain.Keyword}.
 */
@Service
@Transactional
public class KeywordService {

    private static final Logger LOG = LoggerFactory.getLogger(KeywordService.class);

    private final KeywordRepository keywordRepository;

    private final KeywordMapper keywordMapper;

    public KeywordService(KeywordRepository keywordRepository, KeywordMapper keywordMapper) {
        this.keywordRepository = keywordRepository;
        this.keywordMapper = keywordMapper;
    }

    /**
     * Save a keyword.
     *
     * @param keywordDTO the entity to save.
     * @return the persisted entity.
     */
    public KeywordDTO save(KeywordDTO keywordDTO) {
        LOG.debug("Request to save Keyword : {}", keywordDTO);
        Keyword keyword = keywordMapper.toEntity(keywordDTO);
        keyword = keywordRepository.save(keyword);
        return keywordMapper.toDto(keyword);
    }

    /**
     * Update a keyword.
     *
     * @param keywordDTO the entity to save.
     * @return the persisted entity.
     */
    public KeywordDTO update(KeywordDTO keywordDTO) {
        LOG.debug("Request to update Keyword : {}", keywordDTO);
        Keyword keyword = keywordMapper.toEntity(keywordDTO);
        keyword = keywordRepository.save(keyword);
        return keywordMapper.toDto(keyword);
    }

    /**
     * Partially update a keyword.
     *
     * @param keywordDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<KeywordDTO> partialUpdate(KeywordDTO keywordDTO) {
        LOG.debug("Request to partially update Keyword : {}", keywordDTO);

        return keywordRepository
            .findById(keywordDTO.getId())
            .map(existingKeyword -> {
                keywordMapper.partialUpdate(existingKeyword, keywordDTO);

                return existingKeyword;
            })
            .map(keywordRepository::save)
            .map(keywordMapper::toDto);
    }

    /**
     * Get all the keywords with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<KeywordDTO> findAllWithEagerRelationships(Pageable pageable) {
        return keywordRepository.findAllWithEagerRelationships(pageable).map(keywordMapper::toDto);
    }

    /**
     * Get one keyword by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<KeywordDTO> findOne(Long id) {
        LOG.debug("Request to get Keyword : {}", id);
        return keywordRepository.findOneWithEagerRelationships(id).map(keywordMapper::toDto);
    }

    /**
     * Delete the keyword by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Keyword : {}", id);
        keywordRepository.deleteById(id);
    }
}
