package com.nexaflow.project.client.dto;

public record CreateNotificationRequest(
    Long organizationId,
    String recipientLogin,
    String title,
    String message,
    NotificationType type,
    SourceType sourceType,
    Long sourceId
) {}
