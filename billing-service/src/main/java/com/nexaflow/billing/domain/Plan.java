package com.nexaflow.billing.domain;

import com.nexaflow.billing.domain.enumeration.PlanCode;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * A Plan.
 */
@Entity
@Table(name = "plan")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Plan implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "code", nullable = false, unique = true)
    private PlanCode code;

    @NotNull
    @Size(max = 100)
    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @NotNull
    @DecimalMin(value = "0")
    @Column(name = "price_monthly", precision = 21, scale = 2, nullable = false)
    private BigDecimal priceMonthly;

    @NotNull
    @Min(value = 0)
    @Column(name = "max_projects", nullable = false)
    private Integer maxProjects;

    @NotNull
    @Min(value = 0)
    @Column(name = "max_users", nullable = false)
    private Integer maxUsers;

    @NotNull
    @Min(value = 0)
    @Column(name = "max_tasks", nullable = false)
    private Integer maxTasks;

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Plan id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PlanCode getCode() {
        return this.code;
    }

    public Plan code(PlanCode code) {
        this.setCode(code);
        return this;
    }

    public void setCode(PlanCode code) {
        this.code = code;
    }

    public String getName() {
        return this.name;
    }

    public Plan name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPriceMonthly() {
        return this.priceMonthly;
    }

    public Plan priceMonthly(BigDecimal priceMonthly) {
        this.setPriceMonthly(priceMonthly);
        return this;
    }

    public void setPriceMonthly(BigDecimal priceMonthly) {
        this.priceMonthly = priceMonthly;
    }

    public Integer getMaxProjects() {
        return this.maxProjects;
    }

    public Plan maxProjects(Integer maxProjects) {
        this.setMaxProjects(maxProjects);
        return this;
    }

    public void setMaxProjects(Integer maxProjects) {
        this.maxProjects = maxProjects;
    }

    public Integer getMaxUsers() {
        return this.maxUsers;
    }

    public Plan maxUsers(Integer maxUsers) {
        this.setMaxUsers(maxUsers);
        return this;
    }

    public void setMaxUsers(Integer maxUsers) {
        this.maxUsers = maxUsers;
    }

    public Integer getMaxTasks() {
        return this.maxTasks;
    }

    public Plan maxTasks(Integer maxTasks) {
        this.setMaxTasks(maxTasks);
        return this;
    }

    public void setMaxTasks(Integer maxTasks) {
        this.maxTasks = maxTasks;
    }

    public Boolean getActive() {
        return this.active;
    }

    public Plan active(Boolean active) {
        this.setActive(active);
        return this;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public Plan createdAt(Instant createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return this.updatedAt;
    }

    public Plan updatedAt(Instant updatedAt) {
        this.setUpdatedAt(updatedAt);
        return this;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Plan)) {
            return false;
        }
        return getId() != null && getId().equals(((Plan) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Plan{" +
            "id=" + getId() +
            ", code='" + getCode() + "'" +
            ", name='" + getName() + "'" +
            ", priceMonthly=" + getPriceMonthly() +
            ", maxProjects=" + getMaxProjects() +
            ", maxUsers=" + getMaxUsers() +
            ", maxTasks=" + getMaxTasks() +
            ", active='" + getActive() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
            "}";
    }
}
