package com.nexaflow.billing.service.criteria;

import com.nexaflow.billing.domain.enumeration.PlanCode;
import com.nexaflow.billing.domain.enumeration.SubscriptionStatus;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.nexaflow.billing.domain.Subscription} entity. This class is used
 * in {@link com.nexaflow.billing.web.rest.SubscriptionResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /subscriptions?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class SubscriptionCriteria implements Serializable, Criteria {

    /**
     * Class for filtering PlanCode
     */
    public static class PlanCodeFilter extends Filter<PlanCode> {

        public PlanCodeFilter() {}

        public PlanCodeFilter(PlanCodeFilter filter) {
            super(filter);
        }

        @Override
        public PlanCodeFilter copy() {
            return new PlanCodeFilter(this);
        }
    }

    /**
     * Class for filtering SubscriptionStatus
     */
    public static class SubscriptionStatusFilter extends Filter<SubscriptionStatus> {

        public SubscriptionStatusFilter() {}

        public SubscriptionStatusFilter(SubscriptionStatusFilter filter) {
            super(filter);
        }

        @Override
        public SubscriptionStatusFilter copy() {
            return new SubscriptionStatusFilter(this);
        }
    }

    @Serial
    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private LongFilter organizationId;

    private PlanCodeFilter planCode;

    private SubscriptionStatusFilter status;

    private InstantFilter startedAt;

    private InstantFilter expiresAt;

    private StringFilter createdBy;

    private InstantFilter createdAt;

    private InstantFilter updatedAt;

    private Boolean distinct;

    public SubscriptionCriteria() {}

    public SubscriptionCriteria(SubscriptionCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.organizationId = other.optionalOrganizationId().map(LongFilter::copy).orElse(null);
        this.planCode = other.optionalPlanCode().map(PlanCodeFilter::copy).orElse(null);
        this.status = other.optionalStatus().map(SubscriptionStatusFilter::copy).orElse(null);
        this.startedAt = other.optionalStartedAt().map(InstantFilter::copy).orElse(null);
        this.expiresAt = other.optionalExpiresAt().map(InstantFilter::copy).orElse(null);
        this.createdBy = other.optionalCreatedBy().map(StringFilter::copy).orElse(null);
        this.createdAt = other.optionalCreatedAt().map(InstantFilter::copy).orElse(null);
        this.updatedAt = other.optionalUpdatedAt().map(InstantFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public SubscriptionCriteria copy() {
        return new SubscriptionCriteria(this);
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

    public PlanCodeFilter getPlanCode() {
        return planCode;
    }

    public Optional<PlanCodeFilter> optionalPlanCode() {
        return Optional.ofNullable(planCode);
    }

    public PlanCodeFilter planCode() {
        if (planCode == null) {
            setPlanCode(new PlanCodeFilter());
        }
        return planCode;
    }

    public void setPlanCode(PlanCodeFilter planCode) {
        this.planCode = planCode;
    }

    public SubscriptionStatusFilter getStatus() {
        return status;
    }

    public Optional<SubscriptionStatusFilter> optionalStatus() {
        return Optional.ofNullable(status);
    }

    public SubscriptionStatusFilter status() {
        if (status == null) {
            setStatus(new SubscriptionStatusFilter());
        }
        return status;
    }

    public void setStatus(SubscriptionStatusFilter status) {
        this.status = status;
    }

    public InstantFilter getStartedAt() {
        return startedAt;
    }

    public Optional<InstantFilter> optionalStartedAt() {
        return Optional.ofNullable(startedAt);
    }

    public InstantFilter startedAt() {
        if (startedAt == null) {
            setStartedAt(new InstantFilter());
        }
        return startedAt;
    }

    public void setStartedAt(InstantFilter startedAt) {
        this.startedAt = startedAt;
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

    public StringFilter getCreatedBy() {
        return createdBy;
    }

    public Optional<StringFilter> optionalCreatedBy() {
        return Optional.ofNullable(createdBy);
    }

    public StringFilter createdBy() {
        if (createdBy == null) {
            setCreatedBy(new StringFilter());
        }
        return createdBy;
    }

    public void setCreatedBy(StringFilter createdBy) {
        this.createdBy = createdBy;
    }

    public InstantFilter getCreatedAt() {
        return createdAt;
    }

    public Optional<InstantFilter> optionalCreatedAt() {
        return Optional.ofNullable(createdAt);
    }

    public InstantFilter createdAt() {
        if (createdAt == null) {
            setCreatedAt(new InstantFilter());
        }
        return createdAt;
    }

    public void setCreatedAt(InstantFilter createdAt) {
        this.createdAt = createdAt;
    }

    public InstantFilter getUpdatedAt() {
        return updatedAt;
    }

    public Optional<InstantFilter> optionalUpdatedAt() {
        return Optional.ofNullable(updatedAt);
    }

    public InstantFilter updatedAt() {
        if (updatedAt == null) {
            setUpdatedAt(new InstantFilter());
        }
        return updatedAt;
    }

    public void setUpdatedAt(InstantFilter updatedAt) {
        this.updatedAt = updatedAt;
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
        final SubscriptionCriteria that = (SubscriptionCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(organizationId, that.organizationId) &&
            Objects.equals(planCode, that.planCode) &&
            Objects.equals(status, that.status) &&
            Objects.equals(startedAt, that.startedAt) &&
            Objects.equals(expiresAt, that.expiresAt) &&
            Objects.equals(createdBy, that.createdBy) &&
            Objects.equals(createdAt, that.createdAt) &&
            Objects.equals(updatedAt, that.updatedAt) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, organizationId, planCode, status, startedAt, expiresAt, createdBy, createdAt, updatedAt, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "SubscriptionCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalOrganizationId().map(f -> "organizationId=" + f + ", ").orElse("") +
            optionalPlanCode().map(f -> "planCode=" + f + ", ").orElse("") +
            optionalStatus().map(f -> "status=" + f + ", ").orElse("") +
            optionalStartedAt().map(f -> "startedAt=" + f + ", ").orElse("") +
            optionalExpiresAt().map(f -> "expiresAt=" + f + ", ").orElse("") +
            optionalCreatedBy().map(f -> "createdBy=" + f + ", ").orElse("") +
            optionalCreatedAt().map(f -> "createdAt=" + f + ", ").orElse("") +
            optionalUpdatedAt().map(f -> "updatedAt=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
