package com.mycompany.myapp.web.rest;

import static com.mycompany.myapp.domain.CompetitorAsserts.*;
import static com.mycompany.myapp.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.AnalystProfile;
import com.mycompany.myapp.domain.Competitor;
import com.mycompany.myapp.repository.CompetitorRepository;
import com.mycompany.myapp.service.dto.CompetitorDTO;
import com.mycompany.myapp.service.mapper.CompetitorMapper;
import jakarta.persistence.EntityManager;
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
 * Integration tests for the {@link CompetitorResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class CompetitorResourceIT {

    private static final String DEFAULT_COMPETITOR_NAME = "AAAAAAAAAA";
    private static final String UPDATED_COMPETITOR_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_WEBSITE_URL = "AAAAAAAAAA";
    private static final String UPDATED_WEBSITE_URL = "BBBBBBBBBB";

    private static final String DEFAULT_INDUSTRY = "AAAAAAAAAA";
    private static final String UPDATED_INDUSTRY = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final Boolean DEFAULT_IS_ACTIVE = false;
    private static final Boolean UPDATED_IS_ACTIVE = true;

    private static final String ENTITY_API_URL = "/api/competitors";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private CompetitorRepository competitorRepository;

    @Autowired
    private CompetitorMapper competitorMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCompetitorMockMvc;

    private Competitor competitor;

    private Competitor insertedCompetitor;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Competitor createEntity() {
        return new Competitor()
            .competitorName(DEFAULT_COMPETITOR_NAME)
            .websiteUrl(DEFAULT_WEBSITE_URL)
            .industry(DEFAULT_INDUSTRY)
            .description(DEFAULT_DESCRIPTION)
            .isActive(DEFAULT_IS_ACTIVE);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Competitor createUpdatedEntity() {
        return new Competitor()
            .competitorName(UPDATED_COMPETITOR_NAME)
            .websiteUrl(UPDATED_WEBSITE_URL)
            .industry(UPDATED_INDUSTRY)
            .description(UPDATED_DESCRIPTION)
            .isActive(UPDATED_IS_ACTIVE);
    }

    @BeforeEach
    void initTest() {
        competitor = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedCompetitor != null) {
            competitorRepository.delete(insertedCompetitor);
            insertedCompetitor = null;
        }
    }

    @Test
    @Transactional
    void createCompetitor() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Competitor
        CompetitorDTO competitorDTO = competitorMapper.toDto(competitor);
        var returnedCompetitorDTO = om.readValue(
            restCompetitorMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(competitorDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            CompetitorDTO.class
        );

        // Validate the Competitor in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedCompetitor = competitorMapper.toEntity(returnedCompetitorDTO);
        assertCompetitorUpdatableFieldsEquals(returnedCompetitor, getPersistedCompetitor(returnedCompetitor));

        insertedCompetitor = returnedCompetitor;
    }

    @Test
    @Transactional
    void createCompetitorWithExistingId() throws Exception {
        // Create the Competitor with an existing ID
        competitor.setId(1L);
        CompetitorDTO competitorDTO = competitorMapper.toDto(competitor);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restCompetitorMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(competitorDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Competitor in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkCompetitorNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        competitor.setCompetitorName(null);

        // Create the Competitor, which fails.
        CompetitorDTO competitorDTO = competitorMapper.toDto(competitor);

        restCompetitorMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(competitorDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkIsActiveIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        competitor.setIsActive(null);

        // Create the Competitor, which fails.
        CompetitorDTO competitorDTO = competitorMapper.toDto(competitor);

        restCompetitorMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(competitorDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllCompetitors() throws Exception {
        // Initialize the database
        insertedCompetitor = competitorRepository.saveAndFlush(competitor);

        // Get all the competitorList
        restCompetitorMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(competitor.getId().intValue())))
            .andExpect(jsonPath("$.[*].competitorName").value(hasItem(DEFAULT_COMPETITOR_NAME)))
            .andExpect(jsonPath("$.[*].websiteUrl").value(hasItem(DEFAULT_WEBSITE_URL)))
            .andExpect(jsonPath("$.[*].industry").value(hasItem(DEFAULT_INDUSTRY)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].isActive").value(hasItem(DEFAULT_IS_ACTIVE)));
    }

    @Test
    @Transactional
    void getCompetitor() throws Exception {
        // Initialize the database
        insertedCompetitor = competitorRepository.saveAndFlush(competitor);

        // Get the competitor
        restCompetitorMockMvc
            .perform(get(ENTITY_API_URL_ID, competitor.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(competitor.getId().intValue()))
            .andExpect(jsonPath("$.competitorName").value(DEFAULT_COMPETITOR_NAME))
            .andExpect(jsonPath("$.websiteUrl").value(DEFAULT_WEBSITE_URL))
            .andExpect(jsonPath("$.industry").value(DEFAULT_INDUSTRY))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.isActive").value(DEFAULT_IS_ACTIVE));
    }

    @Test
    @Transactional
    void getCompetitorsByIdFiltering() throws Exception {
        // Initialize the database
        insertedCompetitor = competitorRepository.saveAndFlush(competitor);

        Long id = competitor.getId();

        defaultCompetitorFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultCompetitorFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultCompetitorFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllCompetitorsByCompetitorNameIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCompetitor = competitorRepository.saveAndFlush(competitor);

        // Get all the competitorList where competitorName equals to
        defaultCompetitorFiltering("competitorName.equals=" + DEFAULT_COMPETITOR_NAME, "competitorName.equals=" + UPDATED_COMPETITOR_NAME);
    }

    @Test
    @Transactional
    void getAllCompetitorsByCompetitorNameIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCompetitor = competitorRepository.saveAndFlush(competitor);

        // Get all the competitorList where competitorName in
        defaultCompetitorFiltering(
            "competitorName.in=" + DEFAULT_COMPETITOR_NAME + "," + UPDATED_COMPETITOR_NAME,
            "competitorName.in=" + UPDATED_COMPETITOR_NAME
        );
    }

    @Test
    @Transactional
    void getAllCompetitorsByCompetitorNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCompetitor = competitorRepository.saveAndFlush(competitor);

        // Get all the competitorList where competitorName is not null
        defaultCompetitorFiltering("competitorName.specified=true", "competitorName.specified=false");
    }

    @Test
    @Transactional
    void getAllCompetitorsByCompetitorNameContainsSomething() throws Exception {
        // Initialize the database
        insertedCompetitor = competitorRepository.saveAndFlush(competitor);

        // Get all the competitorList where competitorName contains
        defaultCompetitorFiltering(
            "competitorName.contains=" + DEFAULT_COMPETITOR_NAME,
            "competitorName.contains=" + UPDATED_COMPETITOR_NAME
        );
    }

    @Test
    @Transactional
    void getAllCompetitorsByCompetitorNameNotContainsSomething() throws Exception {
        // Initialize the database
        insertedCompetitor = competitorRepository.saveAndFlush(competitor);

        // Get all the competitorList where competitorName does not contain
        defaultCompetitorFiltering(
            "competitorName.doesNotContain=" + UPDATED_COMPETITOR_NAME,
            "competitorName.doesNotContain=" + DEFAULT_COMPETITOR_NAME
        );
    }

    @Test
    @Transactional
    void getAllCompetitorsByWebsiteUrlIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCompetitor = competitorRepository.saveAndFlush(competitor);

        // Get all the competitorList where websiteUrl equals to
        defaultCompetitorFiltering("websiteUrl.equals=" + DEFAULT_WEBSITE_URL, "websiteUrl.equals=" + UPDATED_WEBSITE_URL);
    }

    @Test
    @Transactional
    void getAllCompetitorsByWebsiteUrlIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCompetitor = competitorRepository.saveAndFlush(competitor);

        // Get all the competitorList where websiteUrl in
        defaultCompetitorFiltering(
            "websiteUrl.in=" + DEFAULT_WEBSITE_URL + "," + UPDATED_WEBSITE_URL,
            "websiteUrl.in=" + UPDATED_WEBSITE_URL
        );
    }

    @Test
    @Transactional
    void getAllCompetitorsByWebsiteUrlIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCompetitor = competitorRepository.saveAndFlush(competitor);

        // Get all the competitorList where websiteUrl is not null
        defaultCompetitorFiltering("websiteUrl.specified=true", "websiteUrl.specified=false");
    }

    @Test
    @Transactional
    void getAllCompetitorsByWebsiteUrlContainsSomething() throws Exception {
        // Initialize the database
        insertedCompetitor = competitorRepository.saveAndFlush(competitor);

        // Get all the competitorList where websiteUrl contains
        defaultCompetitorFiltering("websiteUrl.contains=" + DEFAULT_WEBSITE_URL, "websiteUrl.contains=" + UPDATED_WEBSITE_URL);
    }

    @Test
    @Transactional
    void getAllCompetitorsByWebsiteUrlNotContainsSomething() throws Exception {
        // Initialize the database
        insertedCompetitor = competitorRepository.saveAndFlush(competitor);

        // Get all the competitorList where websiteUrl does not contain
        defaultCompetitorFiltering("websiteUrl.doesNotContain=" + UPDATED_WEBSITE_URL, "websiteUrl.doesNotContain=" + DEFAULT_WEBSITE_URL);
    }

    @Test
    @Transactional
    void getAllCompetitorsByIndustryIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCompetitor = competitorRepository.saveAndFlush(competitor);

        // Get all the competitorList where industry equals to
        defaultCompetitorFiltering("industry.equals=" + DEFAULT_INDUSTRY, "industry.equals=" + UPDATED_INDUSTRY);
    }

    @Test
    @Transactional
    void getAllCompetitorsByIndustryIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCompetitor = competitorRepository.saveAndFlush(competitor);

        // Get all the competitorList where industry in
        defaultCompetitorFiltering("industry.in=" + DEFAULT_INDUSTRY + "," + UPDATED_INDUSTRY, "industry.in=" + UPDATED_INDUSTRY);
    }

    @Test
    @Transactional
    void getAllCompetitorsByIndustryIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCompetitor = competitorRepository.saveAndFlush(competitor);

        // Get all the competitorList where industry is not null
        defaultCompetitorFiltering("industry.specified=true", "industry.specified=false");
    }

    @Test
    @Transactional
    void getAllCompetitorsByIndustryContainsSomething() throws Exception {
        // Initialize the database
        insertedCompetitor = competitorRepository.saveAndFlush(competitor);

        // Get all the competitorList where industry contains
        defaultCompetitorFiltering("industry.contains=" + DEFAULT_INDUSTRY, "industry.contains=" + UPDATED_INDUSTRY);
    }

    @Test
    @Transactional
    void getAllCompetitorsByIndustryNotContainsSomething() throws Exception {
        // Initialize the database
        insertedCompetitor = competitorRepository.saveAndFlush(competitor);

        // Get all the competitorList where industry does not contain
        defaultCompetitorFiltering("industry.doesNotContain=" + UPDATED_INDUSTRY, "industry.doesNotContain=" + DEFAULT_INDUSTRY);
    }

    @Test
    @Transactional
    void getAllCompetitorsByDescriptionIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCompetitor = competitorRepository.saveAndFlush(competitor);

        // Get all the competitorList where description equals to
        defaultCompetitorFiltering("description.equals=" + DEFAULT_DESCRIPTION, "description.equals=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    void getAllCompetitorsByDescriptionIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCompetitor = competitorRepository.saveAndFlush(competitor);

        // Get all the competitorList where description in
        defaultCompetitorFiltering(
            "description.in=" + DEFAULT_DESCRIPTION + "," + UPDATED_DESCRIPTION,
            "description.in=" + UPDATED_DESCRIPTION
        );
    }

    @Test
    @Transactional
    void getAllCompetitorsByDescriptionIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCompetitor = competitorRepository.saveAndFlush(competitor);

        // Get all the competitorList where description is not null
        defaultCompetitorFiltering("description.specified=true", "description.specified=false");
    }

    @Test
    @Transactional
    void getAllCompetitorsByDescriptionContainsSomething() throws Exception {
        // Initialize the database
        insertedCompetitor = competitorRepository.saveAndFlush(competitor);

        // Get all the competitorList where description contains
        defaultCompetitorFiltering("description.contains=" + DEFAULT_DESCRIPTION, "description.contains=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    void getAllCompetitorsByDescriptionNotContainsSomething() throws Exception {
        // Initialize the database
        insertedCompetitor = competitorRepository.saveAndFlush(competitor);

        // Get all the competitorList where description does not contain
        defaultCompetitorFiltering(
            "description.doesNotContain=" + UPDATED_DESCRIPTION,
            "description.doesNotContain=" + DEFAULT_DESCRIPTION
        );
    }

    @Test
    @Transactional
    void getAllCompetitorsByIsActiveIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCompetitor = competitorRepository.saveAndFlush(competitor);

        // Get all the competitorList where isActive equals to
        defaultCompetitorFiltering("isActive.equals=" + DEFAULT_IS_ACTIVE, "isActive.equals=" + UPDATED_IS_ACTIVE);
    }

    @Test
    @Transactional
    void getAllCompetitorsByIsActiveIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCompetitor = competitorRepository.saveAndFlush(competitor);

        // Get all the competitorList where isActive in
        defaultCompetitorFiltering("isActive.in=" + DEFAULT_IS_ACTIVE + "," + UPDATED_IS_ACTIVE, "isActive.in=" + UPDATED_IS_ACTIVE);
    }

    @Test
    @Transactional
    void getAllCompetitorsByIsActiveIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCompetitor = competitorRepository.saveAndFlush(competitor);

        // Get all the competitorList where isActive is not null
        defaultCompetitorFiltering("isActive.specified=true", "isActive.specified=false");
    }

    @Test
    @Transactional
    void getAllCompetitorsByAnalystProfilesIsEqualToSomething() throws Exception {
        AnalystProfile analystProfiles;
        if (TestUtil.findAll(em, AnalystProfile.class).isEmpty()) {
            competitorRepository.saveAndFlush(competitor);
            analystProfiles = AnalystProfileResourceIT.createEntity(em);
        } else {
            analystProfiles = TestUtil.findAll(em, AnalystProfile.class).get(0);
        }
        em.persist(analystProfiles);
        em.flush();
        competitor.addAnalystProfiles(analystProfiles);
        competitorRepository.saveAndFlush(competitor);
        Long analystProfilesId = analystProfiles.getId();
        // Get all the competitorList where analystProfiles equals to analystProfilesId
        defaultCompetitorShouldBeFound("analystProfilesId.equals=" + analystProfilesId);

        // Get all the competitorList where analystProfiles equals to (analystProfilesId + 1)
        defaultCompetitorShouldNotBeFound("analystProfilesId.equals=" + (analystProfilesId + 1));
    }

    private void defaultCompetitorFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultCompetitorShouldBeFound(shouldBeFound);
        defaultCompetitorShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultCompetitorShouldBeFound(String filter) throws Exception {
        restCompetitorMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(competitor.getId().intValue())))
            .andExpect(jsonPath("$.[*].competitorName").value(hasItem(DEFAULT_COMPETITOR_NAME)))
            .andExpect(jsonPath("$.[*].websiteUrl").value(hasItem(DEFAULT_WEBSITE_URL)))
            .andExpect(jsonPath("$.[*].industry").value(hasItem(DEFAULT_INDUSTRY)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].isActive").value(hasItem(DEFAULT_IS_ACTIVE)));

        // Check, that the count call also returns 1
        restCompetitorMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultCompetitorShouldNotBeFound(String filter) throws Exception {
        restCompetitorMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restCompetitorMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingCompetitor() throws Exception {
        // Get the competitor
        restCompetitorMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingCompetitor() throws Exception {
        // Initialize the database
        insertedCompetitor = competitorRepository.saveAndFlush(competitor);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the competitor
        Competitor updatedCompetitor = competitorRepository.findById(competitor.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedCompetitor are not directly saved in db
        em.detach(updatedCompetitor);
        updatedCompetitor
            .competitorName(UPDATED_COMPETITOR_NAME)
            .websiteUrl(UPDATED_WEBSITE_URL)
            .industry(UPDATED_INDUSTRY)
            .description(UPDATED_DESCRIPTION)
            .isActive(UPDATED_IS_ACTIVE);
        CompetitorDTO competitorDTO = competitorMapper.toDto(updatedCompetitor);

        restCompetitorMockMvc
            .perform(
                put(ENTITY_API_URL_ID, competitorDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(competitorDTO))
            )
            .andExpect(status().isOk());

        // Validate the Competitor in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedCompetitorToMatchAllProperties(updatedCompetitor);
    }

    @Test
    @Transactional
    void putNonExistingCompetitor() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        competitor.setId(longCount.incrementAndGet());

        // Create the Competitor
        CompetitorDTO competitorDTO = competitorMapper.toDto(competitor);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCompetitorMockMvc
            .perform(
                put(ENTITY_API_URL_ID, competitorDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(competitorDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Competitor in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchCompetitor() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        competitor.setId(longCount.incrementAndGet());

        // Create the Competitor
        CompetitorDTO competitorDTO = competitorMapper.toDto(competitor);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCompetitorMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(competitorDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Competitor in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamCompetitor() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        competitor.setId(longCount.incrementAndGet());

        // Create the Competitor
        CompetitorDTO competitorDTO = competitorMapper.toDto(competitor);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCompetitorMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(competitorDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Competitor in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateCompetitorWithPatch() throws Exception {
        // Initialize the database
        insertedCompetitor = competitorRepository.saveAndFlush(competitor);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the competitor using partial update
        Competitor partialUpdatedCompetitor = new Competitor();
        partialUpdatedCompetitor.setId(competitor.getId());

        partialUpdatedCompetitor.competitorName(UPDATED_COMPETITOR_NAME).description(UPDATED_DESCRIPTION).isActive(UPDATED_IS_ACTIVE);

        restCompetitorMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCompetitor.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedCompetitor))
            )
            .andExpect(status().isOk());

        // Validate the Competitor in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCompetitorUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedCompetitor, competitor),
            getPersistedCompetitor(competitor)
        );
    }

    @Test
    @Transactional
    void fullUpdateCompetitorWithPatch() throws Exception {
        // Initialize the database
        insertedCompetitor = competitorRepository.saveAndFlush(competitor);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the competitor using partial update
        Competitor partialUpdatedCompetitor = new Competitor();
        partialUpdatedCompetitor.setId(competitor.getId());

        partialUpdatedCompetitor
            .competitorName(UPDATED_COMPETITOR_NAME)
            .websiteUrl(UPDATED_WEBSITE_URL)
            .industry(UPDATED_INDUSTRY)
            .description(UPDATED_DESCRIPTION)
            .isActive(UPDATED_IS_ACTIVE);

        restCompetitorMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCompetitor.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedCompetitor))
            )
            .andExpect(status().isOk());

        // Validate the Competitor in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCompetitorUpdatableFieldsEquals(partialUpdatedCompetitor, getPersistedCompetitor(partialUpdatedCompetitor));
    }

    @Test
    @Transactional
    void patchNonExistingCompetitor() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        competitor.setId(longCount.incrementAndGet());

        // Create the Competitor
        CompetitorDTO competitorDTO = competitorMapper.toDto(competitor);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCompetitorMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, competitorDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(competitorDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Competitor in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchCompetitor() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        competitor.setId(longCount.incrementAndGet());

        // Create the Competitor
        CompetitorDTO competitorDTO = competitorMapper.toDto(competitor);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCompetitorMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(competitorDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Competitor in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamCompetitor() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        competitor.setId(longCount.incrementAndGet());

        // Create the Competitor
        CompetitorDTO competitorDTO = competitorMapper.toDto(competitor);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCompetitorMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(competitorDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Competitor in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteCompetitor() throws Exception {
        // Initialize the database
        insertedCompetitor = competitorRepository.saveAndFlush(competitor);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the competitor
        restCompetitorMockMvc
            .perform(delete(ENTITY_API_URL_ID, competitor.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return competitorRepository.count();
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

    protected Competitor getPersistedCompetitor(Competitor competitor) {
        return competitorRepository.findById(competitor.getId()).orElseThrow();
    }

    protected void assertPersistedCompetitorToMatchAllProperties(Competitor expectedCompetitor) {
        assertCompetitorAllPropertiesEquals(expectedCompetitor, getPersistedCompetitor(expectedCompetitor));
    }

    protected void assertPersistedCompetitorToMatchUpdatableProperties(Competitor expectedCompetitor) {
        assertCompetitorAllUpdatablePropertiesEquals(expectedCompetitor, getPersistedCompetitor(expectedCompetitor));
    }
}
