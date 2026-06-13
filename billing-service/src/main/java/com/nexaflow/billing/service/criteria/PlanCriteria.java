package com.nexaflow.billing.service.criteria;

import com.nexaflow.billing.domain.enumeration.PlanCode;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.nexaflow.billing.domain.Plan} entity. This class is used
 * in {@link com.nexaflow.billing.web.rest.PlanResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /plans?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class PlanCriteria implements Serializable, Criteria {

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

    @Serial
    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private PlanCodeFilter code;

    private StringFilter name;

    private BigDecimalFilter priceMonthly;

    private IntegerFilter maxProjects;

    private IntegerFilter maxUsers;

    private IntegerFilter maxTasks;

    private BooleanFilter active;

    private InstantFilter createdAt;

    private InstantFilter updatedAt;

    private Boolean distinct;

    public PlanCriteria() {}

    public PlanCriteria(PlanCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.code = other.optionalCode().map(PlanCodeFilter::copy).orElse(null);
        this.name = other.optionalName().map(StringFilter::copy).orElse(null);
        this.priceMonthly = other.optionalPriceMonthly().map(BigDecimalFilter::copy).orElse(null);
        this.maxProjects = other.optionalMaxProjects().map(IntegerFilter::copy).orElse(null);
        this.maxUsers = other.optionalMaxUsers().map(IntegerFilter::copy).orElse(null);
        this.maxTasks = other.optionalMaxTasks().map(IntegerFilter::copy).orElse(null);
        this.active = other.optionalActive().map(BooleanFilter::copy).orElse(null);
        this.createdAt = other.optionalCreatedAt().map(InstantFilter::copy).orElse(null);
        this.updatedAt = other.optionalUpdatedAt().map(InstantFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public PlanCriteria copy() {
        return new PlanCriteria(this);
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

    public PlanCodeFilter getCode() {
        return code;
    }

    public Optional<PlanCodeFilter> optionalCode() {
        return Optional.ofNullable(code);
    }

    public PlanCodeFilter code() {
        if (code == null) {
            setCode(new PlanCodeFilter());
        }
        return code;
    }

    public void setCode(PlanCodeFilter code) {
        this.code = code;
    }

    public StringFilter getName() {
        return name;
    }

    public Optional<StringFilter> optionalName() {
        return Optional.ofNullable(name);
    }

    public StringFilter name() {
        if (name == null) {
            setName(new StringFilter());
        }
        return name;
    }

    public void setName(StringFilter name) {
        this.name = name;
    }

    public BigDecimalFilter getPriceMonthly() {
        return priceMonthly;
    }

    public Optional<BigDecimalFilter> optionalPriceMonthly() {
        return Optional.ofNullable(priceMonthly);
    }

    public BigDecimalFilter priceMonthly() {
        if (priceMonthly == null) {
            setPriceMonthly(new BigDecimalFilter());
        }
        return priceMonthly;
    }

    public void setPriceMonthly(BigDecimalFilter priceMonthly) {
        this.priceMonthly = priceMonthly;
    }

    public IntegerFilter getMaxProjects() {
        return maxProjects;
    }

    public Optional<IntegerFilter> optionalMaxProjects() {
        return Optional.ofNullable(maxProjects);
    }

    public IntegerFilter maxProjects() {
        if (maxProjects == null) {
            setMaxProjects(new IntegerFilter());
        }
        return maxProjects;
    }

    public void setMaxProjects(IntegerFilter maxProjects) {
        this.maxProjects = maxProjects;
    }

    public IntegerFilter getMaxUsers() {
        return maxUsers;
    }

    public Optional<IntegerFilter> optionalMaxUsers() {
        return Optional.ofNullable(maxUsers);
    }

    public IntegerFilter maxUsers() {
        if (maxUsers == null) {
            setMaxUsers(new IntegerFilter());
        }
        return maxUsers;
    }

    public void setMaxUsers(IntegerFilter maxUsers) {
        this.maxUsers = maxUsers;
    }

    public IntegerFilter getMaxTasks() {
        return maxTasks;
    }

    public Optional<IntegerFilter> optionalMaxTasks() {
        return Optional.ofNullable(maxTasks);
    }

    public IntegerFilter maxTasks() {
        if (maxTasks == null) {
            setMaxTasks(new IntegerFilter());
        }
        return maxTasks;
    }

    public void setMaxTasks(IntegerFilter maxTasks) {
        this.maxTasks = maxTasks;
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
        final PlanCriteria that = (PlanCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(code, that.code) &&
            Objects.equals(name, that.name) &&
            Objects.equals(priceMonthly, that.priceMonthly) &&
            Objects.equals(maxProjects, that.maxProjects) &&
            Objects.equals(maxUsers, that.maxUsers) &&
            Objects.equals(maxTasks, that.maxTasks) &&
            Objects.equals(active, that.active) &&
            Objects.equals(createdAt, that.createdAt) &&
            Objects.equals(updatedAt, that.updatedAt) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code, name, priceMonthly, maxProjects, maxUsers, maxTasks, active, createdAt, updatedAt, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "PlanCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalCode().map(f -> "code=" + f + ", ").orElse("") +
            optionalName().map(f -> "name=" + f + ", ").orElse("") +
            optionalPriceMonthly().map(f -> "priceMonthly=" + f + ", ").orElse("") +
            optionalMaxProjects().map(f -> "maxProjects=" + f + ", ").orElse("") +
            optionalMaxUsers().map(f -> "maxUsers=" + f + ", ").orElse("") +
            optionalMaxTasks().map(f -> "maxTasks=" + f + ", ").orElse("") +
            optionalActive().map(f -> "active=" + f + ", ").orElse("") +
            optionalCreatedAt().map(f -> "createdAt=" + f + ", ").orElse("") +
            optionalUpdatedAt().map(f -> "updatedAt=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
