package com.nexaflow.project.service;

import com.nexaflow.project.domain.ActivityLog;
import com.nexaflow.project.repository.ActivityLogRepository;
import com.nexaflow.project.security.OrganizationAccessService;
import com.nexaflow.project.service.dto.ActivityLogDTO;
import com.nexaflow.project.service.mapper.ActivityLogMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.nexaflow.project.domain.ActivityLog}.
 */
@Service
@Transactional
public class ActivityLogService {

    private static final Logger LOG = LoggerFactory.getLogger(ActivityLogService.class);

    private final ActivityLogRepository activityLogRepository;

    private final ActivityLogMapper activityLogMapper;

    private final OrganizationAccessService organizationAccessService;

    public ActivityLogService(
        ActivityLogRepository activityLogRepository,
        ActivityLogMapper activityLogMapper,
        OrganizationAccessService organizationAccessService
    ) {
        this.activityLogRepository = activityLogRepository;
        this.activityLogMapper = activityLogMapper;
        this.organizationAccessService = organizationAccessService;
    }

    /**
     * Save a activityLog.
     *
     * @param activityLogDTO the entity to save.
     * @return the persisted entity.
     */
    public ActivityLogDTO save(ActivityLogDTO activityLogDTO) {
        LOG.debug("Request to save ActivityLog : {}", activityLogDTO);
        organizationAccessService.assertMember(activityLogDTO.getOrganizationId());

        ActivityLog activityLog = activityLogMapper.toEntity(activityLogDTO);
        activityLog = activityLogRepository.save(activityLog);
        return activityLogMapper.toDto(activityLog);
    }

    /**
     * Update a activityLog.
     *
     * @param activityLogDTO the entity to save.
     * @return the persisted entity.
     */
    public ActivityLogDTO update(ActivityLogDTO activityLogDTO) {
        LOG.debug("Request to update ActivityLog : {}", activityLogDTO);

        ActivityLog existingActivityLog = activityLogRepository
            .findById(activityLogDTO.getId())
            .orElseThrow(() -> new AccessDeniedException("ActivityLog not found"));

        Long organizationId = existingActivityLog.getOrganizationId();
        organizationAccessService.assertMember(organizationId);
        activityLogDTO.setOrganizationId(organizationId);

        ActivityLog activityLog = activityLogMapper.toEntity(activityLogDTO);
        activityLog = activityLogRepository.save(activityLog);
        return activityLogMapper.toDto(activityLog);
    }

    /**
     * Partially update a activityLog.
     *
     * @param activityLogDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<ActivityLogDTO> partialUpdate(ActivityLogDTO activityLogDTO) {
        LOG.debug("Request to partially update ActivityLog : {}", activityLogDTO);

        return activityLogRepository
            .findById(activityLogDTO.getId())
            .map(existingActivityLog -> {
                Long organizationId = existingActivityLog.getOrganizationId();
                organizationAccessService.assertMember(organizationId);

                activityLogDTO.setOrganizationId(organizationId);
                activityLogMapper.partialUpdate(existingActivityLog, activityLogDTO);
                existingActivityLog.setOrganizationId(organizationId);

                return existingActivityLog;
            })
            .map(activityLogRepository::save)
            .map(activityLogMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ActivityLogDTO> findAllByOrganization(Long organizationId, Pageable pageable) {
        LOG.debug("Request to get ActivityLogs for organization : {}", organizationId);

        organizationAccessService.assertMember(organizationId);

        return activityLogRepository.findByOrganizationId(organizationId, pageable).map(activityLogMapper::toDto);
    }

    /**
     * Get all the activityLogs.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<ActivityLogDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all ActivityLogs");
        return activityLogRepository.findAll(pageable).map(activityLogMapper::toDto);
    }

    /**
     * Get one activityLog by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ActivityLogDTO> findOne(Long id) {
        LOG.debug("Request to get ActivityLog : {}", id);
        return activityLogRepository
            .findById(id)
            .map(activityLog -> {
                organizationAccessService.assertMember(activityLog.getOrganizationId());
                return activityLogMapper.toDto(activityLog);
            });
    }

    /**
     * Delete the activityLog by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete ActivityLog : {}", id);

        ActivityLog existingActivityLog = activityLogRepository
            .findById(id)
            .orElseThrow(() -> new AccessDeniedException("ActivityLog not found"));

        organizationAccessService.assertMember(existingActivityLog.getOrganizationId());

        activityLogRepository.deleteById(id);
    }
}
