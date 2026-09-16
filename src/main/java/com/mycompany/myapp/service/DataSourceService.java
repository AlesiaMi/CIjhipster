package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.Competitor;
import com.mycompany.myapp.domain.DataSource;
import com.mycompany.myapp.domain.enumeration.ManagerPermissionType;
import com.mycompany.myapp.repository.CompetitorRepository;
import com.mycompany.myapp.repository.DataSourceRepository;
import com.mycompany.myapp.service.dto.DataSourceDTO;
import com.mycompany.myapp.service.mapper.DataSourceMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class DataSourceService {

    private static final Logger LOG = LoggerFactory.getLogger(DataSourceService.class);

    private final DataSourceRepository dataSourceRepository;
    private final DataSourceMapper dataSourceMapper;
    private final CompetitorRepository competitorRepository;
    private final TenantCacheVersionService tenantCacheVersionService;
    private final ManagerAccessService managerAccessService;

    public DataSourceService(
        DataSourceRepository dataSourceRepository,
        DataSourceMapper dataSourceMapper,
        CompetitorRepository competitorRepository,
        TenantCacheVersionService tenantCacheVersionService,
        ManagerAccessService managerAccessService
    ) {
        this.dataSourceRepository = dataSourceRepository;
        this.dataSourceMapper = dataSourceMapper;
        this.competitorRepository = competitorRepository;
        this.tenantCacheVersionService = tenantCacheVersionService;
        this.managerAccessService = managerAccessService;
    }

    public DataSourceDTO save(DataSourceDTO dataSourceDTO) {
        LOG.debug("Request to save DataSource : {}", dataSourceDTO);

        Competitor competitor = resolveWritableCompetitor(dataSourceDTO);

        DataSource dataSource = dataSourceMapper.toEntity(dataSourceDTO);

        dataSource.setCompetitor(competitor);

        dataSource = dataSourceRepository.save(dataSource);

        Long ownerId = competitor.getOwner().getId();

        tenantCacheVersionService.invalidateAfterCommit(ownerId);

        return dataSourceMapper.toDto(dataSource);
    }

    public DataSourceDTO update(DataSourceDTO dataSourceDTO) {
        LOG.debug("Request to update DataSource : {}", dataSourceDTO);

        Long id = dataSourceDTO.getId();

        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "DataSource id is required");
        }

        DataSource existingDataSource = findWritableEntity(id);

        Long oldOwnerId = existingDataSource.getCompetitor().getOwner().getId();

        Competitor competitor = resolveWritableCompetitor(dataSourceDTO);

        Long newOwnerId = competitor.getOwner().getId();

        existingDataSource.setSourceName(dataSourceDTO.getSourceName());

        existingDataSource.setUrl(dataSourceDTO.getUrl());

        existingDataSource.setSourceType(dataSourceDTO.getSourceType());

        existingDataSource.setIsActive(dataSourceDTO.getIsActive());

        existingDataSource.setLastCheckedAt(dataSourceDTO.getLastCheckedAt());

        existingDataSource.setCreatedAt(dataSourceDTO.getCreatedAt());

        existingDataSource.setCompetitor(competitor);

        existingDataSource = dataSourceRepository.save(existingDataSource);

        tenantCacheVersionService.invalidateAfterCommit(oldOwnerId);

        if (!oldOwnerId.equals(newOwnerId)) {
            tenantCacheVersionService.invalidateAfterCommit(newOwnerId);
        }

        return dataSourceMapper.toDto(existingDataSource);
    }

    public Optional<DataSourceDTO> partialUpdate(DataSourceDTO dataSourceDTO) {
        LOG.debug("Request to partially update DataSource : {}", dataSourceDTO);

        Long id = dataSourceDTO.getId();

        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "DataSource id is required");
        }

        DataSource existingDataSource = findWritableEntity(id);

        Long oldOwnerId = existingDataSource.getCompetitor().getOwner().getId();

        if (dataSourceDTO.getSourceName() != null) {
            existingDataSource.setSourceName(dataSourceDTO.getSourceName());
        }

        if (dataSourceDTO.getUrl() != null) {
            existingDataSource.setUrl(dataSourceDTO.getUrl());
        }

        if (dataSourceDTO.getSourceType() != null) {
            existingDataSource.setSourceType(dataSourceDTO.getSourceType());
        }

        if (dataSourceDTO.getIsActive() != null) {
            existingDataSource.setIsActive(dataSourceDTO.getIsActive());
        }

        if (dataSourceDTO.getLastCheckedAt() != null) {
            existingDataSource.setLastCheckedAt(dataSourceDTO.getLastCheckedAt());
        }

        if (dataSourceDTO.getCreatedAt() != null) {
            existingDataSource.setCreatedAt(dataSourceDTO.getCreatedAt());
        }

        if (dataSourceDTO.getCompetitor() != null) {
            Competitor competitor = resolveWritableCompetitor(dataSourceDTO);

            existingDataSource.setCompetitor(competitor);
        }

        Long newOwnerId = existingDataSource.getCompetitor().getOwner().getId();

        existingDataSource = dataSourceRepository.save(existingDataSource);

        tenantCacheVersionService.invalidateAfterCommit(oldOwnerId);

        if (!oldOwnerId.equals(newOwnerId)) {
            tenantCacheVersionService.invalidateAfterCommit(newOwnerId);
        }

        return Optional.of(dataSourceMapper.toDto(existingDataSource));
    }

    public Page<DataSourceDTO> findAllWithEagerRelationships(Pageable pageable) {
        return dataSourceRepository.findAllWithEagerRelationships(pageable).map(dataSourceMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Optional<DataSourceDTO> findOne(Long id) {
        LOG.debug("Request to get DataSource : {}", id);

        Optional<DataSource> dataSource = dataSourceRepository.findOneWithEagerRelationships(id);

        if (dataSource.isEmpty()) {
            return Optional.empty();
        }

        Long ownerId = dataSource.orElseThrow().getCompetitor().getOwner().getId();

        if (!managerAccessService.canView(ownerId)) {
            return Optional.empty();
        }

        return dataSource.map(dataSourceMapper::toDto);
    }

    public void delete(Long id) {
        LOG.debug("Request to delete DataSource : {}", id);

        DataSource dataSource = findWritableEntity(id);

        Long ownerId = dataSource.getCompetitor().getOwner().getId();

        dataSourceRepository.delete(dataSource);

        tenantCacheVersionService.invalidateAfterCommit(ownerId);
    }

    @Transactional(readOnly = true)
    public boolean canAccess(Long id) {
        return dataSourceRepository
            .findOneWithEagerRelationships(id)
            .map(DataSource::getCompetitor)
            .map(Competitor::getOwner)
            .map(user -> user.getId())
            .map(managerAccessService::canView)
            .orElse(false);
    }

    private DataSource findWritableEntity(Long id) {
        DataSource dataSource = dataSourceRepository
            .findOneWithEagerRelationships(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "DataSource not found"));

        Long ownerId = dataSource.getCompetitor().getOwner().getId();

        if (!managerAccessService.hasPermission(ownerId, ManagerPermissionType.SOURCES_EDIT)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "SOURCES_EDIT permission is required");
        }

        return dataSource;
    }

    private Competitor resolveWritableCompetitor(DataSourceDTO dataSourceDTO) {
        if (dataSourceDTO.getCompetitor() == null || dataSourceDTO.getCompetitor().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Competitor is required");
        }

        Long competitorId = dataSourceDTO.getCompetitor().getId();

        Competitor competitor = competitorRepository
            .findById(competitorId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Competitor not found"));

        Long ownerId = competitor.getOwner().getId();

        if (!managerAccessService.hasPermission(ownerId, ManagerPermissionType.SOURCES_EDIT)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "SOURCES_EDIT permission is required");
        }

        return competitor;
    }
}
