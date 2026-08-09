package com.mycompany.myapp.web.rest;

import static com.mycompany.myapp.domain.AnalysisResultAsserts.*;
import static com.mycompany.myapp.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.AnalysisResult;
import com.mycompany.myapp.domain.NewsItem;
import com.mycompany.myapp.domain.enumeration.AnalysisStatus;
import com.mycompany.myapp.domain.enumeration.Sentiment;
import com.mycompany.myapp.repository.AnalysisResultRepository;
import com.mycompany.myapp.service.AnalysisResultService;
import com.mycompany.myapp.service.dto.AnalysisResultDTO;
import com.mycompany.myapp.service.mapper.AnalysisResultMapper;
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
 * Integration tests for the {@link AnalysisResultResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class AnalysisResultResourceIT {

    private static final String DEFAULT_SUMMARY = "AAAAAAAAAA";
    private static final String UPDATED_SUMMARY = "BBBBBBBBBB";

    private static final Sentiment DEFAULT_SENTIMENT = Sentiment.POSITIVE;
    private static final Sentiment UPDATED_SENTIMENT = Sentiment.NEGATIVE;

    private static final String DEFAULT_TOPIC = "AAAAAAAAAA";
    private static final String UPDATED_TOPIC = "BBBBBBBBBB";

    private static final String DEFAULT_ENTITIES = "AAAAAAAAAA";
    private static final String UPDATED_ENTITIES = "BBBBBBBBBB";

    private static final String DEFAULT_RISK_SOURCE = "AAAAAAAAAA";
    private static final String UPDATED_RISK_SOURCE = "BBBBBBBBBB";

    private static final AnalysisStatus DEFAULT_STATUS = AnalysisStatus.PENDING;
    private static final AnalysisStatus UPDATED_STATUS = AnalysisStatus.SUCCESS;

    private static final String DEFAULT_MODEL_NAME = "AAAAAAAAAA";
    private static final String UPDATED_MODEL_NAME = "BBBBBBBBBB";

    private static final Instant DEFAULT_ANALYZED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_ANALYZED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_ERROR_MESSAGE = "AAAAAAAAAA";
    private static final String UPDATED_ERROR_MESSAGE = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/analysis-results";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private AnalysisResultRepository analysisResultRepository;

    @Mock
    private AnalysisResultRepository analysisResultRepositoryMock;

    @Autowired
    private AnalysisResultMapper analysisResultMapper;

    @Mock
    private AnalysisResultService analysisResultServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restAnalysisResultMockMvc;

    private AnalysisResult analysisResult;

    private AnalysisResult insertedAnalysisResult;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AnalysisResult createEntity(EntityManager em) {
        AnalysisResult analysisResult = new AnalysisResult()
            .summary(DEFAULT_SUMMARY)
            .sentiment(DEFAULT_SENTIMENT)
            .topic(DEFAULT_TOPIC)
            .entities(DEFAULT_ENTITIES)
            .riskSource(DEFAULT_RISK_SOURCE)
            .status(DEFAULT_STATUS)
            .modelName(DEFAULT_MODEL_NAME)
            .analyzedAt(DEFAULT_ANALYZED_AT)
            .errorMessage(DEFAULT_ERROR_MESSAGE);
        // Add required entity
        NewsItem newsItem;
        if (TestUtil.findAll(em, NewsItem.class).isEmpty()) {
            newsItem = NewsItemResourceIT.createEntity(em);
            em.persist(newsItem);
            em.flush();
        } else {
            newsItem = TestUtil.findAll(em, NewsItem.class).get(0);
        }
        analysisResult.setNewsItem(newsItem);
        return analysisResult;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AnalysisResult createUpdatedEntity(EntityManager em) {
        AnalysisResult updatedAnalysisResult = new AnalysisResult()
            .summary(UPDATED_SUMMARY)
            .sentiment(UPDATED_SENTIMENT)
            .topic(UPDATED_TOPIC)
            .entities(UPDATED_ENTITIES)
            .riskSource(UPDATED_RISK_SOURCE)
            .status(UPDATED_STATUS)
            .modelName(UPDATED_MODEL_NAME)
            .analyzedAt(UPDATED_ANALYZED_AT)
            .errorMessage(UPDATED_ERROR_MESSAGE);
        // Add required entity
        NewsItem newsItem;
        if (TestUtil.findAll(em, NewsItem.class).isEmpty()) {
            newsItem = NewsItemResourceIT.createUpdatedEntity(em);
            em.persist(newsItem);
            em.flush();
        } else {
            newsItem = TestUtil.findAll(em, NewsItem.class).get(0);
        }
        updatedAnalysisResult.setNewsItem(newsItem);
        return updatedAnalysisResult;
    }

    @BeforeEach
    void initTest() {
        analysisResult = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedAnalysisResult != null) {
            analysisResultRepository.delete(insertedAnalysisResult);
            insertedAnalysisResult = null;
        }
    }

    @Test
    @Transactional
    void createAnalysisResult() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the AnalysisResult
        AnalysisResultDTO analysisResultDTO = analysisResultMapper.toDto(analysisResult);
        var returnedAnalysisResultDTO = om.readValue(
            restAnalysisResultMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(analysisResultDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            AnalysisResultDTO.class
        );

        // Validate the AnalysisResult in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedAnalysisResult = analysisResultMapper.toEntity(returnedAnalysisResultDTO);
        assertAnalysisResultUpdatableFieldsEquals(returnedAnalysisResult, getPersistedAnalysisResult(returnedAnalysisResult));

        insertedAnalysisResult = returnedAnalysisResult;
    }

    @Test
    @Transactional
    void createAnalysisResultWithExistingId() throws Exception {
        // Create the AnalysisResult with an existing ID
        analysisResult.setId(1L);
        AnalysisResultDTO analysisResultDTO = analysisResultMapper.toDto(analysisResult);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restAnalysisResultMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(analysisResultDTO)))
            .andExpect(status().isBadRequest());

        // Validate the AnalysisResult in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkSentimentIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        analysisResult.setSentiment(null);

        // Create the AnalysisResult, which fails.
        AnalysisResultDTO analysisResultDTO = analysisResultMapper.toDto(analysisResult);

        restAnalysisResultMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(analysisResultDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        analysisResult.setStatus(null);

        // Create the AnalysisResult, which fails.
        AnalysisResultDTO analysisResultDTO = analysisResultMapper.toDto(analysisResult);

        restAnalysisResultMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(analysisResultDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllAnalysisResults() throws Exception {
        // Initialize the database
        insertedAnalysisResult = analysisResultRepository.saveAndFlush(analysisResult);

        // Get all the analysisResultList
        restAnalysisResultMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(analysisResult.getId().intValue())))
            .andExpect(jsonPath("$.[*].summary").value(hasItem(DEFAULT_SUMMARY)))
            .andExpect(jsonPath("$.[*].sentiment").value(hasItem(DEFAULT_SENTIMENT.toString())))
            .andExpect(jsonPath("$.[*].topic").value(hasItem(DEFAULT_TOPIC)))
            .andExpect(jsonPath("$.[*].entities").value(hasItem(DEFAULT_ENTITIES)))
            .andExpect(jsonPath("$.[*].riskSource").value(hasItem(DEFAULT_RISK_SOURCE)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].modelName").value(hasItem(DEFAULT_MODEL_NAME)))
            .andExpect(jsonPath("$.[*].analyzedAt").value(hasItem(DEFAULT_ANALYZED_AT.toString())))
            .andExpect(jsonPath("$.[*].errorMessage").value(hasItem(DEFAULT_ERROR_MESSAGE)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllAnalysisResultsWithEagerRelationshipsIsEnabled() throws Exception {
        when(analysisResultServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restAnalysisResultMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(analysisResultServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllAnalysisResultsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(analysisResultServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restAnalysisResultMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(analysisResultRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getAnalysisResult() throws Exception {
        // Initialize the database
        insertedAnalysisResult = analysisResultRepository.saveAndFlush(analysisResult);

        // Get the analysisResult
        restAnalysisResultMockMvc
            .perform(get(ENTITY_API_URL_ID, analysisResult.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(analysisResult.getId().intValue()))
            .andExpect(jsonPath("$.summary").value(DEFAULT_SUMMARY))
            .andExpect(jsonPath("$.sentiment").value(DEFAULT_SENTIMENT.toString()))
            .andExpect(jsonPath("$.topic").value(DEFAULT_TOPIC))
            .andExpect(jsonPath("$.entities").value(DEFAULT_ENTITIES))
            .andExpect(jsonPath("$.riskSource").value(DEFAULT_RISK_SOURCE))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.modelName").value(DEFAULT_MODEL_NAME))
            .andExpect(jsonPath("$.analyzedAt").value(DEFAULT_ANALYZED_AT.toString()))
            .andExpect(jsonPath("$.errorMessage").value(DEFAULT_ERROR_MESSAGE));
    }

    @Test
    @Transactional
    void getAnalysisResultsByIdFiltering() throws Exception {
        // Initialize the database
        insertedAnalysisResult = analysisResultRepository.saveAndFlush(analysisResult);

        Long id = analysisResult.getId();

        defaultAnalysisResultFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultAnalysisResultFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultAnalysisResultFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllAnalysisResultsBySummaryIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAnalysisResult = analysisResultRepository.saveAndFlush(analysisResult);

        // Get all the analysisResultList where summary equals to
        defaultAnalysisResultFiltering("summary.equals=" + DEFAULT_SUMMARY, "summary.equals=" + UPDATED_SUMMARY);
    }

    @Test
    @Transactional
    void getAllAnalysisResultsBySummaryIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAnalysisResult = analysisResultRepository.saveAndFlush(analysisResult);

        // Get all the analysisResultList where summary in
        defaultAnalysisResultFiltering("summary.in=" + DEFAULT_SUMMARY + "," + UPDATED_SUMMARY, "summary.in=" + UPDATED_SUMMARY);
    }

    @Test
    @Transactional
    void getAllAnalysisResultsBySummaryIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAnalysisResult = analysisResultRepository.saveAndFlush(analysisResult);

        // Get all the analysisResultList where summary is not null
        defaultAnalysisResultFiltering("summary.specified=true", "summary.specified=false");
    }

    @Test
    @Transactional
    void getAllAnalysisResultsBySummaryContainsSomething() throws Exception {
        // Initialize the database
        insertedAnalysisResult = analysisResultRepository.saveAndFlush(analysisResult);

        // Get all the analysisResultList where summary contains
        defaultAnalysisResultFiltering("summary.contains=" + DEFAULT_SUMMARY, "summary.contains=" + UPDATED_SUMMARY);
    }

    @Test
    @Transactional
    void getAllAnalysisResultsBySummaryNotContainsSomething() throws Exception {
        // Initialize the database
        insertedAnalysisResult = analysisResultRepository.saveAndFlush(analysisResult);

        // Get all the analysisResultList where summary does not contain
        defaultAnalysisResultFiltering("summary.doesNotContain=" + UPDATED_SUMMARY, "summary.doesNotContain=" + DEFAULT_SUMMARY);
    }

    @Test
    @Transactional
    void getAllAnalysisResultsBySentimentIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAnalysisResult = analysisResultRepository.saveAndFlush(analysisResult);

        // Get all the analysisResultList where sentiment equals to
        defaultAnalysisResultFiltering("sentiment.equals=" + DEFAULT_SENTIMENT, "sentiment.equals=" + UPDATED_SENTIMENT);
    }

    @Test
    @Transactional
    void getAllAnalysisResultsBySentimentIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAnalysisResult = analysisResultRepository.saveAndFlush(analysisResult);

        // Get all the analysisResultList where sentiment in
        defaultAnalysisResultFiltering("sentiment.in=" + DEFAULT_SENTIMENT + "," + UPDATED_SENTIMENT, "sentiment.in=" + UPDATED_SENTIMENT);
    }

    @Test
    @Transactional
    void getAllAnalysisResultsBySentimentIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAnalysisResult = analysisResultRepository.saveAndFlush(analysisResult);

        // Get all the analysisResultList where sentiment is not null
        defaultAnalysisResultFiltering("sentiment.specified=true", "sentiment.specified=false");
    }

    @Test
    @Transactional
    void getAllAnalysisResultsByTopicIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAnalysisResult = analysisResultRepository.saveAndFlush(analysisResult);

        // Get all the analysisResultList where topic equals to
        defaultAnalysisResultFiltering("topic.equals=" + DEFAULT_TOPIC, "topic.equals=" + UPDATED_TOPIC);
    }

    @Test
    @Transactional
    void getAllAnalysisResultsByTopicIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAnalysisResult = analysisResultRepository.saveAndFlush(analysisResult);

        // Get all the analysisResultList where topic in
        defaultAnalysisResultFiltering("topic.in=" + DEFAULT_TOPIC + "," + UPDATED_TOPIC, "topic.in=" + UPDATED_TOPIC);
    }

    @Test
    @Transactional
    void getAllAnalysisResultsByTopicIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAnalysisResult = analysisResultRepository.saveAndFlush(analysisResult);

        // Get all the analysisResultList where topic is not null
        defaultAnalysisResultFiltering("topic.specified=true", "topic.specified=false");
    }

    @Test
    @Transactional
    void getAllAnalysisResultsByTopicContainsSomething() throws Exception {
        // Initialize the database
        insertedAnalysisResult = analysisResultRepository.saveAndFlush(analysisResult);

        // Get all the analysisResultList where topic contains
        defaultAnalysisResultFiltering("topic.contains=" + DEFAULT_TOPIC, "topic.contains=" + UPDATED_TOPIC);
    }

    @Test
    @Transactional
    void getAllAnalysisResultsByTopicNotContainsSomething() throws Exception {
        // Initialize the database
        insertedAnalysisResult = analysisResultRepository.saveAndFlush(analysisResult);

        // Get all the analysisResultList where topic does not contain
        defaultAnalysisResultFiltering("topic.doesNotContain=" + UPDATED_TOPIC, "topic.doesNotContain=" + DEFAULT_TOPIC);
    }

    @Test
    @Transactional
    void getAllAnalysisResultsByEntitiesIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAnalysisResult = analysisResultRepository.saveAndFlush(analysisResult);

        // Get all the analysisResultList where entities equals to
        defaultAnalysisResultFiltering("entities.equals=" + DEFAULT_ENTITIES, "entities.equals=" + UPDATED_ENTITIES);
    }

    @Test
    @Transactional
    void getAllAnalysisResultsByEntitiesIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAnalysisResult = analysisResultRepository.saveAndFlush(analysisResult);

        // Get all the analysisResultList where entities in
        defaultAnalysisResultFiltering("entities.in=" + DEFAULT_ENTITIES + "," + UPDATED_ENTITIES, "entities.in=" + UPDATED_ENTITIES);
    }

    @Test
    @Transactional
    void getAllAnalysisResultsByEntitiesIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAnalysisResult = analysisResultRepository.saveAndFlush(analysisResult);

        // Get all the analysisResultList where entities is not null
        defaultAnalysisResultFiltering("entities.specified=true", "entities.specified=false");
    }

    @Test
    @Transactional
    void getAllAnalysisResultsByEntitiesContainsSomething() throws Exception {
        // Initialize the database
        insertedAnalysisResult = analysisResultRepository.saveAndFlush(analysisResult);

        // Get all the analysisResultList where entities contains
        defaultAnalysisResultFiltering("entities.contains=" + DEFAULT_ENTITIES, "entities.contains=" + UPDATED_ENTITIES);
    }

    @Test
    @Transactional
    void getAllAnalysisResultsByEntitiesNotContainsSomething() throws Exception {
        // Initialize the database
        insertedAnalysisResult = analysisResultRepository.saveAndFlush(analysisResult);

        // Get all the analysisResultList where entities does not contain
        defaultAnalysisResultFiltering("entities.doesNotContain=" + UPDATED_ENTITIES, "entities.doesNotContain=" + DEFAULT_ENTITIES);
    }

    @Test
    @Transactional
    void getAllAnalysisResultsByRiskSourceIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAnalysisResult = analysisResultRepository.saveAndFlush(analysisResult);

        // Get all the analysisResultList where riskSource equals to
        defaultAnalysisResultFiltering("riskSource.equals=" + DEFAULT_RISK_SOURCE, "riskSource.equals=" + UPDATED_RISK_SOURCE);
    }

    @Test
    @Transactional
    void getAllAnalysisResultsByRiskSourceIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAnalysisResult = analysisResultRepository.saveAndFlush(analysisResult);

        // Get all the analysisResultList where riskSource in
        defaultAnalysisResultFiltering(
            "riskSource.in=" + DEFAULT_RISK_SOURCE + "," + UPDATED_RISK_SOURCE,
            "riskSource.in=" + UPDATED_RISK_SOURCE
        );
    }

    @Test
    @Transactional
    void getAllAnalysisResultsByRiskSourceIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAnalysisResult = analysisResultRepository.saveAndFlush(analysisResult);

        // Get all the analysisResultList where riskSource is not null
        defaultAnalysisResultFiltering("riskSource.specified=true", "riskSource.specified=false");
    }

    @Test
    @Transactional
    void getAllAnalysisResultsByRiskSourceContainsSomething() throws Exception {
        // Initialize the database
        insertedAnalysisResult = analysisResultRepository.saveAndFlush(analysisResult);

        // Get all the analysisResultList where riskSource contains
        defaultAnalysisResultFiltering("riskSource.contains=" + DEFAULT_RISK_SOURCE, "riskSource.contains=" + UPDATED_RISK_SOURCE);
    }

    @Test
    @Transactional
    void getAllAnalysisResultsByRiskSourceNotContainsSomething() throws Exception {
        // Initialize the database
        insertedAnalysisResult = analysisResultRepository.saveAndFlush(analysisResult);

        // Get all the analysisResultList where riskSource does not contain
        defaultAnalysisResultFiltering(
            "riskSource.doesNotContain=" + UPDATED_RISK_SOURCE,
            "riskSource.doesNotContain=" + DEFAULT_RISK_SOURCE
        );
    }

    @Test
    @Transactional
    void getAllAnalysisResultsByStatusIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAnalysisResult = analysisResultRepository.saveAndFlush(analysisResult);

        // Get all the analysisResultList where status equals to
        defaultAnalysisResultFiltering("status.equals=" + DEFAULT_STATUS, "status.equals=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllAnalysisResultsByStatusIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAnalysisResult = analysisResultRepository.saveAndFlush(analysisResult);

        // Get all the analysisResultList where status in
        defaultAnalysisResultFiltering("status.in=" + DEFAULT_STATUS + "," + UPDATED_STATUS, "status.in=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllAnalysisResultsByStatusIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAnalysisResult = analysisResultRepository.saveAndFlush(analysisResult);

        // Get all the analysisResultList where status is not null
        defaultAnalysisResultFiltering("status.specified=true", "status.specified=false");
    }

    @Test
    @Transactional
    void getAllAnalysisResultsByModelNameIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAnalysisResult = analysisResultRepository.saveAndFlush(analysisResult);

        // Get all the analysisResultList where modelName equals to
        defaultAnalysisResultFiltering("modelName.equals=" + DEFAULT_MODEL_NAME, "modelName.equals=" + UPDATED_MODEL_NAME);
    }

    @Test
    @Transactional
    void getAllAnalysisResultsByModelNameIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAnalysisResult = analysisResultRepository.saveAndFlush(analysisResult);

        // Get all the analysisResultList where modelName in
        defaultAnalysisResultFiltering(
            "modelName.in=" + DEFAULT_MODEL_NAME + "," + UPDATED_MODEL_NAME,
            "modelName.in=" + UPDATED_MODEL_NAME
        );
    }

    @Test
    @Transactional
    void getAllAnalysisResultsByModelNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAnalysisResult = analysisResultRepository.saveAndFlush(analysisResult);

        // Get all the analysisResultList where modelName is not null
        defaultAnalysisResultFiltering("modelName.specified=true", "modelName.specified=false");
    }

    @Test
    @Transactional
    void getAllAnalysisResultsByModelNameContainsSomething() throws Exception {
        // Initialize the database
        insertedAnalysisResult = analysisResultRepository.saveAndFlush(analysisResult);

        // Get all the analysisResultList where modelName contains
        defaultAnalysisResultFiltering("modelName.contains=" + DEFAULT_MODEL_NAME, "modelName.contains=" + UPDATED_MODEL_NAME);
    }

    @Test
    @Transactional
    void getAllAnalysisResultsByModelNameNotContainsSomething() throws Exception {
        // Initialize the database
        insertedAnalysisResult = analysisResultRepository.saveAndFlush(analysisResult);

        // Get all the analysisResultList where modelName does not contain
        defaultAnalysisResultFiltering("modelName.doesNotContain=" + UPDATED_MODEL_NAME, "modelName.doesNotContain=" + DEFAULT_MODEL_NAME);
    }

    @Test
    @Transactional
    void getAllAnalysisResultsByAnalyzedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAnalysisResult = analysisResultRepository.saveAndFlush(analysisResult);

        // Get all the analysisResultList where analyzedAt equals to
        defaultAnalysisResultFiltering("analyzedAt.equals=" + DEFAULT_ANALYZED_AT, "analyzedAt.equals=" + UPDATED_ANALYZED_AT);
    }

    @Test
    @Transactional
    void getAllAnalysisResultsByAnalyzedAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAnalysisResult = analysisResultRepository.saveAndFlush(analysisResult);

        // Get all the analysisResultList where analyzedAt in
        defaultAnalysisResultFiltering(
            "analyzedAt.in=" + DEFAULT_ANALYZED_AT + "," + UPDATED_ANALYZED_AT,
            "analyzedAt.in=" + UPDATED_ANALYZED_AT
        );
    }

    @Test
    @Transactional
    void getAllAnalysisResultsByAnalyzedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAnalysisResult = analysisResultRepository.saveAndFlush(analysisResult);

        // Get all the analysisResultList where analyzedAt is not null
        defaultAnalysisResultFiltering("analyzedAt.specified=true", "analyzedAt.specified=false");
    }

    @Test
    @Transactional
    void getAllAnalysisResultsByErrorMessageIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAnalysisResult = analysisResultRepository.saveAndFlush(analysisResult);

        // Get all the analysisResultList where errorMessage equals to
        defaultAnalysisResultFiltering("errorMessage.equals=" + DEFAULT_ERROR_MESSAGE, "errorMessage.equals=" + UPDATED_ERROR_MESSAGE);
    }

    @Test
    @Transactional
    void getAllAnalysisResultsByErrorMessageIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAnalysisResult = analysisResultRepository.saveAndFlush(analysisResult);

        // Get all the analysisResultList where errorMessage in
        defaultAnalysisResultFiltering(
            "errorMessage.in=" + DEFAULT_ERROR_MESSAGE + "," + UPDATED_ERROR_MESSAGE,
            "errorMessage.in=" + UPDATED_ERROR_MESSAGE
        );
    }

    @Test
    @Transactional
    void getAllAnalysisResultsByErrorMessageIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAnalysisResult = analysisResultRepository.saveAndFlush(analysisResult);

        // Get all the analysisResultList where errorMessage is not null
        defaultAnalysisResultFiltering("errorMessage.specified=true", "errorMessage.specified=false");
    }

    @Test
    @Transactional
    void getAllAnalysisResultsByErrorMessageContainsSomething() throws Exception {
        // Initialize the database
        insertedAnalysisResult = analysisResultRepository.saveAndFlush(analysisResult);

        // Get all the analysisResultList where errorMessage contains
        defaultAnalysisResultFiltering("errorMessage.contains=" + DEFAULT_ERROR_MESSAGE, "errorMessage.contains=" + UPDATED_ERROR_MESSAGE);
    }

    @Test
    @Transactional
    void getAllAnalysisResultsByErrorMessageNotContainsSomething() throws Exception {
        // Initialize the database
        insertedAnalysisResult = analysisResultRepository.saveAndFlush(analysisResult);

        // Get all the analysisResultList where errorMessage does not contain
        defaultAnalysisResultFiltering(
            "errorMessage.doesNotContain=" + UPDATED_ERROR_MESSAGE,
            "errorMessage.doesNotContain=" + DEFAULT_ERROR_MESSAGE
        );
    }

    @Test
    @Transactional
    void getAllAnalysisResultsByNewsItemIsEqualToSomething() throws Exception {
        // Get already existing entity
        NewsItem newsItem = analysisResult.getNewsItem();
        analysisResultRepository.saveAndFlush(analysisResult);
        Long newsItemId = newsItem.getId();
        // Get all the analysisResultList where newsItem equals to newsItemId
        defaultAnalysisResultShouldBeFound("newsItemId.equals=" + newsItemId);

        // Get all the analysisResultList where newsItem equals to (newsItemId + 1)
        defaultAnalysisResultShouldNotBeFound("newsItemId.equals=" + (newsItemId + 1));
    }

    private void defaultAnalysisResultFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultAnalysisResultShouldBeFound(shouldBeFound);
        defaultAnalysisResultShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultAnalysisResultShouldBeFound(String filter) throws Exception {
        restAnalysisResultMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(analysisResult.getId().intValue())))
            .andExpect(jsonPath("$.[*].summary").value(hasItem(DEFAULT_SUMMARY)))
            .andExpect(jsonPath("$.[*].sentiment").value(hasItem(DEFAULT_SENTIMENT.toString())))
            .andExpect(jsonPath("$.[*].topic").value(hasItem(DEFAULT_TOPIC)))
            .andExpect(jsonPath("$.[*].entities").value(hasItem(DEFAULT_ENTITIES)))
            .andExpect(jsonPath("$.[*].riskSource").value(hasItem(DEFAULT_RISK_SOURCE)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].modelName").value(hasItem(DEFAULT_MODEL_NAME)))
            .andExpect(jsonPath("$.[*].analyzedAt").value(hasItem(DEFAULT_ANALYZED_AT.toString())))
            .andExpect(jsonPath("$.[*].errorMessage").value(hasItem(DEFAULT_ERROR_MESSAGE)));

        // Check, that the count call also returns 1
        restAnalysisResultMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultAnalysisResultShouldNotBeFound(String filter) throws Exception {
        restAnalysisResultMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restAnalysisResultMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingAnalysisResult() throws Exception {
        // Get the analysisResult
        restAnalysisResultMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingAnalysisResult() throws Exception {
        // Initialize the database
        insertedAnalysisResult = analysisResultRepository.saveAndFlush(analysisResult);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the analysisResult
        AnalysisResult updatedAnalysisResult = analysisResultRepository.findById(analysisResult.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedAnalysisResult are not directly saved in db
        em.detach(updatedAnalysisResult);
        updatedAnalysisResult
            .summary(UPDATED_SUMMARY)
            .sentiment(UPDATED_SENTIMENT)
            .topic(UPDATED_TOPIC)
            .entities(UPDATED_ENTITIES)
            .riskSource(UPDATED_RISK_SOURCE)
            .status(UPDATED_STATUS)
            .modelName(UPDATED_MODEL_NAME)
            .analyzedAt(UPDATED_ANALYZED_AT)
            .errorMessage(UPDATED_ERROR_MESSAGE);
        AnalysisResultDTO analysisResultDTO = analysisResultMapper.toDto(updatedAnalysisResult);

        restAnalysisResultMockMvc
            .perform(
                put(ENTITY_API_URL_ID, analysisResultDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(analysisResultDTO))
            )
            .andExpect(status().isOk());

        // Validate the AnalysisResult in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedAnalysisResultToMatchAllProperties(updatedAnalysisResult);
    }

    @Test
    @Transactional
    void putNonExistingAnalysisResult() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        analysisResult.setId(longCount.incrementAndGet());

        // Create the AnalysisResult
        AnalysisResultDTO analysisResultDTO = analysisResultMapper.toDto(analysisResult);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAnalysisResultMockMvc
            .perform(
                put(ENTITY_API_URL_ID, analysisResultDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(analysisResultDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AnalysisResult in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchAnalysisResult() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        analysisResult.setId(longCount.incrementAndGet());

        // Create the AnalysisResult
        AnalysisResultDTO analysisResultDTO = analysisResultMapper.toDto(analysisResult);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAnalysisResultMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(analysisResultDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AnalysisResult in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamAnalysisResult() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        analysisResult.setId(longCount.incrementAndGet());

        // Create the AnalysisResult
        AnalysisResultDTO analysisResultDTO = analysisResultMapper.toDto(analysisResult);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAnalysisResultMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(analysisResultDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the AnalysisResult in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateAnalysisResultWithPatch() throws Exception {
        // Initialize the database
        insertedAnalysisResult = analysisResultRepository.saveAndFlush(analysisResult);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the analysisResult using partial update
        AnalysisResult partialUpdatedAnalysisResult = new AnalysisResult();
        partialUpdatedAnalysisResult.setId(analysisResult.getId());

        partialUpdatedAnalysisResult
            .sentiment(UPDATED_SENTIMENT)
            .entities(UPDATED_ENTITIES)
            .riskSource(UPDATED_RISK_SOURCE)
            .modelName(UPDATED_MODEL_NAME);

        restAnalysisResultMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAnalysisResult.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedAnalysisResult))
            )
            .andExpect(status().isOk());

        // Validate the AnalysisResult in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAnalysisResultUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedAnalysisResult, analysisResult),
            getPersistedAnalysisResult(analysisResult)
        );
    }

    @Test
    @Transactional
    void fullUpdateAnalysisResultWithPatch() throws Exception {
        // Initialize the database
        insertedAnalysisResult = analysisResultRepository.saveAndFlush(analysisResult);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the analysisResult using partial update
        AnalysisResult partialUpdatedAnalysisResult = new AnalysisResult();
        partialUpdatedAnalysisResult.setId(analysisResult.getId());

        partialUpdatedAnalysisResult
            .summary(UPDATED_SUMMARY)
            .sentiment(UPDATED_SENTIMENT)
            .topic(UPDATED_TOPIC)
            .entities(UPDATED_ENTITIES)
            .riskSource(UPDATED_RISK_SOURCE)
            .status(UPDATED_STATUS)
            .modelName(UPDATED_MODEL_NAME)
            .analyzedAt(UPDATED_ANALYZED_AT)
            .errorMessage(UPDATED_ERROR_MESSAGE);

        restAnalysisResultMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAnalysisResult.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedAnalysisResult))
            )
            .andExpect(status().isOk());

        // Validate the AnalysisResult in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAnalysisResultUpdatableFieldsEquals(partialUpdatedAnalysisResult, getPersistedAnalysisResult(partialUpdatedAnalysisResult));
    }

    @Test
    @Transactional
    void patchNonExistingAnalysisResult() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        analysisResult.setId(longCount.incrementAndGet());

        // Create the AnalysisResult
        AnalysisResultDTO analysisResultDTO = analysisResultMapper.toDto(analysisResult);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAnalysisResultMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, analysisResultDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(analysisResultDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AnalysisResult in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchAnalysisResult() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        analysisResult.setId(longCount.incrementAndGet());

        // Create the AnalysisResult
        AnalysisResultDTO analysisResultDTO = analysisResultMapper.toDto(analysisResult);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAnalysisResultMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(analysisResultDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AnalysisResult in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamAnalysisResult() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        analysisResult.setId(longCount.incrementAndGet());

        // Create the AnalysisResult
        AnalysisResultDTO analysisResultDTO = analysisResultMapper.toDto(analysisResult);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAnalysisResultMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(analysisResultDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the AnalysisResult in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteAnalysisResult() throws Exception {
        // Initialize the database
        insertedAnalysisResult = analysisResultRepository.saveAndFlush(analysisResult);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the analysisResult
        restAnalysisResultMockMvc
            .perform(delete(ENTITY_API_URL_ID, analysisResult.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return analysisResultRepository.count();
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

    protected AnalysisResult getPersistedAnalysisResult(AnalysisResult analysisResult) {
        return analysisResultRepository.findById(analysisResult.getId()).orElseThrow();
    }

    protected void assertPersistedAnalysisResultToMatchAllProperties(AnalysisResult expectedAnalysisResult) {
        assertAnalysisResultAllPropertiesEquals(expectedAnalysisResult, getPersistedAnalysisResult(expectedAnalysisResult));
    }

    protected void assertPersistedAnalysisResultToMatchUpdatableProperties(AnalysisResult expectedAnalysisResult) {
        assertAnalysisResultAllUpdatablePropertiesEquals(expectedAnalysisResult, getPersistedAnalysisResult(expectedAnalysisResult));
    }
}
