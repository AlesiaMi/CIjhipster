package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.domain.enumeration.ManagerPermissionType;
import com.mycompany.myapp.repository.NewsItemRepository;
import com.mycompany.myapp.security.AuthoritiesConstants;
import com.mycompany.myapp.security.SecurityUtils;
import com.mycompany.myapp.service.ManagerAccessService;
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
@RequestMapping("/api/news-items")
public class NewsItemResource {

    private static final Logger LOG = LoggerFactory.getLogger(NewsItemResource.class);
    private static final String ENTITY_NAME = "newsItem";

    @Value("${jhipster.clientApp.name:cIjhipster}")
    private String applicationName;

    private final NewsItemService newsItemService;
    private final NewsItemRepository newsItemRepository;
    private final NewsItemQueryService newsItemQueryService;
    private final ManagerAccessService managerAccessService;

    public NewsItemResource(
        NewsItemService newsItemService,
        NewsItemRepository newsItemRepository,
        NewsItemQueryService newsItemQueryService,
        ManagerAccessService managerAccessService
    ) {
        this.newsItemService = newsItemService;
        this.newsItemRepository = newsItemRepository;
        this.newsItemQueryService = newsItemQueryService;
        this.managerAccessService = managerAccessService;
    }

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

    @GetMapping("")
    public ResponseEntity<List<NewsItemDTO>> getAllNewsItems(
        NewsItemCriteria criteria,
        @RequestParam(required = false) Long clientUserId,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get NewsItems by criteria: {}", criteria);

        applyOwnerFilter(criteria, clientUserId);

        Page<NewsItemDTO> page = newsItemQueryService.findByCriteria(criteria, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);

        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countNewsItems(NewsItemCriteria criteria, @RequestParam(required = false) Long clientUserId) {
        LOG.debug("REST request to count NewsItems by criteria: {}", criteria);
        applyOwnerFilter(criteria, clientUserId);
        return ResponseEntity.ok().body(newsItemQueryService.countByCriteria(criteria));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NewsItemDTO> getNewsItem(@PathVariable("id") Long id) {
        LOG.debug("REST request to get NewsItem : {}", id);
        Optional<NewsItemDTO> newsItemDTO = newsItemService.findOne(id);
        return ResponseUtil.wrapOrNotFound(newsItemDTO);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteNewsItem(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete NewsItem : {}", id);
        newsItemService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    private void applyOwnerFilter(NewsItemCriteria criteria, Long clientUserId) {
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

            if (!managerAccessService.hasPermission(clientUserId, ManagerPermissionType.NEWS_VIEW)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "NEWS_VIEW permission is required");
            }

            return clientUserId;
        }

        return SecurityUtils.getCurrentUserId().orElseThrow(() ->
            new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Current user id not found")
        );
    }
}
