package com.nexaflow.billing.client;

import com.nexaflow.billing.client.dto.CurrentMembershipDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", path = "/api/workspaces")
public interface WorkspaceClient {
    @GetMapping("/{organizationId}/membership/me")
    CurrentMembershipDTO getCurrentMembership(@PathVariable("organizationId") Long organizationId);
}
