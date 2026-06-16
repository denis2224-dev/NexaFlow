package com.nexaflow.notification.service;

import com.nexaflow.notification.domain.Notification;
import com.nexaflow.notification.repository.NotificationRepository;
import com.nexaflow.notification.security.SecurityUtils;
import com.nexaflow.notification.service.dto.CreateNotificationRequest;
import com.nexaflow.notification.service.dto.NotificationDTO;
import com.nexaflow.notification.service.mapper.NotificationMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service Implementation for managing {@link com.nexaflow.notification.domain.Notification}.
 */
@Service
@Transactional
public class NotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    private final NotificationMapper notificationMapper;

    public NotificationService(NotificationRepository notificationRepository, NotificationMapper notificationMapper) {
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
    }

    /**
     * Save a notification.
     *
     * @param notificationDTO the entity to save.
     * @return the persisted entity.
     */
    public NotificationDTO save(NotificationDTO notificationDTO) {
        LOG.debug("Request to save Notification : {}", notificationDTO);
        Notification notification = notificationMapper.toEntity(notificationDTO);
        notification = notificationRepository.save(notification);
        return notificationMapper.toDto(notification);
    }

    /**
     * Create a notification from an internal service request.
     *
     * @param request the notification request to create.
     * @return the persisted entity.
     */
    public NotificationDTO createInternalNotification(CreateNotificationRequest request) {
        LOG.debug("Request to create Notification from internal request : {}", request);
        Notification notification = new Notification()
            .organizationId(request.getOrganizationId())
            .recipientLogin(request.getRecipientLogin())
            .title(request.getTitle())
            .message(request.getMessage())
            .type(request.getType())
            .sourceType(request.getSourceType())
            .sourceId(request.getSourceId())
            .isRead(false)
            .createdAt(Instant.now());

        notification = notificationRepository.save(notification);
        return notificationMapper.toDto(notification);
    }

    /**
     * Update a notification.
     *
     * @param notificationDTO the entity to save.
     * @return the persisted entity.
     */
    public NotificationDTO update(NotificationDTO notificationDTO) {
        LOG.debug("Request to update Notification : {}", notificationDTO);
        Notification notification = notificationMapper.toEntity(notificationDTO);
        notification = notificationRepository.save(notification);
        return notificationMapper.toDto(notification);
    }

    /**
     * Partially update a notification.
     *
     * @param notificationDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<NotificationDTO> partialUpdate(NotificationDTO notificationDTO) {
        LOG.debug("Request to partially update Notification : {}", notificationDTO);

        return notificationRepository
            .findById(notificationDTO.getId())
            .map(existingNotification -> {
                notificationMapper.partialUpdate(existingNotification, notificationDTO);

                return existingNotification;
            })
            .map(notificationRepository::save)
            .map(notificationMapper::toDto);
    }

    /**
     * Get one notification by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<NotificationDTO> findOne(Long id) {
        LOG.debug("Request to get Notification : {}", id);
        return notificationRepository.findById(id).map(notificationMapper::toDto);
    }

    /**
     * Get notifications for the current user.
     *
     * @param pageable the pagination information.
     * @return notifications addressed to the current user.
     */
    @Transactional(readOnly = true)
    public List<NotificationDTO> getMyNotifications(Long organizationId, Pageable pageable) {
        String currentUserLogin = getCurrentUserLogin();
        LOG.debug("Request to get Notifications for current user : {}", currentUserLogin);
        List<Notification> notifications = organizationId == null
            ? notificationRepository.findByRecipientLoginOrderByCreatedAtDesc(currentUserLogin, pageable)
            : notificationRepository.findByOrganizationIdAndRecipientLoginOrderByCreatedAtDesc(organizationId, currentUserLogin, pageable);
        return notifications.stream().map(notificationMapper::toDto).toList();
    }

    /**
     * Get the latest notifications for the current user.
     *
     * @param limit the maximum number of notifications to return.
     * @return latest notifications addressed to the current user.
     */
    @Transactional(readOnly = true)
    public List<NotificationDTO> getMyLatestNotifications(Long organizationId, int limit) {
        int pageSize = Math.max(1, Math.min(limit, 50));
        return getMyNotifications(organizationId, PageRequest.of(0, pageSize));
    }

    /**
     * Count unread notifications for the current user.
     *
     * @return unread notification count.
     */
    @Transactional(readOnly = true)
    public long getMyUnreadCount(Long organizationId) {
        String currentUserLogin = getCurrentUserLogin();
        LOG.debug("Request to count unread Notifications for current user : {}", currentUserLogin);
        return organizationId == null
            ? notificationRepository.countByRecipientLoginAndIsReadFalse(currentUserLogin)
            : notificationRepository.countByOrganizationIdAndRecipientLoginAndIsReadFalse(organizationId, currentUserLogin);
    }

    /**
     * Mark one current-user notification as read.
     *
     * @param id the id of the notification to mark as read.
     * @return the updated notification.
     */
    public NotificationDTO markAsRead(Long id, Long organizationId) {
        String currentUserLogin = getCurrentUserLogin();
        LOG.debug("Request to mark Notification as read : {}, {}", id, currentUserLogin);
        Notification notification = notificationRepository
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));

        if (!currentUserLogin.equals(notification.getRecipientLogin())) {
            throw new AccessDeniedException("Notification does not belong to current user");
        }

        if (organizationId != null && !organizationId.equals(notification.getOrganizationId())) {
            throw new AccessDeniedException("Notification does not belong to selected organization");
        }

        if (!Boolean.TRUE.equals(notification.getIsRead())) {
            notification.setIsRead(true);
            notification = notificationRepository.save(notification);
        }

        return notificationMapper.toDto(notification);
    }

    /**
     * Mark all current-user notifications as read.
     */
    public void markAllAsRead(Long organizationId) {
        String currentUserLogin = getCurrentUserLogin();
        LOG.debug("Request to mark all Notifications as read for current user : {}", currentUserLogin);
        if (organizationId == null) {
            notificationRepository.markAllAsReadByRecipientLogin(currentUserLogin);
        } else {
            notificationRepository.markAllAsReadByOrganizationIdAndRecipientLogin(organizationId, currentUserLogin);
        }
    }

    /**
     * Delete the notification by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Notification : {}", id);
        notificationRepository.deleteById(id);
    }

    private String getCurrentUserLogin() {
        return SecurityUtils.getCurrentUserLogin().orElseThrow(() ->
            new AccessDeniedException("Current user is not authenticated")
        );
    }
}
