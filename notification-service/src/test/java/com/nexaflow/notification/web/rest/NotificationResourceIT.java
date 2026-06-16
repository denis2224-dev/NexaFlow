package com.nexaflow.notification.web.rest;

import static com.nexaflow.notification.domain.NotificationAsserts.*;
import static com.nexaflow.notification.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexaflow.notification.IntegrationTest;
import com.nexaflow.notification.domain.Notification;
import com.nexaflow.notification.domain.enumeration.NotificationType;
import com.nexaflow.notification.domain.enumeration.SourceType;
import com.nexaflow.notification.repository.NotificationRepository;
import com.nexaflow.notification.security.AuthoritiesConstants;
import com.nexaflow.notification.service.dto.CreateNotificationRequest;
import com.nexaflow.notification.service.dto.NotificationDTO;
import com.nexaflow.notification.service.mapper.NotificationMapper;
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
 * Integration tests for the {@link NotificationResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(username = "user", authorities = AuthoritiesConstants.ADMIN)
class NotificationResourceIT {

    private static final Long DEFAULT_ORGANIZATION_ID = 1L;
    private static final Long UPDATED_ORGANIZATION_ID = 2L;
    private static final Long SMALLER_ORGANIZATION_ID = 1L - 1L;

    private static final String DEFAULT_RECIPIENT_LOGIN = "AAAAAAAAAA";
    private static final String UPDATED_RECIPIENT_LOGIN = "BBBBBBBBBB";

    private static final String DEFAULT_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBB";

    private static final String DEFAULT_MESSAGE = "AAAAAAAAAA";
    private static final String UPDATED_MESSAGE = "BBBBBBBBBB";

    private static final NotificationType DEFAULT_TYPE = NotificationType.TASK_ASSIGNED;
    private static final NotificationType UPDATED_TYPE = NotificationType.COMMENT_ADDED;

    private static final SourceType DEFAULT_SOURCE_TYPE = SourceType.PROJECT;
    private static final SourceType UPDATED_SOURCE_TYPE = SourceType.TASK;

    private static final Long DEFAULT_SOURCE_ID = 1L;
    private static final Long UPDATED_SOURCE_ID = 2L;
    private static final Long SMALLER_SOURCE_ID = 1L - 1L;

    private static final Boolean DEFAULT_IS_READ = false;
    private static final Boolean UPDATED_IS_READ = true;

    private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/notifications";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String INTERNAL_ENTITY_API_URL = "/api/internal/notifications";
    private static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";
    private static final String ORGANIZATION_ID_HEADER = "X-Organization-Id";
    private static final String INTERNAL_TOKEN = "test-internal-notification-token";
    private static final String CURRENT_USER_LOGIN = "user";
    private static final String OTHER_USER_LOGIN = "other-user";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restNotificationMockMvc;

    private Notification notification;

    private Notification insertedNotification;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Notification createEntity() {
        return new Notification()
            .organizationId(DEFAULT_ORGANIZATION_ID)
            .recipientLogin(DEFAULT_RECIPIENT_LOGIN)
            .title(DEFAULT_TITLE)
            .message(DEFAULT_MESSAGE)
            .type(DEFAULT_TYPE)
            .sourceType(DEFAULT_SOURCE_TYPE)
            .sourceId(DEFAULT_SOURCE_ID)
            .isRead(DEFAULT_IS_READ)
            .createdAt(DEFAULT_CREATED_AT);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Notification createUpdatedEntity() {
        return new Notification()
            .organizationId(UPDATED_ORGANIZATION_ID)
            .recipientLogin(UPDATED_RECIPIENT_LOGIN)
            .title(UPDATED_TITLE)
            .message(UPDATED_MESSAGE)
            .type(UPDATED_TYPE)
            .sourceType(UPDATED_SOURCE_TYPE)
            .sourceId(UPDATED_SOURCE_ID)
            .isRead(UPDATED_IS_READ)
            .createdAt(UPDATED_CREATED_AT);
    }

    @BeforeEach
    void initTest() {
        notification = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedNotification != null) {
            notificationRepository.delete(insertedNotification);
            insertedNotification = null;
        }
    }

    @Test
    @Transactional
    void createNotification() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Notification
        NotificationDTO notificationDTO = notificationMapper.toDto(notification);
        var returnedNotificationDTO = om.readValue(
            restNotificationMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(notificationDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            NotificationDTO.class
        );

        // Validate the Notification in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedNotification = notificationMapper.toEntity(returnedNotificationDTO);
        assertNotificationUpdatableFieldsEquals(returnedNotification, getPersistedNotification(returnedNotification));

        insertedNotification = returnedNotification;
    }

    @Test
    @Transactional
    void createNotificationWithExistingId() throws Exception {
        // Create the Notification with an existing ID
        notification.setId(1L);
        NotificationDTO notificationDTO = notificationMapper.toDto(notification);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restNotificationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(notificationDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Notification in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkOrganizationIdIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        notification.setOrganizationId(null);

        // Create the Notification, which fails.
        NotificationDTO notificationDTO = notificationMapper.toDto(notification);

        restNotificationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(notificationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkRecipientLoginIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        notification.setRecipientLogin(null);

        // Create the Notification, which fails.
        NotificationDTO notificationDTO = notificationMapper.toDto(notification);

        restNotificationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(notificationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkTitleIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        notification.setTitle(null);

        // Create the Notification, which fails.
        NotificationDTO notificationDTO = notificationMapper.toDto(notification);

        restNotificationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(notificationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkTypeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        notification.setType(null);

        // Create the Notification, which fails.
        NotificationDTO notificationDTO = notificationMapper.toDto(notification);

        restNotificationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(notificationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkIsReadIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        notification.setIsRead(null);

        // Create the Notification, which fails.
        NotificationDTO notificationDTO = notificationMapper.toDto(notification);

        restNotificationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(notificationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCreatedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        notification.setCreatedAt(null);

        // Create the Notification, which fails.
        NotificationDTO notificationDTO = notificationMapper.toDto(notification);

        restNotificationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(notificationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllNotifications() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList
        restNotificationMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(notification.getId().intValue())))
            .andExpect(jsonPath("$.[*].organizationId").value(hasItem(DEFAULT_ORGANIZATION_ID.intValue())))
            .andExpect(jsonPath("$.[*].recipientLogin").value(hasItem(DEFAULT_RECIPIENT_LOGIN)))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].message").value(hasItem(DEFAULT_MESSAGE)))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].sourceType").value(hasItem(DEFAULT_SOURCE_TYPE.toString())))
            .andExpect(jsonPath("$.[*].sourceId").value(hasItem(DEFAULT_SOURCE_ID.intValue())))
            .andExpect(jsonPath("$.[*].isRead").value(hasItem(DEFAULT_IS_READ)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())));
    }

    @Test
    @Transactional
    void getNotification() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get the notification
        restNotificationMockMvc
            .perform(get(ENTITY_API_URL_ID, notification.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(notification.getId().intValue()))
            .andExpect(jsonPath("$.organizationId").value(DEFAULT_ORGANIZATION_ID.intValue()))
            .andExpect(jsonPath("$.recipientLogin").value(DEFAULT_RECIPIENT_LOGIN))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE))
            .andExpect(jsonPath("$.message").value(DEFAULT_MESSAGE))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()))
            .andExpect(jsonPath("$.sourceType").value(DEFAULT_SOURCE_TYPE.toString()))
            .andExpect(jsonPath("$.sourceId").value(DEFAULT_SOURCE_ID.intValue()))
            .andExpect(jsonPath("$.isRead").value(DEFAULT_IS_READ))
            .andExpect(jsonPath("$.createdAt").value(DEFAULT_CREATED_AT.toString()));
    }

    @Test
    @Transactional
    void getNotificationsByIdFiltering() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        Long id = notification.getId();

        defaultNotificationFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultNotificationFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultNotificationFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllNotificationsByOrganizationIdIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList where organizationId equals to
        defaultNotificationFiltering(
            "organizationId.equals=" + DEFAULT_ORGANIZATION_ID,
            "organizationId.equals=" + UPDATED_ORGANIZATION_ID
        );
    }

    @Test
    @Transactional
    void getAllNotificationsByOrganizationIdIsInShouldWork() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList where organizationId in
        defaultNotificationFiltering(
            "organizationId.in=" + DEFAULT_ORGANIZATION_ID + "," + UPDATED_ORGANIZATION_ID,
            "organizationId.in=" + UPDATED_ORGANIZATION_ID
        );
    }

    @Test
    @Transactional
    void getAllNotificationsByOrganizationIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList where organizationId is not null
        defaultNotificationFiltering("organizationId.specified=true", "organizationId.specified=false");
    }

    @Test
    @Transactional
    void getAllNotificationsByOrganizationIdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList where organizationId is greater than or equal to
        defaultNotificationFiltering(
            "organizationId.greaterThanOrEqual=" + DEFAULT_ORGANIZATION_ID,
            "organizationId.greaterThanOrEqual=" + UPDATED_ORGANIZATION_ID
        );
    }

    @Test
    @Transactional
    void getAllNotificationsByOrganizationIdIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList where organizationId is less than or equal to
        defaultNotificationFiltering(
            "organizationId.lessThanOrEqual=" + DEFAULT_ORGANIZATION_ID,
            "organizationId.lessThanOrEqual=" + SMALLER_ORGANIZATION_ID
        );
    }

    @Test
    @Transactional
    void getAllNotificationsByOrganizationIdIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList where organizationId is less than
        defaultNotificationFiltering(
            "organizationId.lessThan=" + UPDATED_ORGANIZATION_ID,
            "organizationId.lessThan=" + DEFAULT_ORGANIZATION_ID
        );
    }

    @Test
    @Transactional
    void getAllNotificationsByOrganizationIdIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList where organizationId is greater than
        defaultNotificationFiltering(
            "organizationId.greaterThan=" + SMALLER_ORGANIZATION_ID,
            "organizationId.greaterThan=" + DEFAULT_ORGANIZATION_ID
        );
    }

    @Test
    @Transactional
    void getAllNotificationsByRecipientLoginIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList where recipientLogin equals to
        defaultNotificationFiltering(
            "recipientLogin.equals=" + DEFAULT_RECIPIENT_LOGIN,
            "recipientLogin.equals=" + UPDATED_RECIPIENT_LOGIN
        );
    }

    @Test
    @Transactional
    void getAllNotificationsByRecipientLoginIsInShouldWork() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList where recipientLogin in
        defaultNotificationFiltering(
            "recipientLogin.in=" + DEFAULT_RECIPIENT_LOGIN + "," + UPDATED_RECIPIENT_LOGIN,
            "recipientLogin.in=" + UPDATED_RECIPIENT_LOGIN
        );
    }

    @Test
    @Transactional
    void getAllNotificationsByRecipientLoginIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList where recipientLogin is not null
        defaultNotificationFiltering("recipientLogin.specified=true", "recipientLogin.specified=false");
    }

    @Test
    @Transactional
    void getAllNotificationsByRecipientLoginContainsSomething() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList where recipientLogin contains
        defaultNotificationFiltering(
            "recipientLogin.contains=" + DEFAULT_RECIPIENT_LOGIN,
            "recipientLogin.contains=" + UPDATED_RECIPIENT_LOGIN
        );
    }

    @Test
    @Transactional
    void getAllNotificationsByRecipientLoginNotContainsSomething() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList where recipientLogin does not contain
        defaultNotificationFiltering(
            "recipientLogin.doesNotContain=" + UPDATED_RECIPIENT_LOGIN,
            "recipientLogin.doesNotContain=" + DEFAULT_RECIPIENT_LOGIN
        );
    }

    @Test
    @Transactional
    void getAllNotificationsByTitleIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList where title equals to
        defaultNotificationFiltering("title.equals=" + DEFAULT_TITLE, "title.equals=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    void getAllNotificationsByTitleIsInShouldWork() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList where title in
        defaultNotificationFiltering("title.in=" + DEFAULT_TITLE + "," + UPDATED_TITLE, "title.in=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    void getAllNotificationsByTitleIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList where title is not null
        defaultNotificationFiltering("title.specified=true", "title.specified=false");
    }

    @Test
    @Transactional
    void getAllNotificationsByTitleContainsSomething() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList where title contains
        defaultNotificationFiltering("title.contains=" + DEFAULT_TITLE, "title.contains=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    void getAllNotificationsByTitleNotContainsSomething() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList where title does not contain
        defaultNotificationFiltering("title.doesNotContain=" + UPDATED_TITLE, "title.doesNotContain=" + DEFAULT_TITLE);
    }

    @Test
    @Transactional
    void getAllNotificationsByTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList where type equals to
        defaultNotificationFiltering("type.equals=" + DEFAULT_TYPE, "type.equals=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    void getAllNotificationsByTypeIsInShouldWork() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList where type in
        defaultNotificationFiltering("type.in=" + DEFAULT_TYPE + "," + UPDATED_TYPE, "type.in=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    void getAllNotificationsByTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList where type is not null
        defaultNotificationFiltering("type.specified=true", "type.specified=false");
    }

    @Test
    @Transactional
    void getAllNotificationsBySourceTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList where sourceType equals to
        defaultNotificationFiltering("sourceType.equals=" + DEFAULT_SOURCE_TYPE, "sourceType.equals=" + UPDATED_SOURCE_TYPE);
    }

    @Test
    @Transactional
    void getAllNotificationsBySourceTypeIsInShouldWork() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList where sourceType in
        defaultNotificationFiltering(
            "sourceType.in=" + DEFAULT_SOURCE_TYPE + "," + UPDATED_SOURCE_TYPE,
            "sourceType.in=" + UPDATED_SOURCE_TYPE
        );
    }

    @Test
    @Transactional
    void getAllNotificationsBySourceTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList where sourceType is not null
        defaultNotificationFiltering("sourceType.specified=true", "sourceType.specified=false");
    }

    @Test
    @Transactional
    void getAllNotificationsBySourceIdIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList where sourceId equals to
        defaultNotificationFiltering("sourceId.equals=" + DEFAULT_SOURCE_ID, "sourceId.equals=" + UPDATED_SOURCE_ID);
    }

    @Test
    @Transactional
    void getAllNotificationsBySourceIdIsInShouldWork() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList where sourceId in
        defaultNotificationFiltering("sourceId.in=" + DEFAULT_SOURCE_ID + "," + UPDATED_SOURCE_ID, "sourceId.in=" + UPDATED_SOURCE_ID);
    }

    @Test
    @Transactional
    void getAllNotificationsBySourceIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList where sourceId is not null
        defaultNotificationFiltering("sourceId.specified=true", "sourceId.specified=false");
    }

    @Test
    @Transactional
    void getAllNotificationsBySourceIdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList where sourceId is greater than or equal to
        defaultNotificationFiltering(
            "sourceId.greaterThanOrEqual=" + DEFAULT_SOURCE_ID,
            "sourceId.greaterThanOrEqual=" + UPDATED_SOURCE_ID
        );
    }

    @Test
    @Transactional
    void getAllNotificationsBySourceIdIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList where sourceId is less than or equal to
        defaultNotificationFiltering("sourceId.lessThanOrEqual=" + DEFAULT_SOURCE_ID, "sourceId.lessThanOrEqual=" + SMALLER_SOURCE_ID);
    }

    @Test
    @Transactional
    void getAllNotificationsBySourceIdIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList where sourceId is less than
        defaultNotificationFiltering("sourceId.lessThan=" + UPDATED_SOURCE_ID, "sourceId.lessThan=" + DEFAULT_SOURCE_ID);
    }

    @Test
    @Transactional
    void getAllNotificationsBySourceIdIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList where sourceId is greater than
        defaultNotificationFiltering("sourceId.greaterThan=" + SMALLER_SOURCE_ID, "sourceId.greaterThan=" + DEFAULT_SOURCE_ID);
    }

    @Test
    @Transactional
    void getAllNotificationsByIsReadIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList where isRead equals to
        defaultNotificationFiltering("isRead.equals=" + DEFAULT_IS_READ, "isRead.equals=" + UPDATED_IS_READ);
    }

    @Test
    @Transactional
    void getAllNotificationsByIsReadIsInShouldWork() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList where isRead in
        defaultNotificationFiltering("isRead.in=" + DEFAULT_IS_READ + "," + UPDATED_IS_READ, "isRead.in=" + UPDATED_IS_READ);
    }

    @Test
    @Transactional
    void getAllNotificationsByIsReadIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList where isRead is not null
        defaultNotificationFiltering("isRead.specified=true", "isRead.specified=false");
    }

    @Test
    @Transactional
    void getAllNotificationsByCreatedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList where createdAt equals to
        defaultNotificationFiltering("createdAt.equals=" + DEFAULT_CREATED_AT, "createdAt.equals=" + UPDATED_CREATED_AT);
    }

    @Test
    @Transactional
    void getAllNotificationsByCreatedAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList where createdAt in
        defaultNotificationFiltering("createdAt.in=" + DEFAULT_CREATED_AT + "," + UPDATED_CREATED_AT, "createdAt.in=" + UPDATED_CREATED_AT);
    }

    @Test
    @Transactional
    void getAllNotificationsByCreatedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        // Get all the notificationList where createdAt is not null
        defaultNotificationFiltering("createdAt.specified=true", "createdAt.specified=false");
    }

    private void defaultNotificationFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultNotificationShouldBeFound(shouldBeFound);
        defaultNotificationShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultNotificationShouldBeFound(String filter) throws Exception {
        restNotificationMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(notification.getId().intValue())))
            .andExpect(jsonPath("$.[*].organizationId").value(hasItem(DEFAULT_ORGANIZATION_ID.intValue())))
            .andExpect(jsonPath("$.[*].recipientLogin").value(hasItem(DEFAULT_RECIPIENT_LOGIN)))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].message").value(hasItem(DEFAULT_MESSAGE)))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].sourceType").value(hasItem(DEFAULT_SOURCE_TYPE.toString())))
            .andExpect(jsonPath("$.[*].sourceId").value(hasItem(DEFAULT_SOURCE_ID.intValue())))
            .andExpect(jsonPath("$.[*].isRead").value(hasItem(DEFAULT_IS_READ)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())));

        // Check, that the count call also returns 1
        restNotificationMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultNotificationShouldNotBeFound(String filter) throws Exception {
        restNotificationMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restNotificationMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingNotification() throws Exception {
        // Get the notification
        restNotificationMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingNotification() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the notification
        Notification updatedNotification = notificationRepository.findById(notification.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedNotification are not directly saved in db
        em.detach(updatedNotification);
        updatedNotification
            .organizationId(UPDATED_ORGANIZATION_ID)
            .recipientLogin(UPDATED_RECIPIENT_LOGIN)
            .title(UPDATED_TITLE)
            .message(UPDATED_MESSAGE)
            .type(UPDATED_TYPE)
            .sourceType(UPDATED_SOURCE_TYPE)
            .sourceId(UPDATED_SOURCE_ID)
            .isRead(UPDATED_IS_READ)
            .createdAt(UPDATED_CREATED_AT);
        NotificationDTO notificationDTO = notificationMapper.toDto(updatedNotification);

        restNotificationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, notificationDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(notificationDTO))
            )
            .andExpect(status().isOk());

        // Validate the Notification in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedNotificationToMatchAllProperties(updatedNotification);
    }

    @Test
    @Transactional
    void putNonExistingNotification() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        notification.setId(longCount.incrementAndGet());

        // Create the Notification
        NotificationDTO notificationDTO = notificationMapper.toDto(notification);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restNotificationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, notificationDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(notificationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Notification in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchNotification() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        notification.setId(longCount.incrementAndGet());

        // Create the Notification
        NotificationDTO notificationDTO = notificationMapper.toDto(notification);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restNotificationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(notificationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Notification in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamNotification() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        notification.setId(longCount.incrementAndGet());

        // Create the Notification
        NotificationDTO notificationDTO = notificationMapper.toDto(notification);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restNotificationMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(notificationDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Notification in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateNotificationWithPatch() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the notification using partial update
        Notification partialUpdatedNotification = new Notification();
        partialUpdatedNotification.setId(notification.getId());

        partialUpdatedNotification
            .organizationId(UPDATED_ORGANIZATION_ID)
            .title(UPDATED_TITLE)
            .type(UPDATED_TYPE)
            .sourceType(UPDATED_SOURCE_TYPE)
            .sourceId(UPDATED_SOURCE_ID)
            .isRead(UPDATED_IS_READ)
            .createdAt(UPDATED_CREATED_AT);

        restNotificationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedNotification.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedNotification))
            )
            .andExpect(status().isOk());

        // Validate the Notification in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertNotificationUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedNotification, notification),
            getPersistedNotification(notification)
        );
    }

    @Test
    @Transactional
    void fullUpdateNotificationWithPatch() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the notification using partial update
        Notification partialUpdatedNotification = new Notification();
        partialUpdatedNotification.setId(notification.getId());

        partialUpdatedNotification
            .organizationId(UPDATED_ORGANIZATION_ID)
            .recipientLogin(UPDATED_RECIPIENT_LOGIN)
            .title(UPDATED_TITLE)
            .message(UPDATED_MESSAGE)
            .type(UPDATED_TYPE)
            .sourceType(UPDATED_SOURCE_TYPE)
            .sourceId(UPDATED_SOURCE_ID)
            .isRead(UPDATED_IS_READ)
            .createdAt(UPDATED_CREATED_AT);

        restNotificationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedNotification.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedNotification))
            )
            .andExpect(status().isOk());

        // Validate the Notification in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertNotificationUpdatableFieldsEquals(partialUpdatedNotification, getPersistedNotification(partialUpdatedNotification));
    }

    @Test
    @Transactional
    void patchNonExistingNotification() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        notification.setId(longCount.incrementAndGet());

        // Create the Notification
        NotificationDTO notificationDTO = notificationMapper.toDto(notification);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restNotificationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, notificationDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(notificationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Notification in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchNotification() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        notification.setId(longCount.incrementAndGet());

        // Create the Notification
        NotificationDTO notificationDTO = notificationMapper.toDto(notification);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restNotificationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(notificationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Notification in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamNotification() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        notification.setId(longCount.incrementAndGet());

        // Create the Notification
        NotificationDTO notificationDTO = notificationMapper.toDto(notification);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restNotificationMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(notificationDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Notification in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteNotification() throws Exception {
        // Initialize the database
        insertedNotification = notificationRepository.saveAndFlush(notification);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the notification
        restNotificationMockMvc
            .perform(delete(ENTITY_API_URL_ID, notification.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    @Test
    @Transactional
    @WithMockUser(username = CURRENT_USER_LOGIN)
    void generatedCrudEndpointsRequireAdmin() throws Exception {
        insertedNotification = notificationRepository.saveAndFlush(notification);
        NotificationDTO notificationDTO = notificationMapper.toDto(notification);

        restNotificationMockMvc.perform(get(ENTITY_API_URL)).andExpect(status().isForbidden());
        restNotificationMockMvc.perform(get(ENTITY_API_URL + "/count")).andExpect(status().isForbidden());
        restNotificationMockMvc.perform(get(ENTITY_API_URL_ID, notification.getId())).andExpect(status().isForbidden());
        restNotificationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(notificationDTO)))
            .andExpect(status().isForbidden());
        restNotificationMockMvc
            .perform(put(ENTITY_API_URL_ID, notification.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(notificationDTO)))
            .andExpect(status().isForbidden());
        restNotificationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, notification.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(notificationDTO))
            )
            .andExpect(status().isForbidden());
        restNotificationMockMvc.perform(delete(ENTITY_API_URL_ID, notification.getId())).andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    void getMyNotifications() throws Exception {
        notificationRepository.saveAndFlush(
            createNotificationFor(CURRENT_USER_LOGIN, "older notification", false, DEFAULT_CREATED_AT.plusSeconds(1))
        );
        notificationRepository.saveAndFlush(
            createNotificationFor(CURRENT_USER_LOGIN, "newer notification", true, DEFAULT_CREATED_AT.plusSeconds(2))
        );
        notificationRepository.saveAndFlush(createNotificationFor(OTHER_USER_LOGIN, "other notification", false, DEFAULT_CREATED_AT.plusSeconds(3)));

        restNotificationMockMvc
            .perform(get(ENTITY_API_URL + "/my"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$.[0].recipientLogin").value(CURRENT_USER_LOGIN))
            .andExpect(jsonPath("$.[0].title").value("newer notification"))
            .andExpect(jsonPath("$.[1].recipientLogin").value(CURRENT_USER_LOGIN))
            .andExpect(jsonPath("$.[1].title").value("older notification"));
    }

    @Test
    @Transactional
    void getMyNotificationsFiltersByOrganizationHeader() throws Exception {
        notificationRepository.saveAndFlush(
            createNotificationFor(CURRENT_USER_LOGIN, "organization one", false, DEFAULT_CREATED_AT.plusSeconds(1))
        );
        notificationRepository.saveAndFlush(
            createNotificationFor(UPDATED_ORGANIZATION_ID, CURRENT_USER_LOGIN, "organization two", false, DEFAULT_CREATED_AT.plusSeconds(2))
        );

        restNotificationMockMvc
            .perform(get(ENTITY_API_URL + "/my").header(ORGANIZATION_ID_HEADER, UPDATED_ORGANIZATION_ID))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$.[0].organizationId").value(UPDATED_ORGANIZATION_ID.intValue()))
            .andExpect(jsonPath("$.[0].title").value("organization two"));
    }

    @Test
    @Transactional
    void getMyLatestNotifications() throws Exception {
        notificationRepository.saveAndFlush(
            createNotificationFor(CURRENT_USER_LOGIN, "oldest notification", false, DEFAULT_CREATED_AT.plusSeconds(1))
        );
        notificationRepository.saveAndFlush(
            createNotificationFor(CURRENT_USER_LOGIN, "latest notification", false, DEFAULT_CREATED_AT.plusSeconds(2))
        );

        restNotificationMockMvc
            .perform(get(ENTITY_API_URL + "/my/latest").param("limit", "1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$.[0].title").value("latest notification"));
    }

    @Test
    @Transactional
    void countMyUnreadNotifications() throws Exception {
        notificationRepository.saveAndFlush(createNotificationFor(CURRENT_USER_LOGIN, "unread one", false, DEFAULT_CREATED_AT.plusSeconds(1)));
        notificationRepository.saveAndFlush(
            createNotificationFor(UPDATED_ORGANIZATION_ID, CURRENT_USER_LOGIN, "unread org two", false, DEFAULT_CREATED_AT.plusSeconds(1))
        );
        notificationRepository.saveAndFlush(createNotificationFor(CURRENT_USER_LOGIN, "read one", true, DEFAULT_CREATED_AT.plusSeconds(2)));
        notificationRepository.saveAndFlush(createNotificationFor(OTHER_USER_LOGIN, "unread other", false, DEFAULT_CREATED_AT.plusSeconds(3)));

        restNotificationMockMvc.perform(get(ENTITY_API_URL + "/my/unread-count")).andExpect(status().isOk()).andExpect(content().string("2"));
        restNotificationMockMvc
            .perform(get(ENTITY_API_URL + "/my/unread-count").header(ORGANIZATION_ID_HEADER, DEFAULT_ORGANIZATION_ID))
            .andExpect(status().isOk())
            .andExpect(content().string("1"));
    }

    @Test
    @Transactional
    void markNotificationAsRead() throws Exception {
        Notification unreadNotification = notificationRepository.saveAndFlush(
            createNotificationFor(CURRENT_USER_LOGIN, "unread notification", false, DEFAULT_CREATED_AT.plusSeconds(1))
        );

        restNotificationMockMvc
            .perform(put(ENTITY_API_URL + "/{id}/read", unreadNotification.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.isRead").value(true));

        assertThat(notificationRepository.findById(unreadNotification.getId()).orElseThrow().getIsRead()).isTrue();
    }

    @Test
    @Transactional
    void markNotificationAsReadRejectsOtherUserNotification() throws Exception {
        Notification otherUserNotification = notificationRepository.saveAndFlush(
            createNotificationFor(OTHER_USER_LOGIN, "other notification", false, DEFAULT_CREATED_AT.plusSeconds(1))
        );

        restNotificationMockMvc.perform(put(ENTITY_API_URL + "/{id}/read", otherUserNotification.getId())).andExpect(status().isForbidden());

        assertThat(notificationRepository.findById(otherUserNotification.getId()).orElseThrow().getIsRead()).isFalse();
    }

    @Test
    @Transactional
    void markNotificationAsReadReturnsNotFoundWhenMissing() throws Exception {
        restNotificationMockMvc.perform(put(ENTITY_API_URL + "/{id}/read", longCount.incrementAndGet())).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void markNotificationAsReadRejectsDifferentOrganizationHeader() throws Exception {
        Notification unreadNotification = notificationRepository.saveAndFlush(
            createNotificationFor(CURRENT_USER_LOGIN, "unread notification", false, DEFAULT_CREATED_AT.plusSeconds(1))
        );

        restNotificationMockMvc
            .perform(put(ENTITY_API_URL + "/{id}/read", unreadNotification.getId()).header(ORGANIZATION_ID_HEADER, UPDATED_ORGANIZATION_ID))
            .andExpect(status().isForbidden());

        assertThat(notificationRepository.findById(unreadNotification.getId()).orElseThrow().getIsRead()).isFalse();
    }

    @Test
    @Transactional
    void markAllMyNotificationsAsRead() throws Exception {
        Notification firstNotification = notificationRepository.saveAndFlush(
            createNotificationFor(CURRENT_USER_LOGIN, "first unread", false, DEFAULT_CREATED_AT.plusSeconds(1))
        );
        Notification secondNotification = notificationRepository.saveAndFlush(
            createNotificationFor(CURRENT_USER_LOGIN, "second unread", false, DEFAULT_CREATED_AT.plusSeconds(2))
        );
        Notification otherUserNotification = notificationRepository.saveAndFlush(
            createNotificationFor(OTHER_USER_LOGIN, "other unread", false, DEFAULT_CREATED_AT.plusSeconds(3))
        );

        restNotificationMockMvc.perform(put(ENTITY_API_URL + "/my/read-all")).andExpect(status().isNoContent());

        assertThat(notificationRepository.findById(firstNotification.getId()).orElseThrow().getIsRead()).isTrue();
        assertThat(notificationRepository.findById(secondNotification.getId()).orElseThrow().getIsRead()).isTrue();
        assertThat(notificationRepository.findById(otherUserNotification.getId()).orElseThrow().getIsRead()).isFalse();
    }

    @Test
    @Transactional
    void markAllMyNotificationsAsReadFiltersByOrganizationHeader() throws Exception {
        Notification firstNotification = notificationRepository.saveAndFlush(
            createNotificationFor(CURRENT_USER_LOGIN, "first unread", false, DEFAULT_CREATED_AT.plusSeconds(1))
        );
        Notification secondNotification = notificationRepository.saveAndFlush(
            createNotificationFor(UPDATED_ORGANIZATION_ID, CURRENT_USER_LOGIN, "second unread", false, DEFAULT_CREATED_AT.plusSeconds(2))
        );

        restNotificationMockMvc
            .perform(put(ENTITY_API_URL + "/my/read-all").header(ORGANIZATION_ID_HEADER, UPDATED_ORGANIZATION_ID))
            .andExpect(status().isNoContent());

        assertThat(notificationRepository.findById(firstNotification.getId()).orElseThrow().getIsRead()).isFalse();
        assertThat(notificationRepository.findById(secondNotification.getId()).orElseThrow().getIsRead()).isTrue();
    }

    @Test
    @Transactional
    void createInternalNotification() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        CreateNotificationRequest request = createInternalNotificationRequest();

        var returnedNotificationDTO = om.readValue(
            restNotificationMockMvc
                .perform(
                    post(INTERNAL_ENTITY_API_URL)
                        .header(INTERNAL_TOKEN_HEADER, INTERNAL_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(request))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            NotificationDTO.class
        );

        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        Notification persistedNotification = notificationRepository.findById(returnedNotificationDTO.getId()).orElseThrow();
        assertThat(persistedNotification.getRecipientLogin()).isEqualTo(CURRENT_USER_LOGIN);
        assertThat(persistedNotification.getIsRead()).isFalse();
        assertThat(persistedNotification.getCreatedAt()).isNotNull();

        insertedNotification = persistedNotification;
    }

    @Test
    @Transactional
    void createInternalNotificationRejectsMissingToken() throws Exception {
        CreateNotificationRequest request = createInternalNotificationRequest();

        restNotificationMockMvc
            .perform(post(INTERNAL_ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(request)))
            .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    void createInternalNotificationRejectsWrongToken() throws Exception {
        CreateNotificationRequest request = createInternalNotificationRequest();

        restNotificationMockMvc
            .perform(
                post(INTERNAL_ENTITY_API_URL)
                    .header(INTERNAL_TOKEN_HEADER, "wrong-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(request))
            )
            .andExpect(status().isForbidden());
    }

    protected long getRepositoryCount() {
        return notificationRepository.count();
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

    protected Notification getPersistedNotification(Notification notification) {
        return notificationRepository.findById(notification.getId()).orElseThrow();
    }

    protected void assertPersistedNotificationToMatchAllProperties(Notification expectedNotification) {
        assertNotificationAllPropertiesEquals(expectedNotification, getPersistedNotification(expectedNotification));
    }

    protected void assertPersistedNotificationToMatchUpdatableProperties(Notification expectedNotification) {
        assertNotificationAllUpdatablePropertiesEquals(expectedNotification, getPersistedNotification(expectedNotification));
    }

    private static Notification createNotificationFor(String recipientLogin, String title, boolean isRead, Instant createdAt) {
        return createNotificationFor(DEFAULT_ORGANIZATION_ID, recipientLogin, title, isRead, createdAt);
    }

    private static Notification createNotificationFor(
        Long organizationId,
        String recipientLogin,
        String title,
        boolean isRead,
        Instant createdAt
    ) {
        return new Notification()
            .organizationId(organizationId)
            .recipientLogin(recipientLogin)
            .title(title)
            .message(DEFAULT_MESSAGE)
            .type(DEFAULT_TYPE)
            .sourceType(DEFAULT_SOURCE_TYPE)
            .sourceId(DEFAULT_SOURCE_ID)
            .isRead(isRead)
            .createdAt(createdAt);
    }

    private static CreateNotificationRequest createInternalNotificationRequest() {
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setOrganizationId(DEFAULT_ORGANIZATION_ID);
        request.setRecipientLogin(CURRENT_USER_LOGIN);
        request.setTitle(DEFAULT_TITLE);
        request.setMessage(DEFAULT_MESSAGE);
        request.setType(DEFAULT_TYPE);
        request.setSourceType(DEFAULT_SOURCE_TYPE);
        request.setSourceId(DEFAULT_SOURCE_ID);
        return request;
    }
}
