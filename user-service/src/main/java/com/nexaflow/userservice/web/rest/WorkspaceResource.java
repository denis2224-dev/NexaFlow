package com.nexaflow.userservice.web.rest;

import com.nexaflow.userservice.service.WorkspaceService;
import com.nexaflow.userservice.service.dto.AcceptInvitationRequest;
import com.nexaflow.userservice.service.dto.CreateWorkspaceRequest;
import com.nexaflow.userservice.service.dto.InvitationResponseDTO;
import com.nexaflow.userservice.service.dto.InviteUserRequest;
import com.nexaflow.userservice.service.dto.MemberDTO;
import com.nexaflow.userservice.service.dto.WorkspaceDTO;
import jakarta.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workspaces")
public class WorkspaceResource {

    private final WorkspaceService workspaceService;

    public WorkspaceResource(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @PostMapping
    public ResponseEntity<WorkspaceDTO> createWorkspace(@Valid @RequestBody CreateWorkspaceRequest request) throws URISyntaxException {
        WorkspaceDTO result = workspaceService.createWorkspace(request);
        return ResponseEntity.created(new URI("/api/workspaces/" + result.getOrganizationId())).body(result);
    }

    @GetMapping("/my")
    public ResponseEntity<List<WorkspaceDTO>> getMyWorkspaces() {
        return ResponseEntity.ok(workspaceService.findMyWorkspaces());
    }

    @PostMapping("/{organizationId}/invitations")
    public ResponseEntity<InvitationResponseDTO> inviteUser(
        @PathVariable Long organizationId,
        @Valid @RequestBody InviteUserRequest request
    ) throws URISyntaxException {
        InvitationResponseDTO result = workspaceService.inviteUser(organizationId, request);
        return ResponseEntity.created(new URI("/api/workspaces/" + organizationId + "/invitations/" + result.getInvitationId())).body(
            result
        );
    }

    @PostMapping("/invitations/accept")
    public ResponseEntity<WorkspaceDTO> acceptInvitation(@Valid @RequestBody AcceptInvitationRequest request) {
        WorkspaceDTO result = workspaceService.acceptInvitation(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{organizationId}/members")
    public ResponseEntity<List<MemberDTO>> getWorkspaceMembers(@PathVariable Long organizationId) {
        return ResponseEntity.ok(workspaceService.getWorkspaceMembers(organizationId));
    }

    @GetMapping("/{organizationId}/invitations")
    public ResponseEntity<List<InvitationResponseDTO>> getWorkspaceInvitations(@PathVariable Long organizationId) {
        return ResponseEntity.ok(workspaceService.getWorkspaceInvitations(organizationId));
    }
}
