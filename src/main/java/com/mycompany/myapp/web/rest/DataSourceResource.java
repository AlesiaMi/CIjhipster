package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.security.AuthoritiesConstants;
import com.mycompany.myapp.security.SecurityUtils;
import com.mycompany.myapp.service.DataSourceQueryService;
import com.mycompany.myapp.service.DataSourceService;
import com.mycompany.myapp.service.ManagerAccessService;
import com.mycompany.myapp.service.criteria.DataSourceCriteria;
import com.mycompany.myapp.service.dto.DataSourceDTO;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.service.filter.LongFilter;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

@RestController
@RequestMapping("/api/data-sources")
public class DataSourceResource {

    private static final Logger LOG = LoggerFactory.getLogger(DataSourceResource.class);

    private static final String ENTITY_NAME = "dataSource";

    @Value("${jhipster.clientApp.name:cIjhipster}")
    private String applicationName;

    private final DataSourceService dataSourceService;

    private final DataSourceQueryService dataSourceQueryService;

    private final ManagerAccessService managerAccessService;

    public DataSourceResource(
        DataSourceService dataSourceService,
        DataSourceQueryService dataSourceQueryService,
        ManagerAccessService managerAccessService
    ) {
        this.dataSourceService = dataSourceService;

        this.dataSourceQueryService = dataSourceQueryService;

        this.managerAccessService = managerAccessService;
    }

    @PostMapping("")
    public ResponseEntity<DataSourceDTO> createDataSource(@Valid @RequestBody DataSourceDTO dataSourceDTO) throws URISyntaxException {
        LOG.debug("REST request to save DataSource : {}", dataSourceDTO);

        if (dataSourceDTO.getId() != null) {
            throw new BadRequestAlertException("A new dataSource cannot already have an ID", ENTITY_NAME, "idexists");
        }

        dataSourceDTO = dataSourceService.save(dataSourceDTO);

        return ResponseEntity.created(new URI("/api/data-sources/" + dataSourceDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, dataSourceDTO.getId().toString()))
            .body(dataSourceDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DataSourceDTO> updateDataSource(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody DataSourceDTO dataSourceDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update DataSource : {}, {}", id, dataSourceDTO);

        if (dataSourceDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }

        if (!Objects.equals(id, dataSourceDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        dataSourceDTO = dataSourceService.update(dataSourceDTO);

        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, dataSourceDTO.getId().toString()))
            .body(dataSourceDTO);
    }

    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<DataSourceDTO> partialUpdateDataSource(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody DataSourceDTO dataSourceDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update DataSource : {}, {}", id, dataSourceDTO);

        if (dataSourceDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }

        if (!Objects.equals(id, dataSourceDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        Optional<DataSourceDTO> result = dataSourceService.partialUpdate(dataSourceDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, dataSourceDTO.getId().toString())
        );
    }

    @GetMapping("")
    public ResponseEntity<List<DataSourceDTO>> getAllDataSources(
        DataSourceCriteria criteria,
        @RequestParam(name = "clientUserId", required = false) Long clientUserId,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get DataSources by criteria: {}", criteria);

        applyOwnerFilter(criteria, clientUserId);

        Page<DataSourceDTO> page = dataSourceQueryService.findByCriteria(criteria, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);

        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countDataSources(
        DataSourceCriteria criteria,
        @RequestParam(name = "clientUserId", required = false) Long clientUserId
    ) {
        LOG.debug("REST request to count DataSources by criteria: {}", criteria);

        applyOwnerFilter(criteria, clientUserId);

        return ResponseEntity.ok(dataSourceQueryService.countByCriteria(criteria));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DataSourceDTO> getDataSource(@PathVariable("id") Long id) {
        LOG.debug("REST request to get DataSource : {}", id);

        Optional<DataSourceDTO> dataSourceDTO = dataSourceService.findOne(id);

        return ResponseUtil.wrapOrNotFound(dataSourceDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDataSource(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete DataSource : {}", id);

        dataSourceService.delete(id);

        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    private void applyOwnerFilter(DataSourceCriteria criteria, Long clientUserId) {
        Long ownerId = resolveOwnerId(clientUserId);

        if (ownerId == null) {
            return;
        }

        LongFilter ownerFilter = new LongFilter();

        ownerFilter.setEquals(ownerId);

        // Never trust ownerId supplied by HTTP.
        criteria.setOwnerId(ownerFilter);
    }

    private Long resolveOwnerId(Long clientUserId) {
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
            // ADMIN without clientUserId keeps
            // existing global view.
            return clientUserId;
        }

        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.MANAGER)) {
            if (clientUserId == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "clientUserId is required for manager");
            }

            if (!managerAccessService.canView(clientUserId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Manager has no access to this client");
            }

            return clientUserId;
        }

        return SecurityUtils.getCurrentUserId().orElseThrow(() ->
            new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Current user id not found")
        );
    }
}
