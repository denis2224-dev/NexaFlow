package com.nexaflow.project.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service", path = "/api/internal/memberships")
public interface MembershipClient {

    @GetMapping("/check")
    Boolean isMember(@RequestParam("organizationId") Long organizationId, @RequestParam("userLogin") String userLogin);
}
