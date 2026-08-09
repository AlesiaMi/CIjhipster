package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.repository.AnalysisResultRepository;
import com.mycompany.myapp.service.AnalysisResultQueryService;
import com.mycompany.myapp.service.AnalysisResultService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.mycompany.myapp.domain.AnalysisResult}.
 */
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

    public AnalysisResultResource(
        AnalysisResultService analysisResultService,
        AnalysisResultRepository analysisResultRepository,
        AnalysisResultQueryService analysisResultQueryService
    ) {
        this.analysisResultService = analysisResultService;
        this.analysisResultRepository = analysisResultRepository;
        this.analysisResultQueryService = analysisResultQueryService;
    }

    /**
     * {@code POST  /analysis-results} : Create a new analysisResult.
     *
     * @param analysisResultDTO the analysisResultDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new analysisResultDTO, or with status {@code 400 (Bad Request)} if the analysisResult has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
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

    /**
     * {@code PUT  /analysis-results/:id} : Updates an existing analysisResult.
     *
     * @param id the id of the analysisResultDTO to save.
     * @param analysisResultDTO the analysisResultDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated analysisResultDTO,
     * or with status {@code 400 (Bad Request)} if the analysisResultDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the analysisResultDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
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

    /**
     * {@code PATCH  /analysis-results/:id} : Partial updates given fields of an existing analysisResult, field will ignore if it is null
     *
     * @param id the id of the analysisResultDTO to save.
     * @param analysisResultDTO the analysisResultDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated analysisResultDTO,
     * or with status {@code 400 (Bad Request)} if the analysisResultDTO is not valid,
     * or with status {@code 404 (Not Found)} if the analysisResultDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the analysisResultDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
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

    /**
     * {@code GET  /analysis-results} : get all the Analysis Results.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Analysis Results in body.
     */
    @GetMapping("")
    public ResponseEntity<List<AnalysisResultDTO>> getAllAnalysisResults(
        AnalysisResultCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get AnalysisResults by criteria: {}", criteria);

        Page<AnalysisResultDTO> page = analysisResultQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /analysis-results/count} : count all the analysisResults.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countAnalysisResults(AnalysisResultCriteria criteria) {
        LOG.debug("REST request to count AnalysisResults by criteria: {}", criteria);
        return ResponseEntity.ok().body(analysisResultQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /analysis-results/:id} : get the "id" analysisResult.
     *
     * @param id the id of the analysisResultDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the analysisResultDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AnalysisResultDTO> getAnalysisResult(@PathVariable("id") Long id) {
        LOG.debug("REST request to get AnalysisResult : {}", id);
        Optional<AnalysisResultDTO> analysisResultDTO = analysisResultService.findOne(id);
        return ResponseUtil.wrapOrNotFound(analysisResultDTO);
    }

    /**
     * {@code DELETE  /analysis-results/:id} : delete the "id" analysisResult.
     *
     * @param id the id of the analysisResultDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteAnalysisResult(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete AnalysisResult : {}", id);
        analysisResultService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
