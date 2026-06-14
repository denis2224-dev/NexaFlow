package com.nexaflow.billing.service.dto;

public record UsageMetricDTO(long used, long limit, long remaining) {}
