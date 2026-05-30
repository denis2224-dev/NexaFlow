package com.nexaflow.userservice.service.criteria;

import com.nexaflow.userservice.domain.enumeration.InvitationStatus;
import com.nexaflow.userservice.domain.enumeration.MembershipRole;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.nexaflow.userservice.domain.Invitation} entity. This class is used
 * in {@link com.nexaflow.userservice.web.rest.InvitationResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /invitations?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class InvitationCriteria implements Serializable, Criteria {

    /**
     * Class for filtering MembershipRole
     */
    public static class MembershipRoleFilter extends Filter<MembershipRole> {

        public MembershipRoleFilter() {}

        public MembershipRoleFilter(MembershipRoleFilter filter) {
            super(filter);
        }

        @Override
        public MembershipRoleFilter copy() {
            return new MembershipRoleFilter(this);
        }
    }

    /**
     * Class for filtering InvitationStatus
     */
    public static class InvitationStatusFilter extends Filter<InvitationStatus> {

        public InvitationStatusFilter() {}

        public InvitationStatusFilter(InvitationStatusFilter filter) {
            super(filter);
        }

        @Override
        public InvitationStatusFilter copy() {
            return new InvitationStatusFilter(this);
        }
    }

    @Serial
    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter email;

    private StringFilter token;

    private MembershipRoleFilter role;

    private InvitationStatusFilter status;

    private InstantFilter invitedAt;

    private InstantFilter expiresAt;

    private InstantFilter acceptedAt;

    private LongFilter invitedByUserId;

    private StringFilter invitedByLogin;

    private LongFilter organizationId;

    private Boolean distinct;

    public InvitationCriteria() {}

    public InvitationCriteria(InvitationCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.email = other.optionalEmail().map(StringFilter::copy).orElse(null);
        this.token = other.optionalToken().map(StringFilter::copy).orElse(null);
        this.role = other.optionalRole().map(MembershipRoleFilter::copy).orElse(null);
        this.status = other.optionalStatus().map(InvitationStatusFilter::copy).orElse(null);
        this.invitedAt = other.optionalInvitedAt().map(InstantFilter::copy).orElse(null);
        this.expiresAt = other.optionalExpiresAt().map(InstantFilter::copy).orElse(null);
        this.acceptedAt = other.optionalAcceptedAt().map(InstantFilter::copy).orElse(null);
        this.invitedByUserId = other.optionalInvitedByUserId().map(LongFilter::copy).orElse(null);
        this.invitedByLogin = other.optionalInvitedByLogin().map(StringFilter::copy).orElse(null);
        this.organizationId = other.optionalOrganizationId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public InvitationCriteria copy() {
        return new InvitationCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public Optional<LongFilter> optionalId() {
        return Optional.ofNullable(id);
    }

    public LongFilter id() {
        if (id == null) {
            setId(new LongFilter());
        }
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getEmail() {
        return email;
    }

    public Optional<StringFilter> optionalEmail() {
        return Optional.ofNullable(email);
    }

    public StringFilter email() {
        if (email == null) {
            setEmail(new StringFilter());
        }
        return email;
    }

    public void setEmail(StringFilter email) {
        this.email = email;
    }

    public StringFilter getToken() {
        return token;
    }

    public Optional<StringFilter> optionalToken() {
        return Optional.ofNullable(token);
    }

    public StringFilter token() {
        if (token == null) {
            setToken(new StringFilter());
        }
        return token;
    }

    public void setToken(StringFilter token) {
        this.token = token;
    }

    public MembershipRoleFilter getRole() {
        return role;
    }

    public Optional<MembershipRoleFilter> optionalRole() {
        return Optional.ofNullable(role);
    }

    public MembershipRoleFilter role() {
        if (role == null) {
            setRole(new MembershipRoleFilter());
        }
        return role;
    }

    public void setRole(MembershipRoleFilter role) {
        this.role = role;
    }

    public InvitationStatusFilter getStatus() {
        return status;
    }

    public Optional<InvitationStatusFilter> optionalStatus() {
        return Optional.ofNullable(status);
    }

    public InvitationStatusFilter status() {
        if (status == null) {
            setStatus(new InvitationStatusFilter());
        }
        return status;
    }

    public void setStatus(InvitationStatusFilter status) {
        this.status = status;
    }

    public InstantFilter getInvitedAt() {
        return invitedAt;
    }

    public Optional<InstantFilter> optionalInvitedAt() {
        return Optional.ofNullable(invitedAt);
    }

    public InstantFilter invitedAt() {
        if (invitedAt == null) {
            setInvitedAt(new InstantFilter());
        }
        return invitedAt;
    }

    public void setInvitedAt(InstantFilter invitedAt) {
        this.invitedAt = invitedAt;
    }

    public InstantFilter getExpiresAt() {
        return expiresAt;
    }

    public Optional<InstantFilter> optionalExpiresAt() {
        return Optional.ofNullable(expiresAt);
    }

    public InstantFilter expiresAt() {
        if (expiresAt == null) {
            setExpiresAt(new InstantFilter());
        }
        return expiresAt;
    }

    public void setExpiresAt(InstantFilter expiresAt) {
        this.expiresAt = expiresAt;
    }

    public InstantFilter getAcceptedAt() {
        return acceptedAt;
    }

    public Optional<InstantFilter> optionalAcceptedAt() {
        return Optional.ofNullable(acceptedAt);
    }

    public InstantFilter acceptedAt() {
        if (acceptedAt == null) {
            setAcceptedAt(new InstantFilter());
        }
        return acceptedAt;
    }

    public void setAcceptedAt(InstantFilter acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public LongFilter getInvitedByUserId() {
        return invitedByUserId;
    }

    public Optional<LongFilter> optionalInvitedByUserId() {
        return Optional.ofNullable(invitedByUserId);
    }

    public LongFilter invitedByUserId() {
        if (invitedByUserId == null) {
            setInvitedByUserId(new LongFilter());
        }
        return invitedByUserId;
    }

    public void setInvitedByUserId(LongFilter invitedByUserId) {
        this.invitedByUserId = invitedByUserId;
    }

    public StringFilter getInvitedByLogin() {
        return invitedByLogin;
    }

    public Optional<StringFilter> optionalInvitedByLogin() {
        return Optional.ofNullable(invitedByLogin);
    }

    public StringFilter invitedByLogin() {
        if (invitedByLogin == null) {
            setInvitedByLogin(new StringFilter());
        }
        return invitedByLogin;
    }

    public void setInvitedByLogin(StringFilter invitedByLogin) {
        this.invitedByLogin = invitedByLogin;
    }

    public LongFilter getOrganizationId() {
        return organizationId;
    }

    public Optional<LongFilter> optionalOrganizationId() {
        return Optional.ofNullable(organizationId);
    }

    public LongFilter organizationId() {
        if (organizationId == null) {
            setOrganizationId(new LongFilter());
        }
        return organizationId;
    }

    public void setOrganizationId(LongFilter organizationId) {
        this.organizationId = organizationId;
    }

    public Boolean getDistinct() {
        return distinct;
    }

    public Optional<Boolean> optionalDistinct() {
        return Optional.ofNullable(distinct);
    }

    public Boolean distinct() {
        if (distinct == null) {
            setDistinct(true);
        }
        return distinct;
    }

    public void setDistinct(Boolean distinct) {
        this.distinct = distinct;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final InvitationCriteria that = (InvitationCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(email, that.email) &&
            Objects.equals(token, that.token) &&
            Objects.equals(role, that.role) &&
            Objects.equals(status, that.status) &&
            Objects.equals(invitedAt, that.invitedAt) &&
            Objects.equals(expiresAt, that.expiresAt) &&
            Objects.equals(acceptedAt, that.acceptedAt) &&
            Objects.equals(invitedByUserId, that.invitedByUserId) &&
            Objects.equals(invitedByLogin, that.invitedByLogin) &&
            Objects.equals(organizationId, that.organizationId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            email,
            token,
            role,
            status,
            invitedAt,
            expiresAt,
            acceptedAt,
            invitedByUserId,
            invitedByLogin,
            organizationId,
            distinct
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "InvitationCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalEmail().map(f -> "email=" + f + ", ").orElse("") +
            optionalToken().map(f -> "token=" + f + ", ").orElse("") +
            optionalRole().map(f -> "role=" + f + ", ").orElse("") +
            optionalStatus().map(f -> "status=" + f + ", ").orElse("") +
            optionalInvitedAt().map(f -> "invitedAt=" + f + ", ").orElse("") +
            optionalExpiresAt().map(f -> "expiresAt=" + f + ", ").orElse("") +
            optionalAcceptedAt().map(f -> "acceptedAt=" + f + ", ").orElse("") +
            optionalInvitedByUserId().map(f -> "invitedByUserId=" + f + ", ").orElse("") +
            optionalInvitedByLogin().map(f -> "invitedByLogin=" + f + ", ").orElse("") +
            optionalOrganizationId().map(f -> "organizationId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
