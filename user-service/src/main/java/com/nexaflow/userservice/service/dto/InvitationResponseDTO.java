package com.nexaflow.userservice.service.dto;

import com.nexaflow.userservice.domain.enumeration.InvitationStatus;
import com.nexaflow.userservice.domain.enumeration.MembershipRole;
import java.time.Instant;

public class InvitationResponseDTO {

    private Long invitationId;
    private Long organizationId;
    private String email;
    private String token;
    private MembershipRole role;
    private InvitationStatus status;
    private Instant expiresAt;

    public InvitationResponseDTO(
        Long invitationId,
        Long organizationId,
        String email,
        String token,
        MembershipRole role,
        InvitationStatus status,
        Instant expiresAt
    ) {
        this.invitationId = invitationId;
        this.organizationId = organizationId;
        this.email = email;
        this.token = token;
        this.role = role;
        this.status = status;
        this.expiresAt = expiresAt;
    }

    public Long getInvitationId() {
        return invitationId;
    }

    public Long getOrganizationId() {
        return organizationId;
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
}
