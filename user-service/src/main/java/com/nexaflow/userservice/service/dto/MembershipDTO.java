package com.nexaflow.userservice.service.dto;

import com.nexaflow.userservice.domain.enumeration.MembershipRole;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.nexaflow.userservice.domain.Membership} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class MembershipDTO implements Serializable {

    private Long id;

    @NotNull
    private Long userId;

    @NotNull
    @Size(max = 100)
    private String userLogin;

    @NotNull
    @Size(max = 254)
    private String userEmail;

    @NotNull
    private MembershipRole role;

    @NotNull
    private Instant joinedAt;

    @NotNull
    private Boolean active;

    @NotNull
    private OrganizationDTO organization;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public MembershipRole getRole() {
        return role;
    }

    public void setRole(MembershipRole role) {
        this.role = role;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
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
        if (!(o instanceof MembershipDTO)) {
            return false;
        }

        MembershipDTO membershipDTO = (MembershipDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, membershipDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MembershipDTO{" +
            "id=" + getId() +
            ", userId=" + getUserId() +
            ", userLogin='" + getUserLogin() + "'" +
            ", userEmail='" + getUserEmail() + "'" +
            ", role='" + getRole() + "'" +
            ", joinedAt='" + getJoinedAt() + "'" +
            ", active='" + getActive() + "'" +
            ", organization=" + getOrganization() +
            "}";
    }
}
