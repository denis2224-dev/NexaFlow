package com.nexaflow.userservice.web.rest;

import static com.nexaflow.userservice.domain.MembershipAsserts.*;
import static com.nexaflow.userservice.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexaflow.userservice.IntegrationTest;
import com.nexaflow.userservice.domain.Membership;
import com.nexaflow.userservice.domain.Organization;
import com.nexaflow.userservice.domain.enumeration.MembershipRole;
import com.nexaflow.userservice.repository.MembershipRepository;
import com.nexaflow.userservice.service.MembershipService;
import com.nexaflow.userservice.service.dto.MembershipDTO;
import com.nexaflow.userservice.service.mapper.MembershipMapper;
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
 * Integration tests for the {@link MembershipResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class MembershipResourceIT {

    private static final Long DEFAULT_USER_ID = 1L;
    private static final Long UPDATED_USER_ID = 2L;
    private static final Long SMALLER_USER_ID = 1L - 1L;

    private static final String DEFAULT_USER_LOGIN = "AAAAAAAAAA";
    private static final String UPDATED_USER_LOGIN = "BBBBBBBBBB";

    private static final String DEFAULT_USER_EMAIL = "AAAAAAAAAA";
    private static final String UPDATED_USER_EMAIL = "BBBBBBBBBB";

    private static final MembershipRole DEFAULT_ROLE = MembershipRole.OWNER;
    private static final MembershipRole UPDATED_ROLE = MembershipRole.ADMIN;

    private static final Instant DEFAULT_JOINED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_JOINED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final String ENTITY_API_URL = "/api/memberships";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private MembershipRepository membershipRepository;

    @Mock
    private MembershipRepository membershipRepositoryMock;

    @Autowired
    private MembershipMapper membershipMapper;

    @Mock
    private MembershipService membershipServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restMembershipMockMvc;

    private Membership membership;

    private Membership insertedMembership;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Membership createEntity(EntityManager em) {
        Membership membership = new Membership()
            .userId(DEFAULT_USER_ID)
            .userLogin(DEFAULT_USER_LOGIN)
            .userEmail(DEFAULT_USER_EMAIL)
            .role(DEFAULT_ROLE)
            .joinedAt(DEFAULT_JOINED_AT)
            .active(DEFAULT_ACTIVE);
        // Add required entity
        Organization organization;
        if (TestUtil.findAll(em, Organization.class).isEmpty()) {
            organization = OrganizationResourceIT.createEntity();
            em.persist(organization);
            em.flush();
        } else {
            organization = TestUtil.findAll(em, Organization.class).get(0);
        }
        membership.setOrganization(organization);
        return membership;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Membership createUpdatedEntity(EntityManager em) {
        Membership updatedMembership = new Membership()
            .userId(UPDATED_USER_ID)
            .userLogin(UPDATED_USER_LOGIN)
            .userEmail(UPDATED_USER_EMAIL)
            .role(UPDATED_ROLE)
            .joinedAt(UPDATED_JOINED_AT)
            .active(UPDATED_ACTIVE);
        // Add required entity
        Organization organization;
        if (TestUtil.findAll(em, Organization.class).isEmpty()) {
            organization = OrganizationResourceIT.createUpdatedEntity();
            em.persist(organization);
            em.flush();
        } else {
            organization = TestUtil.findAll(em, Organization.class).get(0);
        }
        updatedMembership.setOrganization(organization);
        return updatedMembership;
    }

    @BeforeEach
    void initTest() {
        membership = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedMembership != null) {
            membershipRepository.delete(insertedMembership);
            insertedMembership = null;
        }
    }

    @Test
    @Transactional
    void createMembership() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Membership
        MembershipDTO membershipDTO = membershipMapper.toDto(membership);
        var returnedMembershipDTO = om.readValue(
            restMembershipMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(membershipDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            MembershipDTO.class
        );

        // Validate the Membership in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedMembership = membershipMapper.toEntity(returnedMembershipDTO);
        assertMembershipUpdatableFieldsEquals(returnedMembership, getPersistedMembership(returnedMembership));

        insertedMembership = returnedMembership;
    }

    @Test
    @Transactional
    void createMembershipWithExistingId() throws Exception {
        // Create the Membership with an existing ID
        membership.setId(1L);
        MembershipDTO membershipDTO = membershipMapper.toDto(membership);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restMembershipMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(membershipDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Membership in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkUserIdIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        membership.setUserId(null);

        // Create the Membership, which fails.
        MembershipDTO membershipDTO = membershipMapper.toDto(membership);

        restMembershipMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(membershipDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkUserLoginIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        membership.setUserLogin(null);

        // Create the Membership, which fails.
        MembershipDTO membershipDTO = membershipMapper.toDto(membership);

        restMembershipMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(membershipDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkUserEmailIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        membership.setUserEmail(null);

        // Create the Membership, which fails.
        MembershipDTO membershipDTO = membershipMapper.toDto(membership);

        restMembershipMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(membershipDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkRoleIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        membership.setRole(null);

        // Create the Membership, which fails.
        MembershipDTO membershipDTO = membershipMapper.toDto(membership);

        restMembershipMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(membershipDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkJoinedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        membership.setJoinedAt(null);

        // Create the Membership, which fails.
        MembershipDTO membershipDTO = membershipMapper.toDto(membership);

        restMembershipMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(membershipDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkActiveIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        membership.setActive(null);

        // Create the Membership, which fails.
        MembershipDTO membershipDTO = membershipMapper.toDto(membership);

        restMembershipMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(membershipDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllMemberships() throws Exception {
        // Initialize the database
        insertedMembership = membershipRepository.saveAndFlush(membership);

        // Get all the membershipList
        restMembershipMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(membership.getId().intValue())))
            .andExpect(jsonPath("$.[*].userId").value(hasItem(DEFAULT_USER_ID.intValue())))
            .andExpect(jsonPath("$.[*].userLogin").value(hasItem(DEFAULT_USER_LOGIN)))
            .andExpect(jsonPath("$.[*].userEmail").value(hasItem(DEFAULT_USER_EMAIL)))
            .andExpect(jsonPath("$.[*].role").value(hasItem(DEFAULT_ROLE.toString())))
            .andExpect(jsonPath("$.[*].joinedAt").value(hasItem(DEFAULT_JOINED_AT.toString())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllMembershipsWithEagerRelationshipsIsEnabled() throws Exception {
        when(membershipServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restMembershipMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(membershipServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllMembershipsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(membershipServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restMembershipMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(membershipRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getMembership() throws Exception {
        // Initialize the database
        insertedMembership = membershipRepository.saveAndFlush(membership);

        // Get the membership
        restMembershipMockMvc
            .perform(get(ENTITY_API_URL_ID, membership.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(membership.getId().intValue()))
            .andExpect(jsonPath("$.userId").value(DEFAULT_USER_ID.intValue()))
            .andExpect(jsonPath("$.userLogin").value(DEFAULT_USER_LOGIN))
            .andExpect(jsonPath("$.userEmail").value(DEFAULT_USER_EMAIL))
            .andExpect(jsonPath("$.role").value(DEFAULT_ROLE.toString()))
            .andExpect(jsonPath("$.joinedAt").value(DEFAULT_JOINED_AT.toString()))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE));
    }

    @Test
    @Transactional
    void getMembershipsByIdFiltering() throws Exception {
        // Initialize the database
        insertedMembership = membershipRepository.saveAndFlush(membership);

        Long id = membership.getId();

        defaultMembershipFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultMembershipFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultMembershipFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllMembershipsByUserIdIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedMembership = membershipRepository.saveAndFlush(membership);

        // Get all the membershipList where userId equals to
        defaultMembershipFiltering("userId.equals=" + DEFAULT_USER_ID, "userId.equals=" + UPDATED_USER_ID);
    }

    @Test
    @Transactional
    void getAllMembershipsByUserIdIsInShouldWork() throws Exception {
        // Initialize the database
        insertedMembership = membershipRepository.saveAndFlush(membership);

        // Get all the membershipList where userId in
        defaultMembershipFiltering("userId.in=" + DEFAULT_USER_ID + "," + UPDATED_USER_ID, "userId.in=" + UPDATED_USER_ID);
    }

    @Test
    @Transactional
    void getAllMembershipsByUserIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedMembership = membershipRepository.saveAndFlush(membership);

        // Get all the membershipList where userId is not null
        defaultMembershipFiltering("userId.specified=true", "userId.specified=false");
    }

    @Test
    @Transactional
    void getAllMembershipsByUserIdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedMembership = membershipRepository.saveAndFlush(membership);

        // Get all the membershipList where userId is greater than or equal to
        defaultMembershipFiltering("userId.greaterThanOrEqual=" + DEFAULT_USER_ID, "userId.greaterThanOrEqual=" + UPDATED_USER_ID);
    }

    @Test
    @Transactional
    void getAllMembershipsByUserIdIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedMembership = membershipRepository.saveAndFlush(membership);

        // Get all the membershipList where userId is less than or equal to
        defaultMembershipFiltering("userId.lessThanOrEqual=" + DEFAULT_USER_ID, "userId.lessThanOrEqual=" + SMALLER_USER_ID);
    }

    @Test
    @Transactional
    void getAllMembershipsByUserIdIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedMembership = membershipRepository.saveAndFlush(membership);

        // Get all the membershipList where userId is less than
        defaultMembershipFiltering("userId.lessThan=" + UPDATED_USER_ID, "userId.lessThan=" + DEFAULT_USER_ID);
    }

    @Test
    @Transactional
    void getAllMembershipsByUserIdIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedMembership = membershipRepository.saveAndFlush(membership);

        // Get all the membershipList where userId is greater than
        defaultMembershipFiltering("userId.greaterThan=" + SMALLER_USER_ID, "userId.greaterThan=" + DEFAULT_USER_ID);
    }

    @Test
    @Transactional
    void getAllMembershipsByUserLoginIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedMembership = membershipRepository.saveAndFlush(membership);

        // Get all the membershipList where userLogin equals to
        defaultMembershipFiltering("userLogin.equals=" + DEFAULT_USER_LOGIN, "userLogin.equals=" + UPDATED_USER_LOGIN);
    }

    @Test
    @Transactional
    void getAllMembershipsByUserLoginIsInShouldWork() throws Exception {
        // Initialize the database
        insertedMembership = membershipRepository.saveAndFlush(membership);

        // Get all the membershipList where userLogin in
        defaultMembershipFiltering("userLogin.in=" + DEFAULT_USER_LOGIN + "," + UPDATED_USER_LOGIN, "userLogin.in=" + UPDATED_USER_LOGIN);
    }

    @Test
    @Transactional
    void getAllMembershipsByUserLoginIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedMembership = membershipRepository.saveAndFlush(membership);

        // Get all the membershipList where userLogin is not null
        defaultMembershipFiltering("userLogin.specified=true", "userLogin.specified=false");
    }

    @Test
    @Transactional
    void getAllMembershipsByUserLoginContainsSomething() throws Exception {
        // Initialize the database
        insertedMembership = membershipRepository.saveAndFlush(membership);

        // Get all the membershipList where userLogin contains
        defaultMembershipFiltering("userLogin.contains=" + DEFAULT_USER_LOGIN, "userLogin.contains=" + UPDATED_USER_LOGIN);
    }

    @Test
    @Transactional
    void getAllMembershipsByUserLoginNotContainsSomething() throws Exception {
        // Initialize the database
        insertedMembership = membershipRepository.saveAndFlush(membership);

        // Get all the membershipList where userLogin does not contain
        defaultMembershipFiltering("userLogin.doesNotContain=" + UPDATED_USER_LOGIN, "userLogin.doesNotContain=" + DEFAULT_USER_LOGIN);
    }

    @Test
    @Transactional
    void getAllMembershipsByUserEmailIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedMembership = membershipRepository.saveAndFlush(membership);

        // Get all the membershipList where userEmail equals to
        defaultMembershipFiltering("userEmail.equals=" + DEFAULT_USER_EMAIL, "userEmail.equals=" + UPDATED_USER_EMAIL);
    }

    @Test
    @Transactional
    void getAllMembershipsByUserEmailIsInShouldWork() throws Exception {
        // Initialize the database
        insertedMembership = membershipRepository.saveAndFlush(membership);

        // Get all the membershipList where userEmail in
        defaultMembershipFiltering("userEmail.in=" + DEFAULT_USER_EMAIL + "," + UPDATED_USER_EMAIL, "userEmail.in=" + UPDATED_USER_EMAIL);
    }

    @Test
    @Transactional
    void getAllMembershipsByUserEmailIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedMembership = membershipRepository.saveAndFlush(membership);

        // Get all the membershipList where userEmail is not null
        defaultMembershipFiltering("userEmail.specified=true", "userEmail.specified=false");
    }

    @Test
    @Transactional
    void getAllMembershipsByUserEmailContainsSomething() throws Exception {
        // Initialize the database
        insertedMembership = membershipRepository.saveAndFlush(membership);

        // Get all the membershipList where userEmail contains
        defaultMembershipFiltering("userEmail.contains=" + DEFAULT_USER_EMAIL, "userEmail.contains=" + UPDATED_USER_EMAIL);
    }

    @Test
    @Transactional
    void getAllMembershipsByUserEmailNotContainsSomething() throws Exception {
        // Initialize the database
        insertedMembership = membershipRepository.saveAndFlush(membership);

        // Get all the membershipList where userEmail does not contain
        defaultMembershipFiltering("userEmail.doesNotContain=" + UPDATED_USER_EMAIL, "userEmail.doesNotContain=" + DEFAULT_USER_EMAIL);
    }

    @Test
    @Transactional
    void getAllMembershipsByRoleIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedMembership = membershipRepository.saveAndFlush(membership);

        // Get all the membershipList where role equals to
        defaultMembershipFiltering("role.equals=" + DEFAULT_ROLE, "role.equals=" + UPDATED_ROLE);
    }

    @Test
    @Transactional
    void getAllMembershipsByRoleIsInShouldWork() throws Exception {
        // Initialize the database
        insertedMembership = membershipRepository.saveAndFlush(membership);

        // Get all the membershipList where role in
        defaultMembershipFiltering("role.in=" + DEFAULT_ROLE + "," + UPDATED_ROLE, "role.in=" + UPDATED_ROLE);
    }

    @Test
    @Transactional
    void getAllMembershipsByRoleIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedMembership = membershipRepository.saveAndFlush(membership);

        // Get all the membershipList where role is not null
        defaultMembershipFiltering("role.specified=true", "role.specified=false");
    }

    @Test
    @Transactional
    void getAllMembershipsByJoinedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedMembership = membershipRepository.saveAndFlush(membership);

        // Get all the membershipList where joinedAt equals to
        defaultMembershipFiltering("joinedAt.equals=" + DEFAULT_JOINED_AT, "joinedAt.equals=" + UPDATED_JOINED_AT);
    }

    @Test
    @Transactional
    void getAllMembershipsByJoinedAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedMembership = membershipRepository.saveAndFlush(membership);

        // Get all the membershipList where joinedAt in
        defaultMembershipFiltering("joinedAt.in=" + DEFAULT_JOINED_AT + "," + UPDATED_JOINED_AT, "joinedAt.in=" + UPDATED_JOINED_AT);
    }

    @Test
    @Transactional
    void getAllMembershipsByJoinedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedMembership = membershipRepository.saveAndFlush(membership);

        // Get all the membershipList where joinedAt is not null
        defaultMembershipFiltering("joinedAt.specified=true", "joinedAt.specified=false");
    }

    @Test
    @Transactional
    void getAllMembershipsByActiveIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedMembership = membershipRepository.saveAndFlush(membership);

        // Get all the membershipList where active equals to
        defaultMembershipFiltering("active.equals=" + DEFAULT_ACTIVE, "active.equals=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllMembershipsByActiveIsInShouldWork() throws Exception {
        // Initialize the database
        insertedMembership = membershipRepository.saveAndFlush(membership);

        // Get all the membershipList where active in
        defaultMembershipFiltering("active.in=" + DEFAULT_ACTIVE + "," + UPDATED_ACTIVE, "active.in=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllMembershipsByActiveIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedMembership = membershipRepository.saveAndFlush(membership);

        // Get all the membershipList where active is not null
        defaultMembershipFiltering("active.specified=true", "active.specified=false");
    }

    @Test
    @Transactional
    void getAllMembershipsByOrganizationIsEqualToSomething() throws Exception {
        Organization organization;
        if (TestUtil.findAll(em, Organization.class).isEmpty()) {
            membershipRepository.saveAndFlush(membership);
            organization = OrganizationResourceIT.createEntity();
        } else {
            organization = TestUtil.findAll(em, Organization.class).get(0);
        }
        em.persist(organization);
        em.flush();
        membership.setOrganization(organization);
        membershipRepository.saveAndFlush(membership);
        Long organizationId = organization.getId();
        // Get all the membershipList where organization equals to organizationId
        defaultMembershipShouldBeFound("organizationId.equals=" + organizationId);

        // Get all the membershipList where organization equals to (organizationId + 1)
        defaultMembershipShouldNotBeFound("organizationId.equals=" + (organizationId + 1));
    }

    private void defaultMembershipFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultMembershipShouldBeFound(shouldBeFound);
        defaultMembershipShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultMembershipShouldBeFound(String filter) throws Exception {
        restMembershipMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(membership.getId().intValue())))
            .andExpect(jsonPath("$.[*].userId").value(hasItem(DEFAULT_USER_ID.intValue())))
            .andExpect(jsonPath("$.[*].userLogin").value(hasItem(DEFAULT_USER_LOGIN)))
            .andExpect(jsonPath("$.[*].userEmail").value(hasItem(DEFAULT_USER_EMAIL)))
            .andExpect(jsonPath("$.[*].role").value(hasItem(DEFAULT_ROLE.toString())))
            .andExpect(jsonPath("$.[*].joinedAt").value(hasItem(DEFAULT_JOINED_AT.toString())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE)));

        // Check, that the count call also returns 1
        restMembershipMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultMembershipShouldNotBeFound(String filter) throws Exception {
        restMembershipMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restMembershipMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingMembership() throws Exception {
        // Get the membership
        restMembershipMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingMembership() throws Exception {
        // Initialize the database
        insertedMembership = membershipRepository.saveAndFlush(membership);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the membership
        Membership updatedMembership = membershipRepository.findById(membership.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedMembership are not directly saved in db
        em.detach(updatedMembership);
        updatedMembership
            .userId(UPDATED_USER_ID)
            .userLogin(UPDATED_USER_LOGIN)
            .userEmail(UPDATED_USER_EMAIL)
            .role(UPDATED_ROLE)
            .joinedAt(UPDATED_JOINED_AT)
            .active(UPDATED_ACTIVE);
        MembershipDTO membershipDTO = membershipMapper.toDto(updatedMembership);

        restMembershipMockMvc
            .perform(
                put(ENTITY_API_URL_ID, membershipDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(membershipDTO))
            )
            .andExpect(status().isOk());

        // Validate the Membership in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedMembershipToMatchAllProperties(updatedMembership);
    }

    @Test
    @Transactional
    void putNonExistingMembership() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        membership.setId(longCount.incrementAndGet());

        // Create the Membership
        MembershipDTO membershipDTO = membershipMapper.toDto(membership);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMembershipMockMvc
            .perform(
                put(ENTITY_API_URL_ID, membershipDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(membershipDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Membership in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchMembership() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        membership.setId(longCount.incrementAndGet());

        // Create the Membership
        MembershipDTO membershipDTO = membershipMapper.toDto(membership);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMembershipMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(membershipDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Membership in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamMembership() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        membership.setId(longCount.incrementAndGet());

        // Create the Membership
        MembershipDTO membershipDTO = membershipMapper.toDto(membership);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMembershipMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(membershipDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Membership in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateMembershipWithPatch() throws Exception {
        // Initialize the database
        insertedMembership = membershipRepository.saveAndFlush(membership);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the membership using partial update
        Membership partialUpdatedMembership = new Membership();
        partialUpdatedMembership.setId(membership.getId());

        partialUpdatedMembership.userId(UPDATED_USER_ID).joinedAt(UPDATED_JOINED_AT).active(UPDATED_ACTIVE);

        restMembershipMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMembership.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedMembership))
            )
            .andExpect(status().isOk());

        // Validate the Membership in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMembershipUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedMembership, membership),
            getPersistedMembership(membership)
        );
    }

    @Test
    @Transactional
    void fullUpdateMembershipWithPatch() throws Exception {
        // Initialize the database
        insertedMembership = membershipRepository.saveAndFlush(membership);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the membership using partial update
        Membership partialUpdatedMembership = new Membership();
        partialUpdatedMembership.setId(membership.getId());

        partialUpdatedMembership
            .userId(UPDATED_USER_ID)
            .userLogin(UPDATED_USER_LOGIN)
            .userEmail(UPDATED_USER_EMAIL)
            .role(UPDATED_ROLE)
            .joinedAt(UPDATED_JOINED_AT)
            .active(UPDATED_ACTIVE);

        restMembershipMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMembership.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedMembership))
            )
            .andExpect(status().isOk());

        // Validate the Membership in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMembershipUpdatableFieldsEquals(partialUpdatedMembership, getPersistedMembership(partialUpdatedMembership));
    }

    @Test
    @Transactional
    void patchNonExistingMembership() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        membership.setId(longCount.incrementAndGet());

        // Create the Membership
        MembershipDTO membershipDTO = membershipMapper.toDto(membership);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMembershipMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, membershipDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(membershipDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Membership in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchMembership() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        membership.setId(longCount.incrementAndGet());

        // Create the Membership
        MembershipDTO membershipDTO = membershipMapper.toDto(membership);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMembershipMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(membershipDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Membership in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamMembership() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        membership.setId(longCount.incrementAndGet());

        // Create the Membership
        MembershipDTO membershipDTO = membershipMapper.toDto(membership);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMembershipMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(membershipDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Membership in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteMembership() throws Exception {
        // Initialize the database
        insertedMembership = membershipRepository.saveAndFlush(membership);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the membership
        restMembershipMockMvc
            .perform(delete(ENTITY_API_URL_ID, membership.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return membershipRepository.count();
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

    protected Membership getPersistedMembership(Membership membership) {
        return membershipRepository.findById(membership.getId()).orElseThrow();
    }

    protected void assertPersistedMembershipToMatchAllProperties(Membership expectedMembership) {
        assertMembershipAllPropertiesEquals(expectedMembership, getPersistedMembership(expectedMembership));
    }

    protected void assertPersistedMembershipToMatchUpdatableProperties(Membership expectedMembership) {
        assertMembershipAllUpdatablePropertiesEquals(expectedMembership, getPersistedMembership(expectedMembership));
    }
}
