package com.mycompany.myapp.web.rest;

import static com.mycompany.myapp.domain.KeywordAsserts.*;
import static com.mycompany.myapp.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.Competitor;
import com.mycompany.myapp.domain.Keyword;
import com.mycompany.myapp.repository.KeywordRepository;
import com.mycompany.myapp.service.KeywordService;
import com.mycompany.myapp.service.dto.KeywordDTO;
import com.mycompany.myapp.service.mapper.KeywordMapper;
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
 * Integration tests for the {@link KeywordResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class KeywordResourceIT {

    private static final String DEFAULT_VALUE = "AAAAAAAAAA";
    private static final String UPDATED_VALUE = "BBBBBBBBBB";

    private static final Boolean DEFAULT_IS_ACTIVE = false;
    private static final Boolean UPDATED_IS_ACTIVE = true;

    private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/keywords";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private KeywordRepository keywordRepository;

    @Mock
    private KeywordRepository keywordRepositoryMock;

    @Autowired
    private KeywordMapper keywordMapper;

    @Mock
    private KeywordService keywordServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restKeywordMockMvc;

    private Keyword keyword;

    private Keyword insertedKeyword;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Keyword createEntity(EntityManager em) {
        Keyword keyword = new Keyword().value(DEFAULT_VALUE).isActive(DEFAULT_IS_ACTIVE).createdAt(DEFAULT_CREATED_AT);
        // Add required entity
        Competitor competitor;
        if (TestUtil.findAll(em, Competitor.class).isEmpty()) {
            competitor = CompetitorResourceIT.createEntity();
            em.persist(competitor);
            em.flush();
        } else {
            competitor = TestUtil.findAll(em, Competitor.class).get(0);
        }
        keyword.setCompetitor(competitor);
        return keyword;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Keyword createUpdatedEntity(EntityManager em) {
        Keyword updatedKeyword = new Keyword().value(UPDATED_VALUE).isActive(UPDATED_IS_ACTIVE).createdAt(UPDATED_CREATED_AT);
        // Add required entity
        Competitor competitor;
        if (TestUtil.findAll(em, Competitor.class).isEmpty()) {
            competitor = CompetitorResourceIT.createUpdatedEntity();
            em.persist(competitor);
            em.flush();
        } else {
            competitor = TestUtil.findAll(em, Competitor.class).get(0);
        }
        updatedKeyword.setCompetitor(competitor);
        return updatedKeyword;
    }

    @BeforeEach
    void initTest() {
        keyword = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedKeyword != null) {
            keywordRepository.delete(insertedKeyword);
            insertedKeyword = null;
        }
    }

    @Test
    @Transactional
    void createKeyword() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Keyword
        KeywordDTO keywordDTO = keywordMapper.toDto(keyword);
        var returnedKeywordDTO = om.readValue(
            restKeywordMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(keywordDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            KeywordDTO.class
        );

        // Validate the Keyword in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedKeyword = keywordMapper.toEntity(returnedKeywordDTO);
        assertKeywordUpdatableFieldsEquals(returnedKeyword, getPersistedKeyword(returnedKeyword));

        insertedKeyword = returnedKeyword;
    }

    @Test
    @Transactional
    void createKeywordWithExistingId() throws Exception {
        // Create the Keyword with an existing ID
        keyword.setId(1L);
        KeywordDTO keywordDTO = keywordMapper.toDto(keyword);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restKeywordMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(keywordDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Keyword in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkValueIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        keyword.setValue(null);

        // Create the Keyword, which fails.
        KeywordDTO keywordDTO = keywordMapper.toDto(keyword);

        restKeywordMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(keywordDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkIsActiveIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        keyword.setIsActive(null);

        // Create the Keyword, which fails.
        KeywordDTO keywordDTO = keywordMapper.toDto(keyword);

        restKeywordMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(keywordDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCreatedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        keyword.setCreatedAt(null);

        // Create the Keyword, which fails.
        KeywordDTO keywordDTO = keywordMapper.toDto(keyword);

        restKeywordMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(keywordDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllKeywords() throws Exception {
        // Initialize the database
        insertedKeyword = keywordRepository.saveAndFlush(keyword);

        // Get all the keywordList
        restKeywordMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(keyword.getId().intValue())))
            .andExpect(jsonPath("$.[*].value").value(hasItem(DEFAULT_VALUE)))
            .andExpect(jsonPath("$.[*].isActive").value(hasItem(DEFAULT_IS_ACTIVE)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllKeywordsWithEagerRelationshipsIsEnabled() throws Exception {
        when(keywordServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restKeywordMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(keywordServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllKeywordsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(keywordServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restKeywordMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(keywordRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getKeyword() throws Exception {
        // Initialize the database
        insertedKeyword = keywordRepository.saveAndFlush(keyword);

        // Get the keyword
        restKeywordMockMvc
            .perform(get(ENTITY_API_URL_ID, keyword.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(keyword.getId().intValue()))
            .andExpect(jsonPath("$.value").value(DEFAULT_VALUE))
            .andExpect(jsonPath("$.isActive").value(DEFAULT_IS_ACTIVE))
            .andExpect(jsonPath("$.createdAt").value(DEFAULT_CREATED_AT.toString()));
    }

    @Test
    @Transactional
    void getKeywordsByIdFiltering() throws Exception {
        // Initialize the database
        insertedKeyword = keywordRepository.saveAndFlush(keyword);

        Long id = keyword.getId();

        defaultKeywordFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultKeywordFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultKeywordFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllKeywordsByValueIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedKeyword = keywordRepository.saveAndFlush(keyword);

        // Get all the keywordList where value equals to
        defaultKeywordFiltering("value.equals=" + DEFAULT_VALUE, "value.equals=" + UPDATED_VALUE);
    }

    @Test
    @Transactional
    void getAllKeywordsByValueIsInShouldWork() throws Exception {
        // Initialize the database
        insertedKeyword = keywordRepository.saveAndFlush(keyword);

        // Get all the keywordList where value in
        defaultKeywordFiltering("value.in=" + DEFAULT_VALUE + "," + UPDATED_VALUE, "value.in=" + UPDATED_VALUE);
    }

    @Test
    @Transactional
    void getAllKeywordsByValueIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedKeyword = keywordRepository.saveAndFlush(keyword);

        // Get all the keywordList where value is not null
        defaultKeywordFiltering("value.specified=true", "value.specified=false");
    }

    @Test
    @Transactional
    void getAllKeywordsByValueContainsSomething() throws Exception {
        // Initialize the database
        insertedKeyword = keywordRepository.saveAndFlush(keyword);

        // Get all the keywordList where value contains
        defaultKeywordFiltering("value.contains=" + DEFAULT_VALUE, "value.contains=" + UPDATED_VALUE);
    }

    @Test
    @Transactional
    void getAllKeywordsByValueNotContainsSomething() throws Exception {
        // Initialize the database
        insertedKeyword = keywordRepository.saveAndFlush(keyword);

        // Get all the keywordList where value does not contain
        defaultKeywordFiltering("value.doesNotContain=" + UPDATED_VALUE, "value.doesNotContain=" + DEFAULT_VALUE);
    }

    @Test
    @Transactional
    void getAllKeywordsByIsActiveIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedKeyword = keywordRepository.saveAndFlush(keyword);

        // Get all the keywordList where isActive equals to
        defaultKeywordFiltering("isActive.equals=" + DEFAULT_IS_ACTIVE, "isActive.equals=" + UPDATED_IS_ACTIVE);
    }

    @Test
    @Transactional
    void getAllKeywordsByIsActiveIsInShouldWork() throws Exception {
        // Initialize the database
        insertedKeyword = keywordRepository.saveAndFlush(keyword);

        // Get all the keywordList where isActive in
        defaultKeywordFiltering("isActive.in=" + DEFAULT_IS_ACTIVE + "," + UPDATED_IS_ACTIVE, "isActive.in=" + UPDATED_IS_ACTIVE);
    }

    @Test
    @Transactional
    void getAllKeywordsByIsActiveIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedKeyword = keywordRepository.saveAndFlush(keyword);

        // Get all the keywordList where isActive is not null
        defaultKeywordFiltering("isActive.specified=true", "isActive.specified=false");
    }

    @Test
    @Transactional
    void getAllKeywordsByCreatedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedKeyword = keywordRepository.saveAndFlush(keyword);

        // Get all the keywordList where createdAt equals to
        defaultKeywordFiltering("createdAt.equals=" + DEFAULT_CREATED_AT, "createdAt.equals=" + UPDATED_CREATED_AT);
    }

    @Test
    @Transactional
    void getAllKeywordsByCreatedAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedKeyword = keywordRepository.saveAndFlush(keyword);

        // Get all the keywordList where createdAt in
        defaultKeywordFiltering("createdAt.in=" + DEFAULT_CREATED_AT + "," + UPDATED_CREATED_AT, "createdAt.in=" + UPDATED_CREATED_AT);
    }

    @Test
    @Transactional
    void getAllKeywordsByCreatedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedKeyword = keywordRepository.saveAndFlush(keyword);

        // Get all the keywordList where createdAt is not null
        defaultKeywordFiltering("createdAt.specified=true", "createdAt.specified=false");
    }

    @Test
    @Transactional
    void getAllKeywordsByCompetitorIsEqualToSomething() throws Exception {
        Competitor competitor;
        if (TestUtil.findAll(em, Competitor.class).isEmpty()) {
            keywordRepository.saveAndFlush(keyword);
            competitor = CompetitorResourceIT.createEntity();
        } else {
            competitor = TestUtil.findAll(em, Competitor.class).get(0);
        }
        em.persist(competitor);
        em.flush();
        keyword.setCompetitor(competitor);
        keywordRepository.saveAndFlush(keyword);
        Long competitorId = competitor.getId();
        // Get all the keywordList where competitor equals to competitorId
        defaultKeywordShouldBeFound("competitorId.equals=" + competitorId);

        // Get all the keywordList where competitor equals to (competitorId + 1)
        defaultKeywordShouldNotBeFound("competitorId.equals=" + (competitorId + 1));
    }

    private void defaultKeywordFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultKeywordShouldBeFound(shouldBeFound);
        defaultKeywordShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultKeywordShouldBeFound(String filter) throws Exception {
        restKeywordMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(keyword.getId().intValue())))
            .andExpect(jsonPath("$.[*].value").value(hasItem(DEFAULT_VALUE)))
            .andExpect(jsonPath("$.[*].isActive").value(hasItem(DEFAULT_IS_ACTIVE)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())));

        // Check, that the count call also returns 1
        restKeywordMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultKeywordShouldNotBeFound(String filter) throws Exception {
        restKeywordMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restKeywordMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingKeyword() throws Exception {
        // Get the keyword
        restKeywordMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingKeyword() throws Exception {
        // Initialize the database
        insertedKeyword = keywordRepository.saveAndFlush(keyword);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the keyword
        Keyword updatedKeyword = keywordRepository.findById(keyword.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedKeyword are not directly saved in db
        em.detach(updatedKeyword);
        updatedKeyword.value(UPDATED_VALUE).isActive(UPDATED_IS_ACTIVE).createdAt(UPDATED_CREATED_AT);
        KeywordDTO keywordDTO = keywordMapper.toDto(updatedKeyword);

        restKeywordMockMvc
            .perform(
                put(ENTITY_API_URL_ID, keywordDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(keywordDTO))
            )
            .andExpect(status().isOk());

        // Validate the Keyword in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedKeywordToMatchAllProperties(updatedKeyword);
    }

    @Test
    @Transactional
    void putNonExistingKeyword() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        keyword.setId(longCount.incrementAndGet());

        // Create the Keyword
        KeywordDTO keywordDTO = keywordMapper.toDto(keyword);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restKeywordMockMvc
            .perform(
                put(ENTITY_API_URL_ID, keywordDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(keywordDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Keyword in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchKeyword() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        keyword.setId(longCount.incrementAndGet());

        // Create the Keyword
        KeywordDTO keywordDTO = keywordMapper.toDto(keyword);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restKeywordMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(keywordDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Keyword in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamKeyword() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        keyword.setId(longCount.incrementAndGet());

        // Create the Keyword
        KeywordDTO keywordDTO = keywordMapper.toDto(keyword);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restKeywordMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(keywordDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Keyword in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateKeywordWithPatch() throws Exception {
        // Initialize the database
        insertedKeyword = keywordRepository.saveAndFlush(keyword);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the keyword using partial update
        Keyword partialUpdatedKeyword = new Keyword();
        partialUpdatedKeyword.setId(keyword.getId());

        partialUpdatedKeyword.isActive(UPDATED_IS_ACTIVE);

        restKeywordMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedKeyword.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedKeyword))
            )
            .andExpect(status().isOk());

        // Validate the Keyword in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertKeywordUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedKeyword, keyword), getPersistedKeyword(keyword));
    }

    @Test
    @Transactional
    void fullUpdateKeywordWithPatch() throws Exception {
        // Initialize the database
        insertedKeyword = keywordRepository.saveAndFlush(keyword);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the keyword using partial update
        Keyword partialUpdatedKeyword = new Keyword();
        partialUpdatedKeyword.setId(keyword.getId());

        partialUpdatedKeyword.value(UPDATED_VALUE).isActive(UPDATED_IS_ACTIVE).createdAt(UPDATED_CREATED_AT);

        restKeywordMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedKeyword.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedKeyword))
            )
            .andExpect(status().isOk());

        // Validate the Keyword in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertKeywordUpdatableFieldsEquals(partialUpdatedKeyword, getPersistedKeyword(partialUpdatedKeyword));
    }

    @Test
    @Transactional
    void patchNonExistingKeyword() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        keyword.setId(longCount.incrementAndGet());

        // Create the Keyword
        KeywordDTO keywordDTO = keywordMapper.toDto(keyword);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restKeywordMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, keywordDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(keywordDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Keyword in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchKeyword() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        keyword.setId(longCount.incrementAndGet());

        // Create the Keyword
        KeywordDTO keywordDTO = keywordMapper.toDto(keyword);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restKeywordMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(keywordDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Keyword in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamKeyword() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        keyword.setId(longCount.incrementAndGet());

        // Create the Keyword
        KeywordDTO keywordDTO = keywordMapper.toDto(keyword);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restKeywordMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(keywordDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Keyword in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteKeyword() throws Exception {
        // Initialize the database
        insertedKeyword = keywordRepository.saveAndFlush(keyword);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the keyword
        restKeywordMockMvc
            .perform(delete(ENTITY_API_URL_ID, keyword.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return keywordRepository.count();
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

    protected Keyword getPersistedKeyword(Keyword keyword) {
        return keywordRepository.findById(keyword.getId()).orElseThrow();
    }

    protected void assertPersistedKeywordToMatchAllProperties(Keyword expectedKeyword) {
        assertKeywordAllPropertiesEquals(expectedKeyword, getPersistedKeyword(expectedKeyword));
    }

    protected void assertPersistedKeywordToMatchUpdatableProperties(Keyword expectedKeyword) {
        assertKeywordAllUpdatablePropertiesEquals(expectedKeyword, getPersistedKeyword(expectedKeyword));
    }
}
