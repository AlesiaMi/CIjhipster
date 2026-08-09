package com.mycompany.myapp.web.rest;

import static com.mycompany.myapp.domain.NewsItemAsserts.*;
import static com.mycompany.myapp.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.CollectionRun;
import com.mycompany.myapp.domain.Competitor;
import com.mycompany.myapp.domain.DataSource;
import com.mycompany.myapp.domain.NewsItem;
import com.mycompany.myapp.repository.NewsItemRepository;
import com.mycompany.myapp.service.NewsItemService;
import com.mycompany.myapp.service.dto.NewsItemDTO;
import com.mycompany.myapp.service.mapper.NewsItemMapper;
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
 * Integration tests for the {@link NewsItemResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class NewsItemResourceIT {

    private static final String DEFAULT_EXTERNAL_ID = "AAAAAAAAAA";
    private static final String UPDATED_EXTERNAL_ID = "BBBBBBBBBB";

    private static final String DEFAULT_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBB";

    private static final String DEFAULT_URL = "AAAAAAAAAA";
    private static final String UPDATED_URL = "BBBBBBBBBB";

    private static final String DEFAULT_ORIGINAL_TEXT = "AAAAAAAAAA";
    private static final String UPDATED_ORIGINAL_TEXT = "BBBBBBBBBB";

    private static final Instant DEFAULT_PUBLISHED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_PUBLISHED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_COLLECTED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_COLLECTED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Boolean DEFAULT_IS_DUPLICATE = false;
    private static final Boolean UPDATED_IS_DUPLICATE = true;

    private static final String ENTITY_API_URL = "/api/news-items";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private NewsItemRepository newsItemRepository;

    @Mock
    private NewsItemRepository newsItemRepositoryMock;

    @Autowired
    private NewsItemMapper newsItemMapper;

    @Mock
    private NewsItemService newsItemServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restNewsItemMockMvc;

    private NewsItem newsItem;

    private NewsItem insertedNewsItem;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static NewsItem createEntity(EntityManager em) {
        NewsItem newsItem = new NewsItem()
            .externalId(DEFAULT_EXTERNAL_ID)
            .title(DEFAULT_TITLE)
            .url(DEFAULT_URL)
            .originalText(DEFAULT_ORIGINAL_TEXT)
            .publishedAt(DEFAULT_PUBLISHED_AT)
            .collectedAt(DEFAULT_COLLECTED_AT)
            .isDuplicate(DEFAULT_IS_DUPLICATE);
        // Add required entity
        DataSource dataSource;
        if (TestUtil.findAll(em, DataSource.class).isEmpty()) {
            dataSource = DataSourceResourceIT.createEntity(em);
            em.persist(dataSource);
            em.flush();
        } else {
            dataSource = TestUtil.findAll(em, DataSource.class).get(0);
        }
        newsItem.setDataSource(dataSource);
        // Add required entity
        Competitor competitor;
        if (TestUtil.findAll(em, Competitor.class).isEmpty()) {
            competitor = CompetitorResourceIT.createEntity();
            em.persist(competitor);
            em.flush();
        } else {
            competitor = TestUtil.findAll(em, Competitor.class).get(0);
        }
        newsItem.setCompetitor(competitor);
        return newsItem;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static NewsItem createUpdatedEntity(EntityManager em) {
        NewsItem updatedNewsItem = new NewsItem()
            .externalId(UPDATED_EXTERNAL_ID)
            .title(UPDATED_TITLE)
            .url(UPDATED_URL)
            .originalText(UPDATED_ORIGINAL_TEXT)
            .publishedAt(UPDATED_PUBLISHED_AT)
            .collectedAt(UPDATED_COLLECTED_AT)
            .isDuplicate(UPDATED_IS_DUPLICATE);
        // Add required entity
        DataSource dataSource;
        if (TestUtil.findAll(em, DataSource.class).isEmpty()) {
            dataSource = DataSourceResourceIT.createUpdatedEntity(em);
            em.persist(dataSource);
            em.flush();
        } else {
            dataSource = TestUtil.findAll(em, DataSource.class).get(0);
        }
        updatedNewsItem.setDataSource(dataSource);
        // Add required entity
        Competitor competitor;
        if (TestUtil.findAll(em, Competitor.class).isEmpty()) {
            competitor = CompetitorResourceIT.createUpdatedEntity();
            em.persist(competitor);
            em.flush();
        } else {
            competitor = TestUtil.findAll(em, Competitor.class).get(0);
        }
        updatedNewsItem.setCompetitor(competitor);
        return updatedNewsItem;
    }

    @BeforeEach
    void initTest() {
        newsItem = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedNewsItem != null) {
            newsItemRepository.delete(insertedNewsItem);
            insertedNewsItem = null;
        }
    }

    @Test
    @Transactional
    void createNewsItem() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the NewsItem
        NewsItemDTO newsItemDTO = newsItemMapper.toDto(newsItem);
        var returnedNewsItemDTO = om.readValue(
            restNewsItemMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(newsItemDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            NewsItemDTO.class
        );

        // Validate the NewsItem in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedNewsItem = newsItemMapper.toEntity(returnedNewsItemDTO);
        assertNewsItemUpdatableFieldsEquals(returnedNewsItem, getPersistedNewsItem(returnedNewsItem));

        insertedNewsItem = returnedNewsItem;
    }

    @Test
    @Transactional
    void createNewsItemWithExistingId() throws Exception {
        // Create the NewsItem with an existing ID
        newsItem.setId(1L);
        NewsItemDTO newsItemDTO = newsItemMapper.toDto(newsItem);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restNewsItemMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(newsItemDTO)))
            .andExpect(status().isBadRequest());

        // Validate the NewsItem in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkTitleIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        newsItem.setTitle(null);

        // Create the NewsItem, which fails.
        NewsItemDTO newsItemDTO = newsItemMapper.toDto(newsItem);

        restNewsItemMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(newsItemDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkUrlIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        newsItem.setUrl(null);

        // Create the NewsItem, which fails.
        NewsItemDTO newsItemDTO = newsItemMapper.toDto(newsItem);

        restNewsItemMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(newsItemDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCollectedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        newsItem.setCollectedAt(null);

        // Create the NewsItem, which fails.
        NewsItemDTO newsItemDTO = newsItemMapper.toDto(newsItem);

        restNewsItemMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(newsItemDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkIsDuplicateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        newsItem.setIsDuplicate(null);

        // Create the NewsItem, which fails.
        NewsItemDTO newsItemDTO = newsItemMapper.toDto(newsItem);

        restNewsItemMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(newsItemDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllNewsItems() throws Exception {
        // Initialize the database
        insertedNewsItem = newsItemRepository.saveAndFlush(newsItem);

        // Get all the newsItemList
        restNewsItemMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(newsItem.getId().intValue())))
            .andExpect(jsonPath("$.[*].externalId").value(hasItem(DEFAULT_EXTERNAL_ID)))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].url").value(hasItem(DEFAULT_URL)))
            .andExpect(jsonPath("$.[*].originalText").value(hasItem(DEFAULT_ORIGINAL_TEXT)))
            .andExpect(jsonPath("$.[*].publishedAt").value(hasItem(DEFAULT_PUBLISHED_AT.toString())))
            .andExpect(jsonPath("$.[*].collectedAt").value(hasItem(DEFAULT_COLLECTED_AT.toString())))
            .andExpect(jsonPath("$.[*].isDuplicate").value(hasItem(DEFAULT_IS_DUPLICATE)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllNewsItemsWithEagerRelationshipsIsEnabled() throws Exception {
        when(newsItemServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restNewsItemMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(newsItemServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllNewsItemsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(newsItemServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restNewsItemMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(newsItemRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getNewsItem() throws Exception {
        // Initialize the database
        insertedNewsItem = newsItemRepository.saveAndFlush(newsItem);

        // Get the newsItem
        restNewsItemMockMvc
            .perform(get(ENTITY_API_URL_ID, newsItem.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(newsItem.getId().intValue()))
            .andExpect(jsonPath("$.externalId").value(DEFAULT_EXTERNAL_ID))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE))
            .andExpect(jsonPath("$.url").value(DEFAULT_URL))
            .andExpect(jsonPath("$.originalText").value(DEFAULT_ORIGINAL_TEXT))
            .andExpect(jsonPath("$.publishedAt").value(DEFAULT_PUBLISHED_AT.toString()))
            .andExpect(jsonPath("$.collectedAt").value(DEFAULT_COLLECTED_AT.toString()))
            .andExpect(jsonPath("$.isDuplicate").value(DEFAULT_IS_DUPLICATE));
    }

    @Test
    @Transactional
    void getNewsItemsByIdFiltering() throws Exception {
        // Initialize the database
        insertedNewsItem = newsItemRepository.saveAndFlush(newsItem);

        Long id = newsItem.getId();

        defaultNewsItemFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultNewsItemFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultNewsItemFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllNewsItemsByExternalIdIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedNewsItem = newsItemRepository.saveAndFlush(newsItem);

        // Get all the newsItemList where externalId equals to
        defaultNewsItemFiltering("externalId.equals=" + DEFAULT_EXTERNAL_ID, "externalId.equals=" + UPDATED_EXTERNAL_ID);
    }

    @Test
    @Transactional
    void getAllNewsItemsByExternalIdIsInShouldWork() throws Exception {
        // Initialize the database
        insertedNewsItem = newsItemRepository.saveAndFlush(newsItem);

        // Get all the newsItemList where externalId in
        defaultNewsItemFiltering(
            "externalId.in=" + DEFAULT_EXTERNAL_ID + "," + UPDATED_EXTERNAL_ID,
            "externalId.in=" + UPDATED_EXTERNAL_ID
        );
    }

    @Test
    @Transactional
    void getAllNewsItemsByExternalIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedNewsItem = newsItemRepository.saveAndFlush(newsItem);

        // Get all the newsItemList where externalId is not null
        defaultNewsItemFiltering("externalId.specified=true", "externalId.specified=false");
    }

    @Test
    @Transactional
    void getAllNewsItemsByExternalIdContainsSomething() throws Exception {
        // Initialize the database
        insertedNewsItem = newsItemRepository.saveAndFlush(newsItem);

        // Get all the newsItemList where externalId contains
        defaultNewsItemFiltering("externalId.contains=" + DEFAULT_EXTERNAL_ID, "externalId.contains=" + UPDATED_EXTERNAL_ID);
    }

    @Test
    @Transactional
    void getAllNewsItemsByExternalIdNotContainsSomething() throws Exception {
        // Initialize the database
        insertedNewsItem = newsItemRepository.saveAndFlush(newsItem);

        // Get all the newsItemList where externalId does not contain
        defaultNewsItemFiltering("externalId.doesNotContain=" + UPDATED_EXTERNAL_ID, "externalId.doesNotContain=" + DEFAULT_EXTERNAL_ID);
    }

    @Test
    @Transactional
    void getAllNewsItemsByTitleIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedNewsItem = newsItemRepository.saveAndFlush(newsItem);

        // Get all the newsItemList where title equals to
        defaultNewsItemFiltering("title.equals=" + DEFAULT_TITLE, "title.equals=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    void getAllNewsItemsByTitleIsInShouldWork() throws Exception {
        // Initialize the database
        insertedNewsItem = newsItemRepository.saveAndFlush(newsItem);

        // Get all the newsItemList where title in
        defaultNewsItemFiltering("title.in=" + DEFAULT_TITLE + "," + UPDATED_TITLE, "title.in=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    void getAllNewsItemsByTitleIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedNewsItem = newsItemRepository.saveAndFlush(newsItem);

        // Get all the newsItemList where title is not null
        defaultNewsItemFiltering("title.specified=true", "title.specified=false");
    }

    @Test
    @Transactional
    void getAllNewsItemsByTitleContainsSomething() throws Exception {
        // Initialize the database
        insertedNewsItem = newsItemRepository.saveAndFlush(newsItem);

        // Get all the newsItemList where title contains
        defaultNewsItemFiltering("title.contains=" + DEFAULT_TITLE, "title.contains=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    void getAllNewsItemsByTitleNotContainsSomething() throws Exception {
        // Initialize the database
        insertedNewsItem = newsItemRepository.saveAndFlush(newsItem);

        // Get all the newsItemList where title does not contain
        defaultNewsItemFiltering("title.doesNotContain=" + UPDATED_TITLE, "title.doesNotContain=" + DEFAULT_TITLE);
    }

    @Test
    @Transactional
    void getAllNewsItemsByUrlIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedNewsItem = newsItemRepository.saveAndFlush(newsItem);

        // Get all the newsItemList where url equals to
        defaultNewsItemFiltering("url.equals=" + DEFAULT_URL, "url.equals=" + UPDATED_URL);
    }

    @Test
    @Transactional
    void getAllNewsItemsByUrlIsInShouldWork() throws Exception {
        // Initialize the database
        insertedNewsItem = newsItemRepository.saveAndFlush(newsItem);

        // Get all the newsItemList where url in
        defaultNewsItemFiltering("url.in=" + DEFAULT_URL + "," + UPDATED_URL, "url.in=" + UPDATED_URL);
    }

    @Test
    @Transactional
    void getAllNewsItemsByUrlIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedNewsItem = newsItemRepository.saveAndFlush(newsItem);

        // Get all the newsItemList where url is not null
        defaultNewsItemFiltering("url.specified=true", "url.specified=false");
    }

    @Test
    @Transactional
    void getAllNewsItemsByUrlContainsSomething() throws Exception {
        // Initialize the database
        insertedNewsItem = newsItemRepository.saveAndFlush(newsItem);

        // Get all the newsItemList where url contains
        defaultNewsItemFiltering("url.contains=" + DEFAULT_URL, "url.contains=" + UPDATED_URL);
    }

    @Test
    @Transactional
    void getAllNewsItemsByUrlNotContainsSomething() throws Exception {
        // Initialize the database
        insertedNewsItem = newsItemRepository.saveAndFlush(newsItem);

        // Get all the newsItemList where url does not contain
        defaultNewsItemFiltering("url.doesNotContain=" + UPDATED_URL, "url.doesNotContain=" + DEFAULT_URL);
    }

    @Test
    @Transactional
    void getAllNewsItemsByOriginalTextIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedNewsItem = newsItemRepository.saveAndFlush(newsItem);

        // Get all the newsItemList where originalText equals to
        defaultNewsItemFiltering("originalText.equals=" + DEFAULT_ORIGINAL_TEXT, "originalText.equals=" + UPDATED_ORIGINAL_TEXT);
    }

    @Test
    @Transactional
    void getAllNewsItemsByOriginalTextIsInShouldWork() throws Exception {
        // Initialize the database
        insertedNewsItem = newsItemRepository.saveAndFlush(newsItem);

        // Get all the newsItemList where originalText in
        defaultNewsItemFiltering(
            "originalText.in=" + DEFAULT_ORIGINAL_TEXT + "," + UPDATED_ORIGINAL_TEXT,
            "originalText.in=" + UPDATED_ORIGINAL_TEXT
        );
    }

    @Test
    @Transactional
    void getAllNewsItemsByOriginalTextIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedNewsItem = newsItemRepository.saveAndFlush(newsItem);

        // Get all the newsItemList where originalText is not null
        defaultNewsItemFiltering("originalText.specified=true", "originalText.specified=false");
    }

    @Test
    @Transactional
    void getAllNewsItemsByOriginalTextContainsSomething() throws Exception {
        // Initialize the database
        insertedNewsItem = newsItemRepository.saveAndFlush(newsItem);

        // Get all the newsItemList where originalText contains
        defaultNewsItemFiltering("originalText.contains=" + DEFAULT_ORIGINAL_TEXT, "originalText.contains=" + UPDATED_ORIGINAL_TEXT);
    }

    @Test
    @Transactional
    void getAllNewsItemsByOriginalTextNotContainsSomething() throws Exception {
        // Initialize the database
        insertedNewsItem = newsItemRepository.saveAndFlush(newsItem);

        // Get all the newsItemList where originalText does not contain
        defaultNewsItemFiltering(
            "originalText.doesNotContain=" + UPDATED_ORIGINAL_TEXT,
            "originalText.doesNotContain=" + DEFAULT_ORIGINAL_TEXT
        );
    }

    @Test
    @Transactional
    void getAllNewsItemsByPublishedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedNewsItem = newsItemRepository.saveAndFlush(newsItem);

        // Get all the newsItemList where publishedAt equals to
        defaultNewsItemFiltering("publishedAt.equals=" + DEFAULT_PUBLISHED_AT, "publishedAt.equals=" + UPDATED_PUBLISHED_AT);
    }

    @Test
    @Transactional
    void getAllNewsItemsByPublishedAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedNewsItem = newsItemRepository.saveAndFlush(newsItem);

        // Get all the newsItemList where publishedAt in
        defaultNewsItemFiltering(
            "publishedAt.in=" + DEFAULT_PUBLISHED_AT + "," + UPDATED_PUBLISHED_AT,
            "publishedAt.in=" + UPDATED_PUBLISHED_AT
        );
    }

    @Test
    @Transactional
    void getAllNewsItemsByPublishedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedNewsItem = newsItemRepository.saveAndFlush(newsItem);

        // Get all the newsItemList where publishedAt is not null
        defaultNewsItemFiltering("publishedAt.specified=true", "publishedAt.specified=false");
    }

    @Test
    @Transactional
    void getAllNewsItemsByCollectedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedNewsItem = newsItemRepository.saveAndFlush(newsItem);

        // Get all the newsItemList where collectedAt equals to
        defaultNewsItemFiltering("collectedAt.equals=" + DEFAULT_COLLECTED_AT, "collectedAt.equals=" + UPDATED_COLLECTED_AT);
    }

    @Test
    @Transactional
    void getAllNewsItemsByCollectedAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedNewsItem = newsItemRepository.saveAndFlush(newsItem);

        // Get all the newsItemList where collectedAt in
        defaultNewsItemFiltering(
            "collectedAt.in=" + DEFAULT_COLLECTED_AT + "," + UPDATED_COLLECTED_AT,
            "collectedAt.in=" + UPDATED_COLLECTED_AT
        );
    }

    @Test
    @Transactional
    void getAllNewsItemsByCollectedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedNewsItem = newsItemRepository.saveAndFlush(newsItem);

        // Get all the newsItemList where collectedAt is not null
        defaultNewsItemFiltering("collectedAt.specified=true", "collectedAt.specified=false");
    }

    @Test
    @Transactional
    void getAllNewsItemsByIsDuplicateIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedNewsItem = newsItemRepository.saveAndFlush(newsItem);

        // Get all the newsItemList where isDuplicate equals to
        defaultNewsItemFiltering("isDuplicate.equals=" + DEFAULT_IS_DUPLICATE, "isDuplicate.equals=" + UPDATED_IS_DUPLICATE);
    }

    @Test
    @Transactional
    void getAllNewsItemsByIsDuplicateIsInShouldWork() throws Exception {
        // Initialize the database
        insertedNewsItem = newsItemRepository.saveAndFlush(newsItem);

        // Get all the newsItemList where isDuplicate in
        defaultNewsItemFiltering(
            "isDuplicate.in=" + DEFAULT_IS_DUPLICATE + "," + UPDATED_IS_DUPLICATE,
            "isDuplicate.in=" + UPDATED_IS_DUPLICATE
        );
    }

    @Test
    @Transactional
    void getAllNewsItemsByIsDuplicateIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedNewsItem = newsItemRepository.saveAndFlush(newsItem);

        // Get all the newsItemList where isDuplicate is not null
        defaultNewsItemFiltering("isDuplicate.specified=true", "isDuplicate.specified=false");
    }

    @Test
    @Transactional
    void getAllNewsItemsByDataSourceIsEqualToSomething() throws Exception {
        DataSource dataSource;
        if (TestUtil.findAll(em, DataSource.class).isEmpty()) {
            newsItemRepository.saveAndFlush(newsItem);
            dataSource = DataSourceResourceIT.createEntity(em);
        } else {
            dataSource = TestUtil.findAll(em, DataSource.class).get(0);
        }
        em.persist(dataSource);
        em.flush();
        newsItem.setDataSource(dataSource);
        newsItemRepository.saveAndFlush(newsItem);
        Long dataSourceId = dataSource.getId();
        // Get all the newsItemList where dataSource equals to dataSourceId
        defaultNewsItemShouldBeFound("dataSourceId.equals=" + dataSourceId);

        // Get all the newsItemList where dataSource equals to (dataSourceId + 1)
        defaultNewsItemShouldNotBeFound("dataSourceId.equals=" + (dataSourceId + 1));
    }

    @Test
    @Transactional
    void getAllNewsItemsByCompetitorIsEqualToSomething() throws Exception {
        Competitor competitor;
        if (TestUtil.findAll(em, Competitor.class).isEmpty()) {
            newsItemRepository.saveAndFlush(newsItem);
            competitor = CompetitorResourceIT.createEntity();
        } else {
            competitor = TestUtil.findAll(em, Competitor.class).get(0);
        }
        em.persist(competitor);
        em.flush();
        newsItem.setCompetitor(competitor);
        newsItemRepository.saveAndFlush(newsItem);
        Long competitorId = competitor.getId();
        // Get all the newsItemList where competitor equals to competitorId
        defaultNewsItemShouldBeFound("competitorId.equals=" + competitorId);

        // Get all the newsItemList where competitor equals to (competitorId + 1)
        defaultNewsItemShouldNotBeFound("competitorId.equals=" + (competitorId + 1));
    }

    @Test
    @Transactional
    void getAllNewsItemsByCollectionRunIsEqualToSomething() throws Exception {
        CollectionRun collectionRun;
        if (TestUtil.findAll(em, CollectionRun.class).isEmpty()) {
            newsItemRepository.saveAndFlush(newsItem);
            collectionRun = CollectionRunResourceIT.createEntity();
        } else {
            collectionRun = TestUtil.findAll(em, CollectionRun.class).get(0);
        }
        em.persist(collectionRun);
        em.flush();
        newsItem.setCollectionRun(collectionRun);
        newsItemRepository.saveAndFlush(newsItem);
        Long collectionRunId = collectionRun.getId();
        // Get all the newsItemList where collectionRun equals to collectionRunId
        defaultNewsItemShouldBeFound("collectionRunId.equals=" + collectionRunId);

        // Get all the newsItemList where collectionRun equals to (collectionRunId + 1)
        defaultNewsItemShouldNotBeFound("collectionRunId.equals=" + (collectionRunId + 1));
    }

    private void defaultNewsItemFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultNewsItemShouldBeFound(shouldBeFound);
        defaultNewsItemShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultNewsItemShouldBeFound(String filter) throws Exception {
        restNewsItemMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(newsItem.getId().intValue())))
            .andExpect(jsonPath("$.[*].externalId").value(hasItem(DEFAULT_EXTERNAL_ID)))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].url").value(hasItem(DEFAULT_URL)))
            .andExpect(jsonPath("$.[*].originalText").value(hasItem(DEFAULT_ORIGINAL_TEXT)))
            .andExpect(jsonPath("$.[*].publishedAt").value(hasItem(DEFAULT_PUBLISHED_AT.toString())))
            .andExpect(jsonPath("$.[*].collectedAt").value(hasItem(DEFAULT_COLLECTED_AT.toString())))
            .andExpect(jsonPath("$.[*].isDuplicate").value(hasItem(DEFAULT_IS_DUPLICATE)));

        // Check, that the count call also returns 1
        restNewsItemMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultNewsItemShouldNotBeFound(String filter) throws Exception {
        restNewsItemMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restNewsItemMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingNewsItem() throws Exception {
        // Get the newsItem
        restNewsItemMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingNewsItem() throws Exception {
        // Initialize the database
        insertedNewsItem = newsItemRepository.saveAndFlush(newsItem);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the newsItem
        NewsItem updatedNewsItem = newsItemRepository.findById(newsItem.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedNewsItem are not directly saved in db
        em.detach(updatedNewsItem);
        updatedNewsItem
            .externalId(UPDATED_EXTERNAL_ID)
            .title(UPDATED_TITLE)
            .url(UPDATED_URL)
            .originalText(UPDATED_ORIGINAL_TEXT)
            .publishedAt(UPDATED_PUBLISHED_AT)
            .collectedAt(UPDATED_COLLECTED_AT)
            .isDuplicate(UPDATED_IS_DUPLICATE);
        NewsItemDTO newsItemDTO = newsItemMapper.toDto(updatedNewsItem);

        restNewsItemMockMvc
            .perform(
                put(ENTITY_API_URL_ID, newsItemDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(newsItemDTO))
            )
            .andExpect(status().isOk());

        // Validate the NewsItem in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedNewsItemToMatchAllProperties(updatedNewsItem);
    }

    @Test
    @Transactional
    void putNonExistingNewsItem() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        newsItem.setId(longCount.incrementAndGet());

        // Create the NewsItem
        NewsItemDTO newsItemDTO = newsItemMapper.toDto(newsItem);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restNewsItemMockMvc
            .perform(
                put(ENTITY_API_URL_ID, newsItemDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(newsItemDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the NewsItem in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchNewsItem() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        newsItem.setId(longCount.incrementAndGet());

        // Create the NewsItem
        NewsItemDTO newsItemDTO = newsItemMapper.toDto(newsItem);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restNewsItemMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(newsItemDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the NewsItem in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamNewsItem() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        newsItem.setId(longCount.incrementAndGet());

        // Create the NewsItem
        NewsItemDTO newsItemDTO = newsItemMapper.toDto(newsItem);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restNewsItemMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(newsItemDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the NewsItem in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateNewsItemWithPatch() throws Exception {
        // Initialize the database
        insertedNewsItem = newsItemRepository.saveAndFlush(newsItem);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the newsItem using partial update
        NewsItem partialUpdatedNewsItem = new NewsItem();
        partialUpdatedNewsItem.setId(newsItem.getId());

        partialUpdatedNewsItem.collectedAt(UPDATED_COLLECTED_AT).isDuplicate(UPDATED_IS_DUPLICATE);

        restNewsItemMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedNewsItem.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedNewsItem))
            )
            .andExpect(status().isOk());

        // Validate the NewsItem in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertNewsItemUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedNewsItem, newsItem), getPersistedNewsItem(newsItem));
    }

    @Test
    @Transactional
    void fullUpdateNewsItemWithPatch() throws Exception {
        // Initialize the database
        insertedNewsItem = newsItemRepository.saveAndFlush(newsItem);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the newsItem using partial update
        NewsItem partialUpdatedNewsItem = new NewsItem();
        partialUpdatedNewsItem.setId(newsItem.getId());

        partialUpdatedNewsItem
            .externalId(UPDATED_EXTERNAL_ID)
            .title(UPDATED_TITLE)
            .url(UPDATED_URL)
            .originalText(UPDATED_ORIGINAL_TEXT)
            .publishedAt(UPDATED_PUBLISHED_AT)
            .collectedAt(UPDATED_COLLECTED_AT)
            .isDuplicate(UPDATED_IS_DUPLICATE);

        restNewsItemMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedNewsItem.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedNewsItem))
            )
            .andExpect(status().isOk());

        // Validate the NewsItem in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertNewsItemUpdatableFieldsEquals(partialUpdatedNewsItem, getPersistedNewsItem(partialUpdatedNewsItem));
    }

    @Test
    @Transactional
    void patchNonExistingNewsItem() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        newsItem.setId(longCount.incrementAndGet());

        // Create the NewsItem
        NewsItemDTO newsItemDTO = newsItemMapper.toDto(newsItem);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restNewsItemMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, newsItemDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(newsItemDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the NewsItem in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchNewsItem() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        newsItem.setId(longCount.incrementAndGet());

        // Create the NewsItem
        NewsItemDTO newsItemDTO = newsItemMapper.toDto(newsItem);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restNewsItemMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(newsItemDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the NewsItem in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamNewsItem() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        newsItem.setId(longCount.incrementAndGet());

        // Create the NewsItem
        NewsItemDTO newsItemDTO = newsItemMapper.toDto(newsItem);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restNewsItemMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(newsItemDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the NewsItem in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteNewsItem() throws Exception {
        // Initialize the database
        insertedNewsItem = newsItemRepository.saveAndFlush(newsItem);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the newsItem
        restNewsItemMockMvc
            .perform(delete(ENTITY_API_URL_ID, newsItem.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return newsItemRepository.count();
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

    protected NewsItem getPersistedNewsItem(NewsItem newsItem) {
        return newsItemRepository.findById(newsItem.getId()).orElseThrow();
    }

    protected void assertPersistedNewsItemToMatchAllProperties(NewsItem expectedNewsItem) {
        assertNewsItemAllPropertiesEquals(expectedNewsItem, getPersistedNewsItem(expectedNewsItem));
    }

    protected void assertPersistedNewsItemToMatchUpdatableProperties(NewsItem expectedNewsItem) {
        assertNewsItemAllUpdatablePropertiesEquals(expectedNewsItem, getPersistedNewsItem(expectedNewsItem));
    }
}
