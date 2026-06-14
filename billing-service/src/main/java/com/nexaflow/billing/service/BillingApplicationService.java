package com.nexaflow.billing.service;

import com.nexaflow.billing.domain.Plan;
import com.nexaflow.billing.domain.Subscription;
import com.nexaflow.billing.domain.enumeration.PlanCode;
import com.nexaflow.billing.domain.enumeration.SubscriptionStatus;
import com.nexaflow.billing.repository.PlanRepository;
import com.nexaflow.billing.repository.SubscriptionRepository;
import com.nexaflow.billing.security.SecurityUtils;
import com.nexaflow.billing.service.dto.*;
import com.nexaflow.billing.service.mapper.PlanMapper;
import com.nexaflow.billing.service.mapper.SubscriptionMapper;
import com.nexaflow.billing.web.rest.errors.BadRequestAlertException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class BillingApplicationService {

    private static final String ENTITY_NAME = "billing";

    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PlanMapper planMapper;
    private final SubscriptionMapper subscriptionMapper;

    public BillingApplicationService(
        PlanRepository planRepository,
        SubscriptionRepository subscriptionRepository,
        PlanMapper planMapper,
        SubscriptionMapper subscriptionMapper
    ) {
        this.planRepository = planRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.planMapper = planMapper;
        this.subscriptionMapper = subscriptionMapper;
    }

    @Transactional(readOnly = true)
    public List<PlanDTO> getActivePlans() {
        return planRepository.findAllByActiveTrueOrderByPriceMonthlyAsc().stream().map(planMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public Optional<SubscriptionDTO> getCurrentSubscription(Long organizationId) {
        return findActiveSubscription(organizationId).map(subscriptionMapper::toDto);
    }

    public SubscriptionDTO activateSubscription(ActivateSubscriptionRequest request) {
        Instant now = Instant.now();
        Plan plan = planRepository
            .findByCodeAndActiveTrue(request.planCode())
            .orElseThrow(() -> new BadRequestAlertException("Plan is not active", ENTITY_NAME, "plannotactive"));

        findActiveSubscriptions(request.organizationId(), now).forEach(subscription -> {
            subscription.setStatus(SubscriptionStatus.CANCELLED);
            subscription.setExpiresAt(now);
            subscription.setUpdatedAt(now);
        });

        Subscription subscription = new Subscription()
            .organizationId(request.organizationId())
            .planCode(plan.getCode())
            .status(SubscriptionStatus.ACTIVE)
            .startedAt(now)
            .createdBy(resolveCreatedBy(request.createdBy()))
            .createdAt(now)
            .updatedAt(now);

        return subscriptionMapper.toDto(subscriptionRepository.save(subscription));
    }

    public SubscriptionDTO cancelSubscription(CancelSubscriptionRequest request) {
        Instant now = Instant.now();
        Subscription subscription = findActiveSubscription(request.organizationId(), now)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No active subscription found"));

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setExpiresAt(now);
        subscription.setUpdatedAt(now);

        return subscriptionMapper.toDto(subscriptionRepository.save(subscription));
    }

    @Transactional(readOnly = true)
    public BillingUsageDTO getUsage(Long organizationId) {
        Subscription subscription = findActiveSubscription(organizationId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No active subscription found"));
        Plan plan = getActivePlan(subscription.getPlanCode());

        return toUsage(organizationId, subscriptionMapper.toDto(subscription), plan);
    }

    @Transactional(readOnly = true)
    public AccessCheckDTO checkAccess(Long organizationId, BillingFeature feature, int requestedAmount) {
        Optional<Subscription> subscription = findActiveSubscription(organizationId);
        if (subscription.isEmpty()) {
            return new AccessCheckDTO(organizationId, feature, requestedAmount, false, 0, 0, 0, "NO_ACTIVE_SUBSCRIPTION");
        }

        Optional<Plan> plan = planRepository.findByCodeAndActiveTrue(subscription.orElseThrow().getPlanCode());
        if (plan.isEmpty()) {
            return new AccessCheckDTO(organizationId, feature, requestedAmount, false, 0, 0, 0, "PLAN_NOT_ACTIVE");
        }

        int used = getUsedAmount(organizationId, feature);
        int limit = getLimit(plan.orElseThrow(), feature);
        int remaining = Math.max(limit - used, 0);
        boolean allowed = requestedAmount <= remaining;
        return new AccessCheckDTO(
            organizationId,
            feature,
            requestedAmount,
            allowed,
            used,
            limit,
            remaining,
            allowed ? "ALLOWED" : "LIMIT_EXCEEDED"
        );
    }

    private BillingUsageDTO toUsage(Long organizationId, SubscriptionDTO subscription, Plan plan) {
        return new BillingUsageDTO(
            organizationId,
            subscription,
            toMetric(organizationId, BillingFeature.PROJECTS, plan.getMaxProjects()),
            toMetric(organizationId, BillingFeature.USERS, plan.getMaxUsers()),
            toMetric(organizationId, BillingFeature.TASKS, plan.getMaxTasks())
        );
    }

    private UsageMetricDTO toMetric(Long organizationId, BillingFeature feature, int limit) {
        int used = getUsedAmount(organizationId, feature);
        return new UsageMetricDTO(used, limit, Math.max(limit - used, 0));
    }

    private int getUsedAmount(Long organizationId, BillingFeature feature) {
        return 0;
    }

    private int getLimit(Plan plan, BillingFeature feature) {
        return switch (feature) {
            case PROJECTS -> plan.getMaxProjects();
            case USERS -> plan.getMaxUsers();
            case TASKS -> plan.getMaxTasks();
        };
    }

    private Plan getActivePlan(PlanCode planCode) {
        return planRepository
            .findByCodeAndActiveTrue(planCode)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Active plan not found"));
    }

    private Optional<Subscription> findActiveSubscription(Long organizationId) {
        return findActiveSubscription(organizationId, Instant.now());
    }

    private Optional<Subscription> findActiveSubscription(Long organizationId, Instant now) {
        return findActiveSubscriptions(organizationId, now).stream().findFirst();
    }

    private List<Subscription> findActiveSubscriptions(Long organizationId, Instant now) {
        return subscriptionRepository
            .findByOrganizationIdAndStatusOrderByStartedAtDesc(organizationId, SubscriptionStatus.ACTIVE)
            .stream()
            .filter(subscription -> subscription.getExpiresAt() == null || subscription.getExpiresAt().isAfter(now))
            .toList();
    }

    private String resolveCreatedBy(String requestedCreatedBy) {
        if (requestedCreatedBy != null && !requestedCreatedBy.isBlank()) {
            return requestedCreatedBy;
        }
        return SecurityUtils.getCurrentUserLogin().orElse(null);
    }
}
