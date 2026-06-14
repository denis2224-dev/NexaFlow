package com.nexaflow.billing.web.rest;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nexaflow.billing.client.ProjectUsageClient;
import com.nexaflow.billing.client.UserUsageClient;
import com.nexaflow.billing.client.WorkspaceClient;
import com.nexaflow.billing.client.dto.CurrentMembershipDTO;
import com.nexaflow.billing.client.dto.MembershipRole;
import com.nexaflow.billing.client.dto.ProjectUsageDTO;
import com.nexaflow.billing.client.dto.UserUsageDTO;
import com.nexaflow.billing.IntegrationTest;
import com.nexaflow.billing.domain.Plan;
import com.nexaflow.billing.domain.enumeration.PlanCode;
import com.nexaflow.billing.repository.PlanRepository;
import com.nexaflow.billing.repository.SubscriptionRepository;
import feign.FeignException;
import feign.Request;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(username = "billing-admin")
class BillingResourceIT {

    private static final long ORGANIZATION_ID = 1001L;

    @Autowired
    private MockMvc restBillingMockMvc;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @MockitoBean
    private ProjectUsageClient projectUsageClient;

    @MockitoBean
    private UserUsageClient userUsageClient;

    @MockitoBean
    private WorkspaceClient workspaceClient;

    @BeforeEach
    void initTest() {
        subscriptionRepository.deleteAll();
        planRepository.deleteAll();
        when(projectUsageClient.getUsage(ORGANIZATION_ID)).thenReturn(new ProjectUsageDTO(ORGANIZATION_ID, 2, 17));
        when(userUsageClient.getUsage(ORGANIZATION_ID)).thenReturn(new UserUsageDTO(ORGANIZATION_ID, 4));
        when(workspaceClient.getCurrentMembership(ORGANIZATION_ID)).thenReturn(
            new CurrentMembershipDTO(ORGANIZATION_ID, MembershipRole.OWNER)
        );
    }

    @Test
    @Transactional
    void organizationScopedEndpointsRejectNonMembers() throws Exception {
        when(workspaceClient.getCurrentMembership(ORGANIZATION_ID)).thenThrow(
            new FeignException.Forbidden(
                "Forbidden",
                Request.create(
                    Request.HttpMethod.GET,
                    "/api/workspaces/" + ORGANIZATION_ID + "/membership/me",
                    Map.of(),
                    null,
                    StandardCharsets.UTF_8,
                    null
                ),
                null,
                Map.of()
            )
        );

        restBillingMockMvc
            .perform(get("/api/billing/usage").param("organizationId", String.valueOf(ORGANIZATION_ID)))
            .andExpect(status().isForbidden());

        restBillingMockMvc
            .perform(
                get("/api/internal/billing/access/check")
                    .param("organizationId", String.valueOf(ORGANIZATION_ID))
                    .param("feature", "PROJECTS")
                    .param("requestedAmount", "1")
            )
            .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    void getPlansReturnsActivePlansOrderedByPrice() throws Exception {
        planRepository.save(defaultPlan(PlanCode.PRO, "Pro", "19.00", 25, 25, 1000, true));
        planRepository.save(defaultPlan(PlanCode.FREE, "Free", "0.00", 3, 5, 50, true));
        planRepository.save(defaultPlan(PlanCode.ENTERPRISE, "Enterprise", "99.00", 100000, 100000, 100000, false));

        restBillingMockMvc
            .perform(get("/api/billing/plans"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$.[0].code").value("FREE"))
            .andExpect(jsonPath("$.[1].code").value("PRO"));
    }

    @Test
    @Transactional
    void activateSubscriptionExposesUsageAndAccessChecksThenCancels() throws Exception {
        planRepository.save(defaultPlan(PlanCode.PRO, "Pro", "19.00", 25, 25, 1000, true));

        restBillingMockMvc
            .perform(
                post("/api/billing/subscription/activate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "organizationId": 1001,
                          "planCode": "PRO"
                        }
                        """)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.organizationId").value(ORGANIZATION_ID))
            .andExpect(jsonPath("$.planCode").value("PRO"))
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            .andExpect(jsonPath("$.createdBy").value("billing-admin"));

        restBillingMockMvc
            .perform(get("/api/billing/subscription/my").param("organizationId", String.valueOf(ORGANIZATION_ID)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.organizationId").value(ORGANIZATION_ID))
            .andExpect(jsonPath("$.planCode").value("PRO"))
            .andExpect(jsonPath("$.status").value("ACTIVE"));

        restBillingMockMvc
            .perform(get("/api/billing/usage").param("organizationId", String.valueOf(ORGANIZATION_ID)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.organizationId").value(ORGANIZATION_ID))
            .andExpect(jsonPath("$.subscription.planCode").value("PRO"))
            .andExpect(jsonPath("$.projects.used").value(2))
            .andExpect(jsonPath("$.projects.limit").value(25))
            .andExpect(jsonPath("$.projects.remaining").value(23))
            .andExpect(jsonPath("$.users.used").value(4))
            .andExpect(jsonPath("$.users.limit").value(25))
            .andExpect(jsonPath("$.users.remaining").value(21))
            .andExpect(jsonPath("$.tasks.used").value(17))
            .andExpect(jsonPath("$.tasks.limit").value(1000));

        restBillingMockMvc
            .perform(
                get("/api/internal/billing/access/check")
                    .param("organizationId", String.valueOf(ORGANIZATION_ID))
                    .param("feature", "USERS")
                    .param("requestedAmount", "21")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.allowed").value(true))
            .andExpect(jsonPath("$.reason").value("ALLOWED"))
            .andExpect(jsonPath("$.used").value(4))
            .andExpect(jsonPath("$.remaining").value(21));

        restBillingMockMvc
            .perform(
                get("/api/internal/billing/access/check")
                    .param("organizationId", String.valueOf(ORGANIZATION_ID))
                    .param("feature", "USERS")
                    .param("requestedAmount", "22")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.allowed").value(false))
            .andExpect(jsonPath("$.reason").value("LIMIT_EXCEEDED"));

        restBillingMockMvc
            .perform(
                post("/api/billing/subscription/cancel")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "organizationId": 1001
                        }
                        """)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.organizationId").value(ORGANIZATION_ID))
            .andExpect(jsonPath("$.status").value("CANCELLED"));

        restBillingMockMvc
            .perform(
                get("/api/internal/billing/access/check")
                    .param("organizationId", String.valueOf(ORGANIZATION_ID))
                    .param("feature", "PROJECTS")
                    .param("requestedAmount", "1")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.allowed").value(false))
            .andExpect(jsonPath("$.reason").value("NO_ACTIVE_SUBSCRIPTION"));
    }

    private static Plan defaultPlan(
        PlanCode code,
        String name,
        String priceMonthly,
        int maxProjects,
        int maxUsers,
        int maxTasks,
        boolean active
    ) {
        Instant now = Instant.now();
        return new Plan()
            .code(code)
            .name(name)
            .priceMonthly(new BigDecimal(priceMonthly))
            .maxProjects(maxProjects)
            .maxUsers(maxUsers)
            .maxTasks(maxTasks)
            .active(active)
            .createdAt(now)
            .updatedAt(now);
    }
}
