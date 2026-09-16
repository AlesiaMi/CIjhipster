package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.Competitor;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.domain.enumeration.ManagerPermissionType;
import com.mycompany.myapp.domain.enumeration.ManagerPermissionType;
import com.mycompany.myapp.repository.CompetitorRepository;
import com.mycompany.myapp.repository.UserRepository;
import com.mycompany.myapp.security.AuthoritiesConstants;
import com.mycompany.myapp.security.SecurityUtils;
import com.mycompany.myapp.service.dto.CompetitorDTO;
import com.mycompany.myapp.service.mapper.CompetitorMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service Implementation for managing {@link com.mycompany.myapp.domain.Competitor}.
 */
@Service
@Transactional
public class CompetitorService {

    private static final Logger LOG = LoggerFactory.getLogger(CompetitorService.class);
    private final CompetitorRepository competitorRepository;
    private final CompetitorMapper competitorMapper;
    private final UserRepository userRepository;
    private final TenantCacheVersionService tenantCacheVersionService;
    private final ManagerAccessService managerAccessService;

    public CompetitorService(
        CompetitorRepository competitorRepository,
        CompetitorMapper competitorMapper,
        UserRepository userRepository,
        TenantCacheVersionService tenantCacheVersionService,
        ManagerAccessService managerAccessService
    ) {
        this.competitorRepository = competitorRepository;
        this.competitorMapper = competitorMapper;
        this.userRepository = userRepository;
        this.tenantCacheVersionService = tenantCacheVersionService;
        this.managerAccessService = managerAccessService;
    }

    /**
     * Save a competitor for the current authenticated user.
     */
    public CompetitorDTO save(CompetitorDTO competitorDTO, Long requestedOwnerId) {
        LOG.debug("Request to save Competitor : {} for owner {}", competitorDTO, requestedOwnerId);

        Long ownerId = resolveCreateOwnerId(requestedOwnerId);

        User owner = userRepository
            .findById(ownerId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner user not found"));

        Competitor competitor = competitorMapper.toEntity(competitorDTO);

        // Owner is controlled only by backend.
        competitor.setOwner(owner);

        competitor = competitorRepository.save(competitor);

        tenantCacheVersionService.invalidateAfterCommit(ownerId);

        return competitorMapper.toDto(competitor);
    }

    public CompetitorDTO update(CompetitorDTO competitorDTO) {
        LOG.debug("Request to update Competitor : {}", competitorDTO);

        Long id = competitorDTO.getId();

        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Competitor id is required");
        }

        Competitor existingCompetitor = findWritableEntity(id);

        Long ownerId = existingCompetitor.getOwner().getId();

        existingCompetitor.setCompetitorName(competitorDTO.getCompetitorName());
        existingCompetitor.setWebsiteUrl(competitorDTO.getWebsiteUrl());
        existingCompetitor.setIndustry(competitorDTO.getIndustry());
        existingCompetitor.setDescription(competitorDTO.getDescription());
        existingCompetitor.setIsActive(competitorDTO.getIsActive());

        existingCompetitor = competitorRepository.save(existingCompetitor);

        tenantCacheVersionService.invalidateAfterCommit(ownerId);

        return competitorMapper.toDto(existingCompetitor);
    }

    public Optional<CompetitorDTO> partialUpdate(CompetitorDTO competitorDTO) {
        LOG.debug("Request to partially update Competitor : {}", competitorDTO);

        Long id = competitorDTO.getId();

        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Competitor id is required");
        }

        Competitor existingCompetitor = findWritableEntity(id);

        Long ownerId = existingCompetitor.getOwner().getId();

        if (competitorDTO.getCompetitorName() != null) {
            existingCompetitor.setCompetitorName(competitorDTO.getCompetitorName());
        }

        if (competitorDTO.getWebsiteUrl() != null) {
            existingCompetitor.setWebsiteUrl(competitorDTO.getWebsiteUrl());
        }

        if (competitorDTO.getIndustry() != null) {
            existingCompetitor.setIndustry(competitorDTO.getIndustry());
        }

        if (competitorDTO.getDescription() != null) {
            existingCompetitor.setDescription(competitorDTO.getDescription());
        }

        if (competitorDTO.getIsActive() != null) {
            existingCompetitor.setIsActive(competitorDTO.getIsActive());
        }

        existingCompetitor = competitorRepository.save(existingCompetitor);

        tenantCacheVersionService.invalidateAfterCommit(ownerId);

        return Optional.of(competitorMapper.toDto(existingCompetitor));
    }

    @Transactional(readOnly = true)
    public Optional<CompetitorDTO> findOne(Long id) {
        LOG.debug("Request to get Competitor : {}", id);

        Optional<Competitor> competitor = competitorRepository.findById(id);

        if (competitor.isEmpty()) {
            return Optional.empty();
        }

        Long ownerId = competitor.orElseThrow().getOwner().getId();

        if (!managerAccessService.canView(ownerId)) {
            return Optional.empty();
        }

        return competitor.map(competitorMapper::toDto);
    }

    public void delete(Long id) {
        LOG.debug("Request to delete Competitor : {}", id);

        Competitor competitor = findWritableEntity(id);

        Long ownerId = competitor.getOwner().getId();

        competitorRepository.delete(competitor);

        tenantCacheVersionService.invalidateAfterCommit(ownerId);
    }

    /**
     * Check whether current user may access this competitor.
     */
    @Transactional(readOnly = true)
    public boolean canAccess(Long id) {
        if (isAdmin()) {
            return competitorRepository.existsById(id);
        }

        Long currentUserId = requireCurrentUserId();

        return competitorRepository.existsByIdAndOwnerId(id, currentUserId);
    }

    private Competitor findWritableEntity(Long id) {
        Competitor competitor = competitorRepository
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Competitor not found"));

        Long ownerId = competitor.getOwner().getId();

        if (!managerAccessService.canView(ownerId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Competitor not found");
        }

        if (!managerAccessService.hasPermission(ownerId, ManagerPermissionType.COMPETITORS_EDIT)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "COMPETITORS_EDIT permission is required");
        }

        return competitor;
    }

    private Long resolveCreateOwnerId(Long requestedOwnerId) {
        Long currentUserId = requireCurrentUserId();

        if (requestedOwnerId == null) {
            if (
                SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.MANAGER) &&
                !SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.USER)
            ) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "clientUserId is required for manager");
            }

            return currentUserId;
        }

        if (requestedOwnerId.equals(currentUserId)) {
            return currentUserId;
        }

        if (managerAccessService.hasPermission(requestedOwnerId, ManagerPermissionType.COMPETITORS_EDIT)) {
            return requestedOwnerId;
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "COMPETITORS_EDIT permission is required");
    }

    private Long requireCurrentUserId() {
        return SecurityUtils.getCurrentUserId().orElseThrow(() ->
            new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Current user id not found")
        );
    }

    private boolean isAdmin() {
        return SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN);
    }
}
