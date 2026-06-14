package com.nexaflow.billing.web.rest;

import com.nexaflow.billing.service.BillingApplicationService;
import com.nexaflow.billing.service.dto.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.ResponseUtil;

@Validated
@RestController
@RequestMapping("/api/billing")
public class BillingResource {

    private final BillingApplicationService billingApplicationService;

    public BillingResource(BillingApplicationService billingApplicationService) {
        this.billingApplicationService = billingApplicationService;
    }

    @GetMapping("/plans")
    public ResponseEntity<List<PlanDTO>> getPlans() {
        return ResponseEntity.ok(billingApplicationService.getActivePlans());
    }

    @GetMapping("/subscription/my")
    public ResponseEntity<SubscriptionDTO> getMySubscription(@RequestParam @Min(1) Long organizationId) {
        return ResponseUtil.wrapOrNotFound(billingApplicationService.getCurrentSubscription(organizationId));
    }

    @PostMapping("/subscription/activate")
    public ResponseEntity<SubscriptionDTO> activateSubscription(@Valid @RequestBody ActivateSubscriptionRequest request) {
        return ResponseEntity.ok(billingApplicationService.activateSubscription(request));
    }

    @PostMapping("/subscription/cancel")
    public ResponseEntity<SubscriptionDTO> cancelSubscription(@Valid @RequestBody CancelSubscriptionRequest request) {
        return ResponseEntity.ok(billingApplicationService.cancelSubscription(request));
    }

    @GetMapping("/usage")
    public ResponseEntity<BillingUsageDTO> getUsage(@RequestParam @Min(1) Long organizationId) {
        return ResponseEntity.ok(billingApplicationService.getUsage(organizationId));
    }
}
