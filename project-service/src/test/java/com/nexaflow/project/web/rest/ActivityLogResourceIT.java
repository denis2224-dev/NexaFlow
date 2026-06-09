package com.nexaflow.project.web.rest;

import static com.nexaflow.project.domain.ActivityLogAsserts.*;
import static com.nexaflow.project.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexaflow.project.IntegrationTest;
import com.nexaflow.project.domain.ActivityLog;
import com.nexaflow.project.domain.enumeration.ActivityAction;
import com.nexaflow.project.domain.enumeration.ActivityEntityType;
import com.nexaflow.project.repository.ActivityLogRepository;
import com.nexaflow.project.service.dto.ActivityLogDTO;
import com.nexaflow.project.service.mapper.ActivityLogMapper;
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
 * Integration tests for the {@link ActivityLogResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ActivityLogResourceIT {

    private static final Long DEFAULT_ORGANIZATION_ID = 1L;
    private static final Long UPDATED_ORGANIZATION_ID = 2L;

    private static final ActivityEntityType DEFAULT_ENTITY_TYPE = ActivityEntityType.PROJECT;
    private static final ActivityEntityType UPDATED_ENTITY_TYPE = ActivityEntityType.TASK;

    private static final Long DEFAULT_ENTITY_ID = 1L;
    private static final Long UPDATED_ENTITY_ID = 2L;

    private static final ActivityAction DEFAULT_ACTION = ActivityAction.PROJECT_CREATED;
    private static final ActivityAction UPDATED_ACTION = ActivityAction.PROJECT_UPDATED;

    private static final String DEFAULT_PERFORMED_BY = "AAAAAAAAAA";
    private static final String UPDATED_PERFORMED_BY = "BBBBBBBBBB";

    private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/activity-logs";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Autowired
    private ActivityLogMapper activityLogMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restActivityLogMockMvc;

    private ActivityLog activityLog;

    private ActivityLog insertedActivityLog;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ActivityLog createEntity() {
        return new ActivityLog()
            .organizationId(DEFAULT_ORGANIZATION_ID)
            .entityType(DEFAULT_ENTITY_TYPE)
            .entityId(DEFAULT_ENTITY_ID)
            .action(DEFAULT_ACTION)
            .performedBy(DEFAULT_PERFORMED_BY)
            .createdAt(DEFAULT_CREATED_AT);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ActivityLog createUpdatedEntity() {
        return new ActivityLog()
            .organizationId(UPDATED_ORGANIZATION_ID)
            .entityType(UPDATED_ENTITY_TYPE)
            .entityId(UPDATED_ENTITY_ID)
            .action(UPDATED_ACTION)
            .performedBy(UPDATED_PERFORMED_BY)
            .createdAt(UPDATED_CREATED_AT);
    }

    @BeforeEach
    void initTest() {
        activityLog = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedActivityLog != null) {
            activityLogRepository.delete(insertedActivityLog);
            insertedActivityLog = null;
        }
    }

    @Test
    @Transactional
    void createActivityLog() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the ActivityLog
        ActivityLogDTO activityLogDTO = activityLogMapper.toDto(activityLog);
        var returnedActivityLogDTO = om.readValue(
            restActivityLogMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(activityLogDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ActivityLogDTO.class
        );

        // Validate the ActivityLog in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedActivityLog = activityLogMapper.toEntity(returnedActivityLogDTO);
        assertActivityLogUpdatableFieldsEquals(returnedActivityLog, getPersistedActivityLog(returnedActivityLog));

        insertedActivityLog = returnedActivityLog;
    }

    @Test
    @Transactional
    void createActivityLogWithExistingId() throws Exception {
        // Create the ActivityLog with an existing ID
        activityLog.setId(1L);
        ActivityLogDTO activityLogDTO = activityLogMapper.toDto(activityLog);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restActivityLogMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(activityLogDTO)))
            .andExpect(status().isBadRequest());

        // Validate the ActivityLog in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkOrganizationIdIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        activityLog.setOrganizationId(null);

        // Create the ActivityLog, which fails.
        ActivityLogDTO activityLogDTO = activityLogMapper.toDto(activityLog);

        restActivityLogMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(activityLogDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkEntityTypeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        activityLog.setEntityType(null);

        // Create the ActivityLog, which fails.
        ActivityLogDTO activityLogDTO = activityLogMapper.toDto(activityLog);

        restActivityLogMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(activityLogDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkEntityIdIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        activityLog.setEntityId(null);

        // Create the ActivityLog, which fails.
        ActivityLogDTO activityLogDTO = activityLogMapper.toDto(activityLog);

        restActivityLogMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(activityLogDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkActionIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        activityLog.setAction(null);

        // Create the ActivityLog, which fails.
        ActivityLogDTO activityLogDTO = activityLogMapper.toDto(activityLog);

        restActivityLogMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(activityLogDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkPerformedByIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        activityLog.setPerformedBy(null);

        // Create the ActivityLog, which fails.
        ActivityLogDTO activityLogDTO = activityLogMapper.toDto(activityLog);

        restActivityLogMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(activityLogDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCreatedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        activityLog.setCreatedAt(null);

        // Create the ActivityLog, which fails.
        ActivityLogDTO activityLogDTO = activityLogMapper.toDto(activityLog);

        restActivityLogMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(activityLogDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllActivityLogs() throws Exception {
        // Initialize the database
        insertedActivityLog = activityLogRepository.saveAndFlush(activityLog);

        // Get all the activityLogList
        restActivityLogMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(activityLog.getId().intValue())))
            .andExpect(jsonPath("$.[*].organizationId").value(hasItem(DEFAULT_ORGANIZATION_ID.intValue())))
            .andExpect(jsonPath("$.[*].entityType").value(hasItem(DEFAULT_ENTITY_TYPE.toString())))
            .andExpect(jsonPath("$.[*].entityId").value(hasItem(DEFAULT_ENTITY_ID.intValue())))
            .andExpect(jsonPath("$.[*].action").value(hasItem(DEFAULT_ACTION.toString())))
            .andExpect(jsonPath("$.[*].performedBy").value(hasItem(DEFAULT_PERFORMED_BY)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())));
    }

    @Test
    @Transactional
    void getActivityLog() throws Exception {
        // Initialize the database
        insertedActivityLog = activityLogRepository.saveAndFlush(activityLog);

        // Get the activityLog
        restActivityLogMockMvc
            .perform(get(ENTITY_API_URL_ID, activityLog.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(activityLog.getId().intValue()))
            .andExpect(jsonPath("$.organizationId").value(DEFAULT_ORGANIZATION_ID.intValue()))
            .andExpect(jsonPath("$.entityType").value(DEFAULT_ENTITY_TYPE.toString()))
            .andExpect(jsonPath("$.entityId").value(DEFAULT_ENTITY_ID.intValue()))
            .andExpect(jsonPath("$.action").value(DEFAULT_ACTION.toString()))
            .andExpect(jsonPath("$.performedBy").value(DEFAULT_PERFORMED_BY))
            .andExpect(jsonPath("$.createdAt").value(DEFAULT_CREATED_AT.toString()));
    }

    @Test
    @Transactional
    void getNonExistingActivityLog() throws Exception {
        // Get the activityLog
        restActivityLogMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingActivityLog() throws Exception {
        // Initialize the database
        insertedActivityLog = activityLogRepository.saveAndFlush(activityLog);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the activityLog
        ActivityLog updatedActivityLog = activityLogRepository.findById(activityLog.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedActivityLog are not directly saved in db
        em.detach(updatedActivityLog);
        updatedActivityLog
            .organizationId(UPDATED_ORGANIZATION_ID)
            .entityType(UPDATED_ENTITY_TYPE)
            .entityId(UPDATED_ENTITY_ID)
            .action(UPDATED_ACTION)
            .performedBy(UPDATED_PERFORMED_BY)
            .createdAt(UPDATED_CREATED_AT);
        ActivityLogDTO activityLogDTO = activityLogMapper.toDto(updatedActivityLog);

        restActivityLogMockMvc
            .perform(
                put(ENTITY_API_URL_ID, activityLogDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(activityLogDTO))
            )
            .andExpect(status().isOk());

        // Validate the ActivityLog in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedActivityLogToMatchAllProperties(updatedActivityLog);
    }

    @Test
    @Transactional
    void putNonExistingActivityLog() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        activityLog.setId(longCount.incrementAndGet());

        // Create the ActivityLog
        ActivityLogDTO activityLogDTO = activityLogMapper.toDto(activityLog);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restActivityLogMockMvc
            .perform(
                put(ENTITY_API_URL_ID, activityLogDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(activityLogDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ActivityLog in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchActivityLog() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        activityLog.setId(longCount.incrementAndGet());

        // Create the ActivityLog
        ActivityLogDTO activityLogDTO = activityLogMapper.toDto(activityLog);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restActivityLogMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(activityLogDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ActivityLog in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamActivityLog() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        activityLog.setId(longCount.incrementAndGet());

        // Create the ActivityLog
        ActivityLogDTO activityLogDTO = activityLogMapper.toDto(activityLog);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restActivityLogMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(activityLogDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the ActivityLog in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateActivityLogWithPatch() throws Exception {
        // Initialize the database
        insertedActivityLog = activityLogRepository.saveAndFlush(activityLog);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the activityLog using partial update
        ActivityLog partialUpdatedActivityLog = new ActivityLog();
        partialUpdatedActivityLog.setId(activityLog.getId());

        partialUpdatedActivityLog.organizationId(UPDATED_ORGANIZATION_ID).entityType(UPDATED_ENTITY_TYPE).entityId(UPDATED_ENTITY_ID);

        restActivityLogMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedActivityLog.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedActivityLog))
            )
            .andExpect(status().isOk());

        // Validate the ActivityLog in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertActivityLogUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedActivityLog, activityLog),
            getPersistedActivityLog(activityLog)
        );
    }

    @Test
    @Transactional
    void fullUpdateActivityLogWithPatch() throws Exception {
        // Initialize the database
        insertedActivityLog = activityLogRepository.saveAndFlush(activityLog);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the activityLog using partial update
        ActivityLog partialUpdatedActivityLog = new ActivityLog();
        partialUpdatedActivityLog.setId(activityLog.getId());

        partialUpdatedActivityLog
            .organizationId(UPDATED_ORGANIZATION_ID)
            .entityType(UPDATED_ENTITY_TYPE)
            .entityId(UPDATED_ENTITY_ID)
            .action(UPDATED_ACTION)
            .performedBy(UPDATED_PERFORMED_BY)
            .createdAt(UPDATED_CREATED_AT);

        restActivityLogMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedActivityLog.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedActivityLog))
            )
            .andExpect(status().isOk());

        // Validate the ActivityLog in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertActivityLogUpdatableFieldsEquals(partialUpdatedActivityLog, getPersistedActivityLog(partialUpdatedActivityLog));
    }

    @Test
    @Transactional
    void patchNonExistingActivityLog() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        activityLog.setId(longCount.incrementAndGet());

        // Create the ActivityLog
        ActivityLogDTO activityLogDTO = activityLogMapper.toDto(activityLog);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restActivityLogMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, activityLogDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(activityLogDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ActivityLog in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchActivityLog() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        activityLog.setId(longCount.incrementAndGet());

        // Create the ActivityLog
        ActivityLogDTO activityLogDTO = activityLogMapper.toDto(activityLog);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restActivityLogMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(activityLogDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ActivityLog in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamActivityLog() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        activityLog.setId(longCount.incrementAndGet());

        // Create the ActivityLog
        ActivityLogDTO activityLogDTO = activityLogMapper.toDto(activityLog);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restActivityLogMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(activityLogDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the ActivityLog in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteActivityLog() throws Exception {
        // Initialize the database
        insertedActivityLog = activityLogRepository.saveAndFlush(activityLog);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the activityLog
        restActivityLogMockMvc
            .perform(delete(ENTITY_API_URL_ID, activityLog.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return activityLogRepository.count();
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

    protected ActivityLog getPersistedActivityLog(ActivityLog activityLog) {
        return activityLogRepository.findById(activityLog.getId()).orElseThrow();
    }

    protected void assertPersistedActivityLogToMatchAllProperties(ActivityLog expectedActivityLog) {
        assertActivityLogAllPropertiesEquals(expectedActivityLog, getPersistedActivityLog(expectedActivityLog));
    }

    protected void assertPersistedActivityLogToMatchUpdatableProperties(ActivityLog expectedActivityLog) {
        assertActivityLogAllUpdatablePropertiesEquals(expectedActivityLog, getPersistedActivityLog(expectedActivityLog));
    }
}
