package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.repository.KeywordRepository;
import com.mycompany.myapp.service.KeywordQueryService;
import com.mycompany.myapp.service.KeywordService;
import com.mycompany.myapp.service.criteria.KeywordCriteria;
import com.mycompany.myapp.service.dto.KeywordDTO;
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
 * REST controller for managing {@link com.mycompany.myapp.domain.Keyword}.
 */
@RestController
@RequestMapping("/api/keywords")
public class KeywordResource {

    private static final Logger LOG = LoggerFactory.getLogger(KeywordResource.class);

    private static final String ENTITY_NAME = "keyword";

    @Value("${jhipster.clientApp.name:cIjhipster}")
    private String applicationName;

    private final KeywordService keywordService;

    private final KeywordRepository keywordRepository;

    private final KeywordQueryService keywordQueryService;

    public KeywordResource(KeywordService keywordService, KeywordRepository keywordRepository, KeywordQueryService keywordQueryService) {
        this.keywordService = keywordService;
        this.keywordRepository = keywordRepository;
        this.keywordQueryService = keywordQueryService;
    }

    /**
     * {@code POST  /keywords} : Create a new keyword.
     *
     * @param keywordDTO the keywordDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new keywordDTO, or with status {@code 400 (Bad Request)} if the keyword has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<KeywordDTO> createKeyword(@Valid @RequestBody KeywordDTO keywordDTO) throws URISyntaxException {
        LOG.debug("REST request to save Keyword : {}", keywordDTO);
        if (keywordDTO.getId() != null) {
            throw new BadRequestAlertException("A new keyword cannot already have an ID", ENTITY_NAME, "idexists");
        }
        keywordDTO = keywordService.save(keywordDTO);
        return ResponseEntity.created(new URI("/api/keywords/" + keywordDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, keywordDTO.getId().toString()))
            .body(keywordDTO);
    }

    /**
     * {@code PUT  /keywords/:id} : Updates an existing keyword.
     *
     * @param id the id of the keywordDTO to save.
     * @param keywordDTO the keywordDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated keywordDTO,
     * or with status {@code 400 (Bad Request)} if the keywordDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the keywordDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<KeywordDTO> updateKeyword(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody KeywordDTO keywordDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Keyword : {}, {}", id, keywordDTO);
        if (keywordDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, keywordDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!keywordRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        keywordDTO = keywordService.update(keywordDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, keywordDTO.getId().toString()))
            .body(keywordDTO);
    }

    /**
     * {@code PATCH  /keywords/:id} : Partial updates given fields of an existing keyword, field will ignore if it is null
     *
     * @param id the id of the keywordDTO to save.
     * @param keywordDTO the keywordDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated keywordDTO,
     * or with status {@code 400 (Bad Request)} if the keywordDTO is not valid,
     * or with status {@code 404 (Not Found)} if the keywordDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the keywordDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<KeywordDTO> partialUpdateKeyword(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody KeywordDTO keywordDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Keyword partially : {}, {}", id, keywordDTO);
        if (keywordDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, keywordDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!keywordRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<KeywordDTO> result = keywordService.partialUpdate(keywordDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, keywordDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /keywords} : get all the Keywords.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Keywords in body.
     */
    @GetMapping("")
    public ResponseEntity<List<KeywordDTO>> getAllKeywords(
        KeywordCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get Keywords by criteria: {}", criteria);

        Page<KeywordDTO> page = keywordQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /keywords/count} : count all the keywords.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countKeywords(KeywordCriteria criteria) {
        LOG.debug("REST request to count Keywords by criteria: {}", criteria);
        return ResponseEntity.ok().body(keywordQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /keywords/:id} : get the "id" keyword.
     *
     * @param id the id of the keywordDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the keywordDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<KeywordDTO> getKeyword(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Keyword : {}", id);
        Optional<KeywordDTO> keywordDTO = keywordService.findOne(id);
        return ResponseUtil.wrapOrNotFound(keywordDTO);
    }

    /**
     * {@code DELETE  /keywords/:id} : delete the "id" keyword.
     *
     * @param id the id of the keywordDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteKeyword(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Keyword : {}", id);
        keywordService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
