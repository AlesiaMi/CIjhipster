package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.DataSource;
import com.mycompany.myapp.repository.DataSourceRepository;
import com.mycompany.myapp.service.dto.DataSourceDTO;
import com.mycompany.myapp.service.mapper.DataSourceMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @CacheEvict(cacheNames = { "dataSourcesPages", "newsItemsPages" }, allEntries = true)
    public DataSourceDTO save(DataSourceDTO dataSourceDTO) {
        LOG.debug("Request to save DataSource : {}", dataSourceDTO);
        DataSource dataSource = dataSourceMapper.toEntity(dataSourceDTO);
        dataSource = dataSourceRepository.save(dataSource);
        return dataSourceMapper.toDto(dataSource);
    }

    @CacheEvict(cacheNames = { "dataSourcesPages", "newsItemsPages" }, allEntries = true)
    public DataSourceDTO update(DataSourceDTO dataSourceDTO) {
        LOG.debug("Request to update DataSource : {}", dataSourceDTO);
        DataSource dataSource = dataSourceMapper.toEntity(dataSourceDTO);
        dataSource = dataSourceRepository.save(dataSource);
        return dataSourceMapper.toDto(dataSource);
    }

    @CacheEvict(cacheNames = { "dataSourcesPages", "newsItemsPages" }, allEntries = true)
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

    public Page<DataSourceDTO> findAllWithEagerRelationships(Pageable pageable) {
        return dataSourceRepository.findAllWithEagerRelationships(pageable).map(dataSourceMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Optional<DataSourceDTO> findOne(Long id) {
        LOG.debug("Request to get DataSource : {}", id);
        return dataSourceRepository.findOneWithEagerRelationships(id).map(dataSourceMapper::toDto);
    }

    @CacheEvict(cacheNames = { "dataSourcesPages", "newsItemsPages" }, allEntries = true)
    public void delete(Long id) {
        LOG.debug("Request to delete DataSource : {}", id);
        dataSourceRepository.deleteById(id);
    }
}
