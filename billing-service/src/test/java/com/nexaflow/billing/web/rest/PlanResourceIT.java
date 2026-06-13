package com.nexaflow.billing.web.rest;

import static com.nexaflow.billing.domain.PlanAsserts.*;
import static com.nexaflow.billing.web.rest.TestUtil.createUpdateProxyForBean;
import static com.nexaflow.billing.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexaflow.billing.IntegrationTest;
import com.nexaflow.billing.domain.Plan;
import com.nexaflow.billing.domain.enumeration.PlanCode;
import com.nexaflow.billing.repository.PlanRepository;
import com.nexaflow.billing.service.dto.PlanDTO;
import com.nexaflow.billing.service.mapper.PlanMapper;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
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
 * Integration tests for the {@link PlanResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class PlanResourceIT {

    private static final PlanCode DEFAULT_CODE = PlanCode.FREE;
    private static final PlanCode UPDATED_CODE = PlanCode.PRO;

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final BigDecimal DEFAULT_PRICE_MONTHLY = new BigDecimal(0);
    private static final BigDecimal UPDATED_PRICE_MONTHLY = new BigDecimal(1);
    private static final BigDecimal SMALLER_PRICE_MONTHLY = new BigDecimal(0 - 1);

    private static final Integer DEFAULT_MAX_PROJECTS = 0;
    private static final Integer UPDATED_MAX_PROJECTS = 1;
    private static final Integer SMALLER_MAX_PROJECTS = 0 - 1;

    private static final Integer DEFAULT_MAX_USERS = 0;
    private static final Integer UPDATED_MAX_USERS = 1;
    private static final Integer SMALLER_MAX_USERS = 0 - 1;

    private static final Integer DEFAULT_MAX_TASKS = 0;
    private static final Integer UPDATED_MAX_TASKS = 1;
    private static final Integer SMALLER_MAX_TASKS = 0 - 1;

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_UPDATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_UPDATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/plans";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private PlanMapper planMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restPlanMockMvc;

    private Plan plan;

    private Plan insertedPlan;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Plan createEntity() {
        return new Plan()
            .code(DEFAULT_CODE)
            .name(DEFAULT_NAME)
            .priceMonthly(DEFAULT_PRICE_MONTHLY)
            .maxProjects(DEFAULT_MAX_PROJECTS)
            .maxUsers(DEFAULT_MAX_USERS)
            .maxTasks(DEFAULT_MAX_TASKS)
            .active(DEFAULT_ACTIVE)
            .createdAt(DEFAULT_CREATED_AT)
            .updatedAt(DEFAULT_UPDATED_AT);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Plan createUpdatedEntity() {
        return new Plan()
            .code(UPDATED_CODE)
            .name(UPDATED_NAME)
            .priceMonthly(UPDATED_PRICE_MONTHLY)
            .maxProjects(UPDATED_MAX_PROJECTS)
            .maxUsers(UPDATED_MAX_USERS)
            .maxTasks(UPDATED_MAX_TASKS)
            .active(UPDATED_ACTIVE)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);
    }

    @BeforeEach
    void initTest() {
        plan = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedPlan != null) {
            planRepository.delete(insertedPlan);
            insertedPlan = null;
        }
    }

    @Test
    @Transactional
    void createPlan() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Plan
        PlanDTO planDTO = planMapper.toDto(plan);
        var returnedPlanDTO = om.readValue(
            restPlanMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(planDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            PlanDTO.class
        );

        // Validate the Plan in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedPlan = planMapper.toEntity(returnedPlanDTO);
        assertPlanUpdatableFieldsEquals(returnedPlan, getPersistedPlan(returnedPlan));

        insertedPlan = returnedPlan;
    }

    @Test
    @Transactional
    void createPlanWithExistingId() throws Exception {
        // Create the Plan with an existing ID
        plan.setId(1L);
        PlanDTO planDTO = planMapper.toDto(plan);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restPlanMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(planDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Plan in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkCodeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        plan.setCode(null);

        // Create the Plan, which fails.
        PlanDTO planDTO = planMapper.toDto(plan);

        restPlanMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(planDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        plan.setName(null);

        // Create the Plan, which fails.
        PlanDTO planDTO = planMapper.toDto(plan);

        restPlanMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(planDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkPriceMonthlyIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        plan.setPriceMonthly(null);

        // Create the Plan, which fails.
        PlanDTO planDTO = planMapper.toDto(plan);

        restPlanMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(planDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkMaxProjectsIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        plan.setMaxProjects(null);

        // Create the Plan, which fails.
        PlanDTO planDTO = planMapper.toDto(plan);

        restPlanMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(planDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkMaxUsersIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        plan.setMaxUsers(null);

        // Create the Plan, which fails.
        PlanDTO planDTO = planMapper.toDto(plan);

        restPlanMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(planDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkMaxTasksIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        plan.setMaxTasks(null);

        // Create the Plan, which fails.
        PlanDTO planDTO = planMapper.toDto(plan);

        restPlanMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(planDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkActiveIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        plan.setActive(null);

        // Create the Plan, which fails.
        PlanDTO planDTO = planMapper.toDto(plan);

        restPlanMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(planDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllPlans() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        // Get all the planList
        restPlanMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(plan.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE.toString())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].priceMonthly").value(hasItem(sameNumber(DEFAULT_PRICE_MONTHLY))))
            .andExpect(jsonPath("$.[*].maxProjects").value(hasItem(DEFAULT_MAX_PROJECTS)))
            .andExpect(jsonPath("$.[*].maxUsers").value(hasItem(DEFAULT_MAX_USERS)))
            .andExpect(jsonPath("$.[*].maxTasks").value(hasItem(DEFAULT_MAX_TASKS)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(DEFAULT_UPDATED_AT.toString())));
    }

    @Test
    @Transactional
    void getPlan() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        // Get the plan
        restPlanMockMvc
            .perform(get(ENTITY_API_URL_ID, plan.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(plan.getId().intValue()))
            .andExpect(jsonPath("$.code").value(DEFAULT_CODE.toString()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.priceMonthly").value(sameNumber(DEFAULT_PRICE_MONTHLY)))
            .andExpect(jsonPath("$.maxProjects").value(DEFAULT_MAX_PROJECTS))
            .andExpect(jsonPath("$.maxUsers").value(DEFAULT_MAX_USERS))
            .andExpect(jsonPath("$.maxTasks").value(DEFAULT_MAX_TASKS))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE))
            .andExpect(jsonPath("$.createdAt").value(DEFAULT_CREATED_AT.toString()))
            .andExpect(jsonPath("$.updatedAt").value(DEFAULT_UPDATED_AT.toString()));
    }

    @Test
    @Transactional
    void getPlansByIdFiltering() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        Long id = plan.getId();

        defaultPlanFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultPlanFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultPlanFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllPlansByCodeIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        // Get all the planList where code equals to
        defaultPlanFiltering("code.equals=" + DEFAULT_CODE, "code.equals=" + UPDATED_CODE);
    }

    @Test
    @Transactional
    void getAllPlansByCodeIsInShouldWork() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        // Get all the planList where code in
        defaultPlanFiltering("code.in=" + DEFAULT_CODE + "," + UPDATED_CODE, "code.in=" + UPDATED_CODE);
    }

    @Test
    @Transactional
    void getAllPlansByCodeIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        // Get all the planList where code is not null
        defaultPlanFiltering("code.specified=true", "code.specified=false");
    }

    @Test
    @Transactional
    void getAllPlansByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        // Get all the planList where name equals to
        defaultPlanFiltering("name.equals=" + DEFAULT_NAME, "name.equals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllPlansByNameIsInShouldWork() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        // Get all the planList where name in
        defaultPlanFiltering("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME, "name.in=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllPlansByNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        // Get all the planList where name is not null
        defaultPlanFiltering("name.specified=true", "name.specified=false");
    }

    @Test
    @Transactional
    void getAllPlansByNameContainsSomething() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        // Get all the planList where name contains
        defaultPlanFiltering("name.contains=" + DEFAULT_NAME, "name.contains=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllPlansByNameNotContainsSomething() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        // Get all the planList where name does not contain
        defaultPlanFiltering("name.doesNotContain=" + UPDATED_NAME, "name.doesNotContain=" + DEFAULT_NAME);
    }

    @Test
    @Transactional
    void getAllPlansByPriceMonthlyIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        // Get all the planList where priceMonthly equals to
        defaultPlanFiltering("priceMonthly.equals=" + DEFAULT_PRICE_MONTHLY, "priceMonthly.equals=" + UPDATED_PRICE_MONTHLY);
    }

    @Test
    @Transactional
    void getAllPlansByPriceMonthlyIsInShouldWork() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        // Get all the planList where priceMonthly in
        defaultPlanFiltering(
            "priceMonthly.in=" + DEFAULT_PRICE_MONTHLY + "," + UPDATED_PRICE_MONTHLY,
            "priceMonthly.in=" + UPDATED_PRICE_MONTHLY
        );
    }

    @Test
    @Transactional
    void getAllPlansByPriceMonthlyIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        // Get all the planList where priceMonthly is not null
        defaultPlanFiltering("priceMonthly.specified=true", "priceMonthly.specified=false");
    }

    @Test
    @Transactional
    void getAllPlansByPriceMonthlyIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        // Get all the planList where priceMonthly is greater than or equal to
        defaultPlanFiltering(
            "priceMonthly.greaterThanOrEqual=" + DEFAULT_PRICE_MONTHLY,
            "priceMonthly.greaterThanOrEqual=" + UPDATED_PRICE_MONTHLY
        );
    }

    @Test
    @Transactional
    void getAllPlansByPriceMonthlyIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        // Get all the planList where priceMonthly is less than or equal to
        defaultPlanFiltering(
            "priceMonthly.lessThanOrEqual=" + DEFAULT_PRICE_MONTHLY,
            "priceMonthly.lessThanOrEqual=" + SMALLER_PRICE_MONTHLY
        );
    }

    @Test
    @Transactional
    void getAllPlansByPriceMonthlyIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        // Get all the planList where priceMonthly is less than
        defaultPlanFiltering("priceMonthly.lessThan=" + UPDATED_PRICE_MONTHLY, "priceMonthly.lessThan=" + DEFAULT_PRICE_MONTHLY);
    }

    @Test
    @Transactional
    void getAllPlansByPriceMonthlyIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        // Get all the planList where priceMonthly is greater than
        defaultPlanFiltering("priceMonthly.greaterThan=" + SMALLER_PRICE_MONTHLY, "priceMonthly.greaterThan=" + DEFAULT_PRICE_MONTHLY);
    }

    @Test
    @Transactional
    void getAllPlansByMaxProjectsIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        // Get all the planList where maxProjects equals to
        defaultPlanFiltering("maxProjects.equals=" + DEFAULT_MAX_PROJECTS, "maxProjects.equals=" + UPDATED_MAX_PROJECTS);
    }

    @Test
    @Transactional
    void getAllPlansByMaxProjectsIsInShouldWork() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        // Get all the planList where maxProjects in
        defaultPlanFiltering(
            "maxProjects.in=" + DEFAULT_MAX_PROJECTS + "," + UPDATED_MAX_PROJECTS,
            "maxProjects.in=" + UPDATED_MAX_PROJECTS
        );
    }

    @Test
    @Transactional
    void getAllPlansByMaxProjectsIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        // Get all the planList where maxProjects is not null
        defaultPlanFiltering("maxProjects.specified=true", "maxProjects.specified=false");
    }

    @Test
    @Transactional
    void getAllPlansByMaxProjectsIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        // Get all the planList where maxProjects is greater than or equal to
        defaultPlanFiltering(
            "maxProjects.greaterThanOrEqual=" + DEFAULT_MAX_PROJECTS,
            "maxProjects.greaterThanOrEqual=" + UPDATED_MAX_PROJECTS
        );
    }

    @Test
    @Transactional
    void getAllPlansByMaxProjectsIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        // Get all the planList where maxProjects is less than or equal to
        defaultPlanFiltering("maxProjects.lessThanOrEqual=" + DEFAULT_MAX_PROJECTS, "maxProjects.lessThanOrEqual=" + SMALLER_MAX_PROJECTS);
    }

    @Test
    @Transactional
    void getAllPlansByMaxProjectsIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        // Get all the planList where maxProjects is less than
        defaultPlanFiltering("maxProjects.lessThan=" + UPDATED_MAX_PROJECTS, "maxProjects.lessThan=" + DEFAULT_MAX_PROJECTS);
    }

    @Test
    @Transactional
    void getAllPlansByMaxProjectsIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        // Get all the planList where maxProjects is greater than
        defaultPlanFiltering("maxProjects.greaterThan=" + SMALLER_MAX_PROJECTS, "maxProjects.greaterThan=" + DEFAULT_MAX_PROJECTS);
    }

    @Test
    @Transactional
    void getAllPlansByMaxUsersIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        // Get all the planList where maxUsers equals to
        defaultPlanFiltering("maxUsers.equals=" + DEFAULT_MAX_USERS, "maxUsers.equals=" + UPDATED_MAX_USERS);
    }

    @Test
    @Transactional
    void getAllPlansByMaxUsersIsInShouldWork() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        // Get all the planList where maxUsers in
        defaultPlanFiltering("maxUsers.in=" + DEFAULT_MAX_USERS + "," + UPDATED_MAX_USERS, "maxUsers.in=" + UPDATED_MAX_USERS);
    }

    @Test
    @Transactional
    void getAllPlansByMaxUsersIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        // Get all the planList where maxUsers is not null
        defaultPlanFiltering("maxUsers.specified=true", "maxUsers.specified=false");
    }

    @Test
    @Transactional
    void getAllPlansByMaxUsersIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        // Get all the planList where maxUsers is greater than or equal to
        defaultPlanFiltering("maxUsers.greaterThanOrEqual=" + DEFAULT_MAX_USERS, "maxUsers.greaterThanOrEqual=" + UPDATED_MAX_USERS);
    }

    @Test
    @Transactional
    void getAllPlansByMaxUsersIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        // Get all the planList where maxUsers is less than or equal to
        defaultPlanFiltering("maxUsers.lessThanOrEqual=" + DEFAULT_MAX_USERS, "maxUsers.lessThanOrEqual=" + SMALLER_MAX_USERS);
    }

    @Test
    @Transactional
    void getAllPlansByMaxUsersIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        // Get all the planList where maxUsers is less than
        defaultPlanFiltering("maxUsers.lessThan=" + UPDATED_MAX_USERS, "maxUsers.lessThan=" + DEFAULT_MAX_USERS);
    }

    @Test
    @Transactional
    void getAllPlansByMaxUsersIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        // Get all the planList where maxUsers is greater than
        defaultPlanFiltering("maxUsers.greaterThan=" + SMALLER_MAX_USERS, "maxUsers.greaterThan=" + DEFAULT_MAX_USERS);
    }

    @Test
    @Transactional
    void getAllPlansByMaxTasksIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        // Get all the planList where maxTasks equals to
        defaultPlanFiltering("maxTasks.equals=" + DEFAULT_MAX_TASKS, "maxTasks.equals=" + UPDATED_MAX_TASKS);
    }

    @Test
    @Transactional
    void getAllPlansByMaxTasksIsInShouldWork() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        // Get all the planList where maxTasks in
        defaultPlanFiltering("maxTasks.in=" + DEFAULT_MAX_TASKS + "," + UPDATED_MAX_TASKS, "maxTasks.in=" + UPDATED_MAX_TASKS);
    }

    @Test
    @Transactional
    void getAllPlansByMaxTasksIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        // Get all the planList where maxTasks is not null
        defaultPlanFiltering("maxTasks.specified=true", "maxTasks.specified=false");
    }

    @Test
    @Transactional
    void getAllPlansByMaxTasksIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        // Get all the planList where maxTasks is greater than or equal to
        defaultPlanFiltering("maxTasks.greaterThanOrEqual=" + DEFAULT_MAX_TASKS, "maxTasks.greaterThanOrEqual=" + UPDATED_MAX_TASKS);
    }

    @Test
    @Transactional
    void getAllPlansByMaxTasksIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        // Get all the planList where maxTasks is less than or equal to
        defaultPlanFiltering("maxTasks.lessThanOrEqual=" + DEFAULT_MAX_TASKS, "maxTasks.lessThanOrEqual=" + SMALLER_MAX_TASKS);
    }

    @Test
    @Transactional
    void getAllPlansByMaxTasksIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        // Get all the planList where maxTasks is less than
        defaultPlanFiltering("maxTasks.lessThan=" + UPDATED_MAX_TASKS, "maxTasks.lessThan=" + DEFAULT_MAX_TASKS);
    }

    @Test
    @Transactional
    void getAllPlansByMaxTasksIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        // Get all the planList where maxTasks is greater than
        defaultPlanFiltering("maxTasks.greaterThan=" + SMALLER_MAX_TASKS, "maxTasks.greaterThan=" + DEFAULT_MAX_TASKS);
    }

    @Test
    @Transactional
    void getAllPlansByActiveIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        // Get all the planList where active equals to
        defaultPlanFiltering("active.equals=" + DEFAULT_ACTIVE, "active.equals=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllPlansByActiveIsInShouldWork() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        // Get all the planList where active in
        defaultPlanFiltering("active.in=" + DEFAULT_ACTIVE + "," + UPDATED_ACTIVE, "active.in=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllPlansByActiveIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        // Get all the planList where active is not null
        defaultPlanFiltering("active.specified=true", "active.specified=false");
    }

    @Test
    @Transactional
    void getAllPlansByCreatedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        // Get all the planList where createdAt equals to
        defaultPlanFiltering("createdAt.equals=" + DEFAULT_CREATED_AT, "createdAt.equals=" + UPDATED_CREATED_AT);
    }

    @Test
    @Transactional
    void getAllPlansByCreatedAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        // Get all the planList where createdAt in
        defaultPlanFiltering("createdAt.in=" + DEFAULT_CREATED_AT + "," + UPDATED_CREATED_AT, "createdAt.in=" + UPDATED_CREATED_AT);
    }

    @Test
    @Transactional
    void getAllPlansByCreatedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        // Get all the planList where createdAt is not null
        defaultPlanFiltering("createdAt.specified=true", "createdAt.specified=false");
    }

    @Test
    @Transactional
    void getAllPlansByUpdatedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        // Get all the planList where updatedAt equals to
        defaultPlanFiltering("updatedAt.equals=" + DEFAULT_UPDATED_AT, "updatedAt.equals=" + UPDATED_UPDATED_AT);
    }

    @Test
    @Transactional
    void getAllPlansByUpdatedAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        // Get all the planList where updatedAt in
        defaultPlanFiltering("updatedAt.in=" + DEFAULT_UPDATED_AT + "," + UPDATED_UPDATED_AT, "updatedAt.in=" + UPDATED_UPDATED_AT);
    }

    @Test
    @Transactional
    void getAllPlansByUpdatedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        // Get all the planList where updatedAt is not null
        defaultPlanFiltering("updatedAt.specified=true", "updatedAt.specified=false");
    }

    private void defaultPlanFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultPlanShouldBeFound(shouldBeFound);
        defaultPlanShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultPlanShouldBeFound(String filter) throws Exception {
        restPlanMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(plan.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE.toString())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].priceMonthly").value(hasItem(sameNumber(DEFAULT_PRICE_MONTHLY))))
            .andExpect(jsonPath("$.[*].maxProjects").value(hasItem(DEFAULT_MAX_PROJECTS)))
            .andExpect(jsonPath("$.[*].maxUsers").value(hasItem(DEFAULT_MAX_USERS)))
            .andExpect(jsonPath("$.[*].maxTasks").value(hasItem(DEFAULT_MAX_TASKS)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(DEFAULT_UPDATED_AT.toString())));

        // Check, that the count call also returns 1
        restPlanMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultPlanShouldNotBeFound(String filter) throws Exception {
        restPlanMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restPlanMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingPlan() throws Exception {
        // Get the plan
        restPlanMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingPlan() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the plan
        Plan updatedPlan = planRepository.findById(plan.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedPlan are not directly saved in db
        em.detach(updatedPlan);
        updatedPlan
            .code(UPDATED_CODE)
            .name(UPDATED_NAME)
            .priceMonthly(UPDATED_PRICE_MONTHLY)
            .maxProjects(UPDATED_MAX_PROJECTS)
            .maxUsers(UPDATED_MAX_USERS)
            .maxTasks(UPDATED_MAX_TASKS)
            .active(UPDATED_ACTIVE)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);
        PlanDTO planDTO = planMapper.toDto(updatedPlan);

        restPlanMockMvc
            .perform(put(ENTITY_API_URL_ID, planDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(planDTO)))
            .andExpect(status().isOk());

        // Validate the Plan in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedPlanToMatchAllProperties(updatedPlan);
    }

    @Test
    @Transactional
    void putNonExistingPlan() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        plan.setId(longCount.incrementAndGet());

        // Create the Plan
        PlanDTO planDTO = planMapper.toDto(plan);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPlanMockMvc
            .perform(put(ENTITY_API_URL_ID, planDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(planDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Plan in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchPlan() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        plan.setId(longCount.incrementAndGet());

        // Create the Plan
        PlanDTO planDTO = planMapper.toDto(plan);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPlanMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(planDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Plan in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamPlan() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        plan.setId(longCount.incrementAndGet());

        // Create the Plan
        PlanDTO planDTO = planMapper.toDto(plan);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPlanMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(planDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Plan in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdatePlanWithPatch() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the plan using partial update
        Plan partialUpdatedPlan = new Plan();
        partialUpdatedPlan.setId(plan.getId());

        partialUpdatedPlan
            .code(UPDATED_CODE)
            .name(UPDATED_NAME)
            .maxProjects(UPDATED_MAX_PROJECTS)
            .maxUsers(UPDATED_MAX_USERS)
            .maxTasks(UPDATED_MAX_TASKS)
            .updatedAt(UPDATED_UPDATED_AT);

        restPlanMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPlan.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedPlan))
            )
            .andExpect(status().isOk());

        // Validate the Plan in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPlanUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedPlan, plan), getPersistedPlan(plan));
    }

    @Test
    @Transactional
    void fullUpdatePlanWithPatch() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the plan using partial update
        Plan partialUpdatedPlan = new Plan();
        partialUpdatedPlan.setId(plan.getId());

        partialUpdatedPlan
            .code(UPDATED_CODE)
            .name(UPDATED_NAME)
            .priceMonthly(UPDATED_PRICE_MONTHLY)
            .maxProjects(UPDATED_MAX_PROJECTS)
            .maxUsers(UPDATED_MAX_USERS)
            .maxTasks(UPDATED_MAX_TASKS)
            .active(UPDATED_ACTIVE)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);

        restPlanMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPlan.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedPlan))
            )
            .andExpect(status().isOk());

        // Validate the Plan in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPlanUpdatableFieldsEquals(partialUpdatedPlan, getPersistedPlan(partialUpdatedPlan));
    }

    @Test
    @Transactional
    void patchNonExistingPlan() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        plan.setId(longCount.incrementAndGet());

        // Create the Plan
        PlanDTO planDTO = planMapper.toDto(plan);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPlanMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, planDTO.getId()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(planDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Plan in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchPlan() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        plan.setId(longCount.incrementAndGet());

        // Create the Plan
        PlanDTO planDTO = planMapper.toDto(plan);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPlanMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(planDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Plan in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamPlan() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        plan.setId(longCount.incrementAndGet());

        // Create the Plan
        PlanDTO planDTO = planMapper.toDto(plan);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPlanMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(planDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Plan in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deletePlan() throws Exception {
        // Initialize the database
        insertedPlan = planRepository.saveAndFlush(plan);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the plan
        restPlanMockMvc
            .perform(delete(ENTITY_API_URL_ID, plan.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return planRepository.count();
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

    protected Plan getPersistedPlan(Plan plan) {
        return planRepository.findById(plan.getId()).orElseThrow();
    }

    protected void assertPersistedPlanToMatchAllProperties(Plan expectedPlan) {
        assertPlanAllPropertiesEquals(expectedPlan, getPersistedPlan(expectedPlan));
    }

    protected void assertPersistedPlanToMatchUpdatableProperties(Plan expectedPlan) {
        assertPlanAllUpdatablePropertiesEquals(expectedPlan, getPersistedPlan(expectedPlan));
    }
}
