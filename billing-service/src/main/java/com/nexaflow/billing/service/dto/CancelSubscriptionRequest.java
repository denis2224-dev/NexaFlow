package com.nexaflow.billing.service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CancelSubscriptionRequest(@NotNull @Min(1) Long organizationId) {}
