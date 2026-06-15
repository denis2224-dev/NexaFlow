package com.nexaflow.project.service;

import com.nexaflow.project.client.NotificationClient;
import com.nexaflow.project.client.dto.CreateNotificationRequest;
import com.nexaflow.project.client.dto.NotificationType;
import com.nexaflow.project.client.dto.SourceType;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationDispatchService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationDispatchService.class);

    private final NotificationClient notificationClient;

    public NotificationDispatchService(NotificationClient notificationClient) {
        this.notificationClient = notificationClient;
    }

    public void taskAssigned(Long organizationId, String recipientLogin, Long taskId, String taskTitle) {
        if (recipientLogin == null || recipientLogin.isBlank()) {
            return;
        }
        send(
            new CreateNotificationRequest(
                organizationId,
                recipientLogin,
                "Task assigned",
                "You were assigned to task: " + taskTitle,
                NotificationType.TASK_ASSIGNED,
                SourceType.TASK,
                taskId
            )
        );
    }

    public void commentAdded(Long organizationId, String recipientLogin, Long commentId, String taskTitle) {
        if (recipientLogin == null || recipientLogin.isBlank()) {
            return;
        }

        send(
            new CreateNotificationRequest(
                organizationId,
                recipientLogin,
                "New comment",
                "A new comment was added to task: " + taskTitle,
                NotificationType.COMMENT_ADDED,
                SourceType.COMMENT,
                commentId
            )
        );
    }

    public void projectUpdated(Long organizationId, String recipientLogin, Long projectId, String projectName) {
        if (recipientLogin == null || recipientLogin.isBlank()) {
            return;
        }

        send(
            new CreateNotificationRequest(
                organizationId,
                recipientLogin,
                "Project updated",
                "Project was updated: " + projectName,
                NotificationType.PROJECT_UPDATED,
                SourceType.PROJECT,
                projectId
            )
        );
    }

    private void send(CreateNotificationRequest request) {
        try {
            notificationClient.createNotification(request);
        } catch (FeignException ex) {
            LOG.warn("Failed to dispatch notification: {}", request, ex);
        }
    }
}
