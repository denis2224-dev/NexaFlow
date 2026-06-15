package com.nexaflow.notification.domain;

import com.nexaflow.notification.domain.enumeration.NotificationType;
import com.nexaflow.notification.domain.enumeration.SourceType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * A Notification.
 */
@Entity
@Table(name = "notification")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Notification implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @NotNull
    @Size(max = 100)
    @Column(name = "recipient_login", length = 100, nullable = false)
    private String recipientLogin;

    @NotNull
    @Size(max = 255)
    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Lob
    @Column(name = "message", nullable = false)
    private String message;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type")
    private SourceType sourceType;

    @Column(name = "source_id")
    private Long sourceId;

    @NotNull
    @Column(name = "is_read", nullable = false)
    private Boolean isRead;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Notification id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrganizationId() {
        return this.organizationId;
    }

    public Notification organizationId(Long organizationId) {
        this.setOrganizationId(organizationId);
        return this;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public String getRecipientLogin() {
        return this.recipientLogin;
    }

    public Notification recipientLogin(String recipientLogin) {
        this.setRecipientLogin(recipientLogin);
        return this;
    }

    public void setRecipientLogin(String recipientLogin) {
        this.recipientLogin = recipientLogin;
    }

    public String getTitle() {
        return this.title;
    }

    public Notification title(String title) {
        this.setTitle(title);
        return this;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return this.message;
    }

    public Notification message(String message) {
        this.setMessage(message);
        return this;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationType getType() {
        return this.type;
    }

    public Notification type(NotificationType type) {
        this.setType(type);
        return this;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public SourceType getSourceType() {
        return this.sourceType;
    }

    public Notification sourceType(SourceType sourceType) {
        this.setSourceType(sourceType);
        return this;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public Long getSourceId() {
        return this.sourceId;
    }

    public Notification sourceId(Long sourceId) {
        this.setSourceId(sourceId);
        return this;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public Boolean getIsRead() {
        return this.isRead;
    }

    public Notification isRead(Boolean isRead) {
        this.setIsRead(isRead);
        return this;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public Notification createdAt(Instant createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Notification)) {
            return false;
        }
        return getId() != null && getId().equals(((Notification) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Notification{" +
            "id=" + getId() +
            ", organizationId=" + getOrganizationId() +
            ", recipientLogin='" + getRecipientLogin() + "'" +
            ", title='" + getTitle() + "'" +
            ", message='" + getMessage() + "'" +
            ", type='" + getType() + "'" +
            ", sourceType='" + getSourceType() + "'" +
            ", sourceId=" + getSourceId() +
            ", isRead='" + getIsRead() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            "}";
    }
}
