package com.nexaflow.userservice.web.rest;

import static com.nexaflow.userservice.domain.OrganizationAsserts.*;
import static com.nexaflow.userservice.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexaflow.userservice.IntegrationTest;
import com.nexaflow.userservice.domain.Organization;
import com.nexaflow.userservice.repository.OrganizationRepository;
import com.nexaflow.userservice.service.dto.OrganizationDTO;
import com.nexaflow.userservice.service.mapper.OrganizationMapper;
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
 * Integration tests for the {@link OrganizationResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class OrganizationResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_SLUG = "AAAAAAAAAA";
    private static final String UPDATED_SLUG = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_UPDATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_UPDATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final String ENTITY_API_URL = "/api/organizations";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizationMapper organizationMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restOrganizationMockMvc;

    private Organization organization;

    private Organization insertedOrganization;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Organization createEntity() {
        return new Organization()
            .name(DEFAULT_NAME)
            .slug(DEFAULT_SLUG)
            .description(DEFAULT_DESCRIPTION)
            .createdAt(DEFAULT_CREATED_AT)
            .updatedAt(DEFAULT_UPDATED_AT)
            .active(DEFAULT_ACTIVE);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Organization createUpdatedEntity() {
        return new Organization()
            .name(UPDATED_NAME)
            .slug(UPDATED_SLUG)
            .description(UPDATED_DESCRIPTION)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT)
            .active(UPDATED_ACTIVE);
    }

    @BeforeEach
    void initTest() {
        organization = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedOrganization != null) {
            organizationRepository.delete(insertedOrganization);
            insertedOrganization = null;
        }
    }

    @Test
    @Transactional
    void createOrganization() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Organization
        OrganizationDTO organizationDTO = organizationMapper.toDto(organization);
        var returnedOrganizationDTO = om.readValue(
            restOrganizationMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(organizationDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            OrganizationDTO.class
        );

        // Validate the Organization in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedOrganization = organizationMapper.toEntity(returnedOrganizationDTO);
        assertOrganizationUpdatableFieldsEquals(returnedOrganization, getPersistedOrganization(returnedOrganization));

        insertedOrganization = returnedOrganization;
    }

    @Test
    @Transactional
    void createOrganizationWithExistingId() throws Exception {
        // Create the Organization with an existing ID
        organization.setId(1L);
        OrganizationDTO organizationDTO = organizationMapper.toDto(organization);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restOrganizationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(organizationDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Organization in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        organization.setName(null);

        // Create the Organization, which fails.
        OrganizationDTO organizationDTO = organizationMapper.toDto(organization);

        restOrganizationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(organizationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkSlugIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        organization.setSlug(null);

        // Create the Organization, which fails.
        OrganizationDTO organizationDTO = organizationMapper.toDto(organization);

        restOrganizationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(organizationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCreatedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        organization.setCreatedAt(null);

        // Create the Organization, which fails.
        OrganizationDTO organizationDTO = organizationMapper.toDto(organization);

        restOrganizationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(organizationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkActiveIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        organization.setActive(null);

        // Create the Organization, which fails.
        OrganizationDTO organizationDTO = organizationMapper.toDto(organization);

        restOrganizationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(organizationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllOrganizations() throws Exception {
        // Initialize the database
        insertedOrganization = organizationRepository.saveAndFlush(organization);

        // Get all the organizationList
        restOrganizationMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(organization.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].slug").value(hasItem(DEFAULT_SLUG)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(DEFAULT_UPDATED_AT.toString())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE)));
    }

    @Test
    @Transactional
    void getOrganization() throws Exception {
        // Initialize the database
        insertedOrganization = organizationRepository.saveAndFlush(organization);

        // Get the organization
        restOrganizationMockMvc
            .perform(get(ENTITY_API_URL_ID, organization.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(organization.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.slug").value(DEFAULT_SLUG))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.createdAt").value(DEFAULT_CREATED_AT.toString()))
            .andExpect(jsonPath("$.updatedAt").value(DEFAULT_UPDATED_AT.toString()))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE));
    }

    @Test
    @Transactional
    void getOrganizationsByIdFiltering() throws Exception {
        // Initialize the database
        insertedOrganization = organizationRepository.saveAndFlush(organization);

        Long id = organization.getId();

        defaultOrganizationFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultOrganizationFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultOrganizationFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllOrganizationsByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedOrganization = organizationRepository.saveAndFlush(organization);

        // Get all the organizationList where name equals to
        defaultOrganizationFiltering("name.equals=" + DEFAULT_NAME, "name.equals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllOrganizationsByNameIsInShouldWork() throws Exception {
        // Initialize the database
        insertedOrganization = organizationRepository.saveAndFlush(organization);

        // Get all the organizationList where name in
        defaultOrganizationFiltering("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME, "name.in=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllOrganizationsByNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedOrganization = organizationRepository.saveAndFlush(organization);

        // Get all the organizationList where name is not null
        defaultOrganizationFiltering("name.specified=true", "name.specified=false");
    }

    @Test
    @Transactional
    void getAllOrganizationsByNameContainsSomething() throws Exception {
        // Initialize the database
        insertedOrganization = organizationRepository.saveAndFlush(organization);

        // Get all the organizationList where name contains
        defaultOrganizationFiltering("name.contains=" + DEFAULT_NAME, "name.contains=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllOrganizationsByNameNotContainsSomething() throws Exception {
        // Initialize the database
        insertedOrganization = organizationRepository.saveAndFlush(organization);

        // Get all the organizationList where name does not contain
        defaultOrganizationFiltering("name.doesNotContain=" + UPDATED_NAME, "name.doesNotContain=" + DEFAULT_NAME);
    }

    @Test
    @Transactional
    void getAllOrganizationsBySlugIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedOrganization = organizationRepository.saveAndFlush(organization);

        // Get all the organizationList where slug equals to
        defaultOrganizationFiltering("slug.equals=" + DEFAULT_SLUG, "slug.equals=" + UPDATED_SLUG);
    }

    @Test
    @Transactional
    void getAllOrganizationsBySlugIsInShouldWork() throws Exception {
        // Initialize the database
        insertedOrganization = organizationRepository.saveAndFlush(organization);

        // Get all the organizationList where slug in
        defaultOrganizationFiltering("slug.in=" + DEFAULT_SLUG + "," + UPDATED_SLUG, "slug.in=" + UPDATED_SLUG);
    }

    @Test
    @Transactional
    void getAllOrganizationsBySlugIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedOrganization = organizationRepository.saveAndFlush(organization);

        // Get all the organizationList where slug is not null
        defaultOrganizationFiltering("slug.specified=true", "slug.specified=false");
    }

    @Test
    @Transactional
    void getAllOrganizationsBySlugContainsSomething() throws Exception {
        // Initialize the database
        insertedOrganization = organizationRepository.saveAndFlush(organization);

        // Get all the organizationList where slug contains
        defaultOrganizationFiltering("slug.contains=" + DEFAULT_SLUG, "slug.contains=" + UPDATED_SLUG);
    }

    @Test
    @Transactional
    void getAllOrganizationsBySlugNotContainsSomething() throws Exception {
        // Initialize the database
        insertedOrganization = organizationRepository.saveAndFlush(organization);

        // Get all the organizationList where slug does not contain
        defaultOrganizationFiltering("slug.doesNotContain=" + UPDATED_SLUG, "slug.doesNotContain=" + DEFAULT_SLUG);
    }

    @Test
    @Transactional
    void getAllOrganizationsByDescriptionIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedOrganization = organizationRepository.saveAndFlush(organization);

        // Get all the organizationList where description equals to
        defaultOrganizationFiltering("description.equals=" + DEFAULT_DESCRIPTION, "description.equals=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    void getAllOrganizationsByDescriptionIsInShouldWork() throws Exception {
        // Initialize the database
        insertedOrganization = organizationRepository.saveAndFlush(organization);

        // Get all the organizationList where description in
        defaultOrganizationFiltering(
            "description.in=" + DEFAULT_DESCRIPTION + "," + UPDATED_DESCRIPTION,
            "description.in=" + UPDATED_DESCRIPTION
        );
    }

    @Test
    @Transactional
    void getAllOrganizationsByDescriptionIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedOrganization = organizationRepository.saveAndFlush(organization);

        // Get all the organizationList where description is not null
        defaultOrganizationFiltering("description.specified=true", "description.specified=false");
    }

    @Test
    @Transactional
    void getAllOrganizationsByDescriptionContainsSomething() throws Exception {
        // Initialize the database
        insertedOrganization = organizationRepository.saveAndFlush(organization);

        // Get all the organizationList where description contains
        defaultOrganizationFiltering("description.contains=" + DEFAULT_DESCRIPTION, "description.contains=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    void getAllOrganizationsByDescriptionNotContainsSomething() throws Exception {
        // Initialize the database
        insertedOrganization = organizationRepository.saveAndFlush(organization);

        // Get all the organizationList where description does not contain
        defaultOrganizationFiltering(
            "description.doesNotContain=" + UPDATED_DESCRIPTION,
            "description.doesNotContain=" + DEFAULT_DESCRIPTION
        );
    }

    @Test
    @Transactional
    void getAllOrganizationsByCreatedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedOrganization = organizationRepository.saveAndFlush(organization);

        // Get all the organizationList where createdAt equals to
        defaultOrganizationFiltering("createdAt.equals=" + DEFAULT_CREATED_AT, "createdAt.equals=" + UPDATED_CREATED_AT);
    }

    @Test
    @Transactional
    void getAllOrganizationsByCreatedAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedOrganization = organizationRepository.saveAndFlush(organization);

        // Get all the organizationList where createdAt in
        defaultOrganizationFiltering("createdAt.in=" + DEFAULT_CREATED_AT + "," + UPDATED_CREATED_AT, "createdAt.in=" + UPDATED_CREATED_AT);
    }

    @Test
    @Transactional
    void getAllOrganizationsByCreatedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedOrganization = organizationRepository.saveAndFlush(organization);

        // Get all the organizationList where createdAt is not null
        defaultOrganizationFiltering("createdAt.specified=true", "createdAt.specified=false");
    }

    @Test
    @Transactional
    void getAllOrganizationsByUpdatedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedOrganization = organizationRepository.saveAndFlush(organization);

        // Get all the organizationList where updatedAt equals to
        defaultOrganizationFiltering("updatedAt.equals=" + DEFAULT_UPDATED_AT, "updatedAt.equals=" + UPDATED_UPDATED_AT);
    }

    @Test
    @Transactional
    void getAllOrganizationsByUpdatedAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedOrganization = organizationRepository.saveAndFlush(organization);

        // Get all the organizationList where updatedAt in
        defaultOrganizationFiltering("updatedAt.in=" + DEFAULT_UPDATED_AT + "," + UPDATED_UPDATED_AT, "updatedAt.in=" + UPDATED_UPDATED_AT);
    }

    @Test
    @Transactional
    void getAllOrganizationsByUpdatedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedOrganization = organizationRepository.saveAndFlush(organization);

        // Get all the organizationList where updatedAt is not null
        defaultOrganizationFiltering("updatedAt.specified=true", "updatedAt.specified=false");
    }

    @Test
    @Transactional
    void getAllOrganizationsByActiveIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedOrganization = organizationRepository.saveAndFlush(organization);

        // Get all the organizationList where active equals to
        defaultOrganizationFiltering("active.equals=" + DEFAULT_ACTIVE, "active.equals=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllOrganizationsByActiveIsInShouldWork() throws Exception {
        // Initialize the database
        insertedOrganization = organizationRepository.saveAndFlush(organization);

        // Get all the organizationList where active in
        defaultOrganizationFiltering("active.in=" + DEFAULT_ACTIVE + "," + UPDATED_ACTIVE, "active.in=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllOrganizationsByActiveIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedOrganization = organizationRepository.saveAndFlush(organization);

        // Get all the organizationList where active is not null
        defaultOrganizationFiltering("active.specified=true", "active.specified=false");
    }

    private void defaultOrganizationFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultOrganizationShouldBeFound(shouldBeFound);
        defaultOrganizationShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultOrganizationShouldBeFound(String filter) throws Exception {
        restOrganizationMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(organization.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].slug").value(hasItem(DEFAULT_SLUG)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(DEFAULT_UPDATED_AT.toString())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE)));

        // Check, that the count call also returns 1
        restOrganizationMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultOrganizationShouldNotBeFound(String filter) throws Exception {
        restOrganizationMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restOrganizationMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingOrganization() throws Exception {
        // Get the organization
        restOrganizationMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingOrganization() throws Exception {
        // Initialize the database
        insertedOrganization = organizationRepository.saveAndFlush(organization);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the organization
        Organization updatedOrganization = organizationRepository.findById(organization.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedOrganization are not directly saved in db
        em.detach(updatedOrganization);
        updatedOrganization
            .name(UPDATED_NAME)
            .slug(UPDATED_SLUG)
            .description(UPDATED_DESCRIPTION)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT)
            .active(UPDATED_ACTIVE);
        OrganizationDTO organizationDTO = organizationMapper.toDto(updatedOrganization);

        restOrganizationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, organizationDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(organizationDTO))
            )
            .andExpect(status().isOk());

        // Validate the Organization in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedOrganizationToMatchAllProperties(updatedOrganization);
    }

    @Test
    @Transactional
    void putNonExistingOrganization() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        organization.setId(longCount.incrementAndGet());

        // Create the Organization
        OrganizationDTO organizationDTO = organizationMapper.toDto(organization);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restOrganizationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, organizationDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(organizationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Organization in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchOrganization() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        organization.setId(longCount.incrementAndGet());

        // Create the Organization
        OrganizationDTO organizationDTO = organizationMapper.toDto(organization);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restOrganizationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(organizationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Organization in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamOrganization() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        organization.setId(longCount.incrementAndGet());

        // Create the Organization
        OrganizationDTO organizationDTO = organizationMapper.toDto(organization);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restOrganizationMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(organizationDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Organization in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateOrganizationWithPatch() throws Exception {
        // Initialize the database
        insertedOrganization = organizationRepository.saveAndFlush(organization);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the organization using partial update
        Organization partialUpdatedOrganization = new Organization();
        partialUpdatedOrganization.setId(organization.getId());

        partialUpdatedOrganization.name(UPDATED_NAME).description(UPDATED_DESCRIPTION).createdAt(UPDATED_CREATED_AT);

        restOrganizationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedOrganization.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedOrganization))
            )
            .andExpect(status().isOk());

        // Validate the Organization in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertOrganizationUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedOrganization, organization),
            getPersistedOrganization(organization)
        );
    }

    @Test
    @Transactional
    void fullUpdateOrganizationWithPatch() throws Exception {
        // Initialize the database
        insertedOrganization = organizationRepository.saveAndFlush(organization);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the organization using partial update
        Organization partialUpdatedOrganization = new Organization();
        partialUpdatedOrganization.setId(organization.getId());

        partialUpdatedOrganization
            .name(UPDATED_NAME)
            .slug(UPDATED_SLUG)
            .description(UPDATED_DESCRIPTION)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT)
            .active(UPDATED_ACTIVE);

        restOrganizationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedOrganization.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedOrganization))
            )
            .andExpect(status().isOk());

        // Validate the Organization in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertOrganizationUpdatableFieldsEquals(partialUpdatedOrganization, getPersistedOrganization(partialUpdatedOrganization));
    }

    @Test
    @Transactional
    void patchNonExistingOrganization() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        organization.setId(longCount.incrementAndGet());

        // Create the Organization
        OrganizationDTO organizationDTO = organizationMapper.toDto(organization);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restOrganizationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, organizationDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(organizationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Organization in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchOrganization() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        organization.setId(longCount.incrementAndGet());

        // Create the Organization
        OrganizationDTO organizationDTO = organizationMapper.toDto(organization);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restOrganizationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(organizationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Organization in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamOrganization() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        organization.setId(longCount.incrementAndGet());

        // Create the Organization
        OrganizationDTO organizationDTO = organizationMapper.toDto(organization);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restOrganizationMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(organizationDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Organization in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteOrganization() throws Exception {
        // Initialize the database
        insertedOrganization = organizationRepository.saveAndFlush(organization);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the organization
        restOrganizationMockMvc
            .perform(delete(ENTITY_API_URL_ID, organization.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return organizationRepository.count();
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

    protected Organization getPersistedOrganization(Organization organization) {
        return organizationRepository.findById(organization.getId()).orElseThrow();
    }

    protected void assertPersistedOrganizationToMatchAllProperties(Organization expectedOrganization) {
        assertOrganizationAllPropertiesEquals(expectedOrganization, getPersistedOrganization(expectedOrganization));
    }

    protected void assertPersistedOrganizationToMatchUpdatableProperties(Organization expectedOrganization) {
        assertOrganizationAllUpdatablePropertiesEquals(expectedOrganization, getPersistedOrganization(expectedOrganization));
    }
}
