package com.nexaflow.userservice.domain;

import com.nexaflow.userservice.domain.enumeration.InvitationStatus;
import com.nexaflow.userservice.domain.enumeration.MembershipRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * A Invitation.
 */
@Entity
@Table(name = "invitation")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Invitation implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(max = 254)
    @Column(name = "email", length = 254, nullable = false)
    private String email;

    @NotNull
    @Size(max = 120)
    @Column(name = "token", length = 120, nullable = false, unique = true)
    private String token;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private MembershipRole role;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InvitationStatus status;

    @NotNull
    @Column(name = "invited_at", nullable = false)
    private Instant invitedAt;

    @NotNull
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    @NotNull
    @Column(name = "invited_by_user_id", nullable = false)
    private Long invitedByUserId;

    @NotNull
    @Size(max = 100)
    @Column(name = "invited_by_login", length = 100, nullable = false)
    private String invitedByLogin;

    @ManyToOne(optional = false)
    @NotNull
    private Organization organization;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Invitation id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return this.email;
    }

    public Invitation email(String email) {
        this.setEmail(email);
        return this;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return this.token;
    }

    public Invitation token(String token) {
        this.setToken(token);
        return this;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public MembershipRole getRole() {
        return this.role;
    }

    public Invitation role(MembershipRole role) {
        this.setRole(role);
        return this;
    }

    public void setRole(MembershipRole role) {
        this.role = role;
    }

    public InvitationStatus getStatus() {
        return this.status;
    }

    public Invitation status(InvitationStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(InvitationStatus status) {
        this.status = status;
    }

    public Instant getInvitedAt() {
        return this.invitedAt;
    }

    public Invitation invitedAt(Instant invitedAt) {
        this.setInvitedAt(invitedAt);
        return this;
    }

    public void setInvitedAt(Instant invitedAt) {
        this.invitedAt = invitedAt;
    }

    public Instant getExpiresAt() {
        return this.expiresAt;
    }

    public Invitation expiresAt(Instant expiresAt) {
        this.setExpiresAt(expiresAt);
        return this;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getAcceptedAt() {
        return this.acceptedAt;
    }

    public Invitation acceptedAt(Instant acceptedAt) {
        this.setAcceptedAt(acceptedAt);
        return this;
    }

    public void setAcceptedAt(Instant acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public Long getInvitedByUserId() {
        return this.invitedByUserId;
    }

    public Invitation invitedByUserId(Long invitedByUserId) {
        this.setInvitedByUserId(invitedByUserId);
        return this;
    }

    public void setInvitedByUserId(Long invitedByUserId) {
        this.invitedByUserId = invitedByUserId;
    }

    public String getInvitedByLogin() {
        return this.invitedByLogin;
    }

    public Invitation invitedByLogin(String invitedByLogin) {
        this.setInvitedByLogin(invitedByLogin);
        return this;
    }

    public void setInvitedByLogin(String invitedByLogin) {
        this.invitedByLogin = invitedByLogin;
    }

    public Organization getOrganization() {
        return this.organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public Invitation organization(Organization organization) {
        this.setOrganization(organization);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Invitation)) {
            return false;
        }
        return getId() != null && getId().equals(((Invitation) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Invitation{" +
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
            "}";
    }
}
