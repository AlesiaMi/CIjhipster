package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.DataSource;
import com.mycompany.myapp.repository.DataSourceRepository;
import com.mycompany.myapp.service.dto.DataSourceDTO;
import com.mycompany.myapp.service.mapper.DataSourceMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.mycompany.myapp.domain.DataSource}.
 */
@Service
@Transactional
public class DataSourceService {

    private static final Logger LOG = LoggerFactory.getLogger(DataSourceService.class);

    private final DataSourceRepository dataSourceRepository;

    private final DataSourceMapper dataSourceMapper;

    public DataSourceService(DataSourceRepository dataSourceRepository, DataSourceMapper dataSourceMapper) {
        this.dataSourceRepository = dataSourceRepository;
        this.dataSourceMapper = dataSourceMapper;
    }

    /**
     * Save a dataSource.
     *
     * @param dataSourceDTO the entity to save.
     * @return the persisted entity.
     */
    public DataSourceDTO save(DataSourceDTO dataSourceDTO) {
        LOG.debug("Request to save DataSource : {}", dataSourceDTO);
        DataSource dataSource = dataSourceMapper.toEntity(dataSourceDTO);
        dataSource = dataSourceRepository.save(dataSource);
        return dataSourceMapper.toDto(dataSource);
    }

    /**
     * Update a dataSource.
     *
     * @param dataSourceDTO the entity to save.
     * @return the persisted entity.
     */
    public DataSourceDTO update(DataSourceDTO dataSourceDTO) {
        LOG.debug("Request to update DataSource : {}", dataSourceDTO);
        DataSource dataSource = dataSourceMapper.toEntity(dataSourceDTO);
        dataSource = dataSourceRepository.save(dataSource);
        return dataSourceMapper.toDto(dataSource);
    }

    /**
     * Partially update a dataSource.
     *
     * @param dataSourceDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<DataSourceDTO> partialUpdate(DataSourceDTO dataSourceDTO) {
        LOG.debug("Request to partially update DataSource : {}", dataSourceDTO);

        return dataSourceRepository
            .findById(dataSourceDTO.getId())
            .map(existingDataSource -> {
                dataSourceMapper.partialUpdate(existingDataSource, dataSourceDTO);

                return existingDataSource;
            })
            .map(dataSourceRepository::save)
            .map(dataSourceMapper::toDto);
    }

    /**
     * Get all the dataSources with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<DataSourceDTO> findAllWithEagerRelationships(Pageable pageable) {
        return dataSourceRepository.findAllWithEagerRelationships(pageable).map(dataSourceMapper::toDto);
    }

    /**
     * Get one dataSource by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<DataSourceDTO> findOne(Long id) {
        LOG.debug("Request to get DataSource : {}", id);
        return dataSourceRepository.findOneWithEagerRelationships(id).map(dataSourceMapper::toDto);
    }

    /**
     * Delete the dataSource by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete DataSource : {}", id);
        dataSourceRepository.deleteById(id);
    }
}
