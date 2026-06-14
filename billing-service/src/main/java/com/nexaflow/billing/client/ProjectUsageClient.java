package com.nexaflow.billing.client;

import com.nexaflow.billing.client.dto.ProjectUsageDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "project-service", path = "/api/internal/projects")
public interface ProjectUsageClient {

    @GetMapping("/usage")
    ProjectUsageDTO getUsage(@RequestParam("organizationId") Long organizationId);
}
