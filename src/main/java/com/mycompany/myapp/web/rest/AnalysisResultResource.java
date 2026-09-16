package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.domain.enumeration.ManagerPermissionType;
import com.mycompany.myapp.repository.AnalysisResultRepository;
import com.mycompany.myapp.security.AuthoritiesConstants;
import com.mycompany.myapp.security.SecurityUtils;
import com.mycompany.myapp.service.AnalysisResultQueryService;
import com.mycompany.myapp.service.AnalysisResultService;
import com.mycompany.myapp.service.ManagerAccessService;
import com.mycompany.myapp.service.criteria.AnalysisResultCriteria;
import com.mycompany.myapp.service.dto.AnalysisResultDTO;
import com.mycompany.myapp.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.service.filter.LongFilter;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

@RestController
@RequestMapping("/api/analysis-results")
public class AnalysisResultResource {

    private static final Logger LOG = LoggerFactory.getLogger(AnalysisResultResource.class);
    private static final String ENTITY_NAME = "analysisResult";

    @Value("${jhipster.clientApp.name:cIjhipster}")
    private String applicationName;

    private final AnalysisResultService analysisResultService;
    private final AnalysisResultRepository analysisResultRepository;
    private final AnalysisResultQueryService analysisResultQueryService;
    private final ManagerAccessService managerAccessService;

    public AnalysisResultResource(
        AnalysisResultService analysisResultService,
        AnalysisResultRepository analysisResultRepository,
        AnalysisResultQueryService analysisResultQueryService,
        ManagerAccessService managerAccessService
    ) {
        this.analysisResultService = analysisResultService;
        this.analysisResultRepository = analysisResultRepository;
        this.analysisResultQueryService = analysisResultQueryService;
        this.managerAccessService = managerAccessService;
    }

    @PostMapping("")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<AnalysisResultDTO> createAnalysisResult(@Valid @RequestBody AnalysisResultDTO analysisResultDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save AnalysisResult : {}", analysisResultDTO);
        if (analysisResultDTO.getId() != null) {
            throw new BadRequestAlertException("A new analysisResult cannot already have an ID", ENTITY_NAME, "idexists");
        }
        analysisResultDTO = analysisResultService.save(analysisResultDTO);
        return ResponseEntity.created(new URI("/api/analysis-results/" + analysisResultDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, analysisResultDTO.getId().toString()))
            .body(analysisResultDTO);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<AnalysisResultDTO> updateAnalysisResult(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody AnalysisResultDTO analysisResultDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update AnalysisResult : {}, {}", id, analysisResultDTO);
        if (analysisResultDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, analysisResultDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!analysisResultRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        analysisResultDTO = analysisResultService.update(analysisResultDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, analysisResultDTO.getId().toString()))
            .body(analysisResultDTO);
    }

    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<AnalysisResultDTO> partialUpdateAnalysisResult(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody AnalysisResultDTO analysisResultDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update AnalysisResult partially : {}, {}", id, analysisResultDTO);
        if (analysisResultDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, analysisResultDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!analysisResultRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<AnalysisResultDTO> result = analysisResultService.partialUpdate(analysisResultDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, analysisResultDTO.getId().toString())
        );
    }

    @GetMapping("")
    public ResponseEntity<List<AnalysisResultDTO>> getAllAnalysisResults(
        AnalysisResultCriteria criteria,
        @RequestParam(required = false) Long clientUserId,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get AnalysisResults by criteria: {}", criteria);
        applyOwnerFilter(criteria, clientUserId);
        Page<AnalysisResultDTO> page = analysisResultQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countAnalysisResults(AnalysisResultCriteria criteria, @RequestParam(required = false) Long clientUserId) {
        LOG.debug("REST request to count AnalysisResults by criteria: {}", criteria);
        applyOwnerFilter(criteria, clientUserId);
        return ResponseEntity.ok().body(analysisResultQueryService.countByCriteria(criteria));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnalysisResultDTO> getAnalysisResult(@PathVariable("id") Long id) {
        LOG.debug("REST request to get AnalysisResult : {}", id);
        Optional<AnalysisResultDTO> analysisResultDTO = analysisResultService.findOne(id);
        return ResponseUtil.wrapOrNotFound(analysisResultDTO);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteAnalysisResult(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete AnalysisResult : {}", id);
        analysisResultService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    private void applyOwnerFilter(AnalysisResultCriteria criteria, Long clientUserId) {
        Long ownerId = resolveOwnerId(clientUserId);
        if (ownerId == null) {
            return;
        }
        LongFilter ownerFilter = new LongFilter();
        ownerFilter.setEquals(ownerId);
        criteria.setOwnerId(ownerFilter);
    }

    private Long resolveOwnerId(Long clientUserId) {
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
            return clientUserId;
        }

        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.MANAGER)) {
            if (clientUserId == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "clientUserId is required for manager");
            }

            if (!managerAccessService.hasPermission(clientUserId, ManagerPermissionType.ANALYSIS_VIEW)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "ANALYSIS_VIEW permission is required");
            }
            return clientUserId;
        }
        return SecurityUtils.getCurrentUserId().orElseThrow(() ->
            new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Current user id not found")
        );
    }
}
