package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.repository.DataSourceRepository;
import com.mycompany.myapp.service.DataSourceQueryService;
import com.mycompany.myapp.service.DataSourceService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.mycompany.myapp.domain.DataSource}.
 */
@RestController
@RequestMapping("/api/data-sources")
public class DataSourceResource {

    private static final Logger LOG = LoggerFactory.getLogger(DataSourceResource.class);

    private static final String ENTITY_NAME = "dataSource";

    @Value("${jhipster.clientApp.name:cIjhipster}")
    private String applicationName;

    private final DataSourceService dataSourceService;

    private final DataSourceRepository dataSourceRepository;

    private final DataSourceQueryService dataSourceQueryService;

    public DataSourceResource(
        DataSourceService dataSourceService,
        DataSourceRepository dataSourceRepository,
        DataSourceQueryService dataSourceQueryService
    ) {
        this.dataSourceService = dataSourceService;
        this.dataSourceRepository = dataSourceRepository;
        this.dataSourceQueryService = dataSourceQueryService;
    }

    /**
     * {@code POST  /data-sources} : Create a new dataSource.
     *
     * @param dataSourceDTO the dataSourceDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new dataSourceDTO, or with status {@code 400 (Bad Request)} if the dataSource has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
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

    /**
     * {@code PUT  /data-sources/:id} : Updates an existing dataSource.
     *
     * @param id the id of the dataSourceDTO to save.
     * @param dataSourceDTO the dataSourceDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated dataSourceDTO,
     * or with status {@code 400 (Bad Request)} if the dataSourceDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the dataSourceDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
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

        if (!dataSourceRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        dataSourceDTO = dataSourceService.update(dataSourceDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, dataSourceDTO.getId().toString()))
            .body(dataSourceDTO);
    }

    /**
     * {@code PATCH  /data-sources/:id} : Partial updates given fields of an existing dataSource, field will ignore if it is null
     *
     * @param id the id of the dataSourceDTO to save.
     * @param dataSourceDTO the dataSourceDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated dataSourceDTO,
     * or with status {@code 400 (Bad Request)} if the dataSourceDTO is not valid,
     * or with status {@code 404 (Not Found)} if the dataSourceDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the dataSourceDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<DataSourceDTO> partialUpdateDataSource(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody DataSourceDTO dataSourceDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update DataSource partially : {}, {}", id, dataSourceDTO);
        if (dataSourceDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, dataSourceDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!dataSourceRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<DataSourceDTO> result = dataSourceService.partialUpdate(dataSourceDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, dataSourceDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /data-sources} : get all the Data Sources.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Data Sources in body.
     */
    @GetMapping("")
    public ResponseEntity<List<DataSourceDTO>> getAllDataSources(
        DataSourceCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get DataSources by criteria: {}", criteria);

        Page<DataSourceDTO> page = dataSourceQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /data-sources/count} : count all the dataSources.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countDataSources(DataSourceCriteria criteria) {
        LOG.debug("REST request to count DataSources by criteria: {}", criteria);
        return ResponseEntity.ok().body(dataSourceQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /data-sources/:id} : get the "id" dataSource.
     *
     * @param id the id of the dataSourceDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the dataSourceDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DataSourceDTO> getDataSource(@PathVariable("id") Long id) {
        LOG.debug("REST request to get DataSource : {}", id);
        Optional<DataSourceDTO> dataSourceDTO = dataSourceService.findOne(id);
        return ResponseUtil.wrapOrNotFound(dataSourceDTO);
    }

    /**
     * {@code DELETE  /data-sources/:id} : delete the "id" dataSource.
     *
     * @param id the id of the dataSourceDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteDataSource(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete DataSource : {}", id);
        dataSourceService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
