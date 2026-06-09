package com.nexaflow.project.service.dto;

import com.nexaflow.project.domain.enumeration.ActivityAction;
import com.nexaflow.project.domain.enumeration.ActivityEntityType;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.nexaflow.project.domain.ActivityLog} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ActivityLogDTO implements Serializable {

    private Long id;

    @NotNull
    private Long organizationId;

    @NotNull
    private ActivityEntityType entityType;

    @NotNull
    private Long entityId;

    @NotNull
    private ActivityAction action;

    @NotNull
    private String performedBy;

    @NotNull
    private Instant createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public ActivityEntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(ActivityEntityType entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public ActivityAction getAction() {
        return action;
    }

    public void setAction(ActivityAction action) {
        this.action = action;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ActivityLogDTO)) {
            return false;
        }

        ActivityLogDTO activityLogDTO = (ActivityLogDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, activityLogDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ActivityLogDTO{" +
            "id=" + getId() +
            ", organizationId=" + getOrganizationId() +
            ", entityType='" + getEntityType() + "'" +
            ", entityId=" + getEntityId() +
            ", action='" + getAction() + "'" +
            ", performedBy='" + getPerformedBy() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            "}";
    }
}
