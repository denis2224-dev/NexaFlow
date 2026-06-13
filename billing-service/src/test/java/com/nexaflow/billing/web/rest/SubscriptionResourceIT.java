package com.nexaflow.billing.web.rest;

import static com.nexaflow.billing.domain.SubscriptionAsserts.*;
import static com.nexaflow.billing.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexaflow.billing.IntegrationTest;
import com.nexaflow.billing.domain.Subscription;
import com.nexaflow.billing.domain.enumeration.PlanCode;
import com.nexaflow.billing.domain.enumeration.SubscriptionStatus;
import com.nexaflow.billing.repository.SubscriptionRepository;
import com.nexaflow.billing.service.dto.SubscriptionDTO;
import com.nexaflow.billing.service.mapper.SubscriptionMapper;
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
 * Integration tests for the {@link SubscriptionResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class SubscriptionResourceIT {

    private static final Long DEFAULT_ORGANIZATION_ID = 1L;
    private static final Long UPDATED_ORGANIZATION_ID = 2L;
    private static final Long SMALLER_ORGANIZATION_ID = 1L - 1L;

    private static final PlanCode DEFAULT_PLAN_CODE = PlanCode.FREE;
    private static final PlanCode UPDATED_PLAN_CODE = PlanCode.PRO;

    private static final SubscriptionStatus DEFAULT_STATUS = SubscriptionStatus.ACTIVE;
    private static final SubscriptionStatus UPDATED_STATUS = SubscriptionStatus.CANCELLED;

    private static final Instant DEFAULT_STARTED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_STARTED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_EXPIRES_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_EXPIRES_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_CREATED_BY = "AAAAAAAAAA";
    private static final String UPDATED_CREATED_BY = "BBBBBBBBBB";

    private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_UPDATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_UPDATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/subscriptions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private SubscriptionMapper subscriptionMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restSubscriptionMockMvc;

    private Subscription subscription;

    private Subscription insertedSubscription;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Subscription createEntity() {
        return new Subscription()
            .organizationId(DEFAULT_ORGANIZATION_ID)
            .planCode(DEFAULT_PLAN_CODE)
            .status(DEFAULT_STATUS)
            .startedAt(DEFAULT_STARTED_AT)
            .expiresAt(DEFAULT_EXPIRES_AT)
            .createdBy(DEFAULT_CREATED_BY)
            .createdAt(DEFAULT_CREATED_AT)
            .updatedAt(DEFAULT_UPDATED_AT);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Subscription createUpdatedEntity() {
        return new Subscription()
            .organizationId(UPDATED_ORGANIZATION_ID)
            .planCode(UPDATED_PLAN_CODE)
            .status(UPDATED_STATUS)
            .startedAt(UPDATED_STARTED_AT)
            .expiresAt(UPDATED_EXPIRES_AT)
            .createdBy(UPDATED_CREATED_BY)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);
    }

    @BeforeEach
    void initTest() {
        subscription = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedSubscription != null) {
            subscriptionRepository.delete(insertedSubscription);
            insertedSubscription = null;
        }
    }

    @Test
    @Transactional
    void createSubscription() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Subscription
        SubscriptionDTO subscriptionDTO = subscriptionMapper.toDto(subscription);
        var returnedSubscriptionDTO = om.readValue(
            restSubscriptionMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(subscriptionDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            SubscriptionDTO.class
        );

        // Validate the Subscription in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedSubscription = subscriptionMapper.toEntity(returnedSubscriptionDTO);
        assertSubscriptionUpdatableFieldsEquals(returnedSubscription, getPersistedSubscription(returnedSubscription));

        insertedSubscription = returnedSubscription;
    }

    @Test
    @Transactional
    void createSubscriptionWithExistingId() throws Exception {
        // Create the Subscription with an existing ID
        subscription.setId(1L);
        SubscriptionDTO subscriptionDTO = subscriptionMapper.toDto(subscription);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restSubscriptionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(subscriptionDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Subscription in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkOrganizationIdIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        subscription.setOrganizationId(null);

        // Create the Subscription, which fails.
        SubscriptionDTO subscriptionDTO = subscriptionMapper.toDto(subscription);

        restSubscriptionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(subscriptionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkPlanCodeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        subscription.setPlanCode(null);

        // Create the Subscription, which fails.
        SubscriptionDTO subscriptionDTO = subscriptionMapper.toDto(subscription);

        restSubscriptionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(subscriptionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        subscription.setStatus(null);

        // Create the Subscription, which fails.
        SubscriptionDTO subscriptionDTO = subscriptionMapper.toDto(subscription);

        restSubscriptionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(subscriptionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStartedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        subscription.setStartedAt(null);

        // Create the Subscription, which fails.
        SubscriptionDTO subscriptionDTO = subscriptionMapper.toDto(subscription);

        restSubscriptionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(subscriptionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllSubscriptions() throws Exception {
        // Initialize the database
        insertedSubscription = subscriptionRepository.saveAndFlush(subscription);

        // Get all the subscriptionList
        restSubscriptionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(subscription.getId().intValue())))
            .andExpect(jsonPath("$.[*].organizationId").value(hasItem(DEFAULT_ORGANIZATION_ID.intValue())))
            .andExpect(jsonPath("$.[*].planCode").value(hasItem(DEFAULT_PLAN_CODE.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].startedAt").value(hasItem(DEFAULT_STARTED_AT.toString())))
            .andExpect(jsonPath("$.[*].expiresAt").value(hasItem(DEFAULT_EXPIRES_AT.toString())))
            .andExpect(jsonPath("$.[*].createdBy").value(hasItem(DEFAULT_CREATED_BY)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(DEFAULT_UPDATED_AT.toString())));
    }

    @Test
    @Transactional
    void getSubscription() throws Exception {
        // Initialize the database
        insertedSubscription = subscriptionRepository.saveAndFlush(subscription);

        // Get the subscription
        restSubscriptionMockMvc
            .perform(get(ENTITY_API_URL_ID, subscription.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(subscription.getId().intValue()))
            .andExpect(jsonPath("$.organizationId").value(DEFAULT_ORGANIZATION_ID.intValue()))
            .andExpect(jsonPath("$.planCode").value(DEFAULT_PLAN_CODE.toString()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.startedAt").value(DEFAULT_STARTED_AT.toString()))
            .andExpect(jsonPath("$.expiresAt").value(DEFAULT_EXPIRES_AT.toString()))
            .andExpect(jsonPath("$.createdBy").value(DEFAULT_CREATED_BY))
            .andExpect(jsonPath("$.createdAt").value(DEFAULT_CREATED_AT.toString()))
            .andExpect(jsonPath("$.updatedAt").value(DEFAULT_UPDATED_AT.toString()));
    }

    @Test
    @Transactional
    void getSubscriptionsByIdFiltering() throws Exception {
        // Initialize the database
        insertedSubscription = subscriptionRepository.saveAndFlush(subscription);

        Long id = subscription.getId();

        defaultSubscriptionFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultSubscriptionFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultSubscriptionFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllSubscriptionsByOrganizationIdIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSubscription = subscriptionRepository.saveAndFlush(subscription);

        // Get all the subscriptionList where organizationId equals to
        defaultSubscriptionFiltering(
            "organizationId.equals=" + DEFAULT_ORGANIZATION_ID,
            "organizationId.equals=" + UPDATED_ORGANIZATION_ID
        );
    }

    @Test
    @Transactional
    void getAllSubscriptionsByOrganizationIdIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSubscription = subscriptionRepository.saveAndFlush(subscription);

        // Get all the subscriptionList where organizationId in
        defaultSubscriptionFiltering(
            "organizationId.in=" + DEFAULT_ORGANIZATION_ID + "," + UPDATED_ORGANIZATION_ID,
            "organizationId.in=" + UPDATED_ORGANIZATION_ID
        );
    }

    @Test
    @Transactional
    void getAllSubscriptionsByOrganizationIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSubscription = subscriptionRepository.saveAndFlush(subscription);

        // Get all the subscriptionList where organizationId is not null
        defaultSubscriptionFiltering("organizationId.specified=true", "organizationId.specified=false");
    }

    @Test
    @Transactional
    void getAllSubscriptionsByOrganizationIdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedSubscription = subscriptionRepository.saveAndFlush(subscription);

        // Get all the subscriptionList where organizationId is greater than or equal to
        defaultSubscriptionFiltering(
            "organizationId.greaterThanOrEqual=" + DEFAULT_ORGANIZATION_ID,
            "organizationId.greaterThanOrEqual=" + UPDATED_ORGANIZATION_ID
        );
    }

    @Test
    @Transactional
    void getAllSubscriptionsByOrganizationIdIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedSubscription = subscriptionRepository.saveAndFlush(subscription);

        // Get all the subscriptionList where organizationId is less than or equal to
        defaultSubscriptionFiltering(
            "organizationId.lessThanOrEqual=" + DEFAULT_ORGANIZATION_ID,
            "organizationId.lessThanOrEqual=" + SMALLER_ORGANIZATION_ID
        );
    }

    @Test
    @Transactional
    void getAllSubscriptionsByOrganizationIdIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedSubscription = subscriptionRepository.saveAndFlush(subscription);

        // Get all the subscriptionList where organizationId is less than
        defaultSubscriptionFiltering(
            "organizationId.lessThan=" + UPDATED_ORGANIZATION_ID,
            "organizationId.lessThan=" + DEFAULT_ORGANIZATION_ID
        );
    }

    @Test
    @Transactional
    void getAllSubscriptionsByOrganizationIdIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedSubscription = subscriptionRepository.saveAndFlush(subscription);

        // Get all the subscriptionList where organizationId is greater than
        defaultSubscriptionFiltering(
            "organizationId.greaterThan=" + SMALLER_ORGANIZATION_ID,
            "organizationId.greaterThan=" + DEFAULT_ORGANIZATION_ID
        );
    }

    @Test
    @Transactional
    void getAllSubscriptionsByPlanCodeIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSubscription = subscriptionRepository.saveAndFlush(subscription);

        // Get all the subscriptionList where planCode equals to
        defaultSubscriptionFiltering("planCode.equals=" + DEFAULT_PLAN_CODE, "planCode.equals=" + UPDATED_PLAN_CODE);
    }

    @Test
    @Transactional
    void getAllSubscriptionsByPlanCodeIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSubscription = subscriptionRepository.saveAndFlush(subscription);

        // Get all the subscriptionList where planCode in
        defaultSubscriptionFiltering("planCode.in=" + DEFAULT_PLAN_CODE + "," + UPDATED_PLAN_CODE, "planCode.in=" + UPDATED_PLAN_CODE);
    }

    @Test
    @Transactional
    void getAllSubscriptionsByPlanCodeIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSubscription = subscriptionRepository.saveAndFlush(subscription);

        // Get all the subscriptionList where planCode is not null
        defaultSubscriptionFiltering("planCode.specified=true", "planCode.specified=false");
    }

    @Test
    @Transactional
    void getAllSubscriptionsByStatusIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSubscription = subscriptionRepository.saveAndFlush(subscription);

        // Get all the subscriptionList where status equals to
        defaultSubscriptionFiltering("status.equals=" + DEFAULT_STATUS, "status.equals=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllSubscriptionsByStatusIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSubscription = subscriptionRepository.saveAndFlush(subscription);

        // Get all the subscriptionList where status in
        defaultSubscriptionFiltering("status.in=" + DEFAULT_STATUS + "," + UPDATED_STATUS, "status.in=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllSubscriptionsByStatusIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSubscription = subscriptionRepository.saveAndFlush(subscription);

        // Get all the subscriptionList where status is not null
        defaultSubscriptionFiltering("status.specified=true", "status.specified=false");
    }

    @Test
    @Transactional
    void getAllSubscriptionsByStartedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSubscription = subscriptionRepository.saveAndFlush(subscription);

        // Get all the subscriptionList where startedAt equals to
        defaultSubscriptionFiltering("startedAt.equals=" + DEFAULT_STARTED_AT, "startedAt.equals=" + UPDATED_STARTED_AT);
    }

    @Test
    @Transactional
    void getAllSubscriptionsByStartedAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSubscription = subscriptionRepository.saveAndFlush(subscription);

        // Get all the subscriptionList where startedAt in
        defaultSubscriptionFiltering("startedAt.in=" + DEFAULT_STARTED_AT + "," + UPDATED_STARTED_AT, "startedAt.in=" + UPDATED_STARTED_AT);
    }

    @Test
    @Transactional
    void getAllSubscriptionsByStartedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSubscription = subscriptionRepository.saveAndFlush(subscription);

        // Get all the subscriptionList where startedAt is not null
        defaultSubscriptionFiltering("startedAt.specified=true", "startedAt.specified=false");
    }

    @Test
    @Transactional
    void getAllSubscriptionsByExpiresAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSubscription = subscriptionRepository.saveAndFlush(subscription);

        // Get all the subscriptionList where expiresAt equals to
        defaultSubscriptionFiltering("expiresAt.equals=" + DEFAULT_EXPIRES_AT, "expiresAt.equals=" + UPDATED_EXPIRES_AT);
    }

    @Test
    @Transactional
    void getAllSubscriptionsByExpiresAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSubscription = subscriptionRepository.saveAndFlush(subscription);

        // Get all the subscriptionList where expiresAt in
        defaultSubscriptionFiltering("expiresAt.in=" + DEFAULT_EXPIRES_AT + "," + UPDATED_EXPIRES_AT, "expiresAt.in=" + UPDATED_EXPIRES_AT);
    }

    @Test
    @Transactional
    void getAllSubscriptionsByExpiresAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSubscription = subscriptionRepository.saveAndFlush(subscription);

        // Get all the subscriptionList where expiresAt is not null
        defaultSubscriptionFiltering("expiresAt.specified=true", "expiresAt.specified=false");
    }

    @Test
    @Transactional
    void getAllSubscriptionsByCreatedByIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSubscription = subscriptionRepository.saveAndFlush(subscription);

        // Get all the subscriptionList where createdBy equals to
        defaultSubscriptionFiltering("createdBy.equals=" + DEFAULT_CREATED_BY, "createdBy.equals=" + UPDATED_CREATED_BY);
    }

    @Test
    @Transactional
    void getAllSubscriptionsByCreatedByIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSubscription = subscriptionRepository.saveAndFlush(subscription);

        // Get all the subscriptionList where createdBy in
        defaultSubscriptionFiltering("createdBy.in=" + DEFAULT_CREATED_BY + "," + UPDATED_CREATED_BY, "createdBy.in=" + UPDATED_CREATED_BY);
    }

    @Test
    @Transactional
    void getAllSubscriptionsByCreatedByIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSubscription = subscriptionRepository.saveAndFlush(subscription);

        // Get all the subscriptionList where createdBy is not null
        defaultSubscriptionFiltering("createdBy.specified=true", "createdBy.specified=false");
    }

    @Test
    @Transactional
    void getAllSubscriptionsByCreatedByContainsSomething() throws Exception {
        // Initialize the database
        insertedSubscription = subscriptionRepository.saveAndFlush(subscription);

        // Get all the subscriptionList where createdBy contains
        defaultSubscriptionFiltering("createdBy.contains=" + DEFAULT_CREATED_BY, "createdBy.contains=" + UPDATED_CREATED_BY);
    }

    @Test
    @Transactional
    void getAllSubscriptionsByCreatedByNotContainsSomething() throws Exception {
        // Initialize the database
        insertedSubscription = subscriptionRepository.saveAndFlush(subscription);

        // Get all the subscriptionList where createdBy does not contain
        defaultSubscriptionFiltering("createdBy.doesNotContain=" + UPDATED_CREATED_BY, "createdBy.doesNotContain=" + DEFAULT_CREATED_BY);
    }

    @Test
    @Transactional
    void getAllSubscriptionsByCreatedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSubscription = subscriptionRepository.saveAndFlush(subscription);

        // Get all the subscriptionList where createdAt equals to
        defaultSubscriptionFiltering("createdAt.equals=" + DEFAULT_CREATED_AT, "createdAt.equals=" + UPDATED_CREATED_AT);
    }

    @Test
    @Transactional
    void getAllSubscriptionsByCreatedAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSubscription = subscriptionRepository.saveAndFlush(subscription);

        // Get all the subscriptionList where createdAt in
        defaultSubscriptionFiltering("createdAt.in=" + DEFAULT_CREATED_AT + "," + UPDATED_CREATED_AT, "createdAt.in=" + UPDATED_CREATED_AT);
    }

    @Test
    @Transactional
    void getAllSubscriptionsByCreatedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSubscription = subscriptionRepository.saveAndFlush(subscription);

        // Get all the subscriptionList where createdAt is not null
        defaultSubscriptionFiltering("createdAt.specified=true", "createdAt.specified=false");
    }

    @Test
    @Transactional
    void getAllSubscriptionsByUpdatedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSubscription = subscriptionRepository.saveAndFlush(subscription);

        // Get all the subscriptionList where updatedAt equals to
        defaultSubscriptionFiltering("updatedAt.equals=" + DEFAULT_UPDATED_AT, "updatedAt.equals=" + UPDATED_UPDATED_AT);
    }

    @Test
    @Transactional
    void getAllSubscriptionsByUpdatedAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSubscription = subscriptionRepository.saveAndFlush(subscription);

        // Get all the subscriptionList where updatedAt in
        defaultSubscriptionFiltering("updatedAt.in=" + DEFAULT_UPDATED_AT + "," + UPDATED_UPDATED_AT, "updatedAt.in=" + UPDATED_UPDATED_AT);
    }

    @Test
    @Transactional
    void getAllSubscriptionsByUpdatedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSubscription = subscriptionRepository.saveAndFlush(subscription);

        // Get all the subscriptionList where updatedAt is not null
        defaultSubscriptionFiltering("updatedAt.specified=true", "updatedAt.specified=false");
    }

    private void defaultSubscriptionFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultSubscriptionShouldBeFound(shouldBeFound);
        defaultSubscriptionShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultSubscriptionShouldBeFound(String filter) throws Exception {
        restSubscriptionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(subscription.getId().intValue())))
            .andExpect(jsonPath("$.[*].organizationId").value(hasItem(DEFAULT_ORGANIZATION_ID.intValue())))
            .andExpect(jsonPath("$.[*].planCode").value(hasItem(DEFAULT_PLAN_CODE.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].startedAt").value(hasItem(DEFAULT_STARTED_AT.toString())))
            .andExpect(jsonPath("$.[*].expiresAt").value(hasItem(DEFAULT_EXPIRES_AT.toString())))
            .andExpect(jsonPath("$.[*].createdBy").value(hasItem(DEFAULT_CREATED_BY)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(DEFAULT_UPDATED_AT.toString())));

        // Check, that the count call also returns 1
        restSubscriptionMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultSubscriptionShouldNotBeFound(String filter) throws Exception {
        restSubscriptionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restSubscriptionMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingSubscription() throws Exception {
        // Get the subscription
        restSubscriptionMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingSubscription() throws Exception {
        // Initialize the database
        insertedSubscription = subscriptionRepository.saveAndFlush(subscription);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the subscription
        Subscription updatedSubscription = subscriptionRepository.findById(subscription.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedSubscription are not directly saved in db
        em.detach(updatedSubscription);
        updatedSubscription
            .organizationId(UPDATED_ORGANIZATION_ID)
            .planCode(UPDATED_PLAN_CODE)
            .status(UPDATED_STATUS)
            .startedAt(UPDATED_STARTED_AT)
            .expiresAt(UPDATED_EXPIRES_AT)
            .createdBy(UPDATED_CREATED_BY)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);
        SubscriptionDTO subscriptionDTO = subscriptionMapper.toDto(updatedSubscription);

        restSubscriptionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, subscriptionDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(subscriptionDTO))
            )
            .andExpect(status().isOk());

        // Validate the Subscription in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedSubscriptionToMatchAllProperties(updatedSubscription);
    }

    @Test
    @Transactional
    void putNonExistingSubscription() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        subscription.setId(longCount.incrementAndGet());

        // Create the Subscription
        SubscriptionDTO subscriptionDTO = subscriptionMapper.toDto(subscription);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSubscriptionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, subscriptionDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(subscriptionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Subscription in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchSubscription() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        subscription.setId(longCount.incrementAndGet());

        // Create the Subscription
        SubscriptionDTO subscriptionDTO = subscriptionMapper.toDto(subscription);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSubscriptionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(subscriptionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Subscription in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamSubscription() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        subscription.setId(longCount.incrementAndGet());

        // Create the Subscription
        SubscriptionDTO subscriptionDTO = subscriptionMapper.toDto(subscription);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSubscriptionMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(subscriptionDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Subscription in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateSubscriptionWithPatch() throws Exception {
        // Initialize the database
        insertedSubscription = subscriptionRepository.saveAndFlush(subscription);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the subscription using partial update
        Subscription partialUpdatedSubscription = new Subscription();
        partialUpdatedSubscription.setId(subscription.getId());

        partialUpdatedSubscription
            .organizationId(UPDATED_ORGANIZATION_ID)
            .status(UPDATED_STATUS)
            .expiresAt(UPDATED_EXPIRES_AT)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);

        restSubscriptionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSubscription.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedSubscription))
            )
            .andExpect(status().isOk());

        // Validate the Subscription in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertSubscriptionUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedSubscription, subscription),
            getPersistedSubscription(subscription)
        );
    }

    @Test
    @Transactional
    void fullUpdateSubscriptionWithPatch() throws Exception {
        // Initialize the database
        insertedSubscription = subscriptionRepository.saveAndFlush(subscription);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the subscription using partial update
        Subscription partialUpdatedSubscription = new Subscription();
        partialUpdatedSubscription.setId(subscription.getId());

        partialUpdatedSubscription
            .organizationId(UPDATED_ORGANIZATION_ID)
            .planCode(UPDATED_PLAN_CODE)
            .status(UPDATED_STATUS)
            .startedAt(UPDATED_STARTED_AT)
            .expiresAt(UPDATED_EXPIRES_AT)
            .createdBy(UPDATED_CREATED_BY)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);

        restSubscriptionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSubscription.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedSubscription))
            )
            .andExpect(status().isOk());

        // Validate the Subscription in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertSubscriptionUpdatableFieldsEquals(partialUpdatedSubscription, getPersistedSubscription(partialUpdatedSubscription));
    }

    @Test
    @Transactional
    void patchNonExistingSubscription() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        subscription.setId(longCount.incrementAndGet());

        // Create the Subscription
        SubscriptionDTO subscriptionDTO = subscriptionMapper.toDto(subscription);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSubscriptionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, subscriptionDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(subscriptionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Subscription in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchSubscription() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        subscription.setId(longCount.incrementAndGet());

        // Create the Subscription
        SubscriptionDTO subscriptionDTO = subscriptionMapper.toDto(subscription);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSubscriptionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(subscriptionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Subscription in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamSubscription() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        subscription.setId(longCount.incrementAndGet());

        // Create the Subscription
        SubscriptionDTO subscriptionDTO = subscriptionMapper.toDto(subscription);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSubscriptionMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(subscriptionDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Subscription in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteSubscription() throws Exception {
        // Initialize the database
        insertedSubscription = subscriptionRepository.saveAndFlush(subscription);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the subscription
        restSubscriptionMockMvc
            .perform(delete(ENTITY_API_URL_ID, subscription.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return subscriptionRepository.count();
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

    protected Subscription getPersistedSubscription(Subscription subscription) {
        return subscriptionRepository.findById(subscription.getId()).orElseThrow();
    }

    protected void assertPersistedSubscriptionToMatchAllProperties(Subscription expectedSubscription) {
        assertSubscriptionAllPropertiesEquals(expectedSubscription, getPersistedSubscription(expectedSubscription));
    }

    protected void assertPersistedSubscriptionToMatchUpdatableProperties(Subscription expectedSubscription) {
        assertSubscriptionAllUpdatablePropertiesEquals(expectedSubscription, getPersistedSubscription(expectedSubscription));
    }
}
