package com.nexaflow.billing.service.dto;

public record AccessCheckDTO(
    Long organizationId,
    BillingFeature feature,
    int requestedAmount,
    boolean allowed,
    int used,
    int limit,
    int remaining,
    String reason
) {}
