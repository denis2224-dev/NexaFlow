package com.nexaflow.userservice.service.dto;

import com.nexaflow.userservice.domain.enumeration.InvitationStatus;
import com.nexaflow.userservice.domain.enumeration.MembershipRole;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.nexaflow.userservice.domain.Invitation} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class InvitationDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 254)
    private String email;

    @NotNull
    @Size(max = 120)
    private String token;

    @NotNull
    private MembershipRole role;

    @NotNull
    private InvitationStatus status;

    @NotNull
    private Instant invitedAt;

    @NotNull
    private Instant expiresAt;

    private Instant acceptedAt;

    @NotNull
    private Long invitedByUserId;

    @NotNull
    @Size(max = 100)
    private String invitedByLogin;

    @NotNull
    private OrganizationDTO organization;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public MembershipRole getRole() {
        return role;
    }

    public void setRole(MembershipRole role) {
        this.role = role;
    }

    public InvitationStatus getStatus() {
        return status;
    }

    public void setStatus(InvitationStatus status) {
        this.status = status;
    }

    public Instant getInvitedAt() {
        return invitedAt;
    }

    public void setInvitedAt(Instant invitedAt) {
        this.invitedAt = invitedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(Instant acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public Long getInvitedByUserId() {
        return invitedByUserId;
    }

    public void setInvitedByUserId(Long invitedByUserId) {
        this.invitedByUserId = invitedByUserId;
    }

    public String getInvitedByLogin() {
        return invitedByLogin;
    }

    public void setInvitedByLogin(String invitedByLogin) {
        this.invitedByLogin = invitedByLogin;
    }

    public OrganizationDTO getOrganization() {
        return organization;
    }

    public void setOrganization(OrganizationDTO organization) {
        this.organization = organization;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof InvitationDTO)) {
            return false;
        }

        InvitationDTO invitationDTO = (InvitationDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, invitationDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "InvitationDTO{" +
            "id=" + getId() +
            ", email='" + getEmail() + "'" +
            ", token='" + getToken() + "'" +
            ", role='" + getRole() + "'" +
            ", status='" + getStatus() + "'" +
            ", invitedAt='" + getInvitedAt() + "'" +
            ", expiresAt='" + getExpiresAt() + "'" +
            ", acceptedAt='" + getAcceptedAt() + "'" +
            ", invitedByUserId=" + getInvitedByUserId() +
            ", invitedByLogin='" + getInvitedByLogin() + "'" +
            ", organization=" + getOrganization() +
            "}";
    }
}
