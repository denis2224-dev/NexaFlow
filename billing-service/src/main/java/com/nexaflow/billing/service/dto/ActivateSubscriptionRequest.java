package com.nexaflow.billing.service.dto;

import com.nexaflow.billing.domain.enumeration.PlanCode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ActivateSubscriptionRequest(
    @NotNull @Min(1) Long organizationId,
    @NotNull PlanCode planCode,
    @Size(max = 100) String createdBy
) {}
