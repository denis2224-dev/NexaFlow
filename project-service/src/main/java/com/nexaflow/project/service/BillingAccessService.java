package com.nexaflow.project.service;

import com.nexaflow.project.client.BillingClient;
import com.nexaflow.project.client.dto.BillingFeature;
import feign.FeignException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class BillingAccessService {

    private final BillingClient billingClient;

    public BillingAccessService(BillingClient billingClient) {
        this.billingClient = billingClient;
    }

    public void assertAllowed(Long organizationId, BillingFeature feature, int requestedAmount) {
        try {
            var result = billingClient.checkAccess(organizationId, feature, requestedAmount);

            if (!result.allowed()) {
                throw new AccessDeniedException("Billing limit exceeded: " + result.reason());
            }
        } catch (FeignException e) {
            throw new AccessDeniedException("Billing access check failed");
        }
    }
}
