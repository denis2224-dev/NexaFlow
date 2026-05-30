package com.nexaflow.userservice.service.dto;

import com.nexaflow.userservice.domain.enumeration.MembershipRole;

public class WorkspaceDTO {

    private Long organizationId;
    private String name;
    private String slug;
    private String description;
    private MembershipRole role;

    public WorkspaceDTO() {}

    public WorkspaceDTO(Long organizationId, String name, String slug, String description, MembershipRole role) {
        this.organizationId = organizationId;
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.role = role;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public String getDescription() {
        return description;
    }

    public MembershipRole getRole() {
        return role;
    }
}
