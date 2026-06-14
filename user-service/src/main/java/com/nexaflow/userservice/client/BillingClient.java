package com.nexaflow.userservice.client;

import com.nexaflow.userservice.client.dto.AccessCheckDTO;
import com.nexaflow.userservice.client.dto.BillingFeature;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "billing-service", path = "/api/internal/billing")
public interface BillingClient {

    @GetMapping("/access/check")
    AccessCheckDTO checkAccess(
        @RequestParam("organizationId") Long organizationId,
        @RequestParam("feature") BillingFeature feature,
        @RequestParam("requestedAmount") int requestedAmount
    );
}
