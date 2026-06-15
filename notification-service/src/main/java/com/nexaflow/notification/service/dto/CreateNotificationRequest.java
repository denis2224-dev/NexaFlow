package com.nexaflow.notification.service.dto;

import com.nexaflow.notification.domain.enumeration.NotificationType;
import com.nexaflow.notification.domain.enumeration.SourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

public class CreateNotificationRequest implements Serializable {

    @NotNull
    private Long organizationId;

    @NotBlank
    @Size(max = 100)
    private String recipientLogin;

    @NotBlank
    @Size(max = 255)
    private String title;

    @NotBlank
    private String message;

    @NotNull
    private NotificationType type;

    private SourceType sourceType;

    private Long sourceId;

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public String getRecipientLogin() {
        return recipientLogin;
    }

    public void setRecipientLogin(String recipientLogin) {
        this.recipientLogin = recipientLogin;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }
}
