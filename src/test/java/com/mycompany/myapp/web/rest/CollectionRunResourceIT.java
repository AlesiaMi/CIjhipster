package com.mycompany.myapp.web.rest;

import static com.mycompany.myapp.domain.CollectionRunAsserts.*;
import static com.mycompany.myapp.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.CollectionRun;
import com.mycompany.myapp.domain.enumeration.RunStatus;
import com.mycompany.myapp.repository.CollectionRunRepository;
import com.mycompany.myapp.service.dto.CollectionRunDTO;
import com.mycompany.myapp.service.mapper.CollectionRunMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link CollectionRunResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class CollectionRunResourceIT {

    private static final Instant DEFAULT_STARTED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_STARTED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_FINISHED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_FINISHED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final RunStatus DEFAULT_STATUS = RunStatus.RUNNING;
    private static final RunStatus UPDATED_STATUS = RunStatus.SUCCESS;

    private static final Integer DEFAULT_FOUND_COUNT = 1;
    private static final Integer UPDATED_FOUND_COUNT = 2;
    private static final Integer SMALLER_FOUND_COUNT = 1 - 1;

    private static final Integer DEFAULT_PROCESSED_COUNT = 1;
    private static final Integer UPDATED_PROCESSED_COUNT = 2;
    private static final Integer SMALLER_PROCESSED_COUNT = 1 - 1;

    private static final String DEFAULT_ERROR_MESSAGE = "AAAAAAAAAA";
    private static final String UPDATED_ERROR_MESSAGE = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/collection-runs";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private CollectionRunRepository collectionRunRepository;

    @Autowired
    private CollectionRunMapper collectionRunMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCollectionRunMockMvc;

    private CollectionRun collectionRun;

    private CollectionRun insertedCollectionRun;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CollectionRun createEntity() {
        return new CollectionRun()
            .startedAt(DEFAULT_STARTED_AT)
            .finishedAt(DEFAULT_FINISHED_AT)
            .status(DEFAULT_STATUS)
            .foundCount(DEFAULT_FOUND_COUNT)
            .processedCount(DEFAULT_PROCESSED_COUNT)
            .errorMessage(DEFAULT_ERROR_MESSAGE);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CollectionRun createUpdatedEntity() {
        return new CollectionRun()
            .startedAt(UPDATED_STARTED_AT)
            .finishedAt(UPDATED_FINISHED_AT)
            .status(UPDATED_STATUS)
            .foundCount(UPDATED_FOUND_COUNT)
            .processedCount(UPDATED_PROCESSED_COUNT)
            .errorMessage(UPDATED_ERROR_MESSAGE);
    }

    @BeforeEach
    void initTest() {
        collectionRun = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedCollectionRun != null) {
            collectionRunRepository.delete(insertedCollectionRun);
            insertedCollectionRun = null;
        }
    }

    @Test
    @Transactional
    void createCollectionRun() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the CollectionRun
        CollectionRunDTO collectionRunDTO = collectionRunMapper.toDto(collectionRun);
        var returnedCollectionRunDTO = om.readValue(
            restCollectionRunMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(collectionRunDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            CollectionRunDTO.class
        );

        // Validate the CollectionRun in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedCollectionRun = collectionRunMapper.toEntity(returnedCollectionRunDTO);
        assertCollectionRunUpdatableFieldsEquals(returnedCollectionRun, getPersistedCollectionRun(returnedCollectionRun));

        insertedCollectionRun = returnedCollectionRun;
    }

    @Test
    @Transactional
    void createCollectionRunWithExistingId() throws Exception {
        // Create the CollectionRun with an existing ID
        collectionRun.setId(1L);
        CollectionRunDTO collectionRunDTO = collectionRunMapper.toDto(collectionRun);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restCollectionRunMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(collectionRunDTO)))
            .andExpect(status().isBadRequest());

        // Validate the CollectionRun in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkStartedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        collectionRun.setStartedAt(null);

        // Create the CollectionRun, which fails.
        CollectionRunDTO collectionRunDTO = collectionRunMapper.toDto(collectionRun);

        restCollectionRunMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(collectionRunDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        collectionRun.setStatus(null);

        // Create the CollectionRun, which fails.
        CollectionRunDTO collectionRunDTO = collectionRunMapper.toDto(collectionRun);

        restCollectionRunMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(collectionRunDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllCollectionRuns() throws Exception {
        // Initialize the database
        insertedCollectionRun = collectionRunRepository.saveAndFlush(collectionRun);

        // Get all the collectionRunList
        restCollectionRunMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(collectionRun.getId().intValue())))
            .andExpect(jsonPath("$.[*].startedAt").value(hasItem(DEFAULT_STARTED_AT.toString())))
            .andExpect(jsonPath("$.[*].finishedAt").value(hasItem(DEFAULT_FINISHED_AT.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].foundCount").value(hasItem(DEFAULT_FOUND_COUNT)))
            .andExpect(jsonPath("$.[*].processedCount").value(hasItem(DEFAULT_PROCESSED_COUNT)))
            .andExpect(jsonPath("$.[*].errorMessage").value(hasItem(DEFAULT_ERROR_MESSAGE)));
    }

    @Test
    @Transactional
    void getCollectionRun() throws Exception {
        // Initialize the database
        insertedCollectionRun = collectionRunRepository.saveAndFlush(collectionRun);

        // Get the collectionRun
        restCollectionRunMockMvc
            .perform(get(ENTITY_API_URL_ID, collectionRun.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(collectionRun.getId().intValue()))
            .andExpect(jsonPath("$.startedAt").value(DEFAULT_STARTED_AT.toString()))
            .andExpect(jsonPath("$.finishedAt").value(DEFAULT_FINISHED_AT.toString()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.foundCount").value(DEFAULT_FOUND_COUNT))
            .andExpect(jsonPath("$.processedCount").value(DEFAULT_PROCESSED_COUNT))
            .andExpect(jsonPath("$.errorMessage").value(DEFAULT_ERROR_MESSAGE));
    }

    @Test
    @Transactional
    void getCollectionRunsByIdFiltering() throws Exception {
        // Initialize the database
        insertedCollectionRun = collectionRunRepository.saveAndFlush(collectionRun);

        Long id = collectionRun.getId();

        defaultCollectionRunFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultCollectionRunFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultCollectionRunFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllCollectionRunsByStartedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCollectionRun = collectionRunRepository.saveAndFlush(collectionRun);

        // Get all the collectionRunList where startedAt equals to
        defaultCollectionRunFiltering("startedAt.equals=" + DEFAULT_STARTED_AT, "startedAt.equals=" + UPDATED_STARTED_AT);
    }

    @Test
    @Transactional
    void getAllCollectionRunsByStartedAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCollectionRun = collectionRunRepository.saveAndFlush(collectionRun);

        // Get all the collectionRunList where startedAt in
        defaultCollectionRunFiltering(
            "startedAt.in=" + DEFAULT_STARTED_AT + "," + UPDATED_STARTED_AT,
            "startedAt.in=" + UPDATED_STARTED_AT
        );
    }

    @Test
    @Transactional
    void getAllCollectionRunsByStartedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCollectionRun = collectionRunRepository.saveAndFlush(collectionRun);

        // Get all the collectionRunList where startedAt is not null
        defaultCollectionRunFiltering("startedAt.specified=true", "startedAt.specified=false");
    }

    @Test
    @Transactional
    void getAllCollectionRunsByFinishedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCollectionRun = collectionRunRepository.saveAndFlush(collectionRun);

        // Get all the collectionRunList where finishedAt equals to
        defaultCollectionRunFiltering("finishedAt.equals=" + DEFAULT_FINISHED_AT, "finishedAt.equals=" + UPDATED_FINISHED_AT);
    }

    @Test
    @Transactional
    void getAllCollectionRunsByFinishedAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCollectionRun = collectionRunRepository.saveAndFlush(collectionRun);

        // Get all the collectionRunList where finishedAt in
        defaultCollectionRunFiltering(
            "finishedAt.in=" + DEFAULT_FINISHED_AT + "," + UPDATED_FINISHED_AT,
            "finishedAt.in=" + UPDATED_FINISHED_AT
        );
    }

    @Test
    @Transactional
    void getAllCollectionRunsByFinishedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCollectionRun = collectionRunRepository.saveAndFlush(collectionRun);

        // Get all the collectionRunList where finishedAt is not null
        defaultCollectionRunFiltering("finishedAt.specified=true", "finishedAt.specified=false");
    }

    @Test
    @Transactional
    void getAllCollectionRunsByStatusIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCollectionRun = collectionRunRepository.saveAndFlush(collectionRun);

        // Get all the collectionRunList where status equals to
        defaultCollectionRunFiltering("status.equals=" + DEFAULT_STATUS, "status.equals=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllCollectionRunsByStatusIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCollectionRun = collectionRunRepository.saveAndFlush(collectionRun);

        // Get all the collectionRunList where status in
        defaultCollectionRunFiltering("status.in=" + DEFAULT_STATUS + "," + UPDATED_STATUS, "status.in=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllCollectionRunsByStatusIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCollectionRun = collectionRunRepository.saveAndFlush(collectionRun);

        // Get all the collectionRunList where status is not null
        defaultCollectionRunFiltering("status.specified=true", "status.specified=false");
    }

    @Test
    @Transactional
    void getAllCollectionRunsByFoundCountIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCollectionRun = collectionRunRepository.saveAndFlush(collectionRun);

        // Get all the collectionRunList where foundCount equals to
        defaultCollectionRunFiltering("foundCount.equals=" + DEFAULT_FOUND_COUNT, "foundCount.equals=" + UPDATED_FOUND_COUNT);
    }

    @Test
    @Transactional
    void getAllCollectionRunsByFoundCountIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCollectionRun = collectionRunRepository.saveAndFlush(collectionRun);

        // Get all the collectionRunList where foundCount in
        defaultCollectionRunFiltering(
            "foundCount.in=" + DEFAULT_FOUND_COUNT + "," + UPDATED_FOUND_COUNT,
            "foundCount.in=" + UPDATED_FOUND_COUNT
        );
    }

    @Test
    @Transactional
    void getAllCollectionRunsByFoundCountIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCollectionRun = collectionRunRepository.saveAndFlush(collectionRun);

        // Get all the collectionRunList where foundCount is not null
        defaultCollectionRunFiltering("foundCount.specified=true", "foundCount.specified=false");
    }

    @Test
    @Transactional
    void getAllCollectionRunsByFoundCountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedCollectionRun = collectionRunRepository.saveAndFlush(collectionRun);

        // Get all the collectionRunList where foundCount is greater than or equal to
        defaultCollectionRunFiltering(
            "foundCount.greaterThanOrEqual=" + DEFAULT_FOUND_COUNT,
            "foundCount.greaterThanOrEqual=" + UPDATED_FOUND_COUNT
        );
    }

    @Test
    @Transactional
    void getAllCollectionRunsByFoundCountIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedCollectionRun = collectionRunRepository.saveAndFlush(collectionRun);

        // Get all the collectionRunList where foundCount is less than or equal to
        defaultCollectionRunFiltering(
            "foundCount.lessThanOrEqual=" + DEFAULT_FOUND_COUNT,
            "foundCount.lessThanOrEqual=" + SMALLER_FOUND_COUNT
        );
    }

    @Test
    @Transactional
    void getAllCollectionRunsByFoundCountIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedCollectionRun = collectionRunRepository.saveAndFlush(collectionRun);

        // Get all the collectionRunList where foundCount is less than
        defaultCollectionRunFiltering("foundCount.lessThan=" + UPDATED_FOUND_COUNT, "foundCount.lessThan=" + DEFAULT_FOUND_COUNT);
    }

    @Test
    @Transactional
    void getAllCollectionRunsByFoundCountIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedCollectionRun = collectionRunRepository.saveAndFlush(collectionRun);

        // Get all the collectionRunList where foundCount is greater than
        defaultCollectionRunFiltering("foundCount.greaterThan=" + SMALLER_FOUND_COUNT, "foundCount.greaterThan=" + DEFAULT_FOUND_COUNT);
    }

    @Test
    @Transactional
    void getAllCollectionRunsByProcessedCountIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCollectionRun = collectionRunRepository.saveAndFlush(collectionRun);

        // Get all the collectionRunList where processedCount equals to
        defaultCollectionRunFiltering(
            "processedCount.equals=" + DEFAULT_PROCESSED_COUNT,
            "processedCount.equals=" + UPDATED_PROCESSED_COUNT
        );
    }

    @Test
    @Transactional
    void getAllCollectionRunsByProcessedCountIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCollectionRun = collectionRunRepository.saveAndFlush(collectionRun);

        // Get all the collectionRunList where processedCount in
        defaultCollectionRunFiltering(
            "processedCount.in=" + DEFAULT_PROCESSED_COUNT + "," + UPDATED_PROCESSED_COUNT,
            "processedCount.in=" + UPDATED_PROCESSED_COUNT
        );
    }

    @Test
    @Transactional
    void getAllCollectionRunsByProcessedCountIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCollectionRun = collectionRunRepository.saveAndFlush(collectionRun);

        // Get all the collectionRunList where processedCount is not null
        defaultCollectionRunFiltering("processedCount.specified=true", "processedCount.specified=false");
    }

    @Test
    @Transactional
    void getAllCollectionRunsByProcessedCountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedCollectionRun = collectionRunRepository.saveAndFlush(collectionRun);

        // Get all the collectionRunList where processedCount is greater than or equal to
        defaultCollectionRunFiltering(
            "processedCount.greaterThanOrEqual=" + DEFAULT_PROCESSED_COUNT,
            "processedCount.greaterThanOrEqual=" + UPDATED_PROCESSED_COUNT
        );
    }

    @Test
    @Transactional
    void getAllCollectionRunsByProcessedCountIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedCollectionRun = collectionRunRepository.saveAndFlush(collectionRun);

        // Get all the collectionRunList where processedCount is less than or equal to
        defaultCollectionRunFiltering(
            "processedCount.lessThanOrEqual=" + DEFAULT_PROCESSED_COUNT,
            "processedCount.lessThanOrEqual=" + SMALLER_PROCESSED_COUNT
        );
    }

    @Test
    @Transactional
    void getAllCollectionRunsByProcessedCountIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedCollectionRun = collectionRunRepository.saveAndFlush(collectionRun);

        // Get all the collectionRunList where processedCount is less than
        defaultCollectionRunFiltering(
            "processedCount.lessThan=" + UPDATED_PROCESSED_COUNT,
            "processedCount.lessThan=" + DEFAULT_PROCESSED_COUNT
        );
    }

    @Test
    @Transactional
    void getAllCollectionRunsByProcessedCountIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedCollectionRun = collectionRunRepository.saveAndFlush(collectionRun);

        // Get all the collectionRunList where processedCount is greater than
        defaultCollectionRunFiltering(
            "processedCount.greaterThan=" + SMALLER_PROCESSED_COUNT,
            "processedCount.greaterThan=" + DEFAULT_PROCESSED_COUNT
        );
    }

    @Test
    @Transactional
    void getAllCollectionRunsByErrorMessageIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCollectionRun = collectionRunRepository.saveAndFlush(collectionRun);

        // Get all the collectionRunList where errorMessage equals to
        defaultCollectionRunFiltering("errorMessage.equals=" + DEFAULT_ERROR_MESSAGE, "errorMessage.equals=" + UPDATED_ERROR_MESSAGE);
    }

    @Test
    @Transactional
    void getAllCollectionRunsByErrorMessageIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCollectionRun = collectionRunRepository.saveAndFlush(collectionRun);

        // Get all the collectionRunList where errorMessage in
        defaultCollectionRunFiltering(
            "errorMessage.in=" + DEFAULT_ERROR_MESSAGE + "," + UPDATED_ERROR_MESSAGE,
            "errorMessage.in=" + UPDATED_ERROR_MESSAGE
        );
    }

    @Test
    @Transactional
    void getAllCollectionRunsByErrorMessageIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCollectionRun = collectionRunRepository.saveAndFlush(collectionRun);

        // Get all the collectionRunList where errorMessage is not null
        defaultCollectionRunFiltering("errorMessage.specified=true", "errorMessage.specified=false");
    }

    @Test
    @Transactional
    void getAllCollectionRunsByErrorMessageContainsSomething() throws Exception {
        // Initialize the database
        insertedCollectionRun = collectionRunRepository.saveAndFlush(collectionRun);

        // Get all the collectionRunList where errorMessage contains
        defaultCollectionRunFiltering("errorMessage.contains=" + DEFAULT_ERROR_MESSAGE, "errorMessage.contains=" + UPDATED_ERROR_MESSAGE);
    }

    @Test
    @Transactional
    void getAllCollectionRunsByErrorMessageNotContainsSomething() throws Exception {
        // Initialize the database
        insertedCollectionRun = collectionRunRepository.saveAndFlush(collectionRun);

        // Get all the collectionRunList where errorMessage does not contain
        defaultCollectionRunFiltering(
            "errorMessage.doesNotContain=" + UPDATED_ERROR_MESSAGE,
            "errorMessage.doesNotContain=" + DEFAULT_ERROR_MESSAGE
        );
    }

    private void defaultCollectionRunFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultCollectionRunShouldBeFound(shouldBeFound);
        defaultCollectionRunShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultCollectionRunShouldBeFound(String filter) throws Exception {
        restCollectionRunMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(collectionRun.getId().intValue())))
            .andExpect(jsonPath("$.[*].startedAt").value(hasItem(DEFAULT_STARTED_AT.toString())))
            .andExpect(jsonPath("$.[*].finishedAt").value(hasItem(DEFAULT_FINISHED_AT.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].foundCount").value(hasItem(DEFAULT_FOUND_COUNT)))
            .andExpect(jsonPath("$.[*].processedCount").value(hasItem(DEFAULT_PROCESSED_COUNT)))
            .andExpect(jsonPath("$.[*].errorMessage").value(hasItem(DEFAULT_ERROR_MESSAGE)));

        // Check, that the count call also returns 1
        restCollectionRunMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultCollectionRunShouldNotBeFound(String filter) throws Exception {
        restCollectionRunMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restCollectionRunMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingCollectionRun() throws Exception {
        // Get the collectionRun
        restCollectionRunMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingCollectionRun() throws Exception {
        // Initialize the database
        insertedCollectionRun = collectionRunRepository.saveAndFlush(collectionRun);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the collectionRun
        CollectionRun updatedCollectionRun = collectionRunRepository.findById(collectionRun.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedCollectionRun are not directly saved in db
        em.detach(updatedCollectionRun);
        updatedCollectionRun
            .startedAt(UPDATED_STARTED_AT)
            .finishedAt(UPDATED_FINISHED_AT)
            .status(UPDATED_STATUS)
            .foundCount(UPDATED_FOUND_COUNT)
            .processedCount(UPDATED_PROCESSED_COUNT)
            .errorMessage(UPDATED_ERROR_MESSAGE);
        CollectionRunDTO collectionRunDTO = collectionRunMapper.toDto(updatedCollectionRun);

        restCollectionRunMockMvc
            .perform(
                put(ENTITY_API_URL_ID, collectionRunDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(collectionRunDTO))
            )
            .andExpect(status().isOk());

        // Validate the CollectionRun in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedCollectionRunToMatchAllProperties(updatedCollectionRun);
    }

    @Test
    @Transactional
    void putNonExistingCollectionRun() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        collectionRun.setId(longCount.incrementAndGet());

        // Create the CollectionRun
        CollectionRunDTO collectionRunDTO = collectionRunMapper.toDto(collectionRun);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCollectionRunMockMvc
            .perform(
                put(ENTITY_API_URL_ID, collectionRunDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(collectionRunDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CollectionRun in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchCollectionRun() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        collectionRun.setId(longCount.incrementAndGet());

        // Create the CollectionRun
        CollectionRunDTO collectionRunDTO = collectionRunMapper.toDto(collectionRun);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCollectionRunMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(collectionRunDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CollectionRun in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamCollectionRun() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        collectionRun.setId(longCount.incrementAndGet());

        // Create the CollectionRun
        CollectionRunDTO collectionRunDTO = collectionRunMapper.toDto(collectionRun);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCollectionRunMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(collectionRunDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the CollectionRun in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateCollectionRunWithPatch() throws Exception {
        // Initialize the database
        insertedCollectionRun = collectionRunRepository.saveAndFlush(collectionRun);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the collectionRun using partial update
        CollectionRun partialUpdatedCollectionRun = new CollectionRun();
        partialUpdatedCollectionRun.setId(collectionRun.getId());

        partialUpdatedCollectionRun.startedAt(UPDATED_STARTED_AT).finishedAt(UPDATED_FINISHED_AT);

        restCollectionRunMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCollectionRun.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedCollectionRun))
            )
            .andExpect(status().isOk());

        // Validate the CollectionRun in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCollectionRunUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedCollectionRun, collectionRun),
            getPersistedCollectionRun(collectionRun)
        );
    }

    @Test
    @Transactional
    void fullUpdateCollectionRunWithPatch() throws Exception {
        // Initialize the database
        insertedCollectionRun = collectionRunRepository.saveAndFlush(collectionRun);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the collectionRun using partial update
        CollectionRun partialUpdatedCollectionRun = new CollectionRun();
        partialUpdatedCollectionRun.setId(collectionRun.getId());

        partialUpdatedCollectionRun
            .startedAt(UPDATED_STARTED_AT)
            .finishedAt(UPDATED_FINISHED_AT)
            .status(UPDATED_STATUS)
            .foundCount(UPDATED_FOUND_COUNT)
            .processedCount(UPDATED_PROCESSED_COUNT)
            .errorMessage(UPDATED_ERROR_MESSAGE);

        restCollectionRunMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCollectionRun.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedCollectionRun))
            )
            .andExpect(status().isOk());

        // Validate the CollectionRun in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCollectionRunUpdatableFieldsEquals(partialUpdatedCollectionRun, getPersistedCollectionRun(partialUpdatedCollectionRun));
    }

    @Test
    @Transactional
    void patchNonExistingCollectionRun() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        collectionRun.setId(longCount.incrementAndGet());

        // Create the CollectionRun
        CollectionRunDTO collectionRunDTO = collectionRunMapper.toDto(collectionRun);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCollectionRunMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, collectionRunDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(collectionRunDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CollectionRun in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchCollectionRun() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        collectionRun.setId(longCount.incrementAndGet());

        // Create the CollectionRun
        CollectionRunDTO collectionRunDTO = collectionRunMapper.toDto(collectionRun);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCollectionRunMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(collectionRunDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CollectionRun in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamCollectionRun() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        collectionRun.setId(longCount.incrementAndGet());

        // Create the CollectionRun
        CollectionRunDTO collectionRunDTO = collectionRunMapper.toDto(collectionRun);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCollectionRunMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(collectionRunDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the CollectionRun in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteCollectionRun() throws Exception {
        // Initialize the database
        insertedCollectionRun = collectionRunRepository.saveAndFlush(collectionRun);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the collectionRun
        restCollectionRunMockMvc
            .perform(delete(ENTITY_API_URL_ID, collectionRun.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return collectionRunRepository.count();
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

    protected CollectionRun getPersistedCollectionRun(CollectionRun collectionRun) {
        return collectionRunRepository.findById(collectionRun.getId()).orElseThrow();
    }

    protected void assertPersistedCollectionRunToMatchAllProperties(CollectionRun expectedCollectionRun) {
        assertCollectionRunAllPropertiesEquals(expectedCollectionRun, getPersistedCollectionRun(expectedCollectionRun));
    }

    protected void assertPersistedCollectionRunToMatchUpdatableProperties(CollectionRun expectedCollectionRun) {
        assertCollectionRunAllUpdatablePropertiesEquals(expectedCollectionRun, getPersistedCollectionRun(expectedCollectionRun));
    }
}
