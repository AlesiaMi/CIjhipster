package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.AnalysisResult;
import com.mycompany.myapp.domain.enumeration.ManagerPermissionType;
import com.mycompany.myapp.repository.AnalysisResultRepository;
import com.mycompany.myapp.security.AuthoritiesConstants;
import com.mycompany.myapp.security.SecurityUtils;
import com.mycompany.myapp.service.dto.AnalysisResultDTO;
import com.mycompany.myapp.service.mapper.AnalysisResultMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AnalysisResultService {

    private static final Logger LOG = LoggerFactory.getLogger(AnalysisResultService.class);
    private final AnalysisResultRepository analysisResultRepository;
    private final AnalysisResultMapper analysisResultMapper;
    private final TenantCacheVersionService tenantCacheVersionService;
    private final ManagerAccessService managerAccessService;

    public AnalysisResultService(
        AnalysisResultRepository analysisResultRepository,
        AnalysisResultMapper analysisResultMapper,
        TenantCacheVersionService tenantCacheVersionService,
        ManagerAccessService managerAccessService
    ) {
        this.analysisResultRepository = analysisResultRepository;
        this.analysisResultMapper = analysisResultMapper;
        this.tenantCacheVersionService = tenantCacheVersionService;
        this.managerAccessService = managerAccessService;
    }

    public AnalysisResultDTO save(AnalysisResultDTO analysisResultDTO) {
        LOG.debug("Request to save AnalysisResult : {}", analysisResultDTO);
        AnalysisResult analysisResult = analysisResultMapper.toEntity(analysisResultDTO);
        analysisResult = analysisResultRepository.save(analysisResult);
        analysisResultRepository.findOwnerIdById(analysisResult.getId()).ifPresent(tenantCacheVersionService::invalidateAfterCommit);

        return analysisResultMapper.toDto(analysisResult);
    }

    public AnalysisResultDTO update(AnalysisResultDTO analysisResultDTO) {
        LOG.debug("Request to update AnalysisResult : {}", analysisResultDTO);
        Long id = analysisResultDTO.getId();
        Optional<Long> oldOwnerId = id != null ? analysisResultRepository.findOwnerIdById(id) : Optional.empty();

        AnalysisResult analysisResult = analysisResultMapper.toEntity(analysisResultDTO);
        analysisResult = analysisResultRepository.save(analysisResult);
        Optional<Long> newOwnerId = analysisResultRepository.findOwnerIdById(analysisResult.getId());

        oldOwnerId.ifPresent(tenantCacheVersionService::invalidateAfterCommit);
        newOwnerId
            .filter(ownerId -> oldOwnerId.map(oldId -> !oldId.equals(ownerId)).orElse(true))
            .ifPresent(tenantCacheVersionService::invalidateAfterCommit);

        return analysisResultMapper.toDto(analysisResult);
    }

    public Optional<AnalysisResultDTO> partialUpdate(AnalysisResultDTO analysisResultDTO) {
        LOG.debug("Request to partially update AnalysisResult : {}", analysisResultDTO);
        Long id = analysisResultDTO.getId();
        if (id == null) {
            return Optional.empty();
        }
        Optional<Long> oldOwnerId = analysisResultRepository.findOwnerIdById(id);

        Optional<AnalysisResultDTO> result = analysisResultRepository
            .findById(id)
            .map(existingAnalysisResult -> {
                analysisResultMapper.partialUpdate(existingAnalysisResult, analysisResultDTO);
                return existingAnalysisResult;
            })
            .map(analysisResultRepository::save)
            .map(analysisResultMapper::toDto);

        if (result.isPresent()) {
            Optional<Long> newOwnerId = analysisResultRepository.findOwnerIdById(id);
            oldOwnerId.ifPresent(tenantCacheVersionService::invalidateAfterCommit);
            newOwnerId
                .filter(ownerId -> oldOwnerId.map(oldId -> !oldId.equals(ownerId)).orElse(true))
                .ifPresent(tenantCacheVersionService::invalidateAfterCommit);
        }

        return result;
    }

    public Page<AnalysisResultDTO> findAllWithEagerRelationships(Pageable pageable) {
        return analysisResultRepository.findAllWithEagerRelationships(pageable).map(analysisResultMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Optional<AnalysisResultDTO> findOne(Long id) {
        LOG.debug("Request to get AnalysisResult : {}", id);
        return analysisResultRepository
            .findOwnerIdById(id)
            .filter(ownerId -> managerAccessService.hasPermission(ownerId, ManagerPermissionType.ANALYSIS_VIEW))
            .flatMap(ownerId -> analysisResultRepository.findOneWithEagerRelationships(id))
            .map(analysisResultMapper::toDto);
    }

    public void delete(Long id) {
        LOG.debug("Request to delete AnalysisResult : {}", id);
        Optional<Long> ownerId = analysisResultRepository.findOwnerIdById(id);
        analysisResultRepository.deleteById(id);
        ownerId.ifPresent(tenantCacheVersionService::invalidateAfterCommit);
    }
}
