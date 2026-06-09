package com.nexaflow.userservice.service.dto;

import com.nexaflow.userservice.domain.enumeration.InvitationStatus;
import com.nexaflow.userservice.domain.enumeration.MembershipRole;
import java.time.Instant;

public class InvitationResponseDTO {

    private Long invitationId;
    private Long organizationId;
    private String organizationName;
    private String organizationSlug;
    private String email;
    private String token;
    private MembershipRole role;
    private InvitationStatus status;
    private Instant expiresAt;
    private String invitedByLogin;

    public InvitationResponseDTO(
        Long invitationId,
        Long organizationId,
        String email,
        String token,
        MembershipRole role,
        InvitationStatus status,
        Instant expiresAt
    ) {
        this(invitationId, organizationId, null, null, email, token, role, status, expiresAt, null);
    }

    public InvitationResponseDTO(
        Long invitationId,
        Long organizationId,
        String organizationName,
        String organizationSlug,
        String email,
        String token,
        MembershipRole role,
        InvitationStatus status,
        Instant expiresAt,
        String invitedByLogin
    ) {
        this.invitationId = invitationId;
        this.organizationId = organizationId;
        this.organizationName = organizationName;
        this.organizationSlug = organizationSlug;
        this.email = email;
        this.token = token;
        this.role = role;
        this.status = status;
        this.expiresAt = expiresAt;
        this.invitedByLogin = invitedByLogin;
    }

    public Long getInvitationId() {
        return invitationId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public String getOrganizationSlug() {
        return organizationSlug;
    }

    public String getEmail() {
        return email;
    }

    public String getToken() {
        return token;
    }

    public MembershipRole getRole() {
        return role;
    }

    public InvitationStatus getStatus() {
        return status;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public String getInvitedByLogin() {
        return invitedByLogin;
    }
}
