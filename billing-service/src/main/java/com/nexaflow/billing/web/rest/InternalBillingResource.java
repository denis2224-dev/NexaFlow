package com.nexaflow.billing.web.rest;

import com.nexaflow.billing.security.OrganizationAccessService;
import com.nexaflow.billing.service.BillingApplicationService;
import com.nexaflow.billing.service.dto.AccessCheckDTO;
import com.nexaflow.billing.service.dto.BillingFeature;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/internal/billing")
public class InternalBillingResource {

    private final BillingApplicationService billingApplicationService;
    private final OrganizationAccessService organizationAccessService;

    public InternalBillingResource(BillingApplicationService billingApplicationService, OrganizationAccessService organizationAccessService) {
        this.billingApplicationService = billingApplicationService;
        this.organizationAccessService = organizationAccessService;
    }

    @GetMapping("/access/check")
    public ResponseEntity<AccessCheckDTO> checkAccess(
        @RequestParam @Min(1) Long organizationId,
        @RequestParam BillingFeature feature,
        @RequestParam(defaultValue = "1") @Min(1) int requestedAmount
    ) {
        organizationAccessService.assertMember(organizationId);
        return ResponseEntity.ok(billingApplicationService.checkAccess(organizationId, feature, requestedAmount));
    }
}
