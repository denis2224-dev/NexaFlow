package com.nexaflow.userservice.web.rest;

import static com.nexaflow.userservice.domain.InvitationAsserts.*;
import static com.nexaflow.userservice.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexaflow.userservice.IntegrationTest;
import com.nexaflow.userservice.domain.Invitation;
import com.nexaflow.userservice.domain.Organization;
import com.nexaflow.userservice.domain.enumeration.InvitationStatus;
import com.nexaflow.userservice.domain.enumeration.MembershipRole;
import com.nexaflow.userservice.repository.InvitationRepository;
import com.nexaflow.userservice.service.InvitationService;
import com.nexaflow.userservice.service.dto.InvitationDTO;
import com.nexaflow.userservice.service.mapper.InvitationMapper;
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
 * Integration tests for the {@link InvitationResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class InvitationResourceIT {

    private static final String DEFAULT_EMAIL = "AAAAAAAAAA";
    private static final String UPDATED_EMAIL = "BBBBBBBBBB";

    private static final String DEFAULT_TOKEN = "AAAAAAAAAA";
    private static final String UPDATED_TOKEN = "BBBBBBBBBB";

    private static final MembershipRole DEFAULT_ROLE = MembershipRole.OWNER;
    private static final MembershipRole UPDATED_ROLE = MembershipRole.ADMIN;

    private static final InvitationStatus DEFAULT_STATUS = InvitationStatus.PENDING;
    private static final InvitationStatus UPDATED_STATUS = InvitationStatus.ACCEPTED;

    private static final Instant DEFAULT_INVITED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_INVITED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_EXPIRES_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_EXPIRES_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_ACCEPTED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_ACCEPTED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Long DEFAULT_INVITED_BY_USER_ID = 1L;
    private static final Long UPDATED_INVITED_BY_USER_ID = 2L;
    private static final Long SMALLER_INVITED_BY_USER_ID = 1L - 1L;

    private static final String DEFAULT_INVITED_BY_LOGIN = "AAAAAAAAAA";
    private static final String UPDATED_INVITED_BY_LOGIN = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/invitations";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private InvitationRepository invitationRepository;

    @Mock
    private InvitationRepository invitationRepositoryMock;

    @Autowired
    private InvitationMapper invitationMapper;

    @Mock
    private InvitationService invitationServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restInvitationMockMvc;

    private Invitation invitation;

    private Invitation insertedInvitation;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Invitation createEntity(EntityManager em) {
        Invitation invitation = new Invitation()
            .email(DEFAULT_EMAIL)
            .token(DEFAULT_TOKEN)
            .role(DEFAULT_ROLE)
            .status(DEFAULT_STATUS)
            .invitedAt(DEFAULT_INVITED_AT)
            .expiresAt(DEFAULT_EXPIRES_AT)
            .acceptedAt(DEFAULT_ACCEPTED_AT)
            .invitedByUserId(DEFAULT_INVITED_BY_USER_ID)
            .invitedByLogin(DEFAULT_INVITED_BY_LOGIN);
        // Add required entity
        Organization organization;
        if (TestUtil.findAll(em, Organization.class).isEmpty()) {
            organization = OrganizationResourceIT.createEntity();
            em.persist(organization);
            em.flush();
        } else {
            organization = TestUtil.findAll(em, Organization.class).get(0);
        }
        invitation.setOrganization(organization);
        return invitation;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Invitation createUpdatedEntity(EntityManager em) {
        Invitation updatedInvitation = new Invitation()
            .email(UPDATED_EMAIL)
            .token(UPDATED_TOKEN)
            .role(UPDATED_ROLE)
            .status(UPDATED_STATUS)
            .invitedAt(UPDATED_INVITED_AT)
            .expiresAt(UPDATED_EXPIRES_AT)
            .acceptedAt(UPDATED_ACCEPTED_AT)
            .invitedByUserId(UPDATED_INVITED_BY_USER_ID)
            .invitedByLogin(UPDATED_INVITED_BY_LOGIN);
        // Add required entity
        Organization organization;
        if (TestUtil.findAll(em, Organization.class).isEmpty()) {
            organization = OrganizationResourceIT.createUpdatedEntity();
            em.persist(organization);
            em.flush();
        } else {
            organization = TestUtil.findAll(em, Organization.class).get(0);
        }
        updatedInvitation.setOrganization(organization);
        return updatedInvitation;
    }

    @BeforeEach
    void initTest() {
        invitation = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedInvitation != null) {
            invitationRepository.delete(insertedInvitation);
            insertedInvitation = null;
        }
    }

    @Test
    @Transactional
    void createInvitation() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Invitation
        InvitationDTO invitationDTO = invitationMapper.toDto(invitation);
        var returnedInvitationDTO = om.readValue(
            restInvitationMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(invitationDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            InvitationDTO.class
        );

        // Validate the Invitation in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedInvitation = invitationMapper.toEntity(returnedInvitationDTO);
        assertInvitationUpdatableFieldsEquals(returnedInvitation, getPersistedInvitation(returnedInvitation));

        insertedInvitation = returnedInvitation;
    }

    @Test
    @Transactional
    void createInvitationWithExistingId() throws Exception {
        // Create the Invitation with an existing ID
        invitation.setId(1L);
        InvitationDTO invitationDTO = invitationMapper.toDto(invitation);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restInvitationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(invitationDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Invitation in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkEmailIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        invitation.setEmail(null);

        // Create the Invitation, which fails.
        InvitationDTO invitationDTO = invitationMapper.toDto(invitation);

        restInvitationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(invitationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkTokenIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        invitation.setToken(null);

        // Create the Invitation, which fails.
        InvitationDTO invitationDTO = invitationMapper.toDto(invitation);

        restInvitationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(invitationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkRoleIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        invitation.setRole(null);

        // Create the Invitation, which fails.
        InvitationDTO invitationDTO = invitationMapper.toDto(invitation);

        restInvitationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(invitationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        invitation.setStatus(null);

        // Create the Invitation, which fails.
        InvitationDTO invitationDTO = invitationMapper.toDto(invitation);

        restInvitationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(invitationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkInvitedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        invitation.setInvitedAt(null);

        // Create the Invitation, which fails.
        InvitationDTO invitationDTO = invitationMapper.toDto(invitation);

        restInvitationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(invitationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkExpiresAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        invitation.setExpiresAt(null);

        // Create the Invitation, which fails.
        InvitationDTO invitationDTO = invitationMapper.toDto(invitation);

        restInvitationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(invitationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkInvitedByUserIdIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        invitation.setInvitedByUserId(null);

        // Create the Invitation, which fails.
        InvitationDTO invitationDTO = invitationMapper.toDto(invitation);

        restInvitationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(invitationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkInvitedByLoginIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        invitation.setInvitedByLogin(null);

        // Create the Invitation, which fails.
        InvitationDTO invitationDTO = invitationMapper.toDto(invitation);

        restInvitationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(invitationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllInvitations() throws Exception {
        // Initialize the database
        insertedInvitation = invitationRepository.saveAndFlush(invitation);

        // Get all the invitationList
        restInvitationMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(invitation.getId().intValue())))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)))
            .andExpect(jsonPath("$.[*].token").value(hasItem(DEFAULT_TOKEN)))
            .andExpect(jsonPath("$.[*].role").value(hasItem(DEFAULT_ROLE.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].invitedAt").value(hasItem(DEFAULT_INVITED_AT.toString())))
            .andExpect(jsonPath("$.[*].expiresAt").value(hasItem(DEFAULT_EXPIRES_AT.toString())))
            .andExpect(jsonPath("$.[*].acceptedAt").value(hasItem(DEFAULT_ACCEPTED_AT.toString())))
            .andExpect(jsonPath("$.[*].invitedByUserId").value(hasItem(DEFAULT_INVITED_BY_USER_ID.intValue())))
            .andExpect(jsonPath("$.[*].invitedByLogin").value(hasItem(DEFAULT_INVITED_BY_LOGIN)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllInvitationsWithEagerRelationshipsIsEnabled() throws Exception {
        when(invitationServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restInvitationMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(invitationServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllInvitationsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(invitationServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restInvitationMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(invitationRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getInvitation() throws Exception {
        // Initialize the database
        insertedInvitation = invitationRepository.saveAndFlush(invitation);

        // Get the invitation
        restInvitationMockMvc
            .perform(get(ENTITY_API_URL_ID, invitation.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(invitation.getId().intValue()))
            .andExpect(jsonPath("$.email").value(DEFAULT_EMAIL))
            .andExpect(jsonPath("$.token").value(DEFAULT_TOKEN))
            .andExpect(jsonPath("$.role").value(DEFAULT_ROLE.toString()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.invitedAt").value(DEFAULT_INVITED_AT.toString()))
            .andExpect(jsonPath("$.expiresAt").value(DEFAULT_EXPIRES_AT.toString()))
            .andExpect(jsonPath("$.acceptedAt").value(DEFAULT_ACCEPTED_AT.toString()))
            .andExpect(jsonPath("$.invitedByUserId").value(DEFAULT_INVITED_BY_USER_ID.intValue()))
            .andExpect(jsonPath("$.invitedByLogin").value(DEFAULT_INVITED_BY_LOGIN));
    }

    @Test
    @Transactional
    void getInvitationsByIdFiltering() throws Exception {
        // Initialize the database
        insertedInvitation = invitationRepository.saveAndFlush(invitation);

        Long id = invitation.getId();

        defaultInvitationFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultInvitationFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultInvitationFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllInvitationsByEmailIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedInvitation = invitationRepository.saveAndFlush(invitation);

        // Get all the invitationList where email equals to
        defaultInvitationFiltering("email.equals=" + DEFAULT_EMAIL, "email.equals=" + UPDATED_EMAIL);
    }

    @Test
    @Transactional
    void getAllInvitationsByEmailIsInShouldWork() throws Exception {
        // Initialize the database
        insertedInvitation = invitationRepository.saveAndFlush(invitation);

        // Get all the invitationList where email in
        defaultInvitationFiltering("email.in=" + DEFAULT_EMAIL + "," + UPDATED_EMAIL, "email.in=" + UPDATED_EMAIL);
    }

    @Test
    @Transactional
    void getAllInvitationsByEmailIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedInvitation = invitationRepository.saveAndFlush(invitation);

        // Get all the invitationList where email is not null
        defaultInvitationFiltering("email.specified=true", "email.specified=false");
    }

    @Test
    @Transactional
    void getAllInvitationsByEmailContainsSomething() throws Exception {
        // Initialize the database
        insertedInvitation = invitationRepository.saveAndFlush(invitation);

        // Get all the invitationList where email contains
        defaultInvitationFiltering("email.contains=" + DEFAULT_EMAIL, "email.contains=" + UPDATED_EMAIL);
    }

    @Test
    @Transactional
    void getAllInvitationsByEmailNotContainsSomething() throws Exception {
        // Initialize the database
        insertedInvitation = invitationRepository.saveAndFlush(invitation);

        // Get all the invitationList where email does not contain
        defaultInvitationFiltering("email.doesNotContain=" + UPDATED_EMAIL, "email.doesNotContain=" + DEFAULT_EMAIL);
    }

    @Test
    @Transactional
    void getAllInvitationsByTokenIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedInvitation = invitationRepository.saveAndFlush(invitation);

        // Get all the invitationList where token equals to
        defaultInvitationFiltering("token.equals=" + DEFAULT_TOKEN, "token.equals=" + UPDATED_TOKEN);
    }

    @Test
    @Transactional
    void getAllInvitationsByTokenIsInShouldWork() throws Exception {
        // Initialize the database
        insertedInvitation = invitationRepository.saveAndFlush(invitation);

        // Get all the invitationList where token in
        defaultInvitationFiltering("token.in=" + DEFAULT_TOKEN + "," + UPDATED_TOKEN, "token.in=" + UPDATED_TOKEN);
    }

    @Test
    @Transactional
    void getAllInvitationsByTokenIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedInvitation = invitationRepository.saveAndFlush(invitation);

        // Get all the invitationList where token is not null
        defaultInvitationFiltering("token.specified=true", "token.specified=false");
    }

    @Test
    @Transactional
    void getAllInvitationsByTokenContainsSomething() throws Exception {
        // Initialize the database
        insertedInvitation = invitationRepository.saveAndFlush(invitation);

        // Get all the invitationList where token contains
        defaultInvitationFiltering("token.contains=" + DEFAULT_TOKEN, "token.contains=" + UPDATED_TOKEN);
    }

    @Test
    @Transactional
    void getAllInvitationsByTokenNotContainsSomething() throws Exception {
        // Initialize the database
        insertedInvitation = invitationRepository.saveAndFlush(invitation);

        // Get all the invitationList where token does not contain
        defaultInvitationFiltering("token.doesNotContain=" + UPDATED_TOKEN, "token.doesNotContain=" + DEFAULT_TOKEN);
    }

    @Test
    @Transactional
    void getAllInvitationsByRoleIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedInvitation = invitationRepository.saveAndFlush(invitation);

        // Get all the invitationList where role equals to
        defaultInvitationFiltering("role.equals=" + DEFAULT_ROLE, "role.equals=" + UPDATED_ROLE);
    }

    @Test
    @Transactional
    void getAllInvitationsByRoleIsInShouldWork() throws Exception {
        // Initialize the database
        insertedInvitation = invitationRepository.saveAndFlush(invitation);

        // Get all the invitationList where role in
        defaultInvitationFiltering("role.in=" + DEFAULT_ROLE + "," + UPDATED_ROLE, "role.in=" + UPDATED_ROLE);
    }

    @Test
    @Transactional
    void getAllInvitationsByRoleIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedInvitation = invitationRepository.saveAndFlush(invitation);

        // Get all the invitationList where role is not null
        defaultInvitationFiltering("role.specified=true", "role.specified=false");
    }

    @Test
    @Transactional
    void getAllInvitationsByStatusIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedInvitation = invitationRepository.saveAndFlush(invitation);

        // Get all the invitationList where status equals to
        defaultInvitationFiltering("status.equals=" + DEFAULT_STATUS, "status.equals=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllInvitationsByStatusIsInShouldWork() throws Exception {
        // Initialize the database
        insertedInvitation = invitationRepository.saveAndFlush(invitation);

        // Get all the invitationList where status in
        defaultInvitationFiltering("status.in=" + DEFAULT_STATUS + "," + UPDATED_STATUS, "status.in=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllInvitationsByStatusIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedInvitation = invitationRepository.saveAndFlush(invitation);

        // Get all the invitationList where status is not null
        defaultInvitationFiltering("status.specified=true", "status.specified=false");
    }

    @Test
    @Transactional
    void getAllInvitationsByInvitedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedInvitation = invitationRepository.saveAndFlush(invitation);

        // Get all the invitationList where invitedAt equals to
        defaultInvitationFiltering("invitedAt.equals=" + DEFAULT_INVITED_AT, "invitedAt.equals=" + UPDATED_INVITED_AT);
    }

    @Test
    @Transactional
    void getAllInvitationsByInvitedAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedInvitation = invitationRepository.saveAndFlush(invitation);

        // Get all the invitationList where invitedAt in
        defaultInvitationFiltering("invitedAt.in=" + DEFAULT_INVITED_AT + "," + UPDATED_INVITED_AT, "invitedAt.in=" + UPDATED_INVITED_AT);
    }

    @Test
    @Transactional
    void getAllInvitationsByInvitedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedInvitation = invitationRepository.saveAndFlush(invitation);

        // Get all the invitationList where invitedAt is not null
        defaultInvitationFiltering("invitedAt.specified=true", "invitedAt.specified=false");
    }

    @Test
    @Transactional
    void getAllInvitationsByExpiresAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedInvitation = invitationRepository.saveAndFlush(invitation);

        // Get all the invitationList where expiresAt equals to
        defaultInvitationFiltering("expiresAt.equals=" + DEFAULT_EXPIRES_AT, "expiresAt.equals=" + UPDATED_EXPIRES_AT);
    }

    @Test
    @Transactional
    void getAllInvitationsByExpiresAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedInvitation = invitationRepository.saveAndFlush(invitation);

        // Get all the invitationList where expiresAt in
        defaultInvitationFiltering("expiresAt.in=" + DEFAULT_EXPIRES_AT + "," + UPDATED_EXPIRES_AT, "expiresAt.in=" + UPDATED_EXPIRES_AT);
    }

    @Test
    @Transactional
    void getAllInvitationsByExpiresAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedInvitation = invitationRepository.saveAndFlush(invitation);

        // Get all the invitationList where expiresAt is not null
        defaultInvitationFiltering("expiresAt.specified=true", "expiresAt.specified=false");
    }

    @Test
    @Transactional
    void getAllInvitationsByAcceptedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedInvitation = invitationRepository.saveAndFlush(invitation);

        // Get all the invitationList where acceptedAt equals to
        defaultInvitationFiltering("acceptedAt.equals=" + DEFAULT_ACCEPTED_AT, "acceptedAt.equals=" + UPDATED_ACCEPTED_AT);
    }

    @Test
    @Transactional
    void getAllInvitationsByAcceptedAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedInvitation = invitationRepository.saveAndFlush(invitation);

        // Get all the invitationList where acceptedAt in
        defaultInvitationFiltering(
            "acceptedAt.in=" + DEFAULT_ACCEPTED_AT + "," + UPDATED_ACCEPTED_AT,
            "acceptedAt.in=" + UPDATED_ACCEPTED_AT
        );
    }

    @Test
    @Transactional
    void getAllInvitationsByAcceptedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedInvitation = invitationRepository.saveAndFlush(invitation);

        // Get all the invitationList where acceptedAt is not null
        defaultInvitationFiltering("acceptedAt.specified=true", "acceptedAt.specified=false");
    }

    @Test
    @Transactional
    void getAllInvitationsByInvitedByUserIdIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedInvitation = invitationRepository.saveAndFlush(invitation);

        // Get all the invitationList where invitedByUserId equals to
        defaultInvitationFiltering(
            "invitedByUserId.equals=" + DEFAULT_INVITED_BY_USER_ID,
            "invitedByUserId.equals=" + UPDATED_INVITED_BY_USER_ID
        );
    }

    @Test
    @Transactional
    void getAllInvitationsByInvitedByUserIdIsInShouldWork() throws Exception {
        // Initialize the database
        insertedInvitation = invitationRepository.saveAndFlush(invitation);

        // Get all the invitationList where invitedByUserId in
        defaultInvitationFiltering(
            "invitedByUserId.in=" + DEFAULT_INVITED_BY_USER_ID + "," + UPDATED_INVITED_BY_USER_ID,
            "invitedByUserId.in=" + UPDATED_INVITED_BY_USER_ID
        );
    }

    @Test
    @Transactional
    void getAllInvitationsByInvitedByUserIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedInvitation = invitationRepository.saveAndFlush(invitation);

        // Get all the invitationList where invitedByUserId is not null
        defaultInvitationFiltering("invitedByUserId.specified=true", "invitedByUserId.specified=false");
    }

    @Test
    @Transactional
    void getAllInvitationsByInvitedByUserIdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedInvitation = invitationRepository.saveAndFlush(invitation);

        // Get all the invitationList where invitedByUserId is greater than or equal to
        defaultInvitationFiltering(
            "invitedByUserId.greaterThanOrEqual=" + DEFAULT_INVITED_BY_USER_ID,
            "invitedByUserId.greaterThanOrEqual=" + UPDATED_INVITED_BY_USER_ID
        );
    }

    @Test
    @Transactional
    void getAllInvitationsByInvitedByUserIdIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedInvitation = invitationRepository.saveAndFlush(invitation);

        // Get all the invitationList where invitedByUserId is less than or equal to
        defaultInvitationFiltering(
            "invitedByUserId.lessThanOrEqual=" + DEFAULT_INVITED_BY_USER_ID,
            "invitedByUserId.lessThanOrEqual=" + SMALLER_INVITED_BY_USER_ID
        );
    }

    @Test
    @Transactional
    void getAllInvitationsByInvitedByUserIdIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedInvitation = invitationRepository.saveAndFlush(invitation);

        // Get all the invitationList where invitedByUserId is less than
        defaultInvitationFiltering(
            "invitedByUserId.lessThan=" + UPDATED_INVITED_BY_USER_ID,
            "invitedByUserId.lessThan=" + DEFAULT_INVITED_BY_USER_ID
        );
    }

    @Test
    @Transactional
    void getAllInvitationsByInvitedByUserIdIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedInvitation = invitationRepository.saveAndFlush(invitation);

        // Get all the invitationList where invitedByUserId is greater than
        defaultInvitationFiltering(
            "invitedByUserId.greaterThan=" + SMALLER_INVITED_BY_USER_ID,
            "invitedByUserId.greaterThan=" + DEFAULT_INVITED_BY_USER_ID
        );
    }

    @Test
    @Transactional
    void getAllInvitationsByInvitedByLoginIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedInvitation = invitationRepository.saveAndFlush(invitation);

        // Get all the invitationList where invitedByLogin equals to
        defaultInvitationFiltering(
            "invitedByLogin.equals=" + DEFAULT_INVITED_BY_LOGIN,
            "invitedByLogin.equals=" + UPDATED_INVITED_BY_LOGIN
        );
    }

    @Test
    @Transactional
    void getAllInvitationsByInvitedByLoginIsInShouldWork() throws Exception {
        // Initialize the database
        insertedInvitation = invitationRepository.saveAndFlush(invitation);

        // Get all the invitationList where invitedByLogin in
        defaultInvitationFiltering(
            "invitedByLogin.in=" + DEFAULT_INVITED_BY_LOGIN + "," + UPDATED_INVITED_BY_LOGIN,
            "invitedByLogin.in=" + UPDATED_INVITED_BY_LOGIN
        );
    }

    @Test
    @Transactional
    void getAllInvitationsByInvitedByLoginIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedInvitation = invitationRepository.saveAndFlush(invitation);

        // Get all the invitationList where invitedByLogin is not null
        defaultInvitationFiltering("invitedByLogin.specified=true", "invitedByLogin.specified=false");
    }

    @Test
    @Transactional
    void getAllInvitationsByInvitedByLoginContainsSomething() throws Exception {
        // Initialize the database
        insertedInvitation = invitationRepository.saveAndFlush(invitation);

        // Get all the invitationList where invitedByLogin contains
        defaultInvitationFiltering(
            "invitedByLogin.contains=" + DEFAULT_INVITED_BY_LOGIN,
            "invitedByLogin.contains=" + UPDATED_INVITED_BY_LOGIN
        );
    }

    @Test
    @Transactional
    void getAllInvitationsByInvitedByLoginNotContainsSomething() throws Exception {
        // Initialize the database
        insertedInvitation = invitationRepository.saveAndFlush(invitation);

        // Get all the invitationList where invitedByLogin does not contain
        defaultInvitationFiltering(
            "invitedByLogin.doesNotContain=" + UPDATED_INVITED_BY_LOGIN,
            "invitedByLogin.doesNotContain=" + DEFAULT_INVITED_BY_LOGIN
        );
    }

    @Test
    @Transactional
    void getAllInvitationsByOrganizationIsEqualToSomething() throws Exception {
        Organization organization;
        if (TestUtil.findAll(em, Organization.class).isEmpty()) {
            invitationRepository.saveAndFlush(invitation);
            organization = OrganizationResourceIT.createEntity();
        } else {
            organization = TestUtil.findAll(em, Organization.class).get(0);
        }
        em.persist(organization);
        em.flush();
        invitation.setOrganization(organization);
        invitationRepository.saveAndFlush(invitation);
        Long organizationId = organization.getId();
        // Get all the invitationList where organization equals to organizationId
        defaultInvitationShouldBeFound("organizationId.equals=" + organizationId);

        // Get all the invitationList where organization equals to (organizationId + 1)
        defaultInvitationShouldNotBeFound("organizationId.equals=" + (organizationId + 1));
    }

    private void defaultInvitationFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultInvitationShouldBeFound(shouldBeFound);
        defaultInvitationShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultInvitationShouldBeFound(String filter) throws Exception {
        restInvitationMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(invitation.getId().intValue())))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)))
            .andExpect(jsonPath("$.[*].token").value(hasItem(DEFAULT_TOKEN)))
            .andExpect(jsonPath("$.[*].role").value(hasItem(DEFAULT_ROLE.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].invitedAt").value(hasItem(DEFAULT_INVITED_AT.toString())))
            .andExpect(jsonPath("$.[*].expiresAt").value(hasItem(DEFAULT_EXPIRES_AT.toString())))
            .andExpect(jsonPath("$.[*].acceptedAt").value(hasItem(DEFAULT_ACCEPTED_AT.toString())))
            .andExpect(jsonPath("$.[*].invitedByUserId").value(hasItem(DEFAULT_INVITED_BY_USER_ID.intValue())))
            .andExpect(jsonPath("$.[*].invitedByLogin").value(hasItem(DEFAULT_INVITED_BY_LOGIN)));

        // Check, that the count call also returns 1
        restInvitationMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultInvitationShouldNotBeFound(String filter) throws Exception {
        restInvitationMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restInvitationMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingInvitation() throws Exception {
        // Get the invitation
        restInvitationMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingInvitation() throws Exception {
        // Initialize the database
        insertedInvitation = invitationRepository.saveAndFlush(invitation);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the invitation
        Invitation updatedInvitation = invitationRepository.findById(invitation.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedInvitation are not directly saved in db
        em.detach(updatedInvitation);
        updatedInvitation
            .email(UPDATED_EMAIL)
            .token(UPDATED_TOKEN)
            .role(UPDATED_ROLE)
            .status(UPDATED_STATUS)
            .invitedAt(UPDATED_INVITED_AT)
            .expiresAt(UPDATED_EXPIRES_AT)
            .acceptedAt(UPDATED_ACCEPTED_AT)
            .invitedByUserId(UPDATED_INVITED_BY_USER_ID)
            .invitedByLogin(UPDATED_INVITED_BY_LOGIN);
        InvitationDTO invitationDTO = invitationMapper.toDto(updatedInvitation);

        restInvitationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, invitationDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(invitationDTO))
            )
            .andExpect(status().isOk());

        // Validate the Invitation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedInvitationToMatchAllProperties(updatedInvitation);
    }

    @Test
    @Transactional
    void putNonExistingInvitation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        invitation.setId(longCount.incrementAndGet());

        // Create the Invitation
        InvitationDTO invitationDTO = invitationMapper.toDto(invitation);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restInvitationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, invitationDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(invitationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Invitation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchInvitation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        invitation.setId(longCount.incrementAndGet());

        // Create the Invitation
        InvitationDTO invitationDTO = invitationMapper.toDto(invitation);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restInvitationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(invitationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Invitation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamInvitation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        invitation.setId(longCount.incrementAndGet());

        // Create the Invitation
        InvitationDTO invitationDTO = invitationMapper.toDto(invitation);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restInvitationMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(invitationDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Invitation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateInvitationWithPatch() throws Exception {
        // Initialize the database
        insertedInvitation = invitationRepository.saveAndFlush(invitation);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the invitation using partial update
        Invitation partialUpdatedInvitation = new Invitation();
        partialUpdatedInvitation.setId(invitation.getId());

        partialUpdatedInvitation.token(UPDATED_TOKEN);

        restInvitationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedInvitation.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedInvitation))
            )
            .andExpect(status().isOk());

        // Validate the Invitation in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertInvitationUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedInvitation, invitation),
            getPersistedInvitation(invitation)
        );
    }

    @Test
    @Transactional
    void fullUpdateInvitationWithPatch() throws Exception {
        // Initialize the database
        insertedInvitation = invitationRepository.saveAndFlush(invitation);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the invitation using partial update
        Invitation partialUpdatedInvitation = new Invitation();
        partialUpdatedInvitation.setId(invitation.getId());

        partialUpdatedInvitation
            .email(UPDATED_EMAIL)
            .token(UPDATED_TOKEN)
            .role(UPDATED_ROLE)
            .status(UPDATED_STATUS)
            .invitedAt(UPDATED_INVITED_AT)
            .expiresAt(UPDATED_EXPIRES_AT)
            .acceptedAt(UPDATED_ACCEPTED_AT)
            .invitedByUserId(UPDATED_INVITED_BY_USER_ID)
            .invitedByLogin(UPDATED_INVITED_BY_LOGIN);

        restInvitationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedInvitation.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedInvitation))
            )
            .andExpect(status().isOk());

        // Validate the Invitation in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertInvitationUpdatableFieldsEquals(partialUpdatedInvitation, getPersistedInvitation(partialUpdatedInvitation));
    }

    @Test
    @Transactional
    void patchNonExistingInvitation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        invitation.setId(longCount.incrementAndGet());

        // Create the Invitation
        InvitationDTO invitationDTO = invitationMapper.toDto(invitation);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restInvitationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, invitationDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(invitationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Invitation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchInvitation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        invitation.setId(longCount.incrementAndGet());

        // Create the Invitation
        InvitationDTO invitationDTO = invitationMapper.toDto(invitation);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restInvitationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(invitationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Invitation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamInvitation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        invitation.setId(longCount.incrementAndGet());

        // Create the Invitation
        InvitationDTO invitationDTO = invitationMapper.toDto(invitation);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restInvitationMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(invitationDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Invitation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteInvitation() throws Exception {
        // Initialize the database
        insertedInvitation = invitationRepository.saveAndFlush(invitation);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the invitation
        restInvitationMockMvc
            .perform(delete(ENTITY_API_URL_ID, invitation.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return invitationRepository.count();
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

    protected Invitation getPersistedInvitation(Invitation invitation) {
        return invitationRepository.findById(invitation.getId()).orElseThrow();
    }

    protected void assertPersistedInvitationToMatchAllProperties(Invitation expectedInvitation) {
        assertInvitationAllPropertiesEquals(expectedInvitation, getPersistedInvitation(expectedInvitation));
    }

    protected void assertPersistedInvitationToMatchUpdatableProperties(Invitation expectedInvitation) {
        assertInvitationAllUpdatablePropertiesEquals(expectedInvitation, getPersistedInvitation(expectedInvitation));
    }
}
