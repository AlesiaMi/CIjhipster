package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.repository.CompetitorRepository;
import com.mycompany.myapp.security.AuthoritiesConstants;
import com.mycompany.myapp.security.SecurityUtils;
import com.mycompany.myapp.service.CompetitorQueryService;
import com.mycompany.myapp.service.CompetitorService;
import com.mycompany.myapp.service.ManagerAccessService;
import com.mycompany.myapp.service.ManagerAccessService;
import com.mycompany.myapp.service.criteria.CompetitorCriteria;
import com.mycompany.myapp.service.dto.CompetitorDTO;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.service.filter.LongFilter;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.mycompany.myapp.domain.Competitor}.
 */
@RestController
@RequestMapping("/api/competitors")
public class CompetitorResource {

    private static final Logger LOG = LoggerFactory.getLogger(CompetitorResource.class);

    private static final String ENTITY_NAME = "competitor";

    @Value("${jhipster.clientApp.name:cIjhipster}")
    private String applicationName;

    private final CompetitorService competitorService;

    private final CompetitorRepository competitorRepository;

    private final CompetitorQueryService competitorQueryService;

    private final ManagerAccessService managerAccessService;

    public CompetitorResource(
        CompetitorService competitorService,
        CompetitorRepository competitorRepository,
        CompetitorQueryService competitorQueryService,
        ManagerAccessService managerAccessService
    ) {
        this.competitorService = competitorService;
        this.competitorRepository = competitorRepository;
        this.competitorQueryService = competitorQueryService;
        this.managerAccessService = managerAccessService;
    }

    /**
     * {@code POST  /competitors} : Create a new competitor.
     *
     * @param competitorDTO the competitorDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new competitorDTO, or with status {@code 400 (Bad Request)} if the competitor has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<CompetitorDTO> createCompetitor(
        @Valid @RequestBody CompetitorDTO competitorDTO,
        @RequestParam(name = "clientUserId", required = false) Long clientUserId
    ) throws URISyntaxException {
        LOG.debug("REST request to save Competitor : {}", competitorDTO);

        if (competitorDTO.getId() != null) {
            throw new BadRequestAlertException("A new competitor cannot already have an ID", ENTITY_NAME, "idexists");
        }

        competitorDTO = competitorService.save(competitorDTO, clientUserId);

        return ResponseEntity.created(new URI("/api/competitors/" + competitorDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, competitorDTO.getId().toString()))
            .body(competitorDTO);
    }

    /**
     * {@code PUT  /competitors/:id} : Updates an existing competitor.
     *
     * @param id the id of the competitorDTO to save.
     * @param competitorDTO the competitorDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated competitorDTO,
     * or with status {@code 400 (Bad Request)} if the competitorDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the competitorDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<CompetitorDTO> updateCompetitor(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody CompetitorDTO competitorDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Competitor : {}, {}", id, competitorDTO);
        if (competitorDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, competitorDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        competitorDTO = competitorService.update(competitorDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, competitorDTO.getId().toString()))
            .body(competitorDTO);
    }

    /**
     * {@code PATCH  /competitors/:id} : Partial updates given fields of an existing competitor, field will ignore if it is null
     *
     * @param id the id of the competitorDTO to save.
     * @param competitorDTO the competitorDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated competitorDTO,
     * or with status {@code 400 (Bad Request)} if the competitorDTO is not valid,
     * or with status {@code 404 (Not Found)} if the competitorDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the competitorDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<CompetitorDTO> partialUpdateCompetitor(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody CompetitorDTO competitorDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Competitor partially : {}, {}", id, competitorDTO);
        if (competitorDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, competitorDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        Optional<CompetitorDTO> result = competitorService.partialUpdate(competitorDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, competitorDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /competitors} : get all the Competitors.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Competitors in body.
     */
    @GetMapping("")
    public ResponseEntity<List<CompetitorDTO>> getAllCompetitors(
        CompetitorCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get Competitors by criteria: {}", criteria);
        applyCurrentUserOwnerFilter(criteria);
        Page<CompetitorDTO> page = competitorQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /competitors/count} : count all the competitors.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countCompetitors(CompetitorCriteria criteria) {
        LOG.debug("REST request to count Competitors by criteria: {}", criteria);

        applyCurrentUserOwnerFilter(criteria);

        return ResponseEntity.ok().body(competitorQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /competitors/:id} : get the "id" competitor.
     *
     * @param id the id of the competitorDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the competitorDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CompetitorDTO> getCompetitor(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Competitor : {}", id);
        Optional<CompetitorDTO> competitorDTO = competitorService.findOne(id);
        return ResponseUtil.wrapOrNotFound(competitorDTO);
    }

    /**
     * {@code DELETE  /competitors/:id} : delete the "id" competitor.
     *
     * @param id the id of the competitorDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompetitor(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Competitor : {}", id);
        competitorService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    private void applyCurrentUserOwnerFilter(CompetitorCriteria criteria) {
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
            return;
        }

        List<Long> ownerIds = managerAccessService.getViewableClientUserIds();

        LongFilter ownerFilter = new LongFilter();

        if (ownerIds.isEmpty()) {
            // Guaranteed empty tenant scope.
            ownerFilter.setEquals(-1L);
        } else if (ownerIds.size() == 1) {
            ownerFilter.setEquals(ownerIds.getFirst());
        } else {
            ownerFilter.setIn(ownerIds);
        }

        // Never trust ownerId supplied by HTTP request.
        criteria.setOwnerId(ownerFilter);
    }
}
