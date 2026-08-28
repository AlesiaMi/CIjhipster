package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.repository.CollectionRunRepository;
import com.mycompany.myapp.service.CollectionJobAsyncService;
import com.mycompany.myapp.service.CollectionRunQueryService;
import com.mycompany.myapp.service.CollectionRunService;
import com.mycompany.myapp.service.criteria.CollectionRunCriteria;
import com.mycompany.myapp.service.dto.CollectionRunDTO;
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
 * REST controller for managing {@link com.mycompany.myapp.domain.CollectionRun}.
 */
@RestController
@RequestMapping("/api/collection-runs")
public class CollectionRunResource {

    private static final Logger LOG = LoggerFactory.getLogger(CollectionRunResource.class);

    private static final String ENTITY_NAME = "collectionRun";

    @Value("${jhipster.clientApp.name:cIjhipster}")
    private String applicationName;

    private final CollectionRunService collectionRunService;

    private final CollectionRunRepository collectionRunRepository;

    private final CollectionRunQueryService collectionRunQueryService;

    private final CollectionJobAsyncService collectionJobAsyncService;

    public CollectionRunResource(
        CollectionRunService collectionRunService,
        CollectionRunRepository collectionRunRepository,
        CollectionRunQueryService collectionRunQueryService,
        CollectionJobAsyncService collectionJobAsyncService
    ) {
        this.collectionRunService = collectionRunService;
        this.collectionRunRepository = collectionRunRepository;
        this.collectionRunQueryService = collectionRunQueryService;
        this.collectionJobAsyncService = collectionJobAsyncService;
    }

    /**
     * {@code POST  /collection-runs} : Create a new collectionRun.
     *
     * @param collectionRunDTO the collectionRunDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new collectionRunDTO, or with status {@code 400 (Bad Request)} if the collectionRun has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<CollectionRunDTO> createCollectionRun(@Valid @RequestBody CollectionRunDTO collectionRunDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save CollectionRun : {}", collectionRunDTO);
        if (collectionRunDTO.getId() != null) {
            throw new BadRequestAlertException("A new collectionRun cannot already have an ID", ENTITY_NAME, "idexists");
        }
        collectionRunDTO = collectionRunService.save(collectionRunDTO);
        return ResponseEntity.created(new URI("/api/collection-runs/" + collectionRunDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, collectionRunDTO.getId().toString()))
            .body(collectionRunDTO);
    }

    /**
     * {@code PUT  /collection-runs/:id} : Updates an existing collectionRun.
     *
     * @param id the id of the collectionRunDTO to save.
     * @param collectionRunDTO the collectionRunDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated collectionRunDTO,
     * or with status {@code 400 (Bad Request)} if the collectionRunDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the collectionRunDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<CollectionRunDTO> updateCollectionRun(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody CollectionRunDTO collectionRunDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update CollectionRun : {}, {}", id, collectionRunDTO);
        if (collectionRunDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, collectionRunDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!collectionRunRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        collectionRunDTO = collectionRunService.update(collectionRunDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, collectionRunDTO.getId().toString()))
            .body(collectionRunDTO);
    }

    /**
     * {@code PATCH  /collection-runs/:id} : Partial updates given fields of an existing collectionRun, field will ignore if it is null
     *
     * @param id the id of the collectionRunDTO to save.
     * @param collectionRunDTO the collectionRunDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated collectionRunDTO,
     * or with status {@code 400 (Bad Request)} if the collectionRunDTO is not valid,
     * or with status {@code 404 (Not Found)} if the collectionRunDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the collectionRunDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<CollectionRunDTO> partialUpdateCollectionRun(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody CollectionRunDTO collectionRunDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update CollectionRun partially : {}, {}", id, collectionRunDTO);
        if (collectionRunDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, collectionRunDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!collectionRunRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<CollectionRunDTO> result = collectionRunService.partialUpdate(collectionRunDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, collectionRunDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /collection-runs} : get all the Collection Runs.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Collection Runs in body.
     */
    @GetMapping("")
    public ResponseEntity<List<CollectionRunDTO>> getAllCollectionRuns(
        CollectionRunCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get CollectionRuns by criteria: {}", criteria);

        Page<CollectionRunDTO> page = collectionRunQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /collection-runs/count} : count all the collectionRuns.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countCollectionRuns(CollectionRunCriteria criteria) {
        LOG.debug("REST request to count CollectionRuns by criteria: {}", criteria);
        return ResponseEntity.ok().body(collectionRunQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /collection-runs/:id} : get the "id" collectionRun.
     *
     * @param id the id of the collectionRunDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the collectionRunDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CollectionRunDTO> getCollectionRun(@PathVariable("id") Long id) {
        LOG.debug("REST request to get CollectionRun : {}", id);
        Optional<CollectionRunDTO> collectionRunDTO = collectionRunService.findOne(id);
        return ResponseUtil.wrapOrNotFound(collectionRunDTO);
    }

    /**
     * {@code DELETE  /collection-runs/:id} : delete the "id" collectionRun.
     *
     * @param id the id of the collectionRunDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteCollectionRun(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete CollectionRun : {}", id);
        collectionRunService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    @PostMapping("/run")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> runCollection() {
        LOG.debug("REST request to start RSS collection asynchronously");

        collectionJobAsyncService.runAsync();

        return ResponseEntity.accepted().build();
    }
}
