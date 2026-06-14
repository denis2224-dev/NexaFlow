package com.nexaflow.project.client.dto;

public record AccessCheckDTO(
    Long organizationId,
    BillingFeature feature,
    int requestedAmount,
    boolean allowed,
    long used,
    long limit,
    long remaining,
    String reason
) {}
