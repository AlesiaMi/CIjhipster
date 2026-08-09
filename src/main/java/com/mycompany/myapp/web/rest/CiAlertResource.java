package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.repository.CiAlertRepository;
import com.mycompany.myapp.service.CiAlertQueryService;
import com.mycompany.myapp.service.CiAlertService;
import com.mycompany.myapp.service.criteria.CiAlertCriteria;
import com.mycompany.myapp.service.dto.CiAlertDTO;
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
 * REST controller for managing {@link com.mycompany.myapp.domain.CiAlert}.
 */
@RestController
@RequestMapping("/api/ci-alerts")
public class CiAlertResource {

    private static final Logger LOG = LoggerFactory.getLogger(CiAlertResource.class);

    private static final String ENTITY_NAME = "ciAlert";

    @Value("${jhipster.clientApp.name:cIjhipster}")
    private String applicationName;

    private final CiAlertService ciAlertService;

    private final CiAlertRepository ciAlertRepository;

    private final CiAlertQueryService ciAlertQueryService;

    public CiAlertResource(CiAlertService ciAlertService, CiAlertRepository ciAlertRepository, CiAlertQueryService ciAlertQueryService) {
        this.ciAlertService = ciAlertService;
        this.ciAlertRepository = ciAlertRepository;
        this.ciAlertQueryService = ciAlertQueryService;
    }

    /**
     * {@code POST  /ci-alerts} : Create a new ciAlert.
     *
     * @param ciAlertDTO the ciAlertDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new ciAlertDTO, or with status {@code 400 (Bad Request)} if the ciAlert has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<CiAlertDTO> createCiAlert(@Valid @RequestBody CiAlertDTO ciAlertDTO) throws URISyntaxException {
        LOG.debug("REST request to save CiAlert : {}", ciAlertDTO);
        if (ciAlertDTO.getId() != null) {
            throw new BadRequestAlertException("A new ciAlert cannot already have an ID", ENTITY_NAME, "idexists");
        }
        ciAlertDTO = ciAlertService.save(ciAlertDTO);
        return ResponseEntity.created(new URI("/api/ci-alerts/" + ciAlertDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, ciAlertDTO.getId().toString()))
            .body(ciAlertDTO);
    }

    /**
     * {@code PUT  /ci-alerts/:id} : Updates an existing ciAlert.
     *
     * @param id the id of the ciAlertDTO to save.
     * @param ciAlertDTO the ciAlertDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated ciAlertDTO,
     * or with status {@code 400 (Bad Request)} if the ciAlertDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the ciAlertDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<CiAlertDTO> updateCiAlert(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody CiAlertDTO ciAlertDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update CiAlert : {}, {}", id, ciAlertDTO);
        if (ciAlertDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, ciAlertDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!ciAlertRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        ciAlertDTO = ciAlertService.update(ciAlertDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, ciAlertDTO.getId().toString()))
            .body(ciAlertDTO);
    }

    /**
     * {@code PATCH  /ci-alerts/:id} : Partial updates given fields of an existing ciAlert, field will ignore if it is null
     *
     * @param id the id of the ciAlertDTO to save.
     * @param ciAlertDTO the ciAlertDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated ciAlertDTO,
     * or with status {@code 400 (Bad Request)} if the ciAlertDTO is not valid,
     * or with status {@code 404 (Not Found)} if the ciAlertDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the ciAlertDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<CiAlertDTO> partialUpdateCiAlert(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody CiAlertDTO ciAlertDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update CiAlert partially : {}, {}", id, ciAlertDTO);
        if (ciAlertDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, ciAlertDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!ciAlertRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<CiAlertDTO> result = ciAlertService.partialUpdate(ciAlertDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, ciAlertDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /ci-alerts} : get all the Ci Alerts.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Ci Alerts in body.
     */
    @GetMapping("")
    public ResponseEntity<List<CiAlertDTO>> getAllCiAlerts(
        CiAlertCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get CiAlerts by criteria: {}", criteria);

        Page<CiAlertDTO> page = ciAlertQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /ci-alerts/count} : count all the ciAlerts.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countCiAlerts(CiAlertCriteria criteria) {
        LOG.debug("REST request to count CiAlerts by criteria: {}", criteria);
        return ResponseEntity.ok().body(ciAlertQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /ci-alerts/:id} : get the "id" ciAlert.
     *
     * @param id the id of the ciAlertDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the ciAlertDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CiAlertDTO> getCiAlert(@PathVariable("id") Long id) {
        LOG.debug("REST request to get CiAlert : {}", id);
        Optional<CiAlertDTO> ciAlertDTO = ciAlertService.findOne(id);
        return ResponseUtil.wrapOrNotFound(ciAlertDTO);
    }

    /**
     * {@code DELETE  /ci-alerts/:id} : delete the "id" ciAlert.
     *
     * @param id the id of the ciAlertDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteCiAlert(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete CiAlert : {}", id);
        ciAlertService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
