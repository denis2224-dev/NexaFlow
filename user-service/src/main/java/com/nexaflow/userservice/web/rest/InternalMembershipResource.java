package com.nexaflow.userservice.web.rest;

import com.nexaflow.userservice.service.WorkspaceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/memberships")
public class InternalMembershipResource {

    private final WorkspaceService workspaceService;

    public InternalMembershipResource(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> checkMembership(@RequestParam Long organizationId, @RequestParam String userLogin) {
        return ResponseEntity.ok(workspaceService.isActiveMember(organizationId, userLogin));
    }
}
