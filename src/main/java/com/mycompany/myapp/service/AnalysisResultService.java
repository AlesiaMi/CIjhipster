package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.AnalysisResult;
import com.mycompany.myapp.repository.AnalysisResultRepository;
import com.mycompany.myapp.service.dto.AnalysisResultDTO;
import com.mycompany.myapp.service.mapper.AnalysisResultMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.mycompany.myapp.domain.AnalysisResult}.
 */
@Service
@Transactional
public class AnalysisResultService {

    private static final Logger LOG = LoggerFactory.getLogger(AnalysisResultService.class);

    private final AnalysisResultRepository analysisResultRepository;

    private final AnalysisResultMapper analysisResultMapper;

    public AnalysisResultService(AnalysisResultRepository analysisResultRepository, AnalysisResultMapper analysisResultMapper) {
        this.analysisResultRepository = analysisResultRepository;
        this.analysisResultMapper = analysisResultMapper;
    }

    /**
     * Save a analysisResult.
     *
     * @param analysisResultDTO the entity to save.
     * @return the persisted entity.
     */
    public AnalysisResultDTO save(AnalysisResultDTO analysisResultDTO) {
        LOG.debug("Request to save AnalysisResult : {}", analysisResultDTO);
        AnalysisResult analysisResult = analysisResultMapper.toEntity(analysisResultDTO);
        analysisResult = analysisResultRepository.save(analysisResult);
        return analysisResultMapper.toDto(analysisResult);
    }

    /**
     * Update a analysisResult.
     *
     * @param analysisResultDTO the entity to save.
     * @return the persisted entity.
     */
    public AnalysisResultDTO update(AnalysisResultDTO analysisResultDTO) {
        LOG.debug("Request to update AnalysisResult : {}", analysisResultDTO);
        AnalysisResult analysisResult = analysisResultMapper.toEntity(analysisResultDTO);
        analysisResult = analysisResultRepository.save(analysisResult);
        return analysisResultMapper.toDto(analysisResult);
    }

    /**
     * Partially update a analysisResult.
     *
     * @param analysisResultDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<AnalysisResultDTO> partialUpdate(AnalysisResultDTO analysisResultDTO) {
        LOG.debug("Request to partially update AnalysisResult : {}", analysisResultDTO);

        return analysisResultRepository
            .findById(analysisResultDTO.getId())
            .map(existingAnalysisResult -> {
                analysisResultMapper.partialUpdate(existingAnalysisResult, analysisResultDTO);

                return existingAnalysisResult;
            })
            .map(analysisResultRepository::save)
            .map(analysisResultMapper::toDto);
    }

    /**
     * Get all the analysisResults with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<AnalysisResultDTO> findAllWithEagerRelationships(Pageable pageable) {
        return analysisResultRepository.findAllWithEagerRelationships(pageable).map(analysisResultMapper::toDto);
    }

    /**
     * Get one analysisResult by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<AnalysisResultDTO> findOne(Long id) {
        LOG.debug("Request to get AnalysisResult : {}", id);
        return analysisResultRepository.findOneWithEagerRelationships(id).map(analysisResultMapper::toDto);
    }

    /**
     * Delete the analysisResult by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete AnalysisResult : {}", id);
        analysisResultRepository.deleteById(id);
    }
}
