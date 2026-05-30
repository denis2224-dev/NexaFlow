package com.nexaflow.userservice.service.criteria;

import com.nexaflow.userservice.domain.enumeration.MembershipRole;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.nexaflow.userservice.domain.Membership} entity. This class is used
 * in {@link com.nexaflow.userservice.web.rest.MembershipResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /memberships?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class MembershipCriteria implements Serializable, Criteria {

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

    @Serial
    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private LongFilter userId;

    private StringFilter userLogin;

    private StringFilter userEmail;

    private MembershipRoleFilter role;

    private InstantFilter joinedAt;

    private BooleanFilter active;

    private LongFilter organizationId;

    private Boolean distinct;

    public MembershipCriteria() {}

    public MembershipCriteria(MembershipCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.userId = other.optionalUserId().map(LongFilter::copy).orElse(null);
        this.userLogin = other.optionalUserLogin().map(StringFilter::copy).orElse(null);
        this.userEmail = other.optionalUserEmail().map(StringFilter::copy).orElse(null);
        this.role = other.optionalRole().map(MembershipRoleFilter::copy).orElse(null);
        this.joinedAt = other.optionalJoinedAt().map(InstantFilter::copy).orElse(null);
        this.active = other.optionalActive().map(BooleanFilter::copy).orElse(null);
        this.organizationId = other.optionalOrganizationId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public MembershipCriteria copy() {
        return new MembershipCriteria(this);
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

    public LongFilter getUserId() {
        return userId;
    }

    public Optional<LongFilter> optionalUserId() {
        return Optional.ofNullable(userId);
    }

    public LongFilter userId() {
        if (userId == null) {
            setUserId(new LongFilter());
        }
        return userId;
    }

    public void setUserId(LongFilter userId) {
        this.userId = userId;
    }

    public StringFilter getUserLogin() {
        return userLogin;
    }

    public Optional<StringFilter> optionalUserLogin() {
        return Optional.ofNullable(userLogin);
    }

    public StringFilter userLogin() {
        if (userLogin == null) {
            setUserLogin(new StringFilter());
        }
        return userLogin;
    }

    public void setUserLogin(StringFilter userLogin) {
        this.userLogin = userLogin;
    }

    public StringFilter getUserEmail() {
        return userEmail;
    }

    public Optional<StringFilter> optionalUserEmail() {
        return Optional.ofNullable(userEmail);
    }

    public StringFilter userEmail() {
        if (userEmail == null) {
            setUserEmail(new StringFilter());
        }
        return userEmail;
    }

    public void setUserEmail(StringFilter userEmail) {
        this.userEmail = userEmail;
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

    public InstantFilter getJoinedAt() {
        return joinedAt;
    }

    public Optional<InstantFilter> optionalJoinedAt() {
        return Optional.ofNullable(joinedAt);
    }

    public InstantFilter joinedAt() {
        if (joinedAt == null) {
            setJoinedAt(new InstantFilter());
        }
        return joinedAt;
    }

    public void setJoinedAt(InstantFilter joinedAt) {
        this.joinedAt = joinedAt;
    }

    public BooleanFilter getActive() {
        return active;
    }

    public Optional<BooleanFilter> optionalActive() {
        return Optional.ofNullable(active);
    }

    public BooleanFilter active() {
        if (active == null) {
            setActive(new BooleanFilter());
        }
        return active;
    }

    public void setActive(BooleanFilter active) {
        this.active = active;
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
        final MembershipCriteria that = (MembershipCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(userId, that.userId) &&
            Objects.equals(userLogin, that.userLogin) &&
            Objects.equals(userEmail, that.userEmail) &&
            Objects.equals(role, that.role) &&
            Objects.equals(joinedAt, that.joinedAt) &&
            Objects.equals(active, that.active) &&
            Objects.equals(organizationId, that.organizationId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, userLogin, userEmail, role, joinedAt, active, organizationId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MembershipCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalUserId().map(f -> "userId=" + f + ", ").orElse("") +
            optionalUserLogin().map(f -> "userLogin=" + f + ", ").orElse("") +
            optionalUserEmail().map(f -> "userEmail=" + f + ", ").orElse("") +
            optionalRole().map(f -> "role=" + f + ", ").orElse("") +
            optionalJoinedAt().map(f -> "joinedAt=" + f + ", ").orElse("") +
            optionalActive().map(f -> "active=" + f + ", ").orElse("") +
            optionalOrganizationId().map(f -> "organizationId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
