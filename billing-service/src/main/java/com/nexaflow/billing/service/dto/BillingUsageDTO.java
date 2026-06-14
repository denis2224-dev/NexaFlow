package com.nexaflow.billing.service.dto;

public record BillingUsageDTO(
    Long organizationId,
    SubscriptionDTO subscription,
    UsageMetricDTO projects,
    UsageMetricDTO users,
    UsageMetricDTO tasks
) {}
