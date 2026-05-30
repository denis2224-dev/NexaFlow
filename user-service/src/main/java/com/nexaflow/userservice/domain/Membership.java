package com.nexaflow.userservice.domain;

import com.nexaflow.userservice.domain.enumeration.MembershipRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * A Membership.
 */
@Entity
@Table(name = "membership")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Membership implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotNull
    @Size(max = 100)
    @Column(name = "user_login", length = 100, nullable = false)
    private String userLogin;

    @NotNull
    @Size(max = 254)
    @Column(name = "user_email", length = 254, nullable = false)
    private String userEmail;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private MembershipRole role;

    @NotNull
    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active;

    @ManyToOne(optional = false)
    @NotNull
    private Organization organization;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Membership id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return this.userId;
    }

    public Membership userId(Long userId) {
        this.setUserId(userId);
        return this;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserLogin() {
        return this.userLogin;
    }

    public Membership userLogin(String userLogin) {
        this.setUserLogin(userLogin);
        return this;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    public String getUserEmail() {
        return this.userEmail;
    }

    public Membership userEmail(String userEmail) {
        this.setUserEmail(userEmail);
        return this;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public MembershipRole getRole() {
        return this.role;
    }

    public Membership role(MembershipRole role) {
        this.setRole(role);
        return this;
    }

    public void setRole(MembershipRole role) {
        this.role = role;
    }

    public Instant getJoinedAt() {
        return this.joinedAt;
    }

    public Membership joinedAt(Instant joinedAt) {
        this.setJoinedAt(joinedAt);
        return this;
    }

    public void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }

    public Boolean getActive() {
        return this.active;
    }

    public Membership active(Boolean active) {
        this.setActive(active);
        return this;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Organization getOrganization() {
        return this.organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public Membership organization(Organization organization) {
        this.setOrganization(organization);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Membership)) {
            return false;
        }
        return getId() != null && getId().equals(((Membership) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Membership{" +
            "id=" + getId() +
            ", userId=" + getUserId() +
            ", userLogin='" + getUserLogin() + "'" +
            ", userEmail='" + getUserEmail() + "'" +
            ", role='" + getRole() + "'" +
            ", joinedAt='" + getJoinedAt() + "'" +
            ", active='" + getActive() + "'" +
            "}";
    }
}
