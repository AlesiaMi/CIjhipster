package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.repository.AnalystProfileRepository;
import com.mycompany.myapp.service.AnalystProfileQueryService;
import com.mycompany.myapp.service.AnalystProfileService;
import com.mycompany.myapp.service.criteria.AnalystProfileCriteria;
import com.mycompany.myapp.service.dto.AnalystProfileDTO;
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
 * REST controller for managing {@link com.mycompany.myapp.domain.AnalystProfile}.
 */
@RestController
@RequestMapping("/api/analyst-profiles")
public class AnalystProfileResource {

    private static final Logger LOG = LoggerFactory.getLogger(AnalystProfileResource.class);

    private static final String ENTITY_NAME = "analystProfile";

    @Value("${jhipster.clientApp.name:cIjhipster}")
    private String applicationName;

    private final AnalystProfileService analystProfileService;

    private final AnalystProfileRepository analystProfileRepository;

    private final AnalystProfileQueryService analystProfileQueryService;

    public AnalystProfileResource(
        AnalystProfileService analystProfileService,
        AnalystProfileRepository analystProfileRepository,
        AnalystProfileQueryService analystProfileQueryService
    ) {
        this.analystProfileService = analystProfileService;
        this.analystProfileRepository = analystProfileRepository;
        this.analystProfileQueryService = analystProfileQueryService;
    }

    /**
     * {@code POST  /analyst-profiles} : Create a new analystProfile.
     *
     * @param analystProfileDTO the analystProfileDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new analystProfileDTO, or with status {@code 400 (Bad Request)} if the analystProfile has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<AnalystProfileDTO> createAnalystProfile(@Valid @RequestBody AnalystProfileDTO analystProfileDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save AnalystProfile : {}", analystProfileDTO);
        if (analystProfileDTO.getId() != null) {
            throw new BadRequestAlertException("A new analystProfile cannot already have an ID", ENTITY_NAME, "idexists");
        }
        analystProfileDTO = analystProfileService.save(analystProfileDTO);
        return ResponseEntity.created(new URI("/api/analyst-profiles/" + analystProfileDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, analystProfileDTO.getId().toString()))
            .body(analystProfileDTO);
    }

    /**
     * {@code PUT  /analyst-profiles/:id} : Updates an existing analystProfile.
     *
     * @param id the id of the analystProfileDTO to save.
     * @param analystProfileDTO the analystProfileDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated analystProfileDTO,
     * or with status {@code 400 (Bad Request)} if the analystProfileDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the analystProfileDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<AnalystProfileDTO> updateAnalystProfile(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody AnalystProfileDTO analystProfileDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update AnalystProfile : {}, {}", id, analystProfileDTO);
        if (analystProfileDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, analystProfileDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!analystProfileRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        analystProfileDTO = analystProfileService.update(analystProfileDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, analystProfileDTO.getId().toString()))
            .body(analystProfileDTO);
    }

    /**
     * {@code PATCH  /analyst-profiles/:id} : Partial updates given fields of an existing analystProfile, field will ignore if it is null
     *
     * @param id the id of the analystProfileDTO to save.
     * @param analystProfileDTO the analystProfileDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated analystProfileDTO,
     * or with status {@code 400 (Bad Request)} if the analystProfileDTO is not valid,
     * or with status {@code 404 (Not Found)} if the analystProfileDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the analystProfileDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<AnalystProfileDTO> partialUpdateAnalystProfile(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody AnalystProfileDTO analystProfileDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update AnalystProfile partially : {}, {}", id, analystProfileDTO);
        if (analystProfileDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, analystProfileDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!analystProfileRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<AnalystProfileDTO> result = analystProfileService.partialUpdate(analystProfileDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, analystProfileDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /analyst-profiles} : get all the Analyst Profiles.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Analyst Profiles in body.
     */
    @GetMapping("")
    public ResponseEntity<List<AnalystProfileDTO>> getAllAnalystProfiles(
        AnalystProfileCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get AnalystProfiles by criteria: {}", criteria);

        Page<AnalystProfileDTO> page = analystProfileQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /analyst-profiles/count} : count all the analystProfiles.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countAnalystProfiles(AnalystProfileCriteria criteria) {
        LOG.debug("REST request to count AnalystProfiles by criteria: {}", criteria);
        return ResponseEntity.ok().body(analystProfileQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /analyst-profiles/:id} : get the "id" analystProfile.
     *
     * @param id the id of the analystProfileDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the analystProfileDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AnalystProfileDTO> getAnalystProfile(@PathVariable("id") Long id) {
        LOG.debug("REST request to get AnalystProfile : {}", id);
        Optional<AnalystProfileDTO> analystProfileDTO = analystProfileService.findOne(id);
        return ResponseUtil.wrapOrNotFound(analystProfileDTO);
    }

    /**
     * {@code DELETE  /analyst-profiles/:id} : delete the "id" analystProfile.
     *
     * @param id the id of the analystProfileDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteAnalystProfile(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete AnalystProfile : {}", id);
        analystProfileService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
