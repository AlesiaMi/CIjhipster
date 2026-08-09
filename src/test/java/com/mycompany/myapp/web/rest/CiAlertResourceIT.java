package com.mycompany.myapp.web.rest;

import static com.mycompany.myapp.domain.CiAlertAsserts.*;
import static com.mycompany.myapp.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.AnalysisResult;
import com.mycompany.myapp.domain.AnalystProfile;
import com.mycompany.myapp.domain.CiAlert;
import com.mycompany.myapp.domain.enumeration.AlertStatus;
import com.mycompany.myapp.domain.enumeration.Severity;
import com.mycompany.myapp.repository.CiAlertRepository;
import com.mycompany.myapp.service.CiAlertService;
import com.mycompany.myapp.service.dto.CiAlertDTO;
import com.mycompany.myapp.service.mapper.CiAlertMapper;
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
 * Integration tests for the {@link CiAlertResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class CiAlertResourceIT {

    private static final String DEFAULT_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBB";

    private static final String DEFAULT_MESSAGE = "AAAAAAAAAA";
    private static final String UPDATED_MESSAGE = "BBBBBBBBBB";

    private static final Severity DEFAULT_SEVERITY = Severity.LOW;
    private static final Severity UPDATED_SEVERITY = Severity.MEDIUM;

    private static final AlertStatus DEFAULT_STATUS = AlertStatus.NEW;
    private static final AlertStatus UPDATED_STATUS = AlertStatus.READ;

    private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_READ_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_READ_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/ci-alerts";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private CiAlertRepository ciAlertRepository;

    @Mock
    private CiAlertRepository ciAlertRepositoryMock;

    @Autowired
    private CiAlertMapper ciAlertMapper;

    @Mock
    private CiAlertService ciAlertServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCiAlertMockMvc;

    private CiAlert ciAlert;

    private CiAlert insertedCiAlert;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CiAlert createEntity(EntityManager em) {
        CiAlert ciAlert = new CiAlert()
            .title(DEFAULT_TITLE)
            .message(DEFAULT_MESSAGE)
            .severity(DEFAULT_SEVERITY)
            .status(DEFAULT_STATUS)
            .createdAt(DEFAULT_CREATED_AT)
            .readAt(DEFAULT_READ_AT);
        // Add required entity
        AnalysisResult analysisResult;
        if (TestUtil.findAll(em, AnalysisResult.class).isEmpty()) {
            analysisResult = AnalysisResultResourceIT.createEntity(em);
            em.persist(analysisResult);
            em.flush();
        } else {
            analysisResult = TestUtil.findAll(em, AnalysisResult.class).get(0);
        }
        ciAlert.setAnalysisResult(analysisResult);
        // Add required entity
        AnalystProfile analystProfile;
        if (TestUtil.findAll(em, AnalystProfile.class).isEmpty()) {
            analystProfile = AnalystProfileResourceIT.createEntity(em);
            em.persist(analystProfile);
            em.flush();
        } else {
            analystProfile = TestUtil.findAll(em, AnalystProfile.class).get(0);
        }
        ciAlert.setAnalystProfile(analystProfile);
        return ciAlert;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CiAlert createUpdatedEntity(EntityManager em) {
        CiAlert updatedCiAlert = new CiAlert()
            .title(UPDATED_TITLE)
            .message(UPDATED_MESSAGE)
            .severity(UPDATED_SEVERITY)
            .status(UPDATED_STATUS)
            .createdAt(UPDATED_CREATED_AT)
            .readAt(UPDATED_READ_AT);
        // Add required entity
        AnalysisResult analysisResult;
        if (TestUtil.findAll(em, AnalysisResult.class).isEmpty()) {
            analysisResult = AnalysisResultResourceIT.createUpdatedEntity(em);
            em.persist(analysisResult);
            em.flush();
        } else {
            analysisResult = TestUtil.findAll(em, AnalysisResult.class).get(0);
        }
        updatedCiAlert.setAnalysisResult(analysisResult);
        // Add required entity
        AnalystProfile analystProfile;
        if (TestUtil.findAll(em, AnalystProfile.class).isEmpty()) {
            analystProfile = AnalystProfileResourceIT.createUpdatedEntity(em);
            em.persist(analystProfile);
            em.flush();
        } else {
            analystProfile = TestUtil.findAll(em, AnalystProfile.class).get(0);
        }
        updatedCiAlert.setAnalystProfile(analystProfile);
        return updatedCiAlert;
    }

    @BeforeEach
    void initTest() {
        ciAlert = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedCiAlert != null) {
            ciAlertRepository.delete(insertedCiAlert);
            insertedCiAlert = null;
        }
    }

    @Test
    @Transactional
    void createCiAlert() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the CiAlert
        CiAlertDTO ciAlertDTO = ciAlertMapper.toDto(ciAlert);
        var returnedCiAlertDTO = om.readValue(
            restCiAlertMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(ciAlertDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            CiAlertDTO.class
        );

        // Validate the CiAlert in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedCiAlert = ciAlertMapper.toEntity(returnedCiAlertDTO);
        assertCiAlertUpdatableFieldsEquals(returnedCiAlert, getPersistedCiAlert(returnedCiAlert));

        insertedCiAlert = returnedCiAlert;
    }

    @Test
    @Transactional
    void createCiAlertWithExistingId() throws Exception {
        // Create the CiAlert with an existing ID
        ciAlert.setId(1L);
        CiAlertDTO ciAlertDTO = ciAlertMapper.toDto(ciAlert);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restCiAlertMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(ciAlertDTO)))
            .andExpect(status().isBadRequest());

        // Validate the CiAlert in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkTitleIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        ciAlert.setTitle(null);

        // Create the CiAlert, which fails.
        CiAlertDTO ciAlertDTO = ciAlertMapper.toDto(ciAlert);

        restCiAlertMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(ciAlertDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkMessageIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        ciAlert.setMessage(null);

        // Create the CiAlert, which fails.
        CiAlertDTO ciAlertDTO = ciAlertMapper.toDto(ciAlert);

        restCiAlertMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(ciAlertDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkSeverityIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        ciAlert.setSeverity(null);

        // Create the CiAlert, which fails.
        CiAlertDTO ciAlertDTO = ciAlertMapper.toDto(ciAlert);

        restCiAlertMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(ciAlertDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        ciAlert.setStatus(null);

        // Create the CiAlert, which fails.
        CiAlertDTO ciAlertDTO = ciAlertMapper.toDto(ciAlert);

        restCiAlertMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(ciAlertDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCreatedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        ciAlert.setCreatedAt(null);

        // Create the CiAlert, which fails.
        CiAlertDTO ciAlertDTO = ciAlertMapper.toDto(ciAlert);

        restCiAlertMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(ciAlertDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllCiAlerts() throws Exception {
        // Initialize the database
        insertedCiAlert = ciAlertRepository.saveAndFlush(ciAlert);

        // Get all the ciAlertList
        restCiAlertMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(ciAlert.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].message").value(hasItem(DEFAULT_MESSAGE)))
            .andExpect(jsonPath("$.[*].severity").value(hasItem(DEFAULT_SEVERITY.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())))
            .andExpect(jsonPath("$.[*].readAt").value(hasItem(DEFAULT_READ_AT.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllCiAlertsWithEagerRelationshipsIsEnabled() throws Exception {
        when(ciAlertServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restCiAlertMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(ciAlertServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllCiAlertsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(ciAlertServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restCiAlertMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(ciAlertRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getCiAlert() throws Exception {
        // Initialize the database
        insertedCiAlert = ciAlertRepository.saveAndFlush(ciAlert);

        // Get the ciAlert
        restCiAlertMockMvc
            .perform(get(ENTITY_API_URL_ID, ciAlert.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(ciAlert.getId().intValue()))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE))
            .andExpect(jsonPath("$.message").value(DEFAULT_MESSAGE))
            .andExpect(jsonPath("$.severity").value(DEFAULT_SEVERITY.toString()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.createdAt").value(DEFAULT_CREATED_AT.toString()))
            .andExpect(jsonPath("$.readAt").value(DEFAULT_READ_AT.toString()));
    }

    @Test
    @Transactional
    void getCiAlertsByIdFiltering() throws Exception {
        // Initialize the database
        insertedCiAlert = ciAlertRepository.saveAndFlush(ciAlert);

        Long id = ciAlert.getId();

        defaultCiAlertFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultCiAlertFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultCiAlertFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllCiAlertsByTitleIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCiAlert = ciAlertRepository.saveAndFlush(ciAlert);

        // Get all the ciAlertList where title equals to
        defaultCiAlertFiltering("title.equals=" + DEFAULT_TITLE, "title.equals=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    void getAllCiAlertsByTitleIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCiAlert = ciAlertRepository.saveAndFlush(ciAlert);

        // Get all the ciAlertList where title in
        defaultCiAlertFiltering("title.in=" + DEFAULT_TITLE + "," + UPDATED_TITLE, "title.in=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    void getAllCiAlertsByTitleIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCiAlert = ciAlertRepository.saveAndFlush(ciAlert);

        // Get all the ciAlertList where title is not null
        defaultCiAlertFiltering("title.specified=true", "title.specified=false");
    }

    @Test
    @Transactional
    void getAllCiAlertsByTitleContainsSomething() throws Exception {
        // Initialize the database
        insertedCiAlert = ciAlertRepository.saveAndFlush(ciAlert);

        // Get all the ciAlertList where title contains
        defaultCiAlertFiltering("title.contains=" + DEFAULT_TITLE, "title.contains=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    void getAllCiAlertsByTitleNotContainsSomething() throws Exception {
        // Initialize the database
        insertedCiAlert = ciAlertRepository.saveAndFlush(ciAlert);

        // Get all the ciAlertList where title does not contain
        defaultCiAlertFiltering("title.doesNotContain=" + UPDATED_TITLE, "title.doesNotContain=" + DEFAULT_TITLE);
    }

    @Test
    @Transactional
    void getAllCiAlertsByMessageIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCiAlert = ciAlertRepository.saveAndFlush(ciAlert);

        // Get all the ciAlertList where message equals to
        defaultCiAlertFiltering("message.equals=" + DEFAULT_MESSAGE, "message.equals=" + UPDATED_MESSAGE);
    }

    @Test
    @Transactional
    void getAllCiAlertsByMessageIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCiAlert = ciAlertRepository.saveAndFlush(ciAlert);

        // Get all the ciAlertList where message in
        defaultCiAlertFiltering("message.in=" + DEFAULT_MESSAGE + "," + UPDATED_MESSAGE, "message.in=" + UPDATED_MESSAGE);
    }

    @Test
    @Transactional
    void getAllCiAlertsByMessageIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCiAlert = ciAlertRepository.saveAndFlush(ciAlert);

        // Get all the ciAlertList where message is not null
        defaultCiAlertFiltering("message.specified=true", "message.specified=false");
    }

    @Test
    @Transactional
    void getAllCiAlertsByMessageContainsSomething() throws Exception {
        // Initialize the database
        insertedCiAlert = ciAlertRepository.saveAndFlush(ciAlert);

        // Get all the ciAlertList where message contains
        defaultCiAlertFiltering("message.contains=" + DEFAULT_MESSAGE, "message.contains=" + UPDATED_MESSAGE);
    }

    @Test
    @Transactional
    void getAllCiAlertsByMessageNotContainsSomething() throws Exception {
        // Initialize the database
        insertedCiAlert = ciAlertRepository.saveAndFlush(ciAlert);

        // Get all the ciAlertList where message does not contain
        defaultCiAlertFiltering("message.doesNotContain=" + UPDATED_MESSAGE, "message.doesNotContain=" + DEFAULT_MESSAGE);
    }

    @Test
    @Transactional
    void getAllCiAlertsBySeverityIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCiAlert = ciAlertRepository.saveAndFlush(ciAlert);

        // Get all the ciAlertList where severity equals to
        defaultCiAlertFiltering("severity.equals=" + DEFAULT_SEVERITY, "severity.equals=" + UPDATED_SEVERITY);
    }

    @Test
    @Transactional
    void getAllCiAlertsBySeverityIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCiAlert = ciAlertRepository.saveAndFlush(ciAlert);

        // Get all the ciAlertList where severity in
        defaultCiAlertFiltering("severity.in=" + DEFAULT_SEVERITY + "," + UPDATED_SEVERITY, "severity.in=" + UPDATED_SEVERITY);
    }

    @Test
    @Transactional
    void getAllCiAlertsBySeverityIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCiAlert = ciAlertRepository.saveAndFlush(ciAlert);

        // Get all the ciAlertList where severity is not null
        defaultCiAlertFiltering("severity.specified=true", "severity.specified=false");
    }

    @Test
    @Transactional
    void getAllCiAlertsByStatusIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCiAlert = ciAlertRepository.saveAndFlush(ciAlert);

        // Get all the ciAlertList where status equals to
        defaultCiAlertFiltering("status.equals=" + DEFAULT_STATUS, "status.equals=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllCiAlertsByStatusIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCiAlert = ciAlertRepository.saveAndFlush(ciAlert);

        // Get all the ciAlertList where status in
        defaultCiAlertFiltering("status.in=" + DEFAULT_STATUS + "," + UPDATED_STATUS, "status.in=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllCiAlertsByStatusIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCiAlert = ciAlertRepository.saveAndFlush(ciAlert);

        // Get all the ciAlertList where status is not null
        defaultCiAlertFiltering("status.specified=true", "status.specified=false");
    }

    @Test
    @Transactional
    void getAllCiAlertsByCreatedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCiAlert = ciAlertRepository.saveAndFlush(ciAlert);

        // Get all the ciAlertList where createdAt equals to
        defaultCiAlertFiltering("createdAt.equals=" + DEFAULT_CREATED_AT, "createdAt.equals=" + UPDATED_CREATED_AT);
    }

    @Test
    @Transactional
    void getAllCiAlertsByCreatedAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCiAlert = ciAlertRepository.saveAndFlush(ciAlert);

        // Get all the ciAlertList where createdAt in
        defaultCiAlertFiltering("createdAt.in=" + DEFAULT_CREATED_AT + "," + UPDATED_CREATED_AT, "createdAt.in=" + UPDATED_CREATED_AT);
    }

    @Test
    @Transactional
    void getAllCiAlertsByCreatedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCiAlert = ciAlertRepository.saveAndFlush(ciAlert);

        // Get all the ciAlertList where createdAt is not null
        defaultCiAlertFiltering("createdAt.specified=true", "createdAt.specified=false");
    }

    @Test
    @Transactional
    void getAllCiAlertsByReadAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCiAlert = ciAlertRepository.saveAndFlush(ciAlert);

        // Get all the ciAlertList where readAt equals to
        defaultCiAlertFiltering("readAt.equals=" + DEFAULT_READ_AT, "readAt.equals=" + UPDATED_READ_AT);
    }

    @Test
    @Transactional
    void getAllCiAlertsByReadAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCiAlert = ciAlertRepository.saveAndFlush(ciAlert);

        // Get all the ciAlertList where readAt in
        defaultCiAlertFiltering("readAt.in=" + DEFAULT_READ_AT + "," + UPDATED_READ_AT, "readAt.in=" + UPDATED_READ_AT);
    }

    @Test
    @Transactional
    void getAllCiAlertsByReadAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCiAlert = ciAlertRepository.saveAndFlush(ciAlert);

        // Get all the ciAlertList where readAt is not null
        defaultCiAlertFiltering("readAt.specified=true", "readAt.specified=false");
    }

    @Test
    @Transactional
    void getAllCiAlertsByAnalysisResultIsEqualToSomething() throws Exception {
        AnalysisResult analysisResult;
        if (TestUtil.findAll(em, AnalysisResult.class).isEmpty()) {
            ciAlertRepository.saveAndFlush(ciAlert);
            analysisResult = AnalysisResultResourceIT.createEntity(em);
        } else {
            analysisResult = TestUtil.findAll(em, AnalysisResult.class).get(0);
        }
        em.persist(analysisResult);
        em.flush();
        ciAlert.setAnalysisResult(analysisResult);
        ciAlertRepository.saveAndFlush(ciAlert);
        Long analysisResultId = analysisResult.getId();
        // Get all the ciAlertList where analysisResult equals to analysisResultId
        defaultCiAlertShouldBeFound("analysisResultId.equals=" + analysisResultId);

        // Get all the ciAlertList where analysisResult equals to (analysisResultId + 1)
        defaultCiAlertShouldNotBeFound("analysisResultId.equals=" + (analysisResultId + 1));
    }

    @Test
    @Transactional
    void getAllCiAlertsByAnalystProfileIsEqualToSomething() throws Exception {
        AnalystProfile analystProfile;
        if (TestUtil.findAll(em, AnalystProfile.class).isEmpty()) {
            ciAlertRepository.saveAndFlush(ciAlert);
            analystProfile = AnalystProfileResourceIT.createEntity(em);
        } else {
            analystProfile = TestUtil.findAll(em, AnalystProfile.class).get(0);
        }
        em.persist(analystProfile);
        em.flush();
        ciAlert.setAnalystProfile(analystProfile);
        ciAlertRepository.saveAndFlush(ciAlert);
        Long analystProfileId = analystProfile.getId();
        // Get all the ciAlertList where analystProfile equals to analystProfileId
        defaultCiAlertShouldBeFound("analystProfileId.equals=" + analystProfileId);

        // Get all the ciAlertList where analystProfile equals to (analystProfileId + 1)
        defaultCiAlertShouldNotBeFound("analystProfileId.equals=" + (analystProfileId + 1));
    }

    private void defaultCiAlertFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultCiAlertShouldBeFound(shouldBeFound);
        defaultCiAlertShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultCiAlertShouldBeFound(String filter) throws Exception {
        restCiAlertMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(ciAlert.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].message").value(hasItem(DEFAULT_MESSAGE)))
            .andExpect(jsonPath("$.[*].severity").value(hasItem(DEFAULT_SEVERITY.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())))
            .andExpect(jsonPath("$.[*].readAt").value(hasItem(DEFAULT_READ_AT.toString())));

        // Check, that the count call also returns 1
        restCiAlertMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultCiAlertShouldNotBeFound(String filter) throws Exception {
        restCiAlertMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restCiAlertMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingCiAlert() throws Exception {
        // Get the ciAlert
        restCiAlertMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingCiAlert() throws Exception {
        // Initialize the database
        insertedCiAlert = ciAlertRepository.saveAndFlush(ciAlert);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the ciAlert
        CiAlert updatedCiAlert = ciAlertRepository.findById(ciAlert.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedCiAlert are not directly saved in db
        em.detach(updatedCiAlert);
        updatedCiAlert
            .title(UPDATED_TITLE)
            .message(UPDATED_MESSAGE)
            .severity(UPDATED_SEVERITY)
            .status(UPDATED_STATUS)
            .createdAt(UPDATED_CREATED_AT)
            .readAt(UPDATED_READ_AT);
        CiAlertDTO ciAlertDTO = ciAlertMapper.toDto(updatedCiAlert);

        restCiAlertMockMvc
            .perform(
                put(ENTITY_API_URL_ID, ciAlertDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(ciAlertDTO))
            )
            .andExpect(status().isOk());

        // Validate the CiAlert in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedCiAlertToMatchAllProperties(updatedCiAlert);
    }

    @Test
    @Transactional
    void putNonExistingCiAlert() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        ciAlert.setId(longCount.incrementAndGet());

        // Create the CiAlert
        CiAlertDTO ciAlertDTO = ciAlertMapper.toDto(ciAlert);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCiAlertMockMvc
            .perform(
                put(ENTITY_API_URL_ID, ciAlertDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(ciAlertDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CiAlert in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchCiAlert() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        ciAlert.setId(longCount.incrementAndGet());

        // Create the CiAlert
        CiAlertDTO ciAlertDTO = ciAlertMapper.toDto(ciAlert);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCiAlertMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(ciAlertDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CiAlert in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamCiAlert() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        ciAlert.setId(longCount.incrementAndGet());

        // Create the CiAlert
        CiAlertDTO ciAlertDTO = ciAlertMapper.toDto(ciAlert);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCiAlertMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(ciAlertDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the CiAlert in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateCiAlertWithPatch() throws Exception {
        // Initialize the database
        insertedCiAlert = ciAlertRepository.saveAndFlush(ciAlert);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the ciAlert using partial update
        CiAlert partialUpdatedCiAlert = new CiAlert();
        partialUpdatedCiAlert.setId(ciAlert.getId());

        restCiAlertMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCiAlert.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedCiAlert))
            )
            .andExpect(status().isOk());

        // Validate the CiAlert in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCiAlertUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedCiAlert, ciAlert), getPersistedCiAlert(ciAlert));
    }

    @Test
    @Transactional
    void fullUpdateCiAlertWithPatch() throws Exception {
        // Initialize the database
        insertedCiAlert = ciAlertRepository.saveAndFlush(ciAlert);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the ciAlert using partial update
        CiAlert partialUpdatedCiAlert = new CiAlert();
        partialUpdatedCiAlert.setId(ciAlert.getId());

        partialUpdatedCiAlert
            .title(UPDATED_TITLE)
            .message(UPDATED_MESSAGE)
            .severity(UPDATED_SEVERITY)
            .status(UPDATED_STATUS)
            .createdAt(UPDATED_CREATED_AT)
            .readAt(UPDATED_READ_AT);

        restCiAlertMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCiAlert.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedCiAlert))
            )
            .andExpect(status().isOk());

        // Validate the CiAlert in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCiAlertUpdatableFieldsEquals(partialUpdatedCiAlert, getPersistedCiAlert(partialUpdatedCiAlert));
    }

    @Test
    @Transactional
    void patchNonExistingCiAlert() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        ciAlert.setId(longCount.incrementAndGet());

        // Create the CiAlert
        CiAlertDTO ciAlertDTO = ciAlertMapper.toDto(ciAlert);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCiAlertMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, ciAlertDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(ciAlertDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CiAlert in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchCiAlert() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        ciAlert.setId(longCount.incrementAndGet());

        // Create the CiAlert
        CiAlertDTO ciAlertDTO = ciAlertMapper.toDto(ciAlert);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCiAlertMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(ciAlertDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CiAlert in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamCiAlert() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        ciAlert.setId(longCount.incrementAndGet());

        // Create the CiAlert
        CiAlertDTO ciAlertDTO = ciAlertMapper.toDto(ciAlert);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCiAlertMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(ciAlertDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the CiAlert in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteCiAlert() throws Exception {
        // Initialize the database
        insertedCiAlert = ciAlertRepository.saveAndFlush(ciAlert);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the ciAlert
        restCiAlertMockMvc
            .perform(delete(ENTITY_API_URL_ID, ciAlert.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return ciAlertRepository.count();
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

    protected CiAlert getPersistedCiAlert(CiAlert ciAlert) {
        return ciAlertRepository.findById(ciAlert.getId()).orElseThrow();
    }

    protected void assertPersistedCiAlertToMatchAllProperties(CiAlert expectedCiAlert) {
        assertCiAlertAllPropertiesEquals(expectedCiAlert, getPersistedCiAlert(expectedCiAlert));
    }

    protected void assertPersistedCiAlertToMatchUpdatableProperties(CiAlert expectedCiAlert) {
        assertCiAlertAllUpdatablePropertiesEquals(expectedCiAlert, getPersistedCiAlert(expectedCiAlert));
    }
}
