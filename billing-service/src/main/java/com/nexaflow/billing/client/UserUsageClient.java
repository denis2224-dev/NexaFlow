package com.nexaflow.billing.client;

import com.nexaflow.billing.client.dto.UserUsageDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "user-service", path = "/api/internal/memberships")
public interface UserUsageClient {

    @GetMapping("/usage")
    UserUsageDTO getUsage(@RequestParam("organizationId") Long organizationId);
}
