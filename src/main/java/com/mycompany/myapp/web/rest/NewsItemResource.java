package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.repository.NewsItemRepository;
import com.mycompany.myapp.service.NewsItemQueryService;
import com.mycompany.myapp.service.NewsItemService;
import com.mycompany.myapp.service.criteria.NewsItemCriteria;
import com.mycompany.myapp.service.dto.NewsItemDTO;
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
 * REST controller for managing {@link com.mycompany.myapp.domain.NewsItem}.
 */
@RestController
@RequestMapping("/api/news-items")
public class NewsItemResource {

    private static final Logger LOG = LoggerFactory.getLogger(NewsItemResource.class);

    private static final String ENTITY_NAME = "newsItem";

    @Value("${jhipster.clientApp.name:cIjhipster}")
    private String applicationName;

    private final NewsItemService newsItemService;

    private final NewsItemRepository newsItemRepository;

    private final NewsItemQueryService newsItemQueryService;

    public NewsItemResource(
        NewsItemService newsItemService,
        NewsItemRepository newsItemRepository,
        NewsItemQueryService newsItemQueryService
    ) {
        this.newsItemService = newsItemService;
        this.newsItemRepository = newsItemRepository;
        this.newsItemQueryService = newsItemQueryService;
    }

    /**
     * {@code POST  /news-items} : Create a new newsItem.
     *
     * @param newsItemDTO the newsItemDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new newsItemDTO, or with status {@code 400 (Bad Request)} if the newsItem has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<NewsItemDTO> createNewsItem(@Valid @RequestBody NewsItemDTO newsItemDTO) throws URISyntaxException {
        LOG.debug("REST request to save NewsItem : {}", newsItemDTO);
        if (newsItemDTO.getId() != null) {
            throw new BadRequestAlertException("A new newsItem cannot already have an ID", ENTITY_NAME, "idexists");
        }
        newsItemDTO = newsItemService.save(newsItemDTO);
        return ResponseEntity.created(new URI("/api/news-items/" + newsItemDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, newsItemDTO.getId().toString()))
            .body(newsItemDTO);
    }

    /**
     * {@code PUT  /news-items/:id} : Updates an existing newsItem.
     *
     * @param id the id of the newsItemDTO to save.
     * @param newsItemDTO the newsItemDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated newsItemDTO,
     * or with status {@code 400 (Bad Request)} if the newsItemDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the newsItemDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<NewsItemDTO> updateNewsItem(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody NewsItemDTO newsItemDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update NewsItem : {}, {}", id, newsItemDTO);
        if (newsItemDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, newsItemDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!newsItemRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        newsItemDTO = newsItemService.update(newsItemDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, newsItemDTO.getId().toString()))
            .body(newsItemDTO);
    }

    /**
     * {@code PATCH  /news-items/:id} : Partial updates given fields of an existing newsItem, field will ignore if it is null
     *
     * @param id the id of the newsItemDTO to save.
     * @param newsItemDTO the newsItemDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated newsItemDTO,
     * or with status {@code 400 (Bad Request)} if the newsItemDTO is not valid,
     * or with status {@code 404 (Not Found)} if the newsItemDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the newsItemDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<NewsItemDTO> partialUpdateNewsItem(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody NewsItemDTO newsItemDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update NewsItem partially : {}, {}", id, newsItemDTO);
        if (newsItemDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, newsItemDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!newsItemRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<NewsItemDTO> result = newsItemService.partialUpdate(newsItemDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, newsItemDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /news-items} : get all the News Items.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of News Items in body.
     */
    @GetMapping("")
    public ResponseEntity<List<NewsItemDTO>> getAllNewsItems(
        NewsItemCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get NewsItems by criteria: {}", criteria);

        Page<NewsItemDTO> page = newsItemQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /news-items/count} : count all the newsItems.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countNewsItems(NewsItemCriteria criteria) {
        LOG.debug("REST request to count NewsItems by criteria: {}", criteria);
        return ResponseEntity.ok().body(newsItemQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /news-items/:id} : get the "id" newsItem.
     *
     * @param id the id of the newsItemDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the newsItemDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<NewsItemDTO> getNewsItem(@PathVariable("id") Long id) {
        LOG.debug("REST request to get NewsItem : {}", id);
        Optional<NewsItemDTO> newsItemDTO = newsItemService.findOne(id);
        return ResponseUtil.wrapOrNotFound(newsItemDTO);
    }

    /**
     * {@code DELETE  /news-items/:id} : delete the "id" newsItem.
     *
     * @param id the id of the newsItemDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteNewsItem(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete NewsItem : {}", id);
        newsItemService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
