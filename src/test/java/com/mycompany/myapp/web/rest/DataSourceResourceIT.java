package com.mycompany.myapp.web.rest;

import static com.mycompany.myapp.domain.DataSourceAsserts.*;
import static com.mycompany.myapp.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.Competitor;
import com.mycompany.myapp.domain.DataSource;
import com.mycompany.myapp.domain.enumeration.SourceType;
import com.mycompany.myapp.repository.DataSourceRepository;
import com.mycompany.myapp.service.DataSourceService;
import com.mycompany.myapp.service.dto.DataSourceDTO;
import com.mycompany.myapp.service.mapper.DataSourceMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link DataSourceResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class DataSourceResourceIT {

    private static final String DEFAULT_SOURCE_NAME = "AAAAAAAAAA";
    private static final String UPDATED_SOURCE_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_URL = "AAAAAAAAAA";
    private static final String UPDATED_URL = "BBBBBBBBBB";

    private static final SourceType DEFAULT_SOURCE_TYPE = SourceType.RSS;
    private static final SourceType UPDATED_SOURCE_TYPE = SourceType.WEBSITE;

    private static final Boolean DEFAULT_IS_ACTIVE = false;
    private static final Boolean UPDATED_IS_ACTIVE = true;

    private static final Instant DEFAULT_LAST_CHECKED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_LAST_CHECKED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/data-sources";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private DataSourceRepository dataSourceRepository;

    @Mock
    private DataSourceRepository dataSourceRepositoryMock;

    @Autowired
    private DataSourceMapper dataSourceMapper;

    @Mock
    private DataSourceService dataSourceServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restDataSourceMockMvc;

    private DataSource dataSource;

    private DataSource insertedDataSource;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static DataSource createEntity(EntityManager em) {
        DataSource dataSource = new DataSource()
            .sourceName(DEFAULT_SOURCE_NAME)
            .url(DEFAULT_URL)
            .sourceType(DEFAULT_SOURCE_TYPE)
            .isActive(DEFAULT_IS_ACTIVE)
            .lastCheckedAt(DEFAULT_LAST_CHECKED_AT)
            .createdAt(DEFAULT_CREATED_AT);
        // Add required entity
        Competitor competitor;
        if (TestUtil.findAll(em, Competitor.class).isEmpty()) {
            competitor = CompetitorResourceIT.createEntity();
            em.persist(competitor);
            em.flush();
        } else {
            competitor = TestUtil.findAll(em, Competitor.class).get(0);
        }
        dataSource.setCompetitor(competitor);
        return dataSource;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static DataSource createUpdatedEntity(EntityManager em) {
        DataSource updatedDataSource = new DataSource()
            .sourceName(UPDATED_SOURCE_NAME)
            .url(UPDATED_URL)
            .sourceType(UPDATED_SOURCE_TYPE)
            .isActive(UPDATED_IS_ACTIVE)
            .lastCheckedAt(UPDATED_LAST_CHECKED_AT)
            .createdAt(UPDATED_CREATED_AT);
        // Add required entity
        Competitor competitor;
        if (TestUtil.findAll(em, Competitor.class).isEmpty()) {
            competitor = CompetitorResourceIT.createUpdatedEntity();
            em.persist(competitor);
            em.flush();
        } else {
            competitor = TestUtil.findAll(em, Competitor.class).get(0);
        }
        updatedDataSource.setCompetitor(competitor);
        return updatedDataSource;
    }

    @BeforeEach
    void initTest() {
        dataSource = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedDataSource != null) {
            dataSourceRepository.delete(insertedDataSource);
            insertedDataSource = null;
        }
    }

    @Test
    @Transactional
    void createDataSource() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the DataSource
        DataSourceDTO dataSourceDTO = dataSourceMapper.toDto(dataSource);
        var returnedDataSourceDTO = om.readValue(
            restDataSourceMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(dataSourceDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            DataSourceDTO.class
        );

        // Validate the DataSource in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedDataSource = dataSourceMapper.toEntity(returnedDataSourceDTO);
        assertDataSourceUpdatableFieldsEquals(returnedDataSource, getPersistedDataSource(returnedDataSource));

        insertedDataSource = returnedDataSource;
    }

    @Test
    @Transactional
    void createDataSourceWithExistingId() throws Exception {
        // Create the DataSource with an existing ID
        dataSource.setId(1L);
        DataSourceDTO dataSourceDTO = dataSourceMapper.toDto(dataSource);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restDataSourceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(dataSourceDTO)))
            .andExpect(status().isBadRequest());

        // Validate the DataSource in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkSourceNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        dataSource.setSourceName(null);

        // Create the DataSource, which fails.
        DataSourceDTO dataSourceDTO = dataSourceMapper.toDto(dataSource);

        restDataSourceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(dataSourceDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkUrlIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        dataSource.setUrl(null);

        // Create the DataSource, which fails.
        DataSourceDTO dataSourceDTO = dataSourceMapper.toDto(dataSource);

        restDataSourceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(dataSourceDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkSourceTypeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        dataSource.setSourceType(null);

        // Create the DataSource, which fails.
        DataSourceDTO dataSourceDTO = dataSourceMapper.toDto(dataSource);

        restDataSourceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(dataSourceDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkIsActiveIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        dataSource.setIsActive(null);

        // Create the DataSource, which fails.
        DataSourceDTO dataSourceDTO = dataSourceMapper.toDto(dataSource);

        restDataSourceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(dataSourceDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCreatedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        dataSource.setCreatedAt(null);

        // Create the DataSource, which fails.
        DataSourceDTO dataSourceDTO = dataSourceMapper.toDto(dataSource);

        restDataSourceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(dataSourceDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllDataSources() throws Exception {
        // Initialize the database
        insertedDataSource = dataSourceRepository.saveAndFlush(dataSource);

        // Get all the dataSourceList
        restDataSourceMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(dataSource.getId().intValue())))
            .andExpect(jsonPath("$.[*].sourceName").value(hasItem(DEFAULT_SOURCE_NAME)))
            .andExpect(jsonPath("$.[*].url").value(hasItem(DEFAULT_URL)))
            .andExpect(jsonPath("$.[*].sourceType").value(hasItem(DEFAULT_SOURCE_TYPE.toString())))
            .andExpect(jsonPath("$.[*].isActive").value(hasItem(DEFAULT_IS_ACTIVE)))
            .andExpect(jsonPath("$.[*].lastCheckedAt").value(hasItem(DEFAULT_LAST_CHECKED_AT.toString())))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllDataSourcesWithEagerRelationshipsIsEnabled() throws Exception {
        when(dataSourceServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restDataSourceMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(dataSourceServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllDataSourcesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(dataSourceServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restDataSourceMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(dataSourceRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getDataSource() throws Exception {
        // Initialize the database
        insertedDataSource = dataSourceRepository.saveAndFlush(dataSource);

        // Get the dataSource
        restDataSourceMockMvc
            .perform(get(ENTITY_API_URL_ID, dataSource.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(dataSource.getId().intValue()))
            .andExpect(jsonPath("$.sourceName").value(DEFAULT_SOURCE_NAME))
            .andExpect(jsonPath("$.url").value(DEFAULT_URL))
            .andExpect(jsonPath("$.sourceType").value(DEFAULT_SOURCE_TYPE.toString()))
            .andExpect(jsonPath("$.isActive").value(DEFAULT_IS_ACTIVE))
            .andExpect(jsonPath("$.lastCheckedAt").value(DEFAULT_LAST_CHECKED_AT.toString()))
            .andExpect(jsonPath("$.createdAt").value(DEFAULT_CREATED_AT.toString()));
    }

    @Test
    @Transactional
    void getDataSourcesByIdFiltering() throws Exception {
        // Initialize the database
        insertedDataSource = dataSourceRepository.saveAndFlush(dataSource);

        Long id = dataSource.getId();

        defaultDataSourceFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultDataSourceFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultDataSourceFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllDataSourcesBySourceNameIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedDataSource = dataSourceRepository.saveAndFlush(dataSource);

        // Get all the dataSourceList where sourceName equals to
        defaultDataSourceFiltering("sourceName.equals=" + DEFAULT_SOURCE_NAME, "sourceName.equals=" + UPDATED_SOURCE_NAME);
    }

    @Test
    @Transactional
    void getAllDataSourcesBySourceNameIsInShouldWork() throws Exception {
        // Initialize the database
        insertedDataSource = dataSourceRepository.saveAndFlush(dataSource);

        // Get all the dataSourceList where sourceName in
        defaultDataSourceFiltering(
            "sourceName.in=" + DEFAULT_SOURCE_NAME + "," + UPDATED_SOURCE_NAME,
            "sourceName.in=" + UPDATED_SOURCE_NAME
        );
    }

    @Test
    @Transactional
    void getAllDataSourcesBySourceNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedDataSource = dataSourceRepository.saveAndFlush(dataSource);

        // Get all the dataSourceList where sourceName is not null
        defaultDataSourceFiltering("sourceName.specified=true", "sourceName.specified=false");
    }

    @Test
    @Transactional
    void getAllDataSourcesBySourceNameContainsSomething() throws Exception {
        // Initialize the database
        insertedDataSource = dataSourceRepository.saveAndFlush(dataSource);

        // Get all the dataSourceList where sourceName contains
        defaultDataSourceFiltering("sourceName.contains=" + DEFAULT_SOURCE_NAME, "sourceName.contains=" + UPDATED_SOURCE_NAME);
    }

    @Test
    @Transactional
    void getAllDataSourcesBySourceNameNotContainsSomething() throws Exception {
        // Initialize the database
        insertedDataSource = dataSourceRepository.saveAndFlush(dataSource);

        // Get all the dataSourceList where sourceName does not contain
        defaultDataSourceFiltering("sourceName.doesNotContain=" + UPDATED_SOURCE_NAME, "sourceName.doesNotContain=" + DEFAULT_SOURCE_NAME);
    }

    @Test
    @Transactional
    void getAllDataSourcesByUrlIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedDataSource = dataSourceRepository.saveAndFlush(dataSource);

        // Get all the dataSourceList where url equals to
        defaultDataSourceFiltering("url.equals=" + DEFAULT_URL, "url.equals=" + UPDATED_URL);
    }

    @Test
    @Transactional
    void getAllDataSourcesByUrlIsInShouldWork() throws Exception {
        // Initialize the database
        insertedDataSource = dataSourceRepository.saveAndFlush(dataSource);

        // Get all the dataSourceList where url in
        defaultDataSourceFiltering("url.in=" + DEFAULT_URL + "," + UPDATED_URL, "url.in=" + UPDATED_URL);
    }

    @Test
    @Transactional
    void getAllDataSourcesByUrlIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedDataSource = dataSourceRepository.saveAndFlush(dataSource);

        // Get all the dataSourceList where url is not null
        defaultDataSourceFiltering("url.specified=true", "url.specified=false");
    }

    @Test
    @Transactional
    void getAllDataSourcesByUrlContainsSomething() throws Exception {
        // Initialize the database
        insertedDataSource = dataSourceRepository.saveAndFlush(dataSource);

        // Get all the dataSourceList where url contains
        defaultDataSourceFiltering("url.contains=" + DEFAULT_URL, "url.contains=" + UPDATED_URL);
    }

    @Test
    @Transactional
    void getAllDataSourcesByUrlNotContainsSomething() throws Exception {
        // Initialize the database
        insertedDataSource = dataSourceRepository.saveAndFlush(dataSource);

        // Get all the dataSourceList where url does not contain
        defaultDataSourceFiltering("url.doesNotContain=" + UPDATED_URL, "url.doesNotContain=" + DEFAULT_URL);
    }

    @Test
    @Transactional
    void getAllDataSourcesBySourceTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedDataSource = dataSourceRepository.saveAndFlush(dataSource);

        // Get all the dataSourceList where sourceType equals to
        defaultDataSourceFiltering("sourceType.equals=" + DEFAULT_SOURCE_TYPE, "sourceType.equals=" + UPDATED_SOURCE_TYPE);
    }

    @Test
    @Transactional
    void getAllDataSourcesBySourceTypeIsInShouldWork() throws Exception {
        // Initialize the database
        insertedDataSource = dataSourceRepository.saveAndFlush(dataSource);

        // Get all the dataSourceList where sourceType in
        defaultDataSourceFiltering(
            "sourceType.in=" + DEFAULT_SOURCE_TYPE + "," + UPDATED_SOURCE_TYPE,
            "sourceType.in=" + UPDATED_SOURCE_TYPE
        );
    }

    @Test
    @Transactional
    void getAllDataSourcesBySourceTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedDataSource = dataSourceRepository.saveAndFlush(dataSource);

        // Get all the dataSourceList where sourceType is not null
        defaultDataSourceFiltering("sourceType.specified=true", "sourceType.specified=false");
    }

    @Test
    @Transactional
    void getAllDataSourcesByIsActiveIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedDataSource = dataSourceRepository.saveAndFlush(dataSource);

        // Get all the dataSourceList where isActive equals to
        defaultDataSourceFiltering("isActive.equals=" + DEFAULT_IS_ACTIVE, "isActive.equals=" + UPDATED_IS_ACTIVE);
    }

    @Test
    @Transactional
    void getAllDataSourcesByIsActiveIsInShouldWork() throws Exception {
        // Initialize the database
        insertedDataSource = dataSourceRepository.saveAndFlush(dataSource);

        // Get all the dataSourceList where isActive in
        defaultDataSourceFiltering("isActive.in=" + DEFAULT_IS_ACTIVE + "," + UPDATED_IS_ACTIVE, "isActive.in=" + UPDATED_IS_ACTIVE);
    }

    @Test
    @Transactional
    void getAllDataSourcesByIsActiveIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedDataSource = dataSourceRepository.saveAndFlush(dataSource);

        // Get all the dataSourceList where isActive is not null
        defaultDataSourceFiltering("isActive.specified=true", "isActive.specified=false");
    }

    @Test
    @Transactional
    void getAllDataSourcesByLastCheckedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedDataSource = dataSourceRepository.saveAndFlush(dataSource);

        // Get all the dataSourceList where lastCheckedAt equals to
        defaultDataSourceFiltering("lastCheckedAt.equals=" + DEFAULT_LAST_CHECKED_AT, "lastCheckedAt.equals=" + UPDATED_LAST_CHECKED_AT);
    }

    @Test
    @Transactional
    void getAllDataSourcesByLastCheckedAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedDataSource = dataSourceRepository.saveAndFlush(dataSource);

        // Get all the dataSourceList where lastCheckedAt in
        defaultDataSourceFiltering(
            "lastCheckedAt.in=" + DEFAULT_LAST_CHECKED_AT + "," + UPDATED_LAST_CHECKED_AT,
            "lastCheckedAt.in=" + UPDATED_LAST_CHECKED_AT
        );
    }

    @Test
    @Transactional
    void getAllDataSourcesByLastCheckedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedDataSource = dataSourceRepository.saveAndFlush(dataSource);

        // Get all the dataSourceList where lastCheckedAt is not null
        defaultDataSourceFiltering("lastCheckedAt.specified=true", "lastCheckedAt.specified=false");
    }

    @Test
    @Transactional
    void getAllDataSourcesByCreatedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedDataSource = dataSourceRepository.saveAndFlush(dataSource);

        // Get all the dataSourceList where createdAt equals to
        defaultDataSourceFiltering("createdAt.equals=" + DEFAULT_CREATED_AT, "createdAt.equals=" + UPDATED_CREATED_AT);
    }

    @Test
    @Transactional
    void getAllDataSourcesByCreatedAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedDataSource = dataSourceRepository.saveAndFlush(dataSource);

        // Get all the dataSourceList where createdAt in
        defaultDataSourceFiltering("createdAt.in=" + DEFAULT_CREATED_AT + "," + UPDATED_CREATED_AT, "createdAt.in=" + UPDATED_CREATED_AT);
    }

    @Test
    @Transactional
    void getAllDataSourcesByCreatedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedDataSource = dataSourceRepository.saveAndFlush(dataSource);

        // Get all the dataSourceList where createdAt is not null
        defaultDataSourceFiltering("createdAt.specified=true", "createdAt.specified=false");
    }

    @Test
    @Transactional
    void getAllDataSourcesByCompetitorIsEqualToSomething() throws Exception {
        Competitor competitor;
        if (TestUtil.findAll(em, Competitor.class).isEmpty()) {
            dataSourceRepository.saveAndFlush(dataSource);
            competitor = CompetitorResourceIT.createEntity();
        } else {
            competitor = TestUtil.findAll(em, Competitor.class).get(0);
        }
        em.persist(competitor);
        em.flush();
        dataSource.setCompetitor(competitor);
        dataSourceRepository.saveAndFlush(dataSource);
        Long competitorId = competitor.getId();
        // Get all the dataSourceList where competitor equals to competitorId
        defaultDataSourceShouldBeFound("competitorId.equals=" + competitorId);

        // Get all the dataSourceList where competitor equals to (competitorId + 1)
        defaultDataSourceShouldNotBeFound("competitorId.equals=" + (competitorId + 1));
    }

    private void defaultDataSourceFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultDataSourceShouldBeFound(shouldBeFound);
        defaultDataSourceShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultDataSourceShouldBeFound(String filter) throws Exception {
        restDataSourceMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(dataSource.getId().intValue())))
            .andExpect(jsonPath("$.[*].sourceName").value(hasItem(DEFAULT_SOURCE_NAME)))
            .andExpect(jsonPath("$.[*].url").value(hasItem(DEFAULT_URL)))
            .andExpect(jsonPath("$.[*].sourceType").value(hasItem(DEFAULT_SOURCE_TYPE.toString())))
            .andExpect(jsonPath("$.[*].isActive").value(hasItem(DEFAULT_IS_ACTIVE)))
            .andExpect(jsonPath("$.[*].lastCheckedAt").value(hasItem(DEFAULT_LAST_CHECKED_AT.toString())))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())));

        // Check, that the count call also returns 1
        restDataSourceMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultDataSourceShouldNotBeFound(String filter) throws Exception {
        restDataSourceMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restDataSourceMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingDataSource() throws Exception {
        // Get the dataSource
        restDataSourceMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingDataSource() throws Exception {
        // Initialize the database
        insertedDataSource = dataSourceRepository.saveAndFlush(dataSource);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the dataSource
        DataSource updatedDataSource = dataSourceRepository.findById(dataSource.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedDataSource are not directly saved in db
        em.detach(updatedDataSource);
        updatedDataSource
            .sourceName(UPDATED_SOURCE_NAME)
            .url(UPDATED_URL)
            .sourceType(UPDATED_SOURCE_TYPE)
            .isActive(UPDATED_IS_ACTIVE)
            .lastCheckedAt(UPDATED_LAST_CHECKED_AT)
            .createdAt(UPDATED_CREATED_AT);
        DataSourceDTO dataSourceDTO = dataSourceMapper.toDto(updatedDataSource);

        restDataSourceMockMvc
            .perform(
                put(ENTITY_API_URL_ID, dataSourceDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(dataSourceDTO))
            )
            .andExpect(status().isOk());

        // Validate the DataSource in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedDataSourceToMatchAllProperties(updatedDataSource);
    }

    @Test
    @Transactional
    void putNonExistingDataSource() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        dataSource.setId(longCount.incrementAndGet());

        // Create the DataSource
        DataSourceDTO dataSourceDTO = dataSourceMapper.toDto(dataSource);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDataSourceMockMvc
            .perform(
                put(ENTITY_API_URL_ID, dataSourceDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(dataSourceDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the DataSource in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchDataSource() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        dataSource.setId(longCount.incrementAndGet());

        // Create the DataSource
        DataSourceDTO dataSourceDTO = dataSourceMapper.toDto(dataSource);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDataSourceMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(dataSourceDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the DataSource in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamDataSource() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        dataSource.setId(longCount.incrementAndGet());

        // Create the DataSource
        DataSourceDTO dataSourceDTO = dataSourceMapper.toDto(dataSource);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDataSourceMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(dataSourceDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the DataSource in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateDataSourceWithPatch() throws Exception {
        // Initialize the database
        insertedDataSource = dataSourceRepository.saveAndFlush(dataSource);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the dataSource using partial update
        DataSource partialUpdatedDataSource = new DataSource();
        partialUpdatedDataSource.setId(dataSource.getId());

        partialUpdatedDataSource.sourceType(UPDATED_SOURCE_TYPE).isActive(UPDATED_IS_ACTIVE).createdAt(UPDATED_CREATED_AT);

        restDataSourceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDataSource.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedDataSource))
            )
            .andExpect(status().isOk());

        // Validate the DataSource in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertDataSourceUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedDataSource, dataSource),
            getPersistedDataSource(dataSource)
        );
    }

    @Test
    @Transactional
    void fullUpdateDataSourceWithPatch() throws Exception {
        // Initialize the database
        insertedDataSource = dataSourceRepository.saveAndFlush(dataSource);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the dataSource using partial update
        DataSource partialUpdatedDataSource = new DataSource();
        partialUpdatedDataSource.setId(dataSource.getId());

        partialUpdatedDataSource
            .sourceName(UPDATED_SOURCE_NAME)
            .url(UPDATED_URL)
            .sourceType(UPDATED_SOURCE_TYPE)
            .isActive(UPDATED_IS_ACTIVE)
            .lastCheckedAt(UPDATED_LAST_CHECKED_AT)
            .createdAt(UPDATED_CREATED_AT);

        restDataSourceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDataSource.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedDataSource))
            )
            .andExpect(status().isOk());

        // Validate the DataSource in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertDataSourceUpdatableFieldsEquals(partialUpdatedDataSource, getPersistedDataSource(partialUpdatedDataSource));
    }

    @Test
    @Transactional
    void patchNonExistingDataSource() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        dataSource.setId(longCount.incrementAndGet());

        // Create the DataSource
        DataSourceDTO dataSourceDTO = dataSourceMapper.toDto(dataSource);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDataSourceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, dataSourceDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(dataSourceDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the DataSource in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchDataSource() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        dataSource.setId(longCount.incrementAndGet());

        // Create the DataSource
        DataSourceDTO dataSourceDTO = dataSourceMapper.toDto(dataSource);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDataSourceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(dataSourceDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the DataSource in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamDataSource() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        dataSource.setId(longCount.incrementAndGet());

        // Create the DataSource
        DataSourceDTO dataSourceDTO = dataSourceMapper.toDto(dataSource);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDataSourceMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(dataSourceDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the DataSource in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteDataSource() throws Exception {
        // Initialize the database
        insertedDataSource = dataSourceRepository.saveAndFlush(dataSource);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the dataSource
        restDataSourceMockMvc
            .perform(delete(ENTITY_API_URL_ID, dataSource.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return dataSourceRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected DataSource getPersistedDataSource(DataSource dataSource) {
        return dataSourceRepository.findById(dataSource.getId()).orElseThrow();
    }

    protected void assertPersistedDataSourceToMatchAllProperties(DataSource expectedDataSource) {
        assertDataSourceAllPropertiesEquals(expectedDataSource, getPersistedDataSource(expectedDataSource));
    }

    protected void assertPersistedDataSourceToMatchUpdatableProperties(DataSource expectedDataSource) {
        assertDataSourceAllUpdatablePropertiesEquals(expectedDataSource, getPersistedDataSource(expectedDataSource));
    }
}
