package com.mycompany.myapp.web.rest;

import static com.mycompany.myapp.domain.AnalystProfileAsserts.*;
import static com.mycompany.myapp.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.AnalystProfile;
import com.mycompany.myapp.domain.Competitor;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.repository.AnalystProfileRepository;
import com.mycompany.myapp.repository.UserRepository;
import com.mycompany.myapp.service.AnalystProfileService;
import com.mycompany.myapp.service.dto.AnalystProfileDTO;
import com.mycompany.myapp.service.mapper.AnalystProfileMapper;
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
 * Integration tests for the {@link AnalystProfileResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class AnalystProfileResourceIT {

    private static final String DEFAULT_DISPLAY_NAME = "AAAAAAAAAA";
    private static final String UPDATED_DISPLAY_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_TELEGRAM_CHAT_ID = "AAAAAAAAAA";
    private static final String UPDATED_TELEGRAM_CHAT_ID = "BBBBBBBBBB";

    private static final Boolean DEFAULT_NOTIFICATION_ENABLED = false;
    private static final Boolean UPDATED_NOTIFICATION_ENABLED = true;

    private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/analyst-profiles";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private AnalystProfileRepository analystProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private AnalystProfileRepository analystProfileRepositoryMock;

    @Autowired
    private AnalystProfileMapper analystProfileMapper;

    @Mock
    private AnalystProfileService analystProfileServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restAnalystProfileMockMvc;

    private AnalystProfile analystProfile;

    private AnalystProfile insertedAnalystProfile;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AnalystProfile createEntity(EntityManager em) {
        AnalystProfile analystProfile = new AnalystProfile()
            .displayName(DEFAULT_DISPLAY_NAME)
            .telegramChatId(DEFAULT_TELEGRAM_CHAT_ID)
            .notificationEnabled(DEFAULT_NOTIFICATION_ENABLED)
            .createdAt(DEFAULT_CREATED_AT);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        analystProfile.setUser(user);
        return analystProfile;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AnalystProfile createUpdatedEntity(EntityManager em) {
        AnalystProfile updatedAnalystProfile = new AnalystProfile()
            .displayName(UPDATED_DISPLAY_NAME)
            .telegramChatId(UPDATED_TELEGRAM_CHAT_ID)
            .notificationEnabled(UPDATED_NOTIFICATION_ENABLED)
            .createdAt(UPDATED_CREATED_AT);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        updatedAnalystProfile.setUser(user);
        return updatedAnalystProfile;
    }

    @BeforeEach
    void initTest() {
        analystProfile = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedAnalystProfile != null) {
            analystProfileRepository.delete(insertedAnalystProfile);
            insertedAnalystProfile = null;
        }
    }

    @Test
    @Transactional
    void createAnalystProfile() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the AnalystProfile
        AnalystProfileDTO analystProfileDTO = analystProfileMapper.toDto(analystProfile);
        var returnedAnalystProfileDTO = om.readValue(
            restAnalystProfileMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(analystProfileDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            AnalystProfileDTO.class
        );

        // Validate the AnalystProfile in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedAnalystProfile = analystProfileMapper.toEntity(returnedAnalystProfileDTO);
        assertAnalystProfileUpdatableFieldsEquals(returnedAnalystProfile, getPersistedAnalystProfile(returnedAnalystProfile));

        insertedAnalystProfile = returnedAnalystProfile;
    }

    @Test
    @Transactional
    void createAnalystProfileWithExistingId() throws Exception {
        // Create the AnalystProfile with an existing ID
        analystProfile.setId(1L);
        AnalystProfileDTO analystProfileDTO = analystProfileMapper.toDto(analystProfile);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restAnalystProfileMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(analystProfileDTO)))
            .andExpect(status().isBadRequest());

        // Validate the AnalystProfile in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkDisplayNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        analystProfile.setDisplayName(null);

        // Create the AnalystProfile, which fails.
        AnalystProfileDTO analystProfileDTO = analystProfileMapper.toDto(analystProfile);

        restAnalystProfileMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(analystProfileDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkNotificationEnabledIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        analystProfile.setNotificationEnabled(null);

        // Create the AnalystProfile, which fails.
        AnalystProfileDTO analystProfileDTO = analystProfileMapper.toDto(analystProfile);

        restAnalystProfileMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(analystProfileDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCreatedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        analystProfile.setCreatedAt(null);

        // Create the AnalystProfile, which fails.
        AnalystProfileDTO analystProfileDTO = analystProfileMapper.toDto(analystProfile);

        restAnalystProfileMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(analystProfileDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllAnalystProfiles() throws Exception {
        // Initialize the database
        insertedAnalystProfile = analystProfileRepository.saveAndFlush(analystProfile);

        // Get all the analystProfileList
        restAnalystProfileMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(analystProfile.getId().intValue())))
            .andExpect(jsonPath("$.[*].displayName").value(hasItem(DEFAULT_DISPLAY_NAME)))
            .andExpect(jsonPath("$.[*].telegramChatId").value(hasItem(DEFAULT_TELEGRAM_CHAT_ID)))
            .andExpect(jsonPath("$.[*].notificationEnabled").value(hasItem(DEFAULT_NOTIFICATION_ENABLED)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllAnalystProfilesWithEagerRelationshipsIsEnabled() throws Exception {
        when(analystProfileServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restAnalystProfileMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(analystProfileServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllAnalystProfilesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(analystProfileServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restAnalystProfileMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(analystProfileRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getAnalystProfile() throws Exception {
        // Initialize the database
        insertedAnalystProfile = analystProfileRepository.saveAndFlush(analystProfile);

        // Get the analystProfile
        restAnalystProfileMockMvc
            .perform(get(ENTITY_API_URL_ID, analystProfile.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(analystProfile.getId().intValue()))
            .andExpect(jsonPath("$.displayName").value(DEFAULT_DISPLAY_NAME))
            .andExpect(jsonPath("$.telegramChatId").value(DEFAULT_TELEGRAM_CHAT_ID))
            .andExpect(jsonPath("$.notificationEnabled").value(DEFAULT_NOTIFICATION_ENABLED))
            .andExpect(jsonPath("$.createdAt").value(DEFAULT_CREATED_AT.toString()));
    }

    @Test
    @Transactional
    void getAnalystProfilesByIdFiltering() throws Exception {
        // Initialize the database
        insertedAnalystProfile = analystProfileRepository.saveAndFlush(analystProfile);

        Long id = analystProfile.getId();

        defaultAnalystProfileFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultAnalystProfileFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultAnalystProfileFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllAnalystProfilesByDisplayNameIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAnalystProfile = analystProfileRepository.saveAndFlush(analystProfile);

        // Get all the analystProfileList where displayName equals to
        defaultAnalystProfileFiltering("displayName.equals=" + DEFAULT_DISPLAY_NAME, "displayName.equals=" + UPDATED_DISPLAY_NAME);
    }

    @Test
    @Transactional
    void getAllAnalystProfilesByDisplayNameIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAnalystProfile = analystProfileRepository.saveAndFlush(analystProfile);

        // Get all the analystProfileList where displayName in
        defaultAnalystProfileFiltering(
            "displayName.in=" + DEFAULT_DISPLAY_NAME + "," + UPDATED_DISPLAY_NAME,
            "displayName.in=" + UPDATED_DISPLAY_NAME
        );
    }

    @Test
    @Transactional
    void getAllAnalystProfilesByDisplayNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAnalystProfile = analystProfileRepository.saveAndFlush(analystProfile);

        // Get all the analystProfileList where displayName is not null
        defaultAnalystProfileFiltering("displayName.specified=true", "displayName.specified=false");
    }

    @Test
    @Transactional
    void getAllAnalystProfilesByDisplayNameContainsSomething() throws Exception {
        // Initialize the database
        insertedAnalystProfile = analystProfileRepository.saveAndFlush(analystProfile);

        // Get all the analystProfileList where displayName contains
        defaultAnalystProfileFiltering("displayName.contains=" + DEFAULT_DISPLAY_NAME, "displayName.contains=" + UPDATED_DISPLAY_NAME);
    }

    @Test
    @Transactional
    void getAllAnalystProfilesByDisplayNameNotContainsSomething() throws Exception {
        // Initialize the database
        insertedAnalystProfile = analystProfileRepository.saveAndFlush(analystProfile);

        // Get all the analystProfileList where displayName does not contain
        defaultAnalystProfileFiltering(
            "displayName.doesNotContain=" + UPDATED_DISPLAY_NAME,
            "displayName.doesNotContain=" + DEFAULT_DISPLAY_NAME
        );
    }

    @Test
    @Transactional
    void getAllAnalystProfilesByTelegramChatIdIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAnalystProfile = analystProfileRepository.saveAndFlush(analystProfile);

        // Get all the analystProfileList where telegramChatId equals to
        defaultAnalystProfileFiltering(
            "telegramChatId.equals=" + DEFAULT_TELEGRAM_CHAT_ID,
            "telegramChatId.equals=" + UPDATED_TELEGRAM_CHAT_ID
        );
    }

    @Test
    @Transactional
    void getAllAnalystProfilesByTelegramChatIdIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAnalystProfile = analystProfileRepository.saveAndFlush(analystProfile);

        // Get all the analystProfileList where telegramChatId in
        defaultAnalystProfileFiltering(
            "telegramChatId.in=" + DEFAULT_TELEGRAM_CHAT_ID + "," + UPDATED_TELEGRAM_CHAT_ID,
            "telegramChatId.in=" + UPDATED_TELEGRAM_CHAT_ID
        );
    }

    @Test
    @Transactional
    void getAllAnalystProfilesByTelegramChatIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAnalystProfile = analystProfileRepository.saveAndFlush(analystProfile);

        // Get all the analystProfileList where telegramChatId is not null
        defaultAnalystProfileFiltering("telegramChatId.specified=true", "telegramChatId.specified=false");
    }

    @Test
    @Transactional
    void getAllAnalystProfilesByTelegramChatIdContainsSomething() throws Exception {
        // Initialize the database
        insertedAnalystProfile = analystProfileRepository.saveAndFlush(analystProfile);

        // Get all the analystProfileList where telegramChatId contains
        defaultAnalystProfileFiltering(
            "telegramChatId.contains=" + DEFAULT_TELEGRAM_CHAT_ID,
            "telegramChatId.contains=" + UPDATED_TELEGRAM_CHAT_ID
        );
    }

    @Test
    @Transactional
    void getAllAnalystProfilesByTelegramChatIdNotContainsSomething() throws Exception {
        // Initialize the database
        insertedAnalystProfile = analystProfileRepository.saveAndFlush(analystProfile);

        // Get all the analystProfileList where telegramChatId does not contain
        defaultAnalystProfileFiltering(
            "telegramChatId.doesNotContain=" + UPDATED_TELEGRAM_CHAT_ID,
            "telegramChatId.doesNotContain=" + DEFAULT_TELEGRAM_CHAT_ID
        );
    }

    @Test
    @Transactional
    void getAllAnalystProfilesByNotificationEnabledIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAnalystProfile = analystProfileRepository.saveAndFlush(analystProfile);

        // Get all the analystProfileList where notificationEnabled equals to
        defaultAnalystProfileFiltering(
            "notificationEnabled.equals=" + DEFAULT_NOTIFICATION_ENABLED,
            "notificationEnabled.equals=" + UPDATED_NOTIFICATION_ENABLED
        );
    }

    @Test
    @Transactional
    void getAllAnalystProfilesByNotificationEnabledIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAnalystProfile = analystProfileRepository.saveAndFlush(analystProfile);

        // Get all the analystProfileList where notificationEnabled in
        defaultAnalystProfileFiltering(
            "notificationEnabled.in=" + DEFAULT_NOTIFICATION_ENABLED + "," + UPDATED_NOTIFICATION_ENABLED,
            "notificationEnabled.in=" + UPDATED_NOTIFICATION_ENABLED
        );
    }

    @Test
    @Transactional
    void getAllAnalystProfilesByNotificationEnabledIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAnalystProfile = analystProfileRepository.saveAndFlush(analystProfile);

        // Get all the analystProfileList where notificationEnabled is not null
        defaultAnalystProfileFiltering("notificationEnabled.specified=true", "notificationEnabled.specified=false");
    }

    @Test
    @Transactional
    void getAllAnalystProfilesByCreatedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAnalystProfile = analystProfileRepository.saveAndFlush(analystProfile);

        // Get all the analystProfileList where createdAt equals to
        defaultAnalystProfileFiltering("createdAt.equals=" + DEFAULT_CREATED_AT, "createdAt.equals=" + UPDATED_CREATED_AT);
    }

    @Test
    @Transactional
    void getAllAnalystProfilesByCreatedAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAnalystProfile = analystProfileRepository.saveAndFlush(analystProfile);

        // Get all the analystProfileList where createdAt in
        defaultAnalystProfileFiltering(
            "createdAt.in=" + DEFAULT_CREATED_AT + "," + UPDATED_CREATED_AT,
            "createdAt.in=" + UPDATED_CREATED_AT
        );
    }

    @Test
    @Transactional
    void getAllAnalystProfilesByCreatedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAnalystProfile = analystProfileRepository.saveAndFlush(analystProfile);

        // Get all the analystProfileList where createdAt is not null
        defaultAnalystProfileFiltering("createdAt.specified=true", "createdAt.specified=false");
    }

    @Test
    @Transactional
    void getAllAnalystProfilesByUserIsEqualToSomething() throws Exception {
        // Get already existing entity
        User user = analystProfile.getUser();
        analystProfileRepository.saveAndFlush(analystProfile);
        Long userId = user.getId();
        // Get all the analystProfileList where user equals to userId
        defaultAnalystProfileShouldBeFound("userId.equals=" + userId);

        // Get all the analystProfileList where user equals to (userId + 1)
        defaultAnalystProfileShouldNotBeFound("userId.equals=" + (userId + 1));
    }

    @Test
    @Transactional
    void getAllAnalystProfilesByCompetitorsIsEqualToSomething() throws Exception {
        Competitor competitors;
        if (TestUtil.findAll(em, Competitor.class).isEmpty()) {
            analystProfileRepository.saveAndFlush(analystProfile);
            competitors = CompetitorResourceIT.createEntity();
        } else {
            competitors = TestUtil.findAll(em, Competitor.class).get(0);
        }
        em.persist(competitors);
        em.flush();
        analystProfile.addCompetitors(competitors);
        analystProfileRepository.saveAndFlush(analystProfile);
        Long competitorsId = competitors.getId();
        // Get all the analystProfileList where competitors equals to competitorsId
        defaultAnalystProfileShouldBeFound("competitorsId.equals=" + competitorsId);

        // Get all the analystProfileList where competitors equals to (competitorsId + 1)
        defaultAnalystProfileShouldNotBeFound("competitorsId.equals=" + (competitorsId + 1));
    }

    private void defaultAnalystProfileFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultAnalystProfileShouldBeFound(shouldBeFound);
        defaultAnalystProfileShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultAnalystProfileShouldBeFound(String filter) throws Exception {
        restAnalystProfileMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(analystProfile.getId().intValue())))
            .andExpect(jsonPath("$.[*].displayName").value(hasItem(DEFAULT_DISPLAY_NAME)))
            .andExpect(jsonPath("$.[*].telegramChatId").value(hasItem(DEFAULT_TELEGRAM_CHAT_ID)))
            .andExpect(jsonPath("$.[*].notificationEnabled").value(hasItem(DEFAULT_NOTIFICATION_ENABLED)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())));

        // Check, that the count call also returns 1
        restAnalystProfileMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultAnalystProfileShouldNotBeFound(String filter) throws Exception {
        restAnalystProfileMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restAnalystProfileMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingAnalystProfile() throws Exception {
        // Get the analystProfile
        restAnalystProfileMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingAnalystProfile() throws Exception {
        // Initialize the database
        insertedAnalystProfile = analystProfileRepository.saveAndFlush(analystProfile);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the analystProfile
        AnalystProfile updatedAnalystProfile = analystProfileRepository.findById(analystProfile.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedAnalystProfile are not directly saved in db
        em.detach(updatedAnalystProfile);
        updatedAnalystProfile
            .displayName(UPDATED_DISPLAY_NAME)
            .telegramChatId(UPDATED_TELEGRAM_CHAT_ID)
            .notificationEnabled(UPDATED_NOTIFICATION_ENABLED)
            .createdAt(UPDATED_CREATED_AT);
        AnalystProfileDTO analystProfileDTO = analystProfileMapper.toDto(updatedAnalystProfile);

        restAnalystProfileMockMvc
            .perform(
                put(ENTITY_API_URL_ID, analystProfileDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(analystProfileDTO))
            )
            .andExpect(status().isOk());

        // Validate the AnalystProfile in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedAnalystProfileToMatchAllProperties(updatedAnalystProfile);
    }

    @Test
    @Transactional
    void putNonExistingAnalystProfile() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        analystProfile.setId(longCount.incrementAndGet());

        // Create the AnalystProfile
        AnalystProfileDTO analystProfileDTO = analystProfileMapper.toDto(analystProfile);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAnalystProfileMockMvc
            .perform(
                put(ENTITY_API_URL_ID, analystProfileDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(analystProfileDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AnalystProfile in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchAnalystProfile() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        analystProfile.setId(longCount.incrementAndGet());

        // Create the AnalystProfile
        AnalystProfileDTO analystProfileDTO = analystProfileMapper.toDto(analystProfile);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAnalystProfileMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(analystProfileDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AnalystProfile in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamAnalystProfile() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        analystProfile.setId(longCount.incrementAndGet());

        // Create the AnalystProfile
        AnalystProfileDTO analystProfileDTO = analystProfileMapper.toDto(analystProfile);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAnalystProfileMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(analystProfileDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the AnalystProfile in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateAnalystProfileWithPatch() throws Exception {
        // Initialize the database
        insertedAnalystProfile = analystProfileRepository.saveAndFlush(analystProfile);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the analystProfile using partial update
        AnalystProfile partialUpdatedAnalystProfile = new AnalystProfile();
        partialUpdatedAnalystProfile.setId(analystProfile.getId());

        partialUpdatedAnalystProfile
            .displayName(UPDATED_DISPLAY_NAME)
            .telegramChatId(UPDATED_TELEGRAM_CHAT_ID)
            .notificationEnabled(UPDATED_NOTIFICATION_ENABLED);

        restAnalystProfileMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAnalystProfile.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedAnalystProfile))
            )
            .andExpect(status().isOk());

        // Validate the AnalystProfile in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAnalystProfileUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedAnalystProfile, analystProfile),
            getPersistedAnalystProfile(analystProfile)
        );
    }

    @Test
    @Transactional
    void fullUpdateAnalystProfileWithPatch() throws Exception {
        // Initialize the database
        insertedAnalystProfile = analystProfileRepository.saveAndFlush(analystProfile);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the analystProfile using partial update
        AnalystProfile partialUpdatedAnalystProfile = new AnalystProfile();
        partialUpdatedAnalystProfile.setId(analystProfile.getId());

        partialUpdatedAnalystProfile
            .displayName(UPDATED_DISPLAY_NAME)
            .telegramChatId(UPDATED_TELEGRAM_CHAT_ID)
            .notificationEnabled(UPDATED_NOTIFICATION_ENABLED)
            .createdAt(UPDATED_CREATED_AT);

        restAnalystProfileMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAnalystProfile.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedAnalystProfile))
            )
            .andExpect(status().isOk());

        // Validate the AnalystProfile in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAnalystProfileUpdatableFieldsEquals(partialUpdatedAnalystProfile, getPersistedAnalystProfile(partialUpdatedAnalystProfile));
    }

    @Test
    @Transactional
    void patchNonExistingAnalystProfile() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        analystProfile.setId(longCount.incrementAndGet());

        // Create the AnalystProfile
        AnalystProfileDTO analystProfileDTO = analystProfileMapper.toDto(analystProfile);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAnalystProfileMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, analystProfileDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(analystProfileDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AnalystProfile in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchAnalystProfile() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        analystProfile.setId(longCount.incrementAndGet());

        // Create the AnalystProfile
        AnalystProfileDTO analystProfileDTO = analystProfileMapper.toDto(analystProfile);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAnalystProfileMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(analystProfileDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AnalystProfile in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamAnalystProfile() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        analystProfile.setId(longCount.incrementAndGet());

        // Create the AnalystProfile
        AnalystProfileDTO analystProfileDTO = analystProfileMapper.toDto(analystProfile);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAnalystProfileMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(analystProfileDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the AnalystProfile in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteAnalystProfile() throws Exception {
        // Initialize the database
        insertedAnalystProfile = analystProfileRepository.saveAndFlush(analystProfile);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the analystProfile
        restAnalystProfileMockMvc
            .perform(delete(ENTITY_API_URL_ID, analystProfile.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return analystProfileRepository.count();
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

    protected AnalystProfile getPersistedAnalystProfile(AnalystProfile analystProfile) {
        return analystProfileRepository.findById(analystProfile.getId()).orElseThrow();
    }

    protected void assertPersistedAnalystProfileToMatchAllProperties(AnalystProfile expectedAnalystProfile) {
        assertAnalystProfileAllPropertiesEquals(expectedAnalystProfile, getPersistedAnalystProfile(expectedAnalystProfile));
    }

    protected void assertPersistedAnalystProfileToMatchUpdatableProperties(AnalystProfile expectedAnalystProfile) {
        assertAnalystProfileAllUpdatablePropertiesEquals(expectedAnalystProfile, getPersistedAnalystProfile(expectedAnalystProfile));
    }
}
