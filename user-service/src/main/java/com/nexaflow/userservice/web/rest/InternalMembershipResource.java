package com.nexaflow.userservice.web.rest;

import com.nexaflow.userservice.repository.MembershipRepository;
import com.nexaflow.userservice.service.WorkspaceService;
import com.nexaflow.userservice.service.dto.UserUsageDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/memberships")
public class InternalMembershipResource {

    private final WorkspaceService workspaceService;
    private final MembershipRepository membershipRepository;

    public InternalMembershipResource(WorkspaceService workspaceService, MembershipRepository membershipRepository) {
        this.workspaceService = workspaceService;
        this.membershipRepository = membershipRepository;
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> checkMembership(@RequestParam Long organizationId, @RequestParam String userLogin) {
        return ResponseEntity.ok(workspaceService.isActiveMember(organizationId, userLogin));
    }

    @GetMapping("/usage")
    public ResponseEntity<UserUsageDTO> getUsage(@RequestParam Long organizationId) {
        long users = membershipRepository.countByOrganizationIdAndActiveTrue(organizationId);
        return ResponseEntity.ok(new UserUsageDTO(organizationId, users));
    }
}
